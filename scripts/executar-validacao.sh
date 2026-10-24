#!/bin/bash

# Para o script imediatamente se houver qualquer falha
set -e

echo "========================================="
echo "  Iniciando Ambiente de Teste de Mídia   "
echo "========================================="

# 1. Cria a pasta de scripts caso ela não exista no seu celular/repositório
mkdir -p scripts

# 2. Configura e instala os módulos necessários do Node.js
if [ ! -f "package.json" ]; then
    echo "Inicializando ambiente Node.js..."
    npm init -y > /dev/null
fi

echo "Verificando ferramentas de leitura de imagem (jsqr e canvas)..."
# Instala o TypeScript, o jsqr e o canvas (necessário para ler pixels de arquivos de imagem)
npm install --no-audit --no-fund jsqr canvas typescript ts-node @types/node

# 3. Executa a validação das imagens usando o ts-node
echo "Iniciando análise do TypeScript..."
npx ts-node scripts/validar-qrs.ts

echo "========================================="
echo "        Processo Concluído com Sucesso!  "
echo "========================================="
