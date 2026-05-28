#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o nounset
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi

cd "$(dirname "$0")"

# Load .env file if present (optional local overrides)
if [[ -f ../.env ]]; then
  set -o allexport
  # shellcheck disable=SC1091
  source ../.env
  set +o allexport
fi

SEPARATOR="\n ################################################## \n"

IMAGE_URI="${IMAGE_URI:-546053716955.dkr.ecr.eu-west-1.amazonaws.com/ajt-serverless:1.0.0}"
NEON_PASSWORD="${NEON_PASSWORD:-}"
JWT_SECRET="${JWT_SECRET:-}"
LLM_API_KEY="${LLM_API_KEY:-}"
DOMAIN_NAME="${DOMAIN_NAME:-ajt.jpje.net}"
REGION="${REGION:-eu-west-1}"

__validate_env_var() {
  local var_name="${1}"
  local var_value="${2}"
  if [ -z "$var_value" ]; then
    echo "Error: ${var_name} environment variable is empty."
    exit 1
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

main() {
  echo "Init ${0##*/}"

  export TF_LOG="INFO"

  echo -e "${SEPARATOR} Init Terraform and install providers. ${SEPARATOR}"
  terraform -chdir=templates init -upgrade

  echo -e "${SEPARATOR} Validate Terraform configuration. ${SEPARATOR}"
  terraform -chdir=templates validate

  echo -e "${SEPARATOR} Validate required environment variables. ${SEPARATOR}"
  __validate_env_var "IMAGE_URI" "${IMAGE_URI}"
  __validate_env_var "NEON_PASSWORD" "${NEON_PASSWORD}"
  __validate_env_var "JWT_SECRET" "${JWT_SECRET}"
  __validate_env_var "LLM_API_KEY" "${LLM_API_KEY}"
  __require_hosted_zone

  echo -e "${SEPARATOR} Run Terraform plan. ${SEPARATOR}"
  terraform -chdir=templates plan -out=tfplan \
    -var "image_uri=${IMAGE_URI}" \
    -var "neon_password=${NEON_PASSWORD}" \
    -var "jwt_secret=${JWT_SECRET}" \
    -var "llm_api_key=${LLM_API_KEY}" \
    -var "hosted_zone_id=${HOSTED_ZONE_ID}" \
    -var "domain_name=${DOMAIN_NAME}" \
    -var "region=${REGION}"

  echo "Done ${0##*/}"
}

time main
