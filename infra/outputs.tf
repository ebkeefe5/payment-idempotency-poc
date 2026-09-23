output "db_address" {
  description = "RDS endpoint - use this to test connection"
  value       = aws_db_instance.db.address
}

output "ecr_repo_url" {
  description = "Push your docker image here"
  value       = aws_ecr_repository.app.repository_url
}

output "ecs_cluster_name" {
  value = aws_ecs_cluster.main.name
}