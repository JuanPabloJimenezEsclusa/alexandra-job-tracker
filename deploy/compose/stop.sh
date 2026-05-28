#!/usr/bin/env bash

# Example of usage: ./stop.sh removeImages

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

SEPARATOR="\n ################################################## \n"
REMOVE_IMAGES="${1:-}"

cd "$(dirname "$0")"

__downServices() {
  echo -e "${SEPARATOR} 🔨 Stop services ${SEPARATOR}"
  docker compose --file docker-compose.yml down --remove-orphans --volumes || true
}

__cleanUp() {
  echo -e "${SEPARATOR} 🧹 Clean up ${SEPARATOR}"
  docker system prune --force --volumes
  docker builder prune --force
  rm -fdr ~/.m2/repository/dev/jpje || true

  if [[ "${REMOVE_IMAGES}" == "removeImages" ]]; then
    docker images --filter reference='*-job-tracker-*' --format '{{.Repository}}:{{.Tag}}' | xargs -I {} docker rmi -f {}
  else
    echo -e "🚧 Skip remove images"
  fi
}

main() {
  __downServices
  __cleanUp
}

echo -e "${SEPARATOR} 🔨 Main: ${0} ${SEPARATOR}"
time main
