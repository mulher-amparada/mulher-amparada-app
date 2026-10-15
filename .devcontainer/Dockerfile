FROM ubuntu:22.04

# Evita perguntas interativas durante a instalação
ENV DEBIAN_FRONTEND=noninteractive

# Instala o sistema base, interface gráfica leve e ferramentas web
RUN apt-get update && apt-get install -y \
    xfce4 \
    xfce4-goodies \
    xvfb \
    x11vnc \
    novnc \
    supervisor \
    curl \
    git \
    chromium-browser \
    && apt-get clean

# Configura as variáveis de ambiente para a tela do OS
ENV DISPLAY=:1
ENV RESOLUTION=1280x720x24

# Copia os arquivos do projeto para dentro do sistema do OS
WORKDIR /home/mulher-amparada
COPY . .

# Expõe a porta do sistema operacional para acesso web
EXPOSE 6080

# Comando para iniciar os serviços do OS
CMD ["/bin/bash", "start-os.sh"]
