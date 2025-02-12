#!/bin/bash

if [ -z "${1}" ]; then
  echo "Usage: ${1} <schema-file> [<application version>]"
  exit 1
fi
SCHEMA_FILE=${1}
APPLICATION_VERSION=${2}

#echo \\"schema\\": \\"" + " ".join(sys.stdin.readlines()).replace("\\n", "").replace("\\"", "\\\\\\"") + "\\"}\'")'
SCHEMA="$(<${SCHEMA_FILE})"
SCHEMA="${SCHEMA//\"/\\\"}"
SCHEMA="${SCHEMA//[$'\t\r\n ']}"
if [ -z ${APPLICATION_VERSION} ]; then
  echo "{ \"schema\": \"$SCHEMA\" }"
else
  echo "{ \"schema\": \"$SCHEMA\", \"metadata\": { \"properties\": { \"major_version\": \"${APPLICATION_VERSION}\" } } }"
fi
