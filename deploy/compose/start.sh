#!/usr/bin/env bash

# Example of usage: ./start.sh native

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

SEPARATOR="\n ################################################## \n"
TYPE="${1:-jvm}"

cd "$(dirname "$0")"
workspace="$(pwd)"

__initServices() {
  local type="${1:-jvm}"
  local dockerfile="${workspace}/Dockerfile"

  cd "${workspace}"

  if [[ "${type}" == "native" ]]; then
    dockerfile="${workspace}/Dockerfile.native"
  fi

  echo "dockerfile: ${dockerfile}"
  DOCKERFILE="${dockerfile}" \
    docker compose --file "${workspace}/docker-compose.yml" --progress=plain \
    up -d --build -V --force-recreate --always-recreate-deps
}

main() {
  __initServices "${TYPE}"
}

echo -e "${SEPARATOR} 🔨 Main: ${0} ${SEPARATOR}"
time main | tee result-start.log
