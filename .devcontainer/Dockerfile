FROM mcr.microsoft.com/devcontainers/base:ubuntu

ENV DEBIAN_FRONTEND=noninteractive
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH="${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools"

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
    unzip \
    wget \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace/mulher-amparada-app

ENV ALLOW_BACKUP=false
ENV SECURITY_FLAG=secure