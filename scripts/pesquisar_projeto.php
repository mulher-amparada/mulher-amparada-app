<?php

declare(strict_types=1);

$raiz = dirname(__DIR__);
$workflow = $raiz . '/workflows/automations.yml';
$saida = $raiz . '/workflows/workflow.md';

if (!is_file($workflow)) {
    fwrite(STDERR, "Erro: automations.yml não encontrado.\n");
    exit(1);
}

$yaml = file_get_contents($workflow);

if ($yaml === false) {
    fwrite(STDERR, "Erro: não foi possível ler automations.yml.\n");
    exit(1);
}

$linhas = preg_split('/\r\n|\n|\r/', $yaml);

if ($linhas === false) {
    fwrite(STDERR, "Erro: não foi possível separar as linhas do YAML.\n");
    exit(1);
}

$jobs = [];
$jobAtual = null;
$etapaAtual = null;
$emJobs = false;
$emSteps = false;
$nivelNeeds = false;

foreach ($linhas as $linha) {
    if (trim($linha) === '' || preg_match('/^\s*#/', $linha)) {
        continue;
    }

    preg_match('/^ */', $linha, $espacos);
    $nivel = strlen($espacos[0]);
    $conteudo = trim($linha);

    if ($nivel === 0) {
        $emJobs = ($conteudo === 'jobs:');
        $jobAtual = null;
        $etapaAtual = null;
        $emSteps = false;
        $nivelNeeds = false;
        continue;
    }

    if (!$emJobs) {
        continue;
    }

    if (
        $nivel === 2
        && preg_match('/^([A-Za-z0-9_-]+):\s*(?:#.*)?$/', $conteudo, $m)
    ) {
        $id = $m[1];

        $jobs[$id] = [
            'id' => $id,
            'name' => $id,
            'runner' => '',
            'needs' => [],
            'steps' => [],
        ];

        $jobAtual = $id;
        $etapaAtual = null;
        $emSteps = false;
        $nivelNeeds = false;
        continue;
    }

    if ($jobAtual === null) {
        continue;
    }

    if ($nivelNeeds && $nivel <= 4) {
        $nivelNeeds = false;
    }

    if ($emSteps && $nivel < 6) {
        $emSteps = false;
        $etapaAtual = null;
    }

    if ($nivel === 4) {
        if (preg_match('/^name:\s*(.+)$/', $conteudo, $m)) {
            $jobs[$jobAtual]['name'] = trim($m[1], " \t\"'");
        } elseif (preg_match('/^runs-on:\s*(.+)$/', $conteudo, $m)) {
            $jobs[$jobAtual]['runner'] = trim($m[1], " \t\"'");
        } elseif (preg_match('/^needs:\s*(.*)$/', $conteudo, $m)) {
            $valor = trim($m[1]);

            if ($valor === '') {
                $nivelNeeds = true;
            } elseif (preg_match('/^\[(.*)\]$/', $valor, $lista)) {
                $jobs[$jobAtual]['needs'] = array_values(array_filter(
                    array_map(
                        fn($item) => trim($item, " \t\"'"),
                        explode(',', $lista[1])
                    )
                ));
            } else {
                $jobs[$jobAtual]['needs'] = [
                    trim($valor, " \t\"'")
                ];
            }
        } elseif ($conteudo === 'steps:') {
            $emSteps = true;
            $etapaAtual = null;
        }

        continue;
    }

    if (
        $nivelNeeds
        && $nivel === 6
        && preg_match('/^-\s*(.+)$/', $conteudo, $m)
    ) {
        $jobs[$jobAtual]['needs'][] = trim($m[1], " \t\"'");
        continue;
    }

    if (
        $emSteps
        && $nivel === 6
        && preg_match('/^-\s*(.*)$/', $conteudo, $m)
    ) {
        $jobs[$jobAtual]['steps'][] = [
            'name' => '',
            'uses' => '',
            'run' => '',
        ];

        $etapaAtual = count($jobs[$jobAtual]['steps']) - 1;
        $resto = trim($m[1]);

        if (preg_match('/^name:\s*(.+)$/', $resto, $campo)) {
            $jobs[$jobAtual]['steps'][$etapaAtual]['name'] =
                trim($campo[1], " \t\"'");
        } elseif (preg_match('/^uses:\s*(.+)$/', $resto, $campo)) {
            $jobs[$jobAtual]['steps'][$etapaAtual]['uses'] =
                trim($campo[1], " \t\"'");
        } elseif (preg_match('/^run:\s*(.*)$/', $resto, $campo)) {
            $jobs[$jobAtual]['steps'][$etapaAtual]['run'] =
                trim($campo[1]) === '' ? 'Bloco de comandos' : trim($campo[1]);
        }

        continue;
    }

    if ($emSteps && $nivel >= 8 && $etapaAtual !== null) {
        if (preg_match('/^name:\s*(.+)$/', $conteudo, $m)) {
            $jobs[$jobAtual]['steps'][$etapaAtual]['name'] =
                trim($m[1], " \t\"'");
        } elseif (preg_match('/^uses:\s*(.+)$/', $conteudo, $m)) {
            $jobs[$jobAtual]['steps'][$etapaAtual]['uses'] =
                trim($m[1], " \t\"'");
        } elseif (preg_match('/^run:\s*(.*)$/', $conteudo, $m)) {
            $jobs[$jobAtual]['steps'][$etapaAtual]['run'] =
                trim($m[1]) === '' ? 'Bloco de comandos' : trim($m[1]);
        }
    }
}

