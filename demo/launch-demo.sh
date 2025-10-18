#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 [javafx|web]"
  exit 1
fi

case "$1" in
  javafx)
    (cd "$(dirname "$0")/javafx-planning" && ../gradlew run)
    ;;
  web)
    (cd "$(dirname "$0")/web-planning" && ../gradlew bootRun)
    ;;
  *)
    echo "Option inconnue : $1"
    exit 1
    ;;
esac
