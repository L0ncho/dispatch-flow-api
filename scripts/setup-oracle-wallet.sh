#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "$0")/.." && pwd)"
wallet_dir="${WALLET_DIR:-${project_root}/Wallet_DISPATCHFLOWDB}"
wallet_zip="${WALLET_ZIP_PATH:-${project_root}/Wallet_DISPATCHFLOWDB.zip}"

if [ -f "${wallet_dir}/tnsnames.ora" ] && { [ -f "${wallet_dir}/cwallet.sso" ] || [ -f "${wallet_dir}/ewallet.p12" ]; }; then
  echo "Wallet Oracle ya disponible en ${wallet_dir}"
  exit 0
fi

if [ ! -f "${wallet_zip}" ]; then
  echo "No se encontró el wallet zip en ${wallet_zip}" >&2
  echo "Copia Wallet_DISPATCHFLOWDB.zip a la raíz del proyecto o define WALLET_ZIP_PATH." >&2
  exit 1
fi

mkdir -p "${wallet_dir}"
unzip -o "${wallet_zip}" -d "${wallet_dir}"
echo "Wallet Oracle descomprimido en ${wallet_dir}"
