variable "db_password" {
  description = "Password for RDS - put in terraform.tfvars"
  type        = string
  sensitive   = true
  #for prod replace with aws_secretsmanager_secret
}

variable "aws_region" {
  description = "where to deploy"
  type        = string
  default     = "us-east-1"
}

variable "app_port" {
  description = "Port your app listens on"
  type        = string
  default     = "8080"
}