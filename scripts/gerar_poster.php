<?php

declare(strict_types=1);

$raiz = dirname(__DIR__);
$pastaMidia = $raiz . '/midia';
$destino = $pastaMidia . '/mulher-amparada.png';

$logos = [
    $raiz . '/user1.png',
    $raiz . '/midia/user1.png',
    $raiz . '/images/user1.png',
    $raiz . '/imagens/user1.png',
];

$logo = null;

foreach ($logos as $caminho) {
    if (is_file($caminho)) {
        $logo = $caminho;
        break;
    }
}

if ($logo === null) {
    exit("Erro: user1.png não encontrado.\n");
}

if (!is_dir($pastaMidia) &&
    !mkdir($pastaMidia, 0775, true) &&
    !is_dir($pastaMidia)) {
    exit("Erro ao criar a pasta midia.\n");
}

$magick = trim((string) shell_exec('command -v magick 2>/dev/null'));

if ($magick === '') {
    $magick = trim((string) shell_exec('command -v convert 2>/dev/null'));
}

if ($magick === '') {
    exit("Erro: ImageMagick não está instalado.\n");
}

$svg = <<<'SVG'
<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="1800" viewBox="0 0 1200 1800">
<defs>
  <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
    <stop offset="0" stop-color="#08070b"/>
    <stop offset=".55" stop-color="#1c0b18"/>
    <stop offset="1" stop-color="#050507"/>
  </linearGradient>
  <linearGradient id="pink" x1="0" y1="0" x2="1" y2="1">
    <stop offset="0" stop-color="#ff8fb5"/>
    <stop offset=".55" stop-color="#ff3f82"/>
    <stop offset="1" stop-color="#a91855"/>
  </linearGradient>
  <linearGradient id="metal" x1="0" y1="0" x2="1" y2="0">
    <stop offset="0" stop-color="#4b4149"/>
    <stop offset=".25" stop-color="#e1d3dc"/>
    <stop offset=".5" stop-color="#62545e"/>
    <stop offset=".8" stop-color="#d9cbd4"/>
    <stop offset="1" stop-color="#51434d"/>
  </linearGradient>
  <linearGradient id="screen" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0" stop-color="#25101f"/>
    <stop offset="1" stop-color="#09090d"/>
  </linearGradient>
  <radialGradient id="glow">
    <stop offset="0" stop-color="#ff3f82" stop-opacity=".5"/>
    <stop offset="1" stop-color="#ff3f82" stop-opacity="0"/>
  </radialGradient>
  <filter id="blur">
    <feGaussianBlur stdDeviation="25"/>
  </filter>
</defs>

<rect width="1200" height="1800" fill="url(#bg)"/>
<circle cx="850" cy="850" r="520" fill="url(#glow)"/>
<circle cx="150" cy="1430" r="350" fill="url(#glow)"/>

<g fill="none" stroke="#ff3f82">
  <path d="M-100 430 C180 170 460 300 740 40" stroke-opacity=".28" stroke-width="3"/>
  <path d="M-80 475 C200 215 490 345 760 80" stroke-opacity=".14" stroke-width="2"/>
  <path d="M630 1740 C900 1490 1050 1320 1280 1280" stroke-opacity=".35" stroke-width="3"/>
</g>

<g fill="#ff8fb5" opacity=".7">
  <circle cx="1010" cy="230" r="4"/>
  <circle cx="1060" cy="270" r="2"/>
  <circle cx="1100" cy="220" r="3"/>
  <circle cx="900" cy="310" r="2"/>
  <circle cx="150" cy="1170" r="3"/>
  <circle cx="200" cy="1210" r="2"/>
  <circle cx="1000" cy="1460" r="3"/>
</g>

<text x="600" y="110" text-anchor="middle" fill="#ff8fb5" font-family="DejaVu Sans,sans-serif" font-size="22" letter-spacing="6">PROJETO ANDROID INDEPENDENTE</text>
<text x="600" y="205" text-anchor="middle" fill="#ffffff" font-family="DejaVu Sans,sans-serif" font-size="70" font-weight="bold">MULHER</text>
<text x="600" y="285" text-anchor="middle" fill="url(#pink)" font-family="DejaVu Sans,sans-serif" font-size="78" font-weight="bold">AMPARADA</text>
<rect x="385" y="315" width="430" height="4" rx="2" fill="url(#pink)"/>
<text x="600" y="365" text-anchor="middle" fill="#f8eaf0" font-family="DejaVu Sans,sans-serif" font-size="25" letter-spacing="2">PELA LIBERDADE FEMININA</text>
<text x="600" y="420" text-anchor="middle" fill="#d8c5d0" font-family="DejaVu Sans,sans-serif" font-size="24">Informação, apoio e segurança.</text>

