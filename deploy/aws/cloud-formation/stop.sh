#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o nounset
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi

cd "$(dirname "$0")"

SEPARATOR="\n##################################################\n"

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

main() {
  echo "Init ${0##*/}"
  __delete_stack
  __delete_log_groups
  echo "Done ${0##*/}"
}

time main
