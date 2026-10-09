<?php

declare(strict_types=1);

$raiz = dirname(__DIR__);
$workflow = $raiz . '/.github/workflows/automations.yml';
$saida = $raiz . '/.github/workflows/workflow.md';

if (!is_file($workflow)) {
    fwrite(STDERR, "Erro: automations.yml não encontrado.\n");
    exit(1);
}

$linhas = file($workflow, FILE_IGNORE_NEW_LINES);

if ($linhas === false) {
    fwrite(STDERR, "Erro: não foi possível ler automations.yml.\n");
    exit(1);
}

$jobs = [];
$emJobs = false;
$jobAtual = null;
$etapaIndice = null;
$nivelNeeds = false;
$nivelSteps = false;

foreach ($linhas as $linha) {
    if (trim($linha) === '' || preg_match('/^\s*#/', $linha)) {
        continue;
    }

    preg_match('/^ */', $linha, $espacos);
    $nivel = strlen($espacos[0]);
    $conteudo = trim($linha);

    if ($nivel === 0) {
        $emJobs = ($conteudo === 'jobs:');
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
            'runs-on' => '',
            'needs' => [],
            'steps' => [],
        ];

        $jobAtual = $id;
        $etapaIndice = null;
        $nivelNeeds = false;
        $nivelSteps = false;

        continue;
    }

    if ($jobAtual === null) {
        continue;
    }

    if ($nivelNeeds && $nivel <= 4) {
        $nivelNeeds = false;
    }

    if ($nivelSteps && $nivel < 6) {
        $nivelSteps = false;
        $etapaIndice = null;
    }

    if ($nivel === 4) {
        if (preg_match('/^name:\s*(.+)$/', $conteudo, $m)) {
            $jobs[$jobAtual]['name'] = trim($m[1], " \t\"'");
        } elseif (preg_match('/^runs-on:\s*(.+)$/', $conteudo, $m)) {
            $jobs[$jobAtual]['runs-on'] = trim($m[1], " \t\"'");
        } elseif (preg_match('/^needs:\s*(.*)$/', $conteudo, $m)) {
            $valor = trim($m[1]);

            if ($valor === '') {
                $nivelNeeds = true;
            } elseif (preg_match('/^\[(.*)\]$/', $valor, $lista)) {
                $jobs[$jobAtual]['needs'] = array_values(
                    array_filter(
                        array_map(
                            fn($item) => trim($item, " \t\"'"),
                            explode(',', $lista[1])
                        )
                    )
                );
            } else {
                $jobs[$jobAtual]['needs'] = [
                    trim($valor, " \t\"'")
                ];
            }
        } elseif ($conteudo === 'steps:') {
            $nivelSteps = true;
            $etapaIndice = null;
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
        $nivelSteps
        && $nivel === 6
        && preg_match('/^-\s*(.*)$/', $conteudo, $m)
    ) {
        $jobs[$jobAtual]['steps'][] = [
            'name' => '',
            'uses' => '',
            'run' => '',
        ];

        $etapaIndice = count($jobs[$jobAtual]['steps']) - 1;
        $resto = trim($m[1]);

        if (preg_match('/^name:\s*(.+)$/', $resto, $campo)) {
            $jobs[$jobAtual]['steps'][$etapaIndice]['name'] =
                trim($campo[1], " \t\"'");
        } elseif (preg_match('/^uses:\s*(.+)$/', $resto, $campo)) {
            $jobs[$jobAtual]['steps'][$etapaIndice]['uses'] =
                trim($campo[1], " \t\"'");
        } elseif (preg_match('/^run:\s*(.*)$/', $resto, $campo)) {
            $jobs[$jobAtual]['steps'][$etapaIndice]['run'] =
                trim($campo[1]) === '' ? 'Comando em bloco' : trim($campo[1]);
        }

        continue;
    }

    if (
        $nivelSteps
        && $nivel >= 8
        && $etapaIndice !== null
    ) {
        if (preg_match('/^name:\s*(.+)$/', $conteudo, $m)) {
            $jobs[$jobAtual]['steps'][$etapaIndice]['name'] =
                trim($m[1], " \t\"'");
        } elseif (preg_match('/^uses:\s*(.+)$/', $conteudo, $m)) {
            $jobs[$jobAtual]['steps'][$etapaIndice]['uses'] =
                trim($m[1], " \t\"'");
        } elseif (preg_match('/^run:\s*(.*)$/', $conteudo, $m)) {
            $jobs[$jobAtual]['steps'][$etapaIndice]['run'] =
                trim($m[1]) === '' ? 'Comando em bloco' : trim($m[1]);
        }
    }
}

if ($jobs === []) {
    fwrite(STDERR, "Erro: nenhum job foi identificado no YAML.\n");
    exit(1);
}

$escapar = static function (string $texto): string {
    return str_replace(
        ["|", "\r", "\n"],
        ["\\|", ' ', ' '],
        $texto
    );
};

$identificador = static function (string $id): string {
    return 'job_' . preg_replace('/[^A-Za-z0-9_]/', '_', $id);
};

