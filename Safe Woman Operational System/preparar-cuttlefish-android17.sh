#!/usr/bin/env bash
set -Eeuo pipefail

BRANCH="android17-release"
MANIFEST_URL="https://android.googlesource.com/platform/manifest"
TARGET="aosp_cf_x86_64_only_phone-aosp_current-userdebug"
WORKDIR="${AOSP_DIR:-$PWD/aosp-android17-cuttlefish}"
JOBS="${JOBS:-8}"
MIN_DISK_GB=400
MIN_RAM_GB=64

fail() {
  printf 'ERRO: %s\n' "$*" >&2
  exit 1
}

[[ "$(uname -s)" == "Linux" ]] || fail "A compilação AOSP exige Linux de 64 bits."
[[ "$(uname -m)" == "x86_64" ]] || fail "Este alvo exige um host x86_64."

command -v git >/dev/null 2>&1 || fail "Instale git."
command -v curl >/dev/null 2>&1 || fail "Instale curl."
command -v python3 >/dev/null 2>&1 || fail "Instale Python 3."

available_disk_kb="$(df -Pk "$(dirname "$WORKDIR")" | awk 'NR==2 {print $4}')"
available_disk_gb="$((available_disk_kb / 1024 / 1024))"
available_ram_kb="$(awk '/MemTotal/ {print $2}' /proc/meminfo)"
available_ram_gb="$((available_ram_kb / 1024 / 1024))"

printf 'Espaço livre: %s GB (mínimo recomendado: %s GB)\n' "$available_disk_gb" "$MIN_DISK_GB"
printf 'RAM: %s GB (mínimo recomendado: %s GB)\n' "$available_ram_gb" "$MIN_RAM_GB"

(( available_disk_gb >= MIN_DISK_GB )) || fail "Espaço insuficiente. Libere pelo menos 400 GB antes de sincronizar."
(( available_ram_gb >= MIN_RAM_GB )) || fail "RAM insuficiente. O requisito oficial é pelo menos 64 GB."

if ! command -v repo >/dev/null 2>&1; then
  mkdir -p "$HOME/bin"
  curl -fsSL https://storage.googleapis.com/git-repo-downloads/repo -o "$HOME/bin/repo"
  chmod a+x "$HOME/bin/repo"
  export PATH="$HOME/bin:$PATH"
fi

mkdir -p "$WORKDIR"
cd "$WORKDIR"

if [[ ! -d .repo ]]; then
  repo init --partial-clone -u "$MANIFEST_URL" -b "$BRANCH"
else
  printf 'Manifesto Repo já inicializado; atualizando para %s.\n' "$BRANCH"
  repo init --partial-clone -u "$MANIFEST_URL" -b "$BRANCH"
fi

repo sync -c -j"$JOBS" --fail-fast

source build/envsetup.sh
lunch "$TARGET"
m -j"$JOBS"

printf '\nCompilação concluída. Artefatos disponíveis em: %s/out\n' "$WORKDIR"
