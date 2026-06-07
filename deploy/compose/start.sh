#!/usr/bin/env bash

# Example of usage: ./start.sh native

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"
workspace="$(pwd)"

__initServices() {
  local type="${1:-jvm}"
  local dockerfile="deploy/compose/Dockerfile"

  cd "${workspace}"

  if [[ "${type}" == "native" ]]; then
    dockerfile="deploy/compose/Dockerfile.native"
  fi
  DOCKERFILE="${dockerfile}" docker-compose --file docker-compose.yml up -d --build --force-recreate
  docker-compose --file docker-compose.yml ps

}

main() {
  __initServices
}

echo -e "${SEPARATOR} 🔨 Main: ${0} ${SEPARATOR}"
time main | tee result-start.log
