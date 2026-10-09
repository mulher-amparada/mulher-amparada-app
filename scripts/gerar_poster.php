<?php

declare(strict_types=1);

$raiz = dirname(__DIR__);
$pastaMidia = $raiz . '/midia';
$destino = $pastaMidia . '/mulher-amparada.png';
$temporario = $pastaMidia . '/poster-gerado.png';

$apiKey = getenv('OPENAI_API_KEY');

if (!$apiKey) {
    exit("Erro: defina a variável OPENAI_API_KEY.\n");
}

$readme = $raiz . '/README.md';

if (!is_file($readme)) {
    exit("Erro: README.md não encontrado.\n");
}

$possiveisLogos = [
    $raiz . '/user1.png',
    $raiz . '/midia/user1.png',
    $raiz . '/images/user1.png',
    $raiz . '/imagens/user1.png',
];

$logo = null;

foreach ($possiveisLogos as $caminho) {
    if (is_file($caminho)) {
        $logo = $caminho;
        break;
    }
}

if ($logo === null) {
    exit("Erro: user1.png não encontrado na raiz ou nas pastas de imagens.\n");
}

if (!is_dir($pastaMidia) &&
    !mkdir($pastaMidia, 0775, true) &&
    !is_dir($pastaMidia)) {
    exit("Erro ao criar a pasta midia.\n");
}

if (!extension_loaded('curl')) {
    exit("Erro: a extensão PHP cURL é necessária.\n");
}

$conteudo = file_get_contents($readme);

if ($conteudo === false) {
    exit("Erro ao ler README.md.\n");
}

$linhas = preg_split('/\R/u', $conteudo) ?: [];
$resumo = [];

foreach ($linhas as $linha) {
    $linha = trim($linha);

    if (preg_match('/^#{1,3}\s+(.+)/u', $linha, $m)) {
        $resumo[] = $m[1];
    } elseif (preg_match('/^[-*]\s+(.+)/u', $linha, $m)) {
        $resumo[] = $m[1];
    }

    if (mb_strlen(implode("\n", $resumo)) > 3500) {
        break;
    }
}

$recursos = implode("\n", $resumo);

$prompt = <<<PROMPT
Create a breathtaking, premium, photorealistic advertising poster for
the real Brazilian Android project "Mulher Amparada Pela Liberdade Feminina".

FORMAT AND ART DIRECTION:
Vertical portrait poster, 2:3 composition, extremely polished premium
technology campaign, cinematic studio photography, deep black background,
glowing vivid pink (#ff3f82), soft pink (#ff8fb5), subtle burgundy accents,
beautiful controlled reflections, realistic metallic materials, volumetric
lighting, elegant shadows, sophisticated typography and balanced editorial
layout. It must look like an expensive professional campaign, not a template.

PHONE:
Show ONLY THE FRONT of a Samsung Galaxy S26 Ultra, with a large realistic
display, refined titanium-colored frame, accurate modern squared-off Ultra
silhouette and rounded screen corners. Three-quarter frontal angle is allowed,
but the screen must face the viewer. Absolutely NO rear view, NO back panel,
NO rear camera array visible, NO second phone showing its back.

The screen displays an elegant fictional presentation of the Mulher Amparada
Android app in a Samsung One UI-inspired interface: recognizable status bar at
the top with time, signal, Wi-Fi and battery indicators, rounded One UI cards,
dark mode, clean spacing, pink accents and a bottom gesture/navigation bar.
Do not imitate a specific existing screenshot.

LOGO:
The provided input image is the authentic project logo reference.
Respect its shape and colors. Do not invent a replacement logo.
Leave a clean, dark, unobstructed area in the upper-left quadrant for the
original logo to be composited afterward. Do not put important text there.

PROJECT CONTENT:
Use the following README-derived project headings and list items as factual
inspiration for the visual features. Prioritize only relevant real features:
{$recursos}

BRANDING:
Project name: "Mulher Amparada"
Subtitle: "Pela Liberdade Feminina"
Tagline: "Informação, apoio e segurança para todas as mulheres."
Emphasize that the project is free and has no advertisements.
Describe it as source-available, not necessarily OSI-approved open source.
Mention that it was developed by a 15-year-old adolescent with ChatGPT support.

VISUAL ELEMENTS:
Elegant pink geometric curves, glowing circles, fine light trails, tasteful
Material-inspired line icons, sophisticated feature cards, atmospheric
pink-lit surfaces and beautiful depth. The phone is the hero object.
Create a cohesive composition with plenty of negative space and readable
Portuguese copy. Use only a few short text elements to avoid clutter.

ACCURACY:
Do not claim availability on Google Play or the Apple App Store.
Do not invent partnerships, certifications, awards, user counts or features.
Do not show the back of the phone.
Do not replace or redraw the real logo.
No watermarks, no mockup marketplace labels, no download-from-store badges.
The result should look exceptionally beautiful, serious, credible and modern.
PROMPT;

$curl = curl_init('https://api.openai.com/v1/images/edits');

curl_setopt_array($curl, [
    CURLOPT_POST => true,
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_CONNECTTIMEOUT => 30,
    CURLOPT_TIMEOUT => 600,
    CURLOPT_HTTPHEADER => [
        'Authorization: Bearer ' . $apiKey,
    ],
    CURLOPT_POSTFIELDS => [
        'model' => 'gpt-image-2.5-sunburst',
        'prompt' => $prompt,
        'size' => '1024x1536',
        'quality' => 'high',
        'image[]' => new CURLFile(
            $logo,
            'image/png',
            'user1.png'
        ),
    ],
]);

$resposta = curl_exec($curl);
$erroCurl = curl_error($curl);
$status = (int) curl_getinfo($curl, CURLINFO_HTTP_CODE);

curl_close($curl);

if ($resposta === false) {
    exit("Erro na conexão com a API: {$erroCurl}\n");
}

$json = json_decode($resposta, true);

if ($status < 200 || $status >= 300) {
    $mensagem = $json['error']['message'] ?? $resposta;
    exit("Erro da API ({$status}): {$mensagem}\n");
}

$base64 = $json['data'][0]['b64_json'] ?? null;

if (!is_string($base64) || $base64 === '') {
    exit("Erro: a API não retornou a imagem esperada.\n");
}

$dadosImagem = base64_decode($base64, true);

if ($dadosImagem === false ||
    file_put_contents($temporario, $dadosImagem) === false) {
    exit("Erro ao salvar a imagem gerada.\n");
}

/*
 * Sobrepõe a logo original para que ela não dependa
 * da reprodução visual feita pelo modelo de IA.
 */

$comando = 'magick ' .
    escapeshellarg($temporario) .
    ' \( ' . escapeshellarg($logo) .
    ' -resize 190x190 \) ' .
    '-gravity northwest -geometry +58+58 -composite ' .
    escapeshellarg($destino);

exec($comando . ' 2>&1', $saidaComando, $codigo);

if ($codigo !== 0 || !is_file($destino)) {
    @unlink($temporario);
    exit(
        "Erro ao aplicar a logo original. Verifique se ImageMagick está instalado.\n" .
        implode("\n", $saidaComando) . "\n"
    );
}

@unlink($temporario);

echo "Pôster gerado: midia/mulher-amparada.png\n";