#!/usr/bin/env bash
set -euo pipefail

wallet_output_dir="${WALLET_OUTPUT_DIR:-wallet}"
wallet_zip="${WALLET_ZIP:-wallet.zip}"

mkdir -p "${wallet_output_dir}"
find "${wallet_output_dir}" -mindepth 1 -delete 2>/dev/null || rm -rf "${wallet_output_dir:?}/"*

if [ -n "${ORACLE_WALLET_BASE64:-}" ]; then
  echo "$ORACLE_WALLET_BASE64" | base64 -d > "${wallet_zip}"
fi

if [ ! -f "${wallet_zip}" ]; then
  echo "No se encontró ${wallet_zip}. Defina ORACLE_WALLET_BASE64 o coloque el zip en la ruta indicada." >&2
  exit 1
fi

unzip -o "${wallet_zip}" -d "${wallet_output_dir}"

if [ ! -f "${wallet_output_dir}/tnsnames.ora" ]; then
  nested_wallet="${wallet_output_dir}/Wallet_DISPATCHFLOWDB"
  if [ -f "${nested_wallet}/tnsnames.ora" ]; then
    shopt -s dotglob nullglob
    mv "${nested_wallet}"/* "${wallet_output_dir}/"
    rmdir "${nested_wallet}" 2>/dev/null || true
  fi
fi

if [ ! -f "${wallet_output_dir}/tnsnames.ora" ]; then
  echo "tnsnames.ora no encontrado en ${wallet_output_dir}/ tras descomprimir ${wallet_zip}." >&2
  echo "Use el zip original de Oracle (Wallet_DISPATCHFLOWDB.zip, archivos en la raíz del zip)." >&2
  exit 1
fi

echo "Wallet Oracle listo en ${wallet_output_dir}/"
