FROM ://microsoft.com

ENV DEBIAN_FRONTEND=noninteractive

# Instala Java 17, ferramentas de compilação do Android e servidores de interface gráfica
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

# Configurações de diretório do projeto Mulher Amparada
WORKDIR /workspace/mulher-amparada-app

ENV ALLOW_BACKUP=false
ENV SECURITY_FLAG=secure
