FROM ://microsoft.com

# Evita perguntas interativas durante a instalação
ENV DEBIAN_FRONTEND=noninteractive

# Instala Java 17, Gradle, dependências de interface gráfica e ferramentas Android
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    sdkmanager \
    gradle \
    python3 \
    xvfb \
    x11vnc \
    fluxbox \
    websockify \
    curl \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Configurações de ambiente do Mulher Amparada
ENV ALLOW_BACKUP=false
ENV TARGET_SDK=37

WORKDIR /workspace/mulher-amparada-app
