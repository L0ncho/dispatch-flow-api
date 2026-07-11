#!/usr/bin/env bash
set -euo pipefail

endpoint="${AWS_S3_ENDPOINT:-http://localhost:4566}"
bucket="${S3_BUCKET_NAME:-dispatch-flow-local}"
region="${AWS_REGION:-us-east-1}"

echo "Esperando LocalStack en ${endpoint}..."
for attempt in $(seq 1 30); do
  if curl -sf "${endpoint}/_localstack/health" | grep -q '"s3": "available"'; then
    echo "LocalStack listo."
    break
  fi
  if [ "${attempt}" -eq 30 ]; then
    echo "LocalStack no respondió a tiempo." >&2
    exit 1
  fi
  sleep 2
done

echo "Creando bucket s3://${bucket}..."
if command -v awslocal >/dev/null 2>&1; then
  awslocal s3 mb "s3://${bucket}" --region "${region}" 2>/dev/null || true
else
  curl -sf -X PUT "${endpoint}/${bucket}" >/dev/null || true
fi

echo "Bucket ${bucket} disponible."
