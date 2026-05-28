output "lambda_function_name" {
  description = "Lambda function name"
  value       = aws_lambda_function.this.function_name
}

output "lambda_url" {
  description = "Direct Lambda function URL"
  value       = aws_lambda_function_url.this.function_url
}

output "api_gateway_url" {
  description = "API Gateway URL"
  value       = aws_apigatewayv2_api.this.api_endpoint
}

output "custom_domain_url" {
  description = "Custom domain URL"
  value       = "https://${var.domain_name}"
}

output "log_group_name" {
  description = "CloudWatch Log Group"
  value       = aws_cloudwatch_log_group.this.name
}
