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
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p "${ANDROID_HOME}/cmdline-tools" \
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip \
       -O /tmp/android-tools.zip \
    && unzip -q /tmp/android-tools.zip -d /tmp/android-tools \
    && mkdir -p "${ANDROID_HOME}/cmdline-tools/latest" \
    && mv /tmp/android-tools/cmdline-tools/* "${ANDROID_HOME}/cmdline-tools/latest/" \
    && rm -rf /tmp/android-tools /tmp/android-tools.zip

RUN yes | sdkmanager --licenses >/dev/null \
    && sdkmanager \
       "platform-tools" \
       "platforms;android-37" \
       "build-tools;37.0.0"

WORKDIR /workspace/mulher-amparada-app

ENV ALLOW_BACKUP=false
ENV SECURITY_FLAG=secure