<!-- Dispositivo frontal -->
<ellipse cx="600" cy="1020" rx="330" ry="500" fill="#ff3f82" opacity=".25" filter="url(#blur)"/>

<g transform="translate(355 470) rotate(-3 245 510)">
  <rect x="0" y="0" width="490" height="1010" rx="70" fill="url(#metal)" stroke="#f8eaf0" stroke-width="2"/>
  <rect x="9" y="9" width="472" height="992" rx="62" fill="#050507"/>
  <rect x="19" y="19" width="452" height="972" rx="54" fill="url(#screen)"/>

  <!-- Barra de status -->
  <text x="48" y="57" fill="#ffffff" font-family="DejaVu Sans,sans-serif" font-size="18" font-weight="bold">9:41</text>
  <g fill="none" stroke="#ffffff" stroke-width="3" stroke-linecap="round">
    <path d="M350 46 Q363 32 376 46"/>
    <path d="M355 52 Q363 44 371 52"/>
    <circle cx="363" cy="57" r="2" fill="#ffffff"/>
    <rect x="390" y="39" width="29" height="17" rx="4"/>
    <rect x="420" y="44" width="4" height="7" rx="1" fill="#ffffff"/>
    <rect x="394" y="43" width="18" height="9" rx="2" fill="#ff8fb5" stroke="none"/>
    <path d="M331 55 L337 42 L343 55 Z" fill="#ffffff" stroke="none"/>
  </g>

  <text x="45" y="112" fill="#ff8fb5" font-family="DejaVu Sans,sans-serif" font-size="15" letter-spacing="2">MULHER AMPARADA</text>
  <text x="45" y="165" fill="#ffffff" font-family="DejaVu Sans,sans-serif" font-size="31" font-weight="bold">Apoio para</text>
  <text x="45" y="205" fill="#ffffff" font-family="DejaVu Sans,sans-serif" font-size="31" font-weight="bold">seguir em frente.</text>
  <text x="45" y="244" fill="#d8c5d0" font-family="DejaVu Sans,sans-serif" font-size="15">Proteção, informação e organização</text>

  <rect x="35" y="276" width="420" height="105" rx="23" fill="#321525" stroke="#ff3f82" stroke-width="2"/>
  <circle cx="82" cy="328" r="25" fill="url(#pink)"/>
  <path d="M82 312 V344 M66 328 H98" stroke="#ffffff" stroke-width="5" stroke-linecap="round"/>
  <text x="122" y="320" fill="#ffffff" font-family="DejaVu Sans,sans-serif" font-size="19" font-weight="bold">Ajuda e emergência</text>
  <text x="122" y="350" fill="#d8c5d0" font-family="DejaVu Sans,sans-serif" font-size="14">Acesso rápido a contatos úteis</text>

  <rect x="35" y="398" width="200" height="130" rx="21" fill="#17131b" stroke="#573044" stroke-width="2"/>
  <circle cx="77" cy="438" r="19" fill="#ff3f82" fill-opacity=".22"/>
  <path d="M68 439 L75 446 L88 430" fill="none" stroke="#ff8fb5" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"/>
  <text x="57" y="483" fill="#ffffff" font-family="DejaVu Sans,sans-serif" font-size="17" font-weight="bold">Direitos</text>
  <text x="57" y="507" fill="#cbb8c4" font-family="DejaVu Sans,sans-serif" font-size="13">Informação útil</text>

  <rect x="255" y="398" width="200" height="130" rx="21" fill="#17131b" stroke="#573044" stroke-width="2"/>
  <circle cx="297" cy="438" r="19" fill="#ff3f82" fill-opacity=".22"/>
  <path d="M287 439 Q297 426 307 439 Q307 449 297 455 Q287 449 287 439Z" fill="none" stroke="#ff8fb5" stroke-width="3"/>
  <text x="277" y="483" fill="#ffffff" font-family="DejaVu Sans,sans-serif" font-size="17" font-weight="bold">Organização</text>
  <text x="277" y="507" fill="#cbb8c4" font-family="DejaVu Sans,sans-serif" font-size="13">Rotina e registros</text>

  <rect x="35" y="545" width="420" height="104" rx="21" fill="#17131b" stroke="#573044" stroke-width="2"/>
  <circle cx="81" cy="597" r="23" fill="#ff3f82" fill-opacity=".22"/>
  <path d="M71 597 L79 605 L93 587" fill="none" stroke="#ff8fb5" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"/>
  <text x="121" y="588" fill="#ffffff" font-family="DejaVu Sans,sans-serif" font-size="19" font-weight="bold">Arquivo seguro</text>
  <text x="121" y="618" fill="#d8c5d0" font-family="DejaVu Sans,sans-serif" font-size="14">Privacidade no dispositivo</text>

  <rect x="35" y="670" width="420" height="83" rx="21" fill="url(#pink)"/>
  <text x="245" y="704" text-anchor="middle" fill="#ffffff" font-family="DejaVu Sans,sans-serif" font-size="18" font-weight="bold">Informação. Amparo. Autonomia.</text>
  <text x="245" y="731" text-anchor="middle" fill="#ffffff" font-family="DejaVu Sans,sans-serif" font-size="13">Um projeto independente e gratuito</text>

  <text x="245" y="800" text-anchor="middle" fill="#ff8fb5" font-family="DejaVu Sans,sans-serif" font-size="15">GRATUITO • SEM ANÚNCIOS</text>
  <text x="245" y="830" text-anchor="middle" fill="#cbb8c4" font-family="DejaVu Sans,sans-serif" font-size="13">Desenvolvido com apoio do ChatGPT</text>

  <!-- Barra de navegação por gestos -->
  <rect x="170" y="951" width="150" height="6" rx="3" fill="#ffffff"/>
