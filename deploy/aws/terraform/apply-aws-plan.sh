#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o nounset
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

main() {
  echo "Init ${0##*/}"

  echo -e "${SEPARATOR} Apply the Terraform plan. ${SEPARATOR}"
  terraform -chdir=templates apply -auto-approve tfplan

  echo -e "${SEPARATOR} Stack outputs. ${SEPARATOR}"
  terraform -chdir=templates output

  echo "Done ${0##*/}"
}

time main
