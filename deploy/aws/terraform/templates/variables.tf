variable "image_uri" {
  description = "ECR image URI for the Lambda function"
  type        = string
}

variable "neon_password" {
  description = "Neon PostgreSQL password"
  type        = string
  sensitive   = true
}

variable "jwt_secret" {
  description = "JWT signing secret (min 32 chars)"
  type        = string
  sensitive   = true
}

variable "llm_api_key" {
  description = "LLM API key for AI analysis"
  type        = string
  sensitive   = true
}

variable "hosted_zone_id" {
  description = "Route53 hosted zone ID for jpje.net"
  type        = string
}

variable "domain_name" {
  description = "Custom domain for the API"
  type        = string
  default     = "ajt.jpje.net"
}

variable "region" {
  description = "AWS region"
  type        = string
  default     = "eu-west-1"
}
