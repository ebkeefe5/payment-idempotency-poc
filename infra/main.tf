provider "aws" {
  region = var.aws_region
}

# --- ECR for backend ---
# Private registry to store the backend Docker images
# Github Actions will build + push here
resource "aws_ecr_repository" "app" {
  name = "poc-app"
}

# --- VPC ---
# Isolated network for all resources. /16 = ~65k IPs
resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"
}

# --- RDS Postgres on 5432 ---
# The payment DB. Not public = only ECS can talk to it inside VPC
resource "aws_db_instance" "db" {
  allocated_storage   = 20
  engine              = "postgres"
  instance_class      = "db.t3.micro" # cheapest for POC
  db_name             = "payments_db"
  username            = "payments_user"
  password            = var.db_password
  skip_final_snapshot = true  # false in prod to leave final backup
  publicly_accessible = false # only accesible from inside VPC
}

# --- ECS ---
# Cluster = parking lot / grouping where our container will run
# Empty for now, no cost until we add a service
resource "aws_ecs_cluster" "main" {
  name = "poc-cluster"
}

# --- IAM for ECS ---
# Lets ECS pull image from ECR and write logs
resource "aws_iam_role" "ecs_exec" {
  name = "poc-ecs-exec"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{ Action = "sts:AssumeRole", Effect = "Allow",
    Principal = { Service = "ecs-tasks.amazonaws.com" } }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_exec" {
  role       = aws_iam_role.ecs_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# --- ECS Task Definition ---
# AWS definition of compose file: how to run our app container
# Points to ECR image, sets CPU/memory, port 8080, and DB env vars
resource "aws_ecs_task_definition" "app" {
  family                   = "poc-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_exec.arn
  container_definitions = jsonencode([{
    name         = "app"
    image        = "${aws_ecr_repository.app.repository_url}:latest"
    essential    = true
    portMappings = [{ containerPort = 8080 }]
    environment = [
      { name = "PORT", value = "8080" },
      { name = "DB_HOST", value = aws_db_instance.db.address },
      { name = "DB_NAME", value = "payments_db" },
      { name = "DB_USER", value = "payments_user" },
      { name = "DB_PASSWORD", value = var.db_password } # move to secrets for prod 
    ]
  }])
}