#!/bin/bash

# Para o script imediatamente se houver qualquer falha
set -e

echo "========================================="
echo "  Iniciando Ambiente de Teste de Mídia   "
echo "========================================="

# 1. Cria a pasta de scripts caso ela não exista
mkdir -p scripts

# 2. Configura e instala os módulos necessários do Node.js
if [ ! -f "package.json" ]; then
    echo "Inicializando ambiente Node.js..."
    npm init -y > /dev/null
fi

echo "Verificando ferramentas de leitura de imagem (jsqr e canvas)..."

# Instala as ferramentas necessárias para a validação
npm install --no-audit --no-fund jsqr canvas typescript tsx @types/node

# 3. Executa a validação das imagens usando o tsx
echo "Iniciando análise do TypeScript..."
npx tsx scripts/validar-qrs.ts

echo "========================================="
echo "        Processo Concluído com Sucesso!  "
echo "========================================="