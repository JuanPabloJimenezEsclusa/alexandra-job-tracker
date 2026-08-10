#!/usr/bin/env bash

# Example of usage: ./start.sh buildImage

set -o errexit
set -o errtrace
set -o nounset
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
workspace="${SCRIPT_DIR}"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

# Load .env file if present (optional local overrides)
if [[ -f "${SCRIPT_DIR}/../.env" ]]; then
  set -o allexport
  # shellcheck disable=SC1091
  source "${SCRIPT_DIR}/../.env"
  set +o allexport
fi

SEPARATOR="\n##################################################\n"

BUILD_IMAGE="${1:-}"
STACK_NAME="${STACK_NAME:-ajt-serverless-stack}"
REGION="${REGION:-eu-west-1}"
AWS_ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com"
ECR_REPOSITORY="${ECR_REPOSITORY:-ajt-serverless}"
ECR_IMAGE_TAG="${ECR_IMAGE_TAG:-1.0.0}"
IMAGE_URI="${IMAGE_URI:-"${ECR_REGISTRY}/${ECR_REPOSITORY}:${ECR_IMAGE_TAG}"}"

__require_aws_cli() {
  if ! command -v aws &>/dev/null; then
    echo "aws CLI not found. Install it first!"
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
      unzip awscliv2.zip
      sudo ./aws/install --update
      rm -rf awscliv2.zip aws
      aws --version
  fi
}

__require_neon_pass() {
  if [[ -z "${NEON_PASSWORD:-}" ]]; then
    echo "NEON_PASSWORD is required."
    echo "1. Create a free database at https://neon.tech"
    echo "2. Run: export NEON_PASSWORD='...'"
    exit 1
  fi
}

__require_jwt_secret() {
  if [[ -z "${JWT_SECRET:-}" ]]; then
    JWT_SECRET="$(openssl rand -hex 32)"
    echo "Generated JWT_SECRET (save this, not shown again): ${JWT_SECRET}"
    export JWT_SECRET
  fi
}

__require_llm_key() {
  if [[ -z "${LLM_API_KEY:-}" ]]; then
    echo "LLM_API_KEY is required (or set to 'sk-placeholder' to skip AI)."
    echo -n "Enter LLM API key: "
    read -rs LLM_API_KEY
    echo
    export LLM_API_KEY
  fi
}

__require_hosted_zone() {
  if [[ -z "${HOSTED_ZONE_ID:-}" ]]; then
    # shellcheck disable=SC2016
    HOSTED_ZONE_ID="$(aws route53 list-hosted-zones --query 'HostedZones[?Name==`jpje.net.`].Id' --output text | sed 's|/hostedzone/||')"
    if [[ -z "${HOSTED_ZONE_ID:-}" ]]; then
      echo "Hosted zone jpje.net not found in Route53."
      echo "Set HOSTED_ZONE_ID manually or create the zone first."
      exit 1
    fi
    export HOSTED_ZONE_ID
  fi
}

__validate() {
  echo -e "${SEPARATOR}🔍 Validate environment${SEPARATOR}"
  __require_aws_cli
  __require_neon_pass
  __require_jwt_secret
  __require_llm_key
  __require_hosted_zone
}

__cleanup_ecr() {
  echo -e "${SEPARATOR}🧹 Clean up ECR images${SEPARATOR}"
  local repo="${ECR_REPOSITORY:-ajt-serverless}"
  if aws ecr describe-repositories --repository-names "${repo}" --region "${REGION}" >/dev/null 2>&1; then
    aws ecr batch-delete-image \
      --repository-name "${repo}" \
      --image-ids "$(aws ecr list-images --repository-name "${repo}" --region "${REGION}" --query 'imageIds[*]' --output json)" \
      --region "${REGION}" 2>/dev/null || true
    aws ecr delete-repository --repository-name "${repo}" --region "${REGION}" 2>/dev/null || true
    echo "Deleted ECR repository: ${repo}"
  fi
}

__create_ecr_repository() {
  echo -e "${SEPARATOR}📦 Create ECR repository${SEPARATOR}"
  aws ecr describe-repositories --repository-names "${ECR_REPOSITORY}" --region "${REGION}" >/dev/null 2>&1 || \
    aws ecr create-repository --repository-name "${ECR_REPOSITORY}" --region "${REGION}"
}

