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

NEON_PASSWORD="${NEON_PASSWORD:-}"
JWT_SECRET="${JWT_SECRET:-}"
DEEPSEEK_API_KEY="${DEEPSEEK_API_KEY:-}"
# shellcheck disable=SC2016
HOSTED_ZONE_ID="$(aws route53 list-hosted-zones --query 'HostedZones[?Name==`jpje.net.`].Id' --output text | sed 's|/hostedzone/||')"
DOMAIN_NAME="${DOMAIN_NAME:-ajt.jpje.net}"
REGION="${REGION:-eu-west-1}"

main() {
  echo "Init ${0##*/}"

  echo -e "${SEPARATOR} Destroy the Terraform stack. ${SEPARATOR}"
  terraform -chdir=templates apply -destroy -auto-approve \
    -var "image_uri=" \
    -var "neon_password=${NEON_PASSWORD}" \
    -var "jwt_secret=${JWT_SECRET}" \
    -var "deepseek_api_key=${DEEPSEEK_API_KEY}" \
    -var "hosted_zone_id=${HOSTED_ZONE_ID}" \
    -var "domain_name=${DOMAIN_NAME}" \
    -var "region=${REGION}"

  echo "Done ${0##*/}"
}

time main
