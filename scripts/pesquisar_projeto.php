<?php

declare(strict_types=1);

$raiz = dirname(__DIR__);
$pasta = $raiz . '/pesquisas';
$data = date('Y-m-d_H-i-s');

if (!is_dir($pasta) && !mkdir($pasta, 0775, true) && !is_dir($pasta)) {
    exit("Erro ao criar a pasta pesquisas.\n");
}

if (!extension_loaded('curl') || !class_exists('DOMDocument')) {
    exit("Instale as extensões PHP cURL e XML.\n");
}

$projeto = 'Mulher Amparada';
$repositorio = 'https://github.com/mulher-amparada/mulher-amparada-app';
$site = 'https://intellicore555-oss.github.io/mulher-amparada-app/';

$consultas = [
    '"Mulher Amparada"',
    '"Mulher Amparada" aplicativo',
    '"Mulher Amparada" Android',
    '"Mulher Amparada Pela Liberdade Feminina"',
    '"mulher-amparada/mulher-amparada-app"',
    '"intellicore555-oss.github.io/mulher-amparada-app"',
    '"mulheramparada"',
    '"mulher amparada" projeto',
    '"mulher-amparada-app" -github',
    '"Mulher Amparada" violência mulheres',
    'site:dev.to/mulher_amparada "Mulher Amparada"',
    'site:tabnews.com.br "Mulher Amparada"',
    'site:hashnode.dev "Mulher Amparada"',
    'site:tumblr.com/mulheramparada "Mulher Amparada"',
    'site:reddit.com "Mulher Amparada" aplicativo',
    'site:linkedin.com "Mulher Amparada" aplicativo',
    'site:youtube.com "Mulher Amparada" aplicativo',
    'site:medium.com "Mulher Amparada" aplicativo',
];

function requisicao(string $url, array $headers = []): ?string
{
    $curl = curl_init($url);

    curl_setopt_array($curl, [
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_FOLLOWLOCATION => true,
        CURLOPT_MAXREDIRS => 5,
        CURLOPT_CONNECTTIMEOUT => 15,
        CURLOPT_TIMEOUT => 35,
        CURLOPT_ENCODING => '',
        CURLOPT_USERAGENT => 'Mozilla/5.0 (compatible; ProjetoResearchBot/1.0)',
        CURLOPT_HTTPHEADER => $headers,
    ]);

    $resposta = curl_exec($curl);
    $status = (int) curl_getinfo($curl, CURLINFO_HTTP_CODE);

    curl_close($curl);

    if (!is_string($resposta) || $status < 200 || $status >= 300) {
        return null;
    }

    return $resposta;
}

function limpar(?string $texto): string
{
    $texto = html_entity_decode(
        strip_tags($texto ?? ''),
        ENT_QUOTES | ENT_HTML5,
        'UTF-8'
    );

    return trim(preg_replace('/\s+/u', ' ', $texto) ?? '');
}

function buscarWeb(string $consulta): array
{
    $url = 'https://html.duckduckgo.com/html/?q=' . urlencode($consulta);
    $html = requisicao($url);

    if ($html === null) {
        return [];
    }

    libxml_use_internal_errors(true);

    $documento = new DOMDocument();

    if (!$documento->loadHTML($html)) {
        libxml_clear_errors();
        return [];
    }

    $xpath = new DOMXPath($documento);
    $nos = $xpath->query('//a[contains(@class,"result__a")]');
    $resultados = [];

    if ($nos !== false) {
        foreach ($nos as $no) {
            $titulo = limpar($no->textContent);
            $link = trim($no->getAttribute('href'));

            if ($titulo === '' || $link === '') {
                continue;
            }

            if (str_starts_with($link, '//')) {
                $link = 'https:' . $link;
            }

            if (!filter_var($link, FILTER_VALIDATE_URL)) {
                continue;
            }

            $pai = $no->parentNode?->parentNode;
            $resumo = '';

            if ($pai !== null) {
                $snippets = (new DOMXPath($documento))->query(
                    './/*[contains(@class,"result__snippet")]',
                    $pai
                );

                if ($snippets !== false && $snippets->length > 0) {
                    $resumo = limpar($snippets->item(0)?->textContent);
                }
            }

            $resultados[] = [
                'fonte' => 'DuckDuckGo',
                'consulta' => $consulta,
                'titulo' => $titulo,
                'url' => $link,
                'resumo' => $resumo,
            ];
        }
    }

    libxml_clear_errors();

    return $resultados;
}

function pesquisarGitHub(string $endpoint, string $tipo): array
{
    $url = 'https://api.github.com/repos/mulher-amparada/mulher-amparada-app'
        . $endpoint;

    $resposta = requisicao($url, [
        'Accept: application/vnd.github+json',
        'X-GitHub-Api-Version: 2022-11-28',
    ]);

    if ($resposta === null) {
        return [];
    }

    $dados = json_decode($resposta, true);

    if (!is_array($dados)) {
        return [];
    }

    if (isset($dados['message'])) {
        return [[
            'fonte' => 'GitHub API',
            'tipo' => $tipo,
            'erro' => $dados['message'],
        ]];
    }

    $lista = array_is_list($dados) ? $dados : [$dados];
    $resultados = [];

    foreach ($lista as $item) {
        if (!is_array($item)) {
            continue;
        }

        $resultados[] = [
            'fonte' => 'GitHub API',
            'tipo' => $tipo,
            'titulo' => $item['name']
                ?? $item['tag_name']
                ?? $item['title']
                ?? $item['full_name']
                ?? $tipo,
            'url' => $item['html_url']
                ?? $item['url']
                ?? $repositorio,
            'resumo' => $item['description']
                ?? $item['body']
                ?? $item['message']
                ?? '',
            'data' => $item['published_at']
                ?? $item['created_at']
                ?? $item['updated_at']
                ?? null,
        ];
    }

    return $resultados;
}

