#!/usr/bin/env bash
set -e

Xvfb :1 -screen 0 1080x1920x24 >/tmp/xvfb.log 2>&1 &
sleep 2

export DISPLAY=:1

fluxbox >/tmp/fluxbox.log 2>&1 &

x11vnc \
  -forever \
  -shared \
  -rfbport 5900 \
  -display :1 \
  >/tmp/x11vnc.log 2>&1 &

websockify --daemon --web=/usr/share/novnc 6080 localhost:5900
