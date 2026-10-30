provider "aws" {
  region  = var.aws_region

  default_tags {
    tags = {
      Project     = "eventsProject"
      Environment = "dev"
      ManagedBy   = "Terraform"
    }
  }
}