$resultados = [];
$falhas = [];

foreach ($consultas as $consulta) {
    echo "Pesquisando: {$consulta}\n";

    $encontrados = buscarWeb($consulta);

    if ($encontrados === []) {
        $falhas[] = [
            'consulta' => $consulta,
            'motivo' => 'Nenhum resultado retornado; pode ter ocorrido bloqueio ou falha.',
        ];
    }

    $resultados = array_merge($resultados, $encontrados);

    usleep(800000);
}

echo "Consultando dados públicos do GitHub...\n";

$endpoints = [
    '' => 'Repositório',
    '/releases?per_page=100' => 'Releases',
    '/issues?state=all&per_page=100' => 'Issues e pull requests',
    '/commits?per_page=100' => 'Commits recentes',
    '/contributors?per_page=100' => 'Contribuidores',
];

$github = [];

foreach ($endpoints as $endpoint => $tipo) {
    $github = array_merge(
        $github,
        pesquisarGitHub($endpoint, $tipo)
    );
}

$unicos = [];

foreach ($resultados as $resultado) {
    $chave = strtolower($resultado['url']);

    if (!isset($unicos[$chave])) {
        $unicos[$chave] = $resultado;
    } elseif (
        ($unicos[$chave]['resumo'] ?? '') === '' &&
        ($resultado['resumo'] ?? '') !== ''
    ) {
        $unicos[$chave]['resumo'] = $resultado['resumo'];
    }
}

$resultados = array_values($unicos);

usort(
    $resultados,
    static fn(array $a, array $b): int =>
        strcasecmp($a['titulo'] ?? '', $b['titulo'] ?? '')
);

$relatorio = [
    'projeto' => $projeto,
    'repositorio' => $repositorio,
    'site' => $site,
    'pesquisado_em' => date(DATE_ATOM),
    'consultas' => $consultas,
    'total_resultados_web' => count($resultados),
    'resultados_web' => $resultados,
    'dados_github' => $github,
    'consultas_sem_resultados' => $falhas,
    'observacao' => 'Os resultados dependem da cobertura e disponibilidade das fontes consultadas.',
];

$arquivoJson = $pasta . '/pesquisa_' . $data . '.json';
$arquivoMd = $pasta . '/pesquisa_' . $data . '.md';

$json = json_encode(
    $relatorio,
    JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE |
    JSON_UNESCAPED_SLASHES | JSON_INVALID_UTF8_SUBSTITUTE
);

if ($json === false || file_put_contents($arquivoJson, $json . "\n") === false) {
    exit("Erro ao salvar o relatório JSON.\n");
}

$md = "# Pesquisa na internet — Mulher Amparada\n\n";
$md .= '- Data: ' . date('d/m/Y H:i:s') . "\n";
$md .= '- Site: ' . $site . "\n";
$md .= '- Repositório: ' . $repositorio . "\n";
$md .= '- Resultados web únicos: ' . count($resultados) . "\n\n";
$md .= "> Pesquisa automatizada. Ausência de resultados não significa ausência de menções na internet.\n\n";

$md .= "## Resultados encontrados na web\n\n";

foreach ($resultados as $i => $resultado) {
    $md .= '### ' . ($i + 1) . '. ' . ($resultado['titulo'] ?? 'Sem título') . "\n\n";
    $md .= '- Fonte: ' . ($resultado['fonte'] ?? 'Não identificada') . "\n";
    $md .= '- Consulta: ' . ($resultado['consulta'] ?? '') . "\n";
    $md .= '- URL: ' . ($resultado['url'] ?? '') . "\n";

    if (($resultado['resumo'] ?? '') !== '') {
        $md .= '- Resumo: ' . $resultado['resumo'] . "\n";
    }

    $md .= "\n";
}

$md .= "## Dados públicos do GitHub\n\n";

foreach ($github as $item) {
    $md .= '### ' . ($item['tipo'] ?? 'Registro') . ': '
        . ($item['titulo'] ?? 'Sem título') . "\n\n";

    if (isset($item['url'])) {
        $md .= '- URL: ' . $item['url'] . "\n";
    }

    if (isset($item['data']) && $item['data'] !== null) {
        $md .= '- Data: ' . $item['data'] . "\n";
    }

    if (($item['resumo'] ?? '') !== '') {
        $md .= '- Detalhes: ' . limpar($item['resumo']) . "\n";
    }

    if (isset($item['erro'])) {
        $md .= '- Erro: ' . $item['erro'] . "\n";
    }

    $md .= "\n";
}

$md .= "## Consultas sem resultados\n\n";

if ($falhas === []) {
    $md .= "Todas as consultas retornaram pelo menos um resultado.\n\n";
} else {
    foreach ($falhas as $falha) {
        $md .= '- `' . $falha['consulta'] . '`: ' . $falha['motivo'] . "\n";
    }
}

if (file_put_contents($arquivoMd, $md) === false) {
    exit("Erro ao salvar o relatório Markdown.\n");
}

echo "\nPesquisa concluída.\n";
echo "Resultados web: " . count($resultados) . "\n";
echo "GitHub: " . count($github) . " registros.\n";
echo "Markdown: pesquisas/" . basename($arquivoMd) . "\n";
echo "JSON: pesquisas/" . basename($arquivoJson) . "\n";