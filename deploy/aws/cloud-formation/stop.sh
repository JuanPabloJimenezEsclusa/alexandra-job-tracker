#!/usr/bin/env bash

# Example of usage: ./stop.sh removeImages

set -o errexit
set -o errtrace
set -o nounset
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi

cd "$(dirname "$0")"

SEPARATOR="\n##################################################\n"

REMOVE_IMAGES="${1:-}"
STACK_NAME="${STACK_NAME:-ajt-serverless-stack}"
REGION="${REGION:-eu-west-1}"

__delete_stack() {
  echo -e "${SEPARATOR}🗑️ Delete API Gateway domain name${SEPARATOR}"
  aws apigatewayv2 delete-domain-name \
    --domain-name ajt.jpje.net \
    --region "${REGION}" 2>/dev/null || true

  echo -e "${SEPARATOR}🗑️ Delete CloudFormation stack${SEPARATOR}"
  aws cloudformation delete-stack \
    --stack-name "${STACK_NAME}" \
    --region "${REGION}"

  echo "Waiting for stack deletion..."
  aws cloudformation wait stack-delete-complete \
    --stack-name "${STACK_NAME}" \
    --region "${REGION}"
  echo "Stack deleted successfully!"
}

__delete_log_groups() {
  echo -e "${SEPARATOR}🗑️ Delete log groups${SEPARATOR}"
  local log_groups=(
    "/aws/lambda/ajt-serverless"
    "/aws/apigateway/ajt-serverless-api"
  )
  for lg in "${log_groups[@]}"; do
    aws logs delete-log-group --log-group-name "${lg}" --region "${REGION}" 2>/dev/null || true
    echo "Deleted: ${lg}"
  done
}

__cleanUp() {
  echo -e "${SEPARATOR} 🧹 Clean up ${SEPARATOR}"
  docker system prune --force --volumes
  docker builder prune --force
  rm -fdr ~/.m2/repository/dev/jpje || true

  if [[ "${REMOVE_IMAGES}" == "removeImages" ]]; then
    docker images \
      --filter reference='*/*ajt-*' \
      --filter reference='*ajt-*' \
      --format '{{.Repository}}:{{.Tag}}' | xargs -I {} docker rmi -f {}
  else
    echo -e "🚧 Skip remove images"
  fi
}

main() {
  echo "Init ${0##*/}"
  __delete_stack
  __delete_log_groups
  __cleanUp
  echo "Done ${0##*/}"
}

time main
