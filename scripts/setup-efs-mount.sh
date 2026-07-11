#!/usr/bin/env bash
set -euo pipefail

# -----------------------------------------------------------------------------
# DISCLAIMER — alcance actual del script
#
# Este script NO forma parte del despliegue por Docker Hub / GitHub Actions.
# Para montar EFS en un EC2 antes del pipeline, usa la guía manual:
#   docs/configuracion-efs-ec2.md
#
# Uso previsto por ahora: desarrollo local simulando producción con ./run-prod
# (por ejemplo montar EFS real en tu máquina o usar EFS_BASE_PATH=./tmp/efs
#  en .env sin ejecutar este script).
# -----------------------------------------------------------------------------

# Monta AWS EFS vía DNS NFS4 (referencia local; ver disclaimer arriba).
#   export EFS_FILE_SYSTEM_ID=fs-020d791d0e1ad4b39
#   export AWS_REGION=us-east-1
#   sudo -E ./scripts/setup-efs-mount.sh

EFS_FILE_SYSTEM_ID="${EFS_FILE_SYSTEM_ID:-}"
EFS_MOUNT_POINT="${EFS_MOUNT_POINT:-/mnt/dispatch-flow-efs}"
EFS_REGION="${EFS_REGION:-${AWS_REGION:-${AWS_DEFAULT_REGION:-}}}"
EFS_DNS="${EFS_DNS:-}"
EFS_NFS_OPTS="${EFS_NFS_OPTS:-nfsvers=4.1,rsize=1048576,wsize=1048576,hard,timeo=600,retrans=2,noresvport}"

if [ -z "${EFS_DNS}" ]; then
  if [ -z "${EFS_FILE_SYSTEM_ID}" ] || [ -z "${EFS_REGION}" ]; then
    echo "Defina EFS_DNS o bien EFS_FILE_SYSTEM_ID + AWS_REGION (o EFS_REGION)." >&2
    echo "Ejemplo DNS: fs-020d791d0e1ad4b39.efs.us-east-1.amazonaws.com" >&2
    exit 1
  fi
  EFS_DNS="${EFS_FILE_SYSTEM_ID}.efs.${EFS_REGION}.amazonaws.com"
fi

if [ "$(id -u)" -ne 0 ]; then
  echo "Ejecute con sudo: sudo -E $0" >&2
  exit 1
fi

install_nfs_client() {
  if [ -f /etc/os-release ]; then
    # shellcheck disable=SC1091
    . /etc/os-release
  fi
  case "${ID:-linux}" in
    amzn)
      dnf install -y nfs-utils 2>/dev/null || yum install -y nfs-utils
      ;;
    ubuntu|debian)
      apt-get update
      apt-get install -y nfs-common
      ;;
    *)
      echo "Instale nfs-utils o nfs-common manualmente." >&2
      exit 1
      ;;
  esac
}

install_nfs_client
mkdir -p "${EFS_MOUNT_POINT}"

if mountpoint -q "${EFS_MOUNT_POINT}"; then
  echo "EFS ya montado en ${EFS_MOUNT_POINT}"
else
  echo "Montando ${EFS_DNS}:/ en ${EFS_MOUNT_POINT}..."
  mount -t nfs4 -o "${EFS_NFS_OPTS}" "${EFS_DNS}:/" "${EFS_MOUNT_POINT}"
fi

fstab_entry="${EFS_DNS}:/ ${EFS_MOUNT_POINT} nfs4 ${EFS_NFS_OPTS},_netdev 0 0"
if ! grep -qF "${EFS_MOUNT_POINT}" /etc/fstab 2>/dev/null; then
  echo "${fstab_entry}" >> /etc/fstab
  echo "Entrada añadida a /etc/fstab"
fi

chmod 1777 "${EFS_MOUNT_POINT}"
echo "EFS listo: ${EFS_DNS}:/ → ${EFS_MOUNT_POINT}"
