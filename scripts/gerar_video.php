<?php

declare(strict_types=1);

const LARGURA = 720;
const ALTURA = 1280;
const FPS = 20;
const SEGUNDOS_CENA = 4;

$raiz = dirname(__DIR__);
$readme = $raiz . '/README.md';
$saida = $raiz . '/midia';
$frames = $saida . '/frames';
$icones = $saida . '/icones';
$video = $saida . '/mulher-amparada.mp4';

if (!is_file($readme)) {
    fwrite(STDERR, "README.md não encontrado.\n");
    exit(1);
}

foreach ([$saida, $frames, $icones] as $pasta) {
    if (!is_dir($pasta) && !mkdir($pasta, 0775, true) && !is_dir($pasta)) {
        throw new RuntimeException("Não foi possível criar: $pasta");
    }
}

function executar(string $comando): void
{
    passthru($comando, $codigo);

    if ($codigo !== 0) {
        throw new RuntimeException("Falha ao executar: $comando");
    }
}

function escapar(string $texto): string
{
    return htmlspecialchars($texto, ENT_QUOTES | ENT_XML1, 'UTF-8');
}

function baixarIcone(string $nome, string $destino): bool
{
    $url = "https://fonts.gstatic.com/s/i/materialicons/{$nome}/v1/24px.svg";

    $contexto = stream_context_create([
        'http' => [
            'timeout' => 15,
            'follow_location' => 1,
            'user_agent' => 'MulherAmparada-VideoGenerator/1.0',
        ],
    ]);

    $svg = @file_get_contents($url, false, $contexto);

    if (!$svg || stripos($svg, '<svg') === false) {
        return false;
    }

    if (stripos($svg, '<parsererror') !== false) {
        return false;
    }

    file_put_contents($destino, $svg);

    return true;
}

function prepararIcones(string $pasta): array
{
    $nomes = [
        'shield' => 'security',
        'book' => 'menu_book',
        'people' => 'people',
        'heart' => 'favorite',
        'phone' => 'call',
        'location' => 'location_on',
        'lock' => 'lock',
        'check' => 'check_circle',
    ];

    $resultado = [];

    foreach ($nomes as $chave => $nomeGoogle) {
        $arquivo = $pasta . '/' . $chave . '.svg';

        if (baixarIcone($nomeGoogle, $arquivo)) {
            $resultado[$chave] = $arquivo;
        }
    }

    return $resultado;
}

function extrairCenas(string $markdown): array
{
    $linhas = preg_split('/\R/u', $markdown) ?: [];
    $cenas = [];
    $titulo = 'Mulher Amparada';
    $texto = [];

    foreach ($linhas as $linha) {
        $linha = trim($linha);

        if ($linha === '') {
            continue;
        }

        if (preg_match('/^#{1,3}\s+(.+)$/u', $linha, $m)) {
            if ($texto !== []) {
                $cenas[] = [
                    'titulo' => $titulo,
                    'texto' => array_slice($texto, 0, 3),
                ];
            }

            $titulo = trim($m[1], " \t#");
            $texto = [];
            continue;
        }

        if (preg_match('/^(?:[-*+]|\d+\.)\s+(.+)$/u', $linha, $m)) {
            $item = trim(strip_tags($m[1]));
            $item = preg_replace('/[`*_~]/u', '', $item) ?? $item;

            if ($item !== '') {
                $texto[] = $item;
            }
        }
    }

    if ($texto !== []) {
        $cenas[] = [
            'titulo' => $titulo,
            'texto' => array_slice($texto, 0, 3),
        ];
    }

    if ($cenas === []) {
        $cenas[] = [
            'titulo' => 'Mulher Amparada',
            'texto' => [
                'Projeto gratuito e sem anúncios',
                'Informação e proteção',
                'Desenvolvimento independente',
            ],
        ];
    }

    return array_slice($cenas, 0, 8);
}

function textoSvg(
    string $texto,
    int $x,
    int $y,
    int $tamanho,
    string $cor,
    string $peso = '500'
): string {
    $texto = escapar(mb_substr($texto, 0, 80));

    return '<text x="' . $x . '" y="' . $y .
        '" font-family="DejaVu Sans, sans-serif" font-size="' .
        $tamanho . '" font-weight="' . $peso . '" fill="' .
        $cor . '">' . $texto . '</text>';
}

