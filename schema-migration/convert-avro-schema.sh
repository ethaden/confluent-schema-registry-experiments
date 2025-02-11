#!/bin/bash

if [ -z "${1}" ]; then
  echo "Usage: ${1} <schema-file>"
  exit 1
fi

#echo \\"schema\\": \\"" + " ".join(sys.stdin.readlines()).replace("\\n", "").replace("\\"", "\\\\\\"") + "\\"}\'")'
SCHEMA="$(<${1})"
SCHEMA="${SCHEMA//\"/\\\"}"
SCHEMA="${SCHEMA//[$'\t\r\n ']}"
echo "{ \"schema\": \"$SCHEMA\" }"