__build_image() {
  echo -e "${SEPARATOR}🐳 Build Lambda Docker image${SEPARATOR}"
  cd "${workspace}"
  if [[ ! -f "${REPO_ROOT}/mvnw" || ! -f "${REPO_ROOT}/pom.xml" ]]; then
    echo "Error: repo root not found at ${REPO_ROOT} (missing mvnw/pom.xml)."
    echo "Expected repository layout: deploy/aws/cloud-formation/start.sh inside the job-tracker repo."
    exit 1
  fi
  # Authenticate with public ECR for the Lambda Web Adapter base image
  aws ecr-public get-login-password --region us-east-1 | \
    docker login --username AWS --password-stdin public.ecr.aws
  docker build -f "${SCRIPT_DIR}/../Dockerfile.native" -t "${ECR_REPOSITORY}:${ECR_IMAGE_TAG}" "${REPO_ROOT}"
}

__login_ecr() {
  echo -e "${SEPARATOR}🔑 Login to ECR${SEPARATOR}"
  aws ecr get-login-password --region "${REGION}" | \
    docker login --username AWS --password-stdin "${ECR_REGISTRY}"
}

__push_image() {
  echo -e "${SEPARATOR}⬆️ Push image to ECR${SEPARATOR}"
  docker tag "${ECR_REPOSITORY}:${ECR_IMAGE_TAG}" "${IMAGE_URI}"
  docker push "${IMAGE_URI}"
}

__manage_lambda_image() {
  echo -e "${SEPARATOR}🚀 Manage Lambda image${SEPARATOR}"
  if [[ "${BUILD_IMAGE}" == "buildImage" ]]; then
    __cleanup_ecr
    __create_ecr_repository
    __build_image
    __login_ecr
    __push_image
  else
    echo "Using pre-built image: ${ECR_REPOSITORY}:${ECR_IMAGE_TAG}"
  fi

  # shellcheck disable=SC2016
  # List images in ECR with the specified tag
  aws ecr describe-images \
    --repository-name "${ECR_REPOSITORY}" \
    --region "${REGION}" \
    --query 'imageDetails[?imageTags.contains(@, `1.0.0`)]' \
    --output table
}

__deploy_stack() {
  echo -e "${SEPARATOR}☁️ Create CloudFormation stack${SEPARATOR}"
  cd "${workspace}"

  aws cloudformation deploy \
    --stack-name "${STACK_NAME}" \
    --template-file ajt-serverless-stack.yml \
    --parameter-overrides \
      ImageUri="${IMAGE_URI}" \
      NeonPass="${NEON_PASSWORD}" \
      JwtSecret="${JWT_SECRET}" \
      LLMApiKey="${LLM_API_KEY}" \
      HostedZoneId="${HOSTED_ZONE_ID}" \
    --capabilities CAPABILITY_NAMED_IAM \
    --region "${REGION}" \
    --no-fail-on-empty-changeset

  echo "Waiting for stack creation..."
  aws cloudformation wait stack-create-complete \
    --stack-name "${STACK_NAME}" \
    --region "${REGION}"

  # Get Lambda function configuration and environment variables
  aws lambda get-function-configuration \
    --function-name ajt-serverless \
    --region "${REGION}" \
    --query '{config:{code:CodeSha256, state:State},env:Environment.Variables}' \
    --output table

  # Test the Lambda function by invoking it with a sample event
  aws lambda invoke \
    --function-name ajt-serverless \
    --region "${REGION}" \
    --payload '{
      "version": "2.0",
      "routeKey": "GET /api/actuator/health",
      "rawPath": "/api/actuator/health",
      "rawQueryString": "",
      "headers": {"accept":"application/json"},
      "requestContext": {"http":{"method":"GET","path":"/api/actuator/health"}},"body":null,"isBase64Encoded":false}' \
    --cli-binary-format raw-in-base64-out \
    /tmp/lambda-output.json && \
  jq ".body | fromjson" < /tmp/lambda-output.json

  # Check the health of the deployed API
  curl -s https://ajt.jpje.net/api/actuator/health \
    | jq -r '.components | to_entries[] | [.key, .value.status] | @tsv' \
    | column -t -s $'\t'
}

main() {
  echo "Init ${0##*/}"
  __validate
  __manage_lambda_image
  __deploy_stack

  echo "Done ${0##*/}"
  echo ""
  echo "Your API is live at: https://ajt.jpje.net"
}

time main