function criarFrame(
    string $arquivo,
    array $cena,
    int $indice,
    int $total,
    array $icones
): void {
    $progresso = ($indice % (FPS * SEGUNDOS_CENA)) /
        (FPS * SEGUNDOS_CENA);

    $entrada = min(1, $progresso * 5);
    $pulso = sin($indice * 0.09);
    $movimento = (int) (24 * $pulso);
    $opacidade = number_format($entrada, 3, '.', '');

    $rosa = '#ff3f82';
    $rosaClaro = '#ff8fb5';
    $preto = '#080808';
    $branco = '#ffffff';

    $circulos = '';

    for ($i = 0; $i < 7; $i++) {
        $x = (int) ((($i * 137 + $indice * (2 + $i % 3)) % 850) - 65);
        $y = (int) ((($i * 211 + $indice * (3 + $i % 2)) % 1400) - 50);
        $r = 12 + (($i * 11) % 48);
        $cor = $i % 2 === 0 ? $rosa : $rosaClaro;
        $alpha = $i % 2 === 0 ? '.8' : '.55';

        $circulos .= '<circle cx="' . $x . '" cy="' . $y .
            '" r="' . $r . '" fill="' . $cor .
            '" opacity="' . $alpha . '"/>';
    }

    $formas = '<g opacity="0.9">' . $circulos .
        '<circle cx="' . (650 + $movimento) .
        '" cy="350" r="110" fill="none" stroke="' .
        $rosa . '" stroke-width="5"/>' .
        '<path d="M -20 ' . (920 + $movimento) .
        ' Q 180 760 340 970 T 760 880" fill="none" stroke="' .
        $rosaClaro . '" stroke-width="7"/>' .
        '</g>';

    $titulo = trim((string) ($cena['titulo'] ?? 'Mulher Amparada'));
    $itens = $cena['texto'] ?? [];

    $svg = '<svg xmlns="http://www.w3.org/2000/svg" width="' .
        LARGURA . '" height="' . ALTURA . '" viewBox="0 0 ' .
        LARGURA . ' ' . ALTURA . '">' .
        '<defs><linearGradient id="fundo" x1="0" y1="0" x2="1" y2="1">' .
        '<stop offset="0" stop-color="#080808"/>' .
        '<stop offset="1" stop-color="#210b17"/>' .
        '</linearGradient></defs>' .
        '<rect width="100%" height="100%" fill="url(#fundo)"/>' .
        $formas .
        '<g opacity="' . $opacidade . '">' .
        '<rect x="48" y="260" width="624" height="720" rx="38" ' .
        'fill="#120c10" stroke="#59243b" stroke-width="2"/>' .
        '<rect x="48" y="260" width="8" height="720" rx="4" fill="' .
        $rosa . '"/>';

    $svg .= textoSvg('MULHER AMPARADA', 80, 330, 23, $rosaClaro, '700');

    $palavras = preg_split('/\s+/u', $titulo) ?: [$titulo];
    $linhasTitulo = [];
    $linha = '';

    foreach ($palavras as $palavra) {
        $teste = trim($linha . ' ' . $palavra);

        if (mb_strlen($teste) > 21 && $linha !== '') {
            $linhasTitulo[] = $linha;
            $linha = $palavra;
        } else {
            $linha = $teste;
        }
    }

    if ($linha !== '') {
        $linhasTitulo[] = $linha;
    }

    $y = 425;

    foreach (array_slice($linhasTitulo, 0, 3) as $linhaTitulo) {
        $svg .= textoSvg($linhaTitulo, 80, $y, 38, $branco, '700');
        $y += 52;
    }

    $y += 40;

    foreach (array_slice($itens, 0, 3) as $i => $item) {
        $yItem = $y + $i * 105;
        $svg .= '<circle cx="94" cy="' . ($yItem - 9) .
            '" r="17" fill="' . $rosa . '"/>';

        $svg .= textoSvg('✓', 85, $yItem, 22, $branco, '700');

        $palavrasItem = preg_split('/\s+/u', (string) $item) ?: [];
        $linhaItem = '';
        $linhasItem = [];

        foreach ($palavrasItem as $palavra) {
            $teste = trim($linhaItem . ' ' . $palavra);

            if (mb_strlen($teste) > 34 && $linhaItem !== '') {
                $linhasItem[] = $linhaItem;
                $linhaItem = $palavra;
            } else {
                $linhaItem = $teste;
            }
        }

        if ($linhaItem !== '') {
            $linhasItem[] = $linhaItem;
        }

        foreach (array_slice($linhasItem, 0, 2) as $j => $texto) {
            $svg .= textoSvg($texto, 125, $yItem + $j * 30, 19, $branco);
        }
    }

    $icone = array_values($icones)[($indice / (FPS * SEGUNDOS_CENA) | 0) % max(1, count($icones))] ?? null;

    if ($icone !== null && is_file($icone)) {
        $conteudoIcone = file_get_contents($icone);

        if ($conteudoIcone !== false &&
            preg_match('/<svg\b[^>]*>(.*?)<\/svg>/si', $conteudoIcone, $m)) {
            $svg .= '<g transform="translate(292 800) scale(5)" fill="' .
                $rosaClaro . '">' . $m[1] . '</g>';
        }
    }

    $svg .= '</g></svg>';

    file_put_contents($arquivo, $svg);
}

$markdown = file_get_contents($readme);

if ($markdown === false) {
    throw new RuntimeException('Não foi possível ler README.md.');
}

$cenas = extrairCenas($markdown);
$iconesDisponiveis = prepararIcones($icones);

if (count($iconesDisponiveis) === 0) {
    fwrite(STDERR, "Não foi possível obter os SVGs do Google Material Icons.\n");
    exit(1);
}

$totalFrames = count($cenas) * SEGUNDOS_CENA * FPS;

for ($i = 0; $i < $totalFrames; $i++) {
    $cenaIndex = min(
        count($cenas) - 1,
        intdiv($i, SEGUNDOS_CENA * FPS)
    );

    $arquivoFrame = sprintf('%s/frame_%05d.svg', $frames, $i);

    criarFrame(
        $arquivoFrame,
        $cenas[$cenaIndex],
        $i,
        $totalFrames,
        $iconesDisponiveis
    );

    $png = sprintf('%s/frame_%05d.png', $frames, $i);

    executar(
        'rsvg-convert -o ' . escapeshellarg($png) .
        ' ' . escapeshellarg($arquivoFrame)
    );

    unlink($arquivoFrame);
}

executar(
    'ffmpeg -y -hide_banner -loglevel error ' .
    '-framerate ' . FPS .
    ' -i ' . escapeshellarg($frames . '/frame_%05d.png') .
    ' -c:v libx264 -preset medium -crf 21 ' .
    '-pix_fmt yuv420p -movflags +faststart ' .
    escapeshellarg($video)
);

echo "Vídeo criado: midia/mulher-amparada.mp4\n";