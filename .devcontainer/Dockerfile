FROM mcr.microsoft.com/devcontainers/base:ubuntu

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    gradle \
    python3 \
    xvfb \
    x11vnc \
    fluxbox \
    websockify \
    novnc \
    curl \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace/mulher-amparada-app

ENV ALLOW_BACKUP=false
ENV SECURITY_FLAG=secure