$rotuloMermaid = static function (string $texto): string {
    return str_replace(
        ['"', '[', ']', "\r", "\n"],
        ["'", '(', ')', ' ', ' '],
        $texto
    );
};

$md = "# Documentação do workflow — Mulher Amparada\n\n";
$md .= "- **Arquivo de origem:** `.github/workflows/automations.yml`\n";
$md .= "- **Jobs identificados:** " . count($jobs) . "\n";
$md .= "- **Etapas identificadas:** " . array_sum(
    array_map(fn($job) => count($job['steps']), $jobs)
) . "\n\n";

$md .= "## Tabela completa de jobs\n\n";
$md .= "| ID do job | Nome | Runner | Dependências | Quantidade de etapas | Etapas |\n";
$md .= "|---|---|---|---|---:|---|\n";

foreach ($jobs as $job) {
    $dependencias = $job['needs'] === []
        ? 'Nenhuma'
        : implode(', ', array_map(
            fn($item) => '`' . $item . '`',
            $job['needs']
        ));

    $etapas = [];

    foreach ($job['steps'] as $indice => $etapa) {
        $nome = $etapa['name'] !== ''
            ? $etapa['name']
            : ($etapa['uses'] !== ''
                ? $etapa['uses']
                : 'Etapa ' . ($indice + 1));

        $detalhe = ($indice + 1) . '. ' . $nome;

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
        . ' | ' . $escapar($job['runs-on'] !== '' ? $job['runs-on'] : 'Não identificado')
        . ' | ' . $escapar($dependencias)
        . ' | ' . count($job['steps'])
        . ' | ' . $escapar(implode('; ', $etapas) ?: 'Nenhuma identificada')
        . " |\n";
}

$md .= "\n## Grafo completo de dependências\n\n";
$md .= "O diagrama abaixo contém todos os jobs identificados e todas as ligações declaradas em `needs`.\n\n";
$md .= "```mermaid\nflowchart TD\n";

foreach ($jobs as $id => $job) {
    $node = $identificador($id);
    $rotulo = $rotuloMermaid($job['name']);

    $md .= '    ' . $node . '["' . $rotulo . "\"]\n";
}

foreach ($jobs as $id => $job) {
    $destino = $identificador($id);

    foreach ($job['needs'] as $dependencia) {
        if (!isset($jobs[$dependencia])) {
            $md .= '    dependencia_' . $identificador($dependencia)
                . '["Dependência externa: '
                . $rotuloMermaid($dependencia)
                . "\"]\n";

            $md .= '    dependencia_' . $identificador($dependencia)
                . ' --> ' . $destino . "\n";

            continue;
        }

        $origem = $identificador($dependencia);
        $md .= '    ' . $origem . ' --> ' . $destino . "\n";
    }
}

$md .= "```\n\n";

$md .= "## Detalhamento de todos os jobs\n\n";

foreach ($jobs as $job) {
    $md .= "### " . $job['name'] . "\n\n";
    $md .= "- **ID:** `" . $job['id'] . "`\n";
    $md .= "- **Runner:** "
        . ($job['runs-on'] !== '' ? '`' . $job['runs-on'] . '`' : 'Não identificado')
        . "\n";

    $md .= "- **Dependências:** "
        . ($job['needs'] === []
            ? 'Nenhuma identificada'
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

    foreach ($job['steps'] as $indice => $etapa) {
        $nome = $etapa['name'] !== ''
            ? $etapa['name']
            : ($etapa['uses'] !== ''
                ? $etapa['uses']
                : 'Etapa ' . ($indice + 1));

        $acao = $etapa['uses'] !== ''
            ? '`' . $etapa['uses'] . '`'
            : '—';

        $execucao = $etapa['run'] !== ''
            ? $etapa['run']
            : '—';

        $md .= '| ' . ($indice + 1)
            . ' | ' . $escapar($nome)
            . ' | ' . $escapar($acao)
            . ' | ' . $escapar($execucao)
            . " |\n";
    }

    $md .= "\n";
}

$md .= "## YAML original completo\n\n";
$md .= "Esta seção preserva o conteúdo integral do arquivo original, incluindo configurações, condições, variáveis, comandos e opções que a tabela não representa.\n\n";

$delimitador = '````';
$md .= $delimitador . "yaml\n";
$md .= implode("\n", $linhas) . "\n";
$md .= $delimitador . "\n";

$pastaSaida = dirname($saida);

if (
    !is_dir($pastaSaida)
    && !mkdir($pastaSaida, 0775, true)
    && !is_dir($pastaSaida)
) {
    fwrite(STDERR, "Erro: não foi possível criar .github/workflows/.\n");
    exit(1);
}

if (file_put_contents($saida, $md) === false) {
    fwrite(STDERR, "Erro: não foi possível gravar workflow.md.\n");
    exit(1);
}

echo "Documentação gerada: .github/workflows/workflow.md\n";
echo "Jobs identificados: " . count($jobs) . "\n";
echo "Etapas identificadas: " . array_sum(
    array_map(fn($job) => count($job['steps']), $jobs)
) . "\n";
