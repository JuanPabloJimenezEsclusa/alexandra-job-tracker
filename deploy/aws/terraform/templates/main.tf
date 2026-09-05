terraform {
  required_version = ">= 1.14.3"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.62.0"
    }
  }
}

provider "aws" {
  region = var.region
}

data "aws_caller_identity" "current" {}

data "aws_region" "current" {}

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

resource "aws_iam_role_policy" "events" {
  name = "events-policy"
  role = aws_iam_role.lambda_exec.name
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["sns:Publish"]
        Resource = [aws_sns_topic.job_events.arn]
      },
      {
        Effect = "Allow"
        Action = [
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ]
        Resource = [aws_sqs_queue.job_analysis.arn]
      },
      {
        Effect   = "Allow"
        Action   = ["kms:Decrypt"]
        Resource = [aws_kms_key.job_events.arn]
      }
    ]
  })
}

#################################################################################################
# Events (SNS + SQS)
#################################################################################################

resource "aws_sns_topic" "job_events" {
  name         = "ajt-job-events"
  display_name = "AJT Job Events"
  # Non-sensitive job posting metadata only — AWS-managed KMS is sufficient.
  kms_master_key_id = "alias/aws/sns"
}

resource "aws_kms_key" "job_events" {
  description         = "KMS key for the AJT SQS queues (grants SNS delivery)"
  enable_key_rotation = true
  policy = jsonencode({
    Version = "2012-10-17"
    Id      = "ajt-job-analysis-key"
    Statement = [
      {
        Sid       = "Enable IAM User Permissions"
        Effect    = "Allow"
        Principal = { AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root" }
        Action    = "kms:*"
        Resource  = "*"
      },
      {
        Sid       = "Allow SNS to encrypt messages delivered to the queue"
        Effect    = "Allow"
        Principal = { Service = "sns.amazonaws.com" }
        Action = [
          "kms:Decrypt",
          "kms:GenerateDataKey",
          "kms:GenerateDataKeyWithoutPlaintext"
        ]
        Resource = "*"
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = aws_sns_topic.job_events.arn
          }
        }
      }
    ]
  })
}

resource "aws_kms_alias" "job_events" {
  name          = "alias/ajt-job-analysis-kms"
  target_key_id = aws_kms_key.job_events.id
}

resource "aws_sqs_queue" "job_analysis" {
  name                       = "ajt-job-analysis"
  visibility_timeout_seconds = 120
  kms_master_key_id          = aws_kms_key.job_events.id
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.job_analysis_dlq.arn
    maxReceiveCount     = 3
  })
}

resource "aws_sqs_queue" "job_analysis_dlq" {
  name              = "ajt-job-analysis-dlq"
  kms_master_key_id = aws_kms_key.job_events.id
}

resource "aws_sns_topic_subscription" "job_events_to_sqs" {
  topic_arn            = aws_sns_topic.job_events.arn
  protocol             = "sqs"
  endpoint             = aws_sqs_queue.job_analysis.arn
  raw_message_delivery = true
  filter_policy_scope  = "MessageAttributes"
  filter_policy = jsonencode({
    eventType = ["JobPostingCreated"]
  })
}

#################################################################################################
# Lambda
#################################################################################################

resource "aws_lambda_function" "this" {
  function_name = "ajt-serverless"
  description   = "Spring Boot native-image job tracker (GraalVM)"
  package_type  = "Image"
  image_uri     = var.image_uri
  role          = aws_iam_role.lambda_exec.arn
  memory_size   = 1024
  timeout       = 60
  publish       = true

  ephemeral_storage {
    size = 512
  }

  environment {
    variables = {
      NEON_PASSWORD                = var.neon_password
      JWT_SECRET                   = var.jwt_secret
      LLM_API_KEY                  = var.llm_api_key
      SNS_TOPIC_ARN                = aws_sns_topic.job_events.arn
      SQS_QUEUE_URL                = aws_sqs_queue.job_analysis.id
      SPRING_PROFILES_ACTIVE       = "aws"
      LOGGING_LEVEL_ROOT           = "WARN"
      LOGGING_LEVEL_COM_JOBTRACKER = "INFO"
      AWS_LAMBDA_SERVER_PORT       = "8080"
      SERVER_PORT                  = "8080"
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
    format          = "$$context.requestId $$context.httpMethod $$context.routeKey $$context.status $$context.protocol $$context.responseLength"
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
