terraform {
  required_version = ">= 1.14.3"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.59.0"
    }
  }
}

provider "aws" {
  region = var.region
}

#################################################################################################
# IAM
#################################################################################################

resource "aws_iam_role" "lambda_exec" {
  name = "ajt-lambda-execution-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect    = "Allow"
        Principal = { Service = "lambda.amazonaws.com" }
        Action    = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_exec" {
  role       = aws_iam_role.lambda_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy" "ecr_pull" {
  name = "ecr-pull-policy"
  role = aws_iam_role.lambda_exec.name
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:GetAuthorizationToken"
        ]
        Resource = "*"
      }
    ]
  })
}

#################################################################################################
# Lambda
#################################################################################################

resource "aws_lambda_function" "this" {
  function_name   = "ajt-serverless"
  description     = "Spring Boot native-image job tracker (GraalVM)"
  package_type    = "Image"
  image_uri       = var.image_uri
  role            = aws_iam_role.lambda_exec.arn
  memory_size     = 1024
  timeout         = 60
  publish         = true

  ephemeral_storage {
    size = 512
  }

  environment {
    variables = {
      NEON_PASSWORD                 = var.neon_password
      JWT_SECRET                    = var.jwt_secret
      DEEPSEEK_API_KEY              = var.deepseek_api_key
      SPRING_PROFILES_ACTIVE        = "aws"
      LOGGING_LEVEL_ROOT            = "WARN"
      LOGGING_LEVEL_COM_JOBTRACKER  = "INFO"
      AWS_LAMBDA_SERVER_PORT        = "8080"
      SERVER_PORT                   = "8080"
    }
  }

  tags = {
    Name = "ajt-serverless"
  }
}

resource "aws_lambda_alias" "live" {
  function_name    = aws_lambda_function.this.arn
  function_version = aws_lambda_function.this.version
  name             = "live"
}

resource "aws_lambda_function_url" "this" {
  function_name      = aws_lambda_function.this.function_name
  authorization_type = "NONE"

  cors {
    allow_origins = ["*"]
    allow_methods = ["*"]
    allow_headers = ["*"]
    max_age       = 3600
  }
}

resource "aws_lambda_permission" "function_url" {
  function_name          = aws_lambda_function.this.function_name
  action                 = "lambda:InvokeFunctionUrl"
  principal              = "*"
  function_url_auth_type = "NONE"
}

#################################################################################################
# API Gateway HTTP API
#################################################################################################

resource "aws_apigatewayv2_api" "this" {
  name          = "ajt-serverless-api"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = ["*"]
    allow_methods = ["*"]
    allow_headers = ["*"]
  }
}

resource "aws_apigatewayv2_integration" "this" {
  api_id                 = aws_apigatewayv2_api.this.id
  integration_type       = "AWS_PROXY"
  integration_uri        = "arn:aws:apigateway:${var.region}:lambda:path/2015-03-31/functions/${aws_lambda_alias.live.arn}/invocations"
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_route" "this" {
  api_id    = aws_apigatewayv2_api.this.id
  route_key = "$default"
  target    = "integrations/${aws_apigatewayv2_integration.this.id}"
}

resource "aws_apigatewayv2_stage" "this" {
  api_id      = aws_apigatewayv2_api.this.id
  name        = "$default"
  auto_deploy = true

  default_route_settings {
    throttling_burst_limit = 20
    throttling_rate_limit  = 5.0
  }

  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api_gateway.arn
    format         = "$$context.requestId $$context.httpMethod $$context.routeKey $$context.status $$context.protocol $$context.responseLength"
  }
}

resource "aws_cloudwatch_log_group" "api_gateway" {
  name              = "/aws/apigateway/${aws_apigatewayv2_api.this.id}"
  retention_in_days = 1
}

resource "aws_lambda_permission" "api_gateway" {
  function_name = aws_lambda_alias.live.arn
  action        = "lambda:InvokeFunction"
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.this.execution_arn}/*"
}

#################################################################################################
# Custom Domain (ACM + Route53)
#################################################################################################

resource "aws_acm_certificate" "this" {
  domain_name       = var.domain_name
  validation_method = "DNS"
}

resource "aws_route53_record" "cert_validation" {
  zone_id = var.hosted_zone_id
  name    = tolist(aws_acm_certificate.this.domain_validation_options)[0].resource_record_name
  type    = tolist(aws_acm_certificate.this.domain_validation_options)[0].resource_record_type
  records = [tolist(aws_acm_certificate.this.domain_validation_options)[0].resource_record_value]
  ttl     = 60
}

resource "aws_acm_certificate_validation" "this" {
  certificate_arn         = aws_acm_certificate.this.arn
  validation_record_fqdns = [aws_route53_record.cert_validation.fqdn]
}

resource "aws_apigatewayv2_domain_name" "this" {
  domain_name = var.domain_name

  domain_name_configuration {
    endpoint_type   = "REGIONAL"
    security_policy = "TLS_1_2"
    certificate_arn = aws_acm_certificate_validation.this.certificate_arn
  }
}

resource "aws_apigatewayv2_api_mapping" "this" {
  domain_name = aws_apigatewayv2_domain_name.this.domain_name
  api_id      = aws_apigatewayv2_api.this.id
  stage       = aws_apigatewayv2_stage.this.id
}

resource "aws_route53_record" "api_alias" {
  zone_id = var.hosted_zone_id
  name    = var.domain_name
  type    = "A"

  alias {
    name                   = aws_apigatewayv2_domain_name.this.domain_name_configuration[0].target_domain_name
    zone_id                = aws_apigatewayv2_domain_name.this.domain_name_configuration[0].hosted_zone_id
    evaluate_target_health = false
  }
}

#################################################################################################
# CloudWatch
#################################################################################################

resource "aws_cloudwatch_log_group" "this" {
  name              = "/aws/lambda/ajt-serverless"
  retention_in_days = 1
}
