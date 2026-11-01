#!/usr/bin/env bash
set -euo pipefail

APK="Código-fonte do app = Mulher Amparada/app-release.apk"
OUT="artefatos-emulador"

mkdir -p "$OUT"

if [[ ! -s "$APK" ]]; then
  echo "ERRO: APK não encontrado ou vazio: $APK"
  exit 1
fi

adb wait-for-device

for tentativa in $(seq 1 60); do
  BOOT="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')"
  if [[ "$BOOT" == "1" ]]; then
    break
  fi
  sleep 2
done

if [[ "$(adb shell getprop sys.boot_completed | tr -d '\r')" != "1" ]]; then
  echo "ERRO: o Android virtual não concluiu a inicialização."
  exit 1
fi

adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

adb install -r "$APK"

if adb root; then
  adb wait-for-device
  adb shell id > "$OUT/acesso-adb.txt"
else
  echo "A imagem não disponibilizou adb root." > "$OUT/acesso-adb.txt"
  adb shell id >> "$OUT/acesso-adb.txt"
fi

adb shell am start -n com.mulheres/.EntradaActivity
sleep 20

adb shell screencap -p /sdcard/mulher-amparada.png
adb pull /sdcard/mulher-amparada.png "$OUT/tela-inicial.png"

adb shell getprop > "$OUT/propriedades-android.txt"
adb logcat -d -t 5000 > "$OUT/logcat.txt"

echo "Emulação concluída."
echo "Versão Android:"
adb shell getprop ro.build.version.release
echo "Arquitetura:"
adb shell getprop ro.product.cpu.abi
echo "Pacote instalado:"
adb shell pm list packages | grep "com.mulheres"
