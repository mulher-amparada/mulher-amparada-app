#!/usr/bin/env bash
set -e

Xvfb :1 -screen 0 1080x1920x24 &
sleep 2

export DISPLAY=:1

fluxbox &
x11vnc -forever -shared -rfbport 5900 -display :1 &
websockify --web /usr/share/novnc 6080 localhost:5900