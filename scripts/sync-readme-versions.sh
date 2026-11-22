#!/usr/bin/env bash
# Sync versions documented in README.md with the values declared in pom.xml.
set -euo pipefail

root=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
pom="$root/pom.xml"
readmes=("$root/README.md")

prop() { sed -n "s:.*<$1>\([^<]*\)</$1>.*:\1:p" "$pom" | head -1; }

# The Spring Boot version is the parent version, not a property.
parent_version=$(awk '/<parent>/{p=1} p && /<version>/{print; exit}' "$pom" |
  sed 's:.*<version>\([^<]*\)</version>.*:\1:')

java_version=$(prop java.version)
boot_minor=$(cut -d. -f1-2 <<<"$parent_version")

# Maven is pinned by the wrapper, not by the pom.
maven_wrapper="$root/.mvn/wrapper/maven-wrapper.properties"
maven_minor=$(sed -n 's:.*apache-maven/\([^/]*\)/.*:\1:p' "$maven_wrapper" |
  head -1 | cut -d. -f1-2)

[[ -n $parent_version && -n $java_version && -n $maven_minor ]] || {
  echo "cannot read versions from $pom or $maven_wrapper" >&2
  exit 1
}

# Each pair is "literal anchor in the doc" -> "value from the pom/wrapper". The
# anchor keeps the replacement away from unrelated numbers; the version right
# after it is fully replaced.
sync() {
  local file=$1 anchor=$2 value=$3
  perl -0777 -pi -e "s/\Q$anchor\E[0-9]+(?:\.[0-9]+)*/$anchor$value/g" "$file"
}

for file in "${readmes[@]}"; do
  before=$(cksum <"$file")
  sync "$file" 'Java ' "$java_version"
  sync "$file" 'JDK ' "$java_version"
  sync "$file" 'Spring%20Boot-' "$boot_minor"
  sync "$file" 'Spring Boot ' "$boot_minor"
  sync "$file" 'Maven multi-module ' "$maven_minor"
  sync "$file" 'Maven ' "$maven_minor"
  if [[ $before == "$(cksum <"$file")" ]]; then
    echo "$(basename "$file"): already up to date"
  else
    echo "$(basename "$file"): updated"
  fi
done

echo "pom: Java $java_version, Spring Boot $parent_version (docs show $boot_minor); wrapper: Maven $maven_minor"