if ($jobs === []) {
    fwrite(STDERR, "Erro: nenhum job foi identificado no YAML.\n");
    exit(1);
}

$escapar = static fn(string $texto): string =>
    str_replace(["|", "\r", "\n"], ["\\|", ' ', ' '], $texto);

$idMermaid = static fn(string $id): string =>
    'job_' . preg_replace('/[^A-Za-z0-9_]/', '_', $id);

$rotuloMermaid = static fn(string $texto): string =>
    str_replace(['"', '[', ']', "\r", "\n"], ["'", '(', ')', ' ', ' '], $texto);

$totalEtapas = array_sum(
    array_map(fn($job) => count($job['steps']), $jobs)
);

$md = "# Documentação do workflow — Mulher Amparada\n\n";
$md .= "- **Arquivo de origem:** `workflows/automations.yml`\n";
$md .= "- **Arquivo gerado:** `workflows/workflow.md`\n";
$md .= "- **Jobs identificados:** " . count($jobs) . "\n";
$md .= "- **Etapas identificadas:** {$totalEtapas}\n\n";

$md .= "## Tabela completa de jobs e etapas\n\n";
$md .= "| ID do job | Nome | Runner | Dependências | Etapas |\n";
$md .= "|---|---|---|---|---|\n";

foreach ($jobs as $job) {
    $dependencias = $job['needs'] === []
        ? 'Nenhuma'
        : implode(', ', array_map(
            fn($item) => '`' . $item . '`',
            $job['needs']
        ));

    $etapas = [];

    foreach ($job['steps'] as $i => $etapa) {
        $nome = $etapa['name'] !== ''
            ? $etapa['name']
            : ($etapa['uses'] !== '' ? $etapa['uses'] : 'Etapa ' . ($i + 1));

        $detalhe = ($i + 1) . '. ' . $nome;

        if ($etapa['uses'] !== '') {
            $detalhe .= ' (`' . $etapa['uses'] . '`)';
        }

        if ($etapa['run'] !== '') {
            $detalhe .= ' — executa comandos';
        }

        $etapas[] = $detalhe;
    }

    $md .= '| `' . $escapar($job['id']) . '`'
        . ' | ' . $escapar($job['name'])
        . ' | ' . $escapar($job['runner'] ?: 'Não identificado')
        . ' | ' . $escapar($dependencias)
        . ' | ' . $escapar(implode('; ', $etapas) ?: 'Nenhuma identificada')
        . " |\n";
}

$md .= "\n## Grafo completo de dependências\n\n";
$md .= "As setas representam as dependências declaradas em `needs` no YAML original.\n\n";
$md .= "```mermaid\nflowchart TD\n";

foreach ($jobs as $id => $job) {
    $md .= '    ' . $idMermaid($id)
        . '["' . $rotuloMermaid($job['name']) . "\"]\n";
}

foreach ($jobs as $id => $job) {
    foreach ($job['needs'] as $dependencia) {
        $origem = $idMermaid($dependencia);
        $destino = $idMermaid($id);

        if (!isset($jobs[$dependencia])) {
            $md .= '    ' . $origem
                . '["Dependência não identificada: '
                . $rotuloMermaid($dependencia) . "\"]\n";
        }

        $md .= "    {$origem} --> {$destino}\n";
    }
}

$md .= "```\n\n";
$md .= "## Detalhamento dos jobs\n\n";

foreach ($jobs as $job) {
    $md .= "### " . $job['name'] . "\n\n";
    $md .= "- **ID:** `" . $job['id'] . "`\n";
    $md .= "- **Runner:** `" . ($job['runner'] ?: 'Não identificado') . "`\n";
    $md .= "- **Dependências:** "
        . ($job['needs'] === []
            ? 'Nenhuma'
            : implode(', ', array_map(
                fn($item) => '`' . $item . '`',
                $job['needs']
            )))
        . "\n\n";

    $md .= "| # | Etapa | Ação | Execução |\n";
    $md .= "|---:|---|---|---|\n";

    if ($job['steps'] === []) {
        $md .= "| — | Nenhuma etapa identificada | — | — |\n\n";
        continue;
    }

    foreach ($job['steps'] as $i => $etapa) {
        $nome = $etapa['name'] !== ''
            ? $etapa['name']
            : ($etapa['uses'] !== '' ? $etapa['uses'] : 'Etapa ' . ($i + 1));

        $acao = $etapa['uses'] !== ''
            ? '`' . $etapa['uses'] . '`'
            : '—';

        $execucao = $etapa['run'] !== ''
            ? $etapa['run']
            : '—';

        $md .= '| ' . ($i + 1)
            . ' | ' . $escapar($nome)
            . ' | ' . $escapar($acao)
            . ' | ' . $escapar($execucao)
            . " |\n";
    }

    $md .= "\n";
}

$md .= "## YAML original completo\n\n";
$md .= "O bloco abaixo reproduz o conteúdo original do arquivo `automations.yml`, preservando comandos, condições, variáveis, comentários e configurações.\n\n";
$md .= "````yaml\n";
$md .= rtrim($yaml) . "\n";
$md .= "````\n";

if (file_put_contents($saida, $md) === false) {
    fwrite(STDERR, "Erro: não foi possível gravar workflows/workflow.md.\n");
    exit(1);
}

echo "Documentação gerada: workflows/workflow.md\n";
echo "Jobs identificados: " . count($jobs) . "\n";
echo "Etapas identificadas: {$totalEtapas}\n";