</g>

<rect x="100" y="1550" width="1000" height="135" rx="28" fill="#171019" stroke="#6d2c4a" stroke-width="2"/>
<circle cx="165" cy="1618" r="24" fill="#ff3f82" fill-opacity=".22"/>
<path d="M165 1604 V1632 M151 1618 H179" stroke="#ff8fb5" stroke-width="4" stroke-linecap="round"/>
<text x="215" y="1608" fill="#ffffff" font-family="DejaVu Sans,sans-serif" font-size="24" font-weight="bold">Gratuito e sem anúncios</text>
<text x="215" y="1647" fill="#cbb8c4" font-family="DejaVu Sans,sans-serif" font-size="18">Projeto independente com código-fonte disponível</text>

<text x="600" y="1735" text-anchor="middle" fill="#ff8fb5" font-family="DejaVu Sans,sans-serif" font-size="17" letter-spacing="3">MULHER AMPARADA</text>
<text x="600" y="1770" text-anchor="middle" fill="#8f7c89" font-family="DejaVu Sans,sans-serif" font-size="14">Pela liberdade feminina</text>
</svg>
SVG;

$svgTemporario = tempnam(sys_get_temp_dir(), 'poster_');

if ($svgTemporario === false ||
    file_put_contents($svgTemporario, $svg) === false) {
    exit("Erro ao preparar o pôster.\n");
}

$comando = escapeshellarg($magick) . ' ' .
    escapeshellarg($svgTemporario) . ' ' .
    '-background "#08070b" -alpha remove -alpha off ' .
    escapeshellarg($destino);

exec($comando . ' 2>&1', $saida, $codigo);
@unlink($svgTemporario);

if ($codigo !== 0 || !is_file($destino)) {
    exit(
        "Erro ao gerar o PNG. Verifique o suporte a SVG do ImageMagick.\n" .
        implode("\n", $saida) . "\n"
    );
}

$temporarioFinal = $destino . '.tmp.png';

$comandoLogo = escapeshellarg($magick) . ' ' .
    escapeshellarg($destino) . ' ' .
    '\( ' . escapeshellarg($logo) . ' -resize 150x150 \) ' .
    '-gravity northwest -geometry +45+25 -composite ' .
    escapeshellarg($temporarioFinal);

exec($comandoLogo . ' 2>&1', $saidaLogo, $codigoLogo);

if ($codigoLogo !== 0 || !is_file($temporarioFinal)) {
    @unlink($destino);
    exit(
        "Erro ao aplicar o logo original.\n" .
        implode("\n", $saidaLogo) . "\n"
    );
}

if (!rename($temporarioFinal, $destino)) {
    @unlink($temporarioFinal);
    exit("Erro ao finalizar o pôster.\n");
}

echo "Pôster gerado: midia/mulher-amparada.png\n";