import * as fs from 'fs';
import * as path from 'path';
import { createContext, Canvas, Image } from 'canvas';
import jsqr from 'jsqr';

// Extensões de imagem suportadas no repositório
const EXTENSOES_VALIDAS = ['.png', '.jpg', '.jpeg'];

async function validarQRCodeNaImagem(caminhoArquivo: string): Promise<boolean> {
  return new Promise((resolve) => {
    const img = new Image();
    
    img.onload = () => {
      // Cria uma área de desenho (canvas) do tamanho da imagem
      const canvas = new Canvas(img.width, img.height);
      const ctx = canvas.getContext('2d');
      
      // Desenha a imagem no canvas para extrair os pixels
      ctx.drawImage(img, 0, 0, img.width, img.height);
      const imgData = ctx.getImageData(0, 0, img.width, img.height);
      
      // O jsqr tenta decodificar o QR Code nos pixels extraídos
      const codigoObtido = jsqr(imgData.data, imgData.width, imgData.height);
      
      if (codigoObtido) {
        console.log(`✅ [VÁLIDO] ${path.basename(caminhoArquivo)}: QR Code lido com sucesso! -> Conteúdo: "${codigoObtido.data}"`);
        resolve(true);
      } else {
        // Se a imagem se chamar "qr.png" ou tiver "qr" no nome, ela OBRIGATORIAMENTE deveria ter um código legível
        if (path.basename(caminhoArquivo).toLowerCase().includes('qr')) {
          console.log(`❌ [ERRO] ${path.basename(caminhoArquivo)}: É um arquivo de QR Code, mas está ilegível ou corrompido!`);
          resolve(false);
        } else {
          // Para outras imagens de interface (como title.png ou user1.png), é normal não ter QR Code
          console.log(`ℹ️ [IGNORADO] ${path.basename(caminhoArquivo)}: Imagem comum de interface (sem QR Code detectado).`);
          resolve(true);
        }
      }
    };

    img.onerror = (err) => {
      console.log(`💥 [ERRO CRÍTICO] Não foi possível carregar a imagem ${path.basename(caminhoArquivo)}:`, err);
      resolve(false);
    };

    // Carrega o arquivo de imagem
    img.src = fs.readFileSync(caminhoArquivo);
  });
}

async function executarVerificacao() {
  // Analisa a raiz do projeto (onde estão as imagens 10.png, v1.png, etc.)
  const diretorioRaiz = process.cwd(); 
  console.log(`=== Iniciando Varredura de Imagens em: ${diretorioRaiz} ===\n`);
  
  const arquivos = fs.readdirSync(diretorioRaiz);
  let possuiErros = false;

  for (const arquivo of arquivos) {
    const extensao = path.extname(arquivo).toLowerCase();
    
    if (EXTENSOES_VALIDAS.includes(extensao)) {
      const caminhoCompleto = path.join(diretorioRaiz, arquivo);
      const resultado = await validarQRCodeNaImagem(caminhoCompleto);
      if (!resultado) possuiErros = true;
    }
  }

  console.log('\n=== Varredura Finalizada ===');
  if (possuiErros) {
    console.log('⚠️ Existem problemas com imagens de QR Code no repositório.');
    process.exit(1); // Fecha com erro para alertar o sistema
  } else {
    console.log('🚀 Todas as imagens estão íntegras e validadas!');
    process.exit(0);
  }
}

executarVerificacao();
