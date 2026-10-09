
<?php

declare(strict_types=1);

$raiz = dirname(__DIR__);
$workflow = $raiz . '/.github/workflows/automations.yml';
$pastaSaida = $raiz . '/pesquisas';
$saida = $pastaSaida . '/workflow.md';

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
$etapaAtual = null;
$nivelRun = null;
$nivelNeeds = null;
$nivelSteps = null;

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
        $etapaAtual = null;
        $nivelRun = null;
        $nivelNeeds = null;
        $nivelSteps = null;

        continue;
    }

    if ($jobAtual === null) {
        continue;
    }

    if ($nivelRun !== null && $nivel <= $nivelRun) {
        $nivelRun = null;
    }

    if ($nivelNeeds !== null && $nivel <= $nivelNeeds) {
        $nivelNeeds = null;
    }

    if ($nivelSteps !== null && $nivel < $nivelSteps) {
        $nivelSteps = null;
        $etapaAtual = null;
    }

    if ($nivel === 4) {
        if (preg_match('/^name:\s*(.+)$/', $conteudo, $m)) {
            $jobs[$jobAtual]['name'] = trim($m[1], " \t\"'");
        } elseif (preg_match('/^runs-on:\s*(.+)$/', $conteudo, $m)) {
            $jobs[$jobAtual]['runs-on'] = trim($m[1], " \t\"'");
        } elseif (preg_match('/^needs:\s*(.*)$/', $conteudo, $m)) {
            $valor = trim($m[1]);

            if ($valor === '') {
                $nivelNeeds = 4;
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
            $nivelSteps = 6;
            $etapaAtual = null;
        }

        continue;
    }

    if ($nivelNeeds === 4 && $nivel === 6 && preg_match('/^-\s*(.+)$/', $conteudo, $m)) {
        $jobs[$jobAtual]['needs'][] = trim($m[1], " \t\"'");
        continue;
    }

    if ($nivelSteps !== null && $nivel === 6 && preg_match('/^-\s*(.*)$/', $conteudo, $m)) {
        $etapaAtual = [
            'name' => '',
            'uses' => '',
            'run' => '',
        ];

        $jobs[$jobAtual]['steps'][] = &$etapaAtual;

        $resto = trim($m[1]);

        if (preg_match('/^name:\s*(.+)$/', $resto, $campo)) {
            $etapaAtual['name'] = trim($campo[1], " \t\"'");
        } elseif (preg_match('/^uses:\s*(.+)$/', $resto, $campo)) {
            $etapaAtual['uses'] = trim($campo[1], " \t\"'");
        }

        continue;
    }

    if ($nivelSteps !== null && $nivel >= 8 && $etapaAtual !== null) {
        if (preg_match('/^name:\s*(.+)$/', $conteudo, $m)) {
            $etapaAtual['name'] = trim($m[1], " \t\"'");
        } elseif (preg_match('/^uses:\s*(.+)$/', $conteudo, $m)) {
            $etapaAtual['uses'] = trim($m[1], " \t\"'");
        } elseif (preg_match('/^run:\s*(.*)$/', $conteudo, $m)) {
            $etapaAtual['run'] = trim($m[1]) === '' ? 'Comando em bloco' : trim($m[1]);
        }

        continue;
    }
}

if ($jobs === []) {
    fwrite(
        STDERR,
        "Erro: nenhum job foi identificado. Verifique se existe jobs: na raiz e se os jobs têm dois espaços de indentação.\n"
    );
    exit(1);
}

$md = "# Documentação do workflow — Mulher Amparada\n\n";
$md .= "Arquivo analisado: `.github/workflows/automations.yml`\n\n";
$md .= "Total de jobs identificados: **" . count($jobs) . "**\n\n";

$md .= "## Fluxo de dependências\n\n";
$md .= "```mermaid\nflowchart TD\n";

foreach ($jobs as $id => $job) {
    $node = 'job_' . preg_replace('/[^A-Za-z0-9_]/', '_', $id);
    $rotulo = str_replace(
        ['"', '[', ']', "\n", "\r"],
        ["'", '(', ')', ' ', ' '],
        $job['name']
    );

    $md .= "    {$node}[\"{$rotulo}\"]\n";
}

foreach ($jobs as $id => $job) {
    $destino = 'job_' . preg_replace('/[^A-Za-z0-9_]/', '_', $id);

    foreach ($job['needs'] as $dependencia) {
        if (!isset($jobs[$dependencia])) {
            continue;
        }

        $origem = 'job_' . preg_replace(
            '/[^A-Za-z0-9_]/',
            '_',
            $dependencia
        );

        $md .= "    {$origem} --> {$destino}\n";
    }
}

$md .= "```\n\n";
$md .= "## Jobs identificados\n\n";

foreach ($jobs as $job) {
    $md .= "### " . $job['name'] . "\n\n";
    $md .= "- **ID:** `" . $job['id'] . "`\n";

    if ($job['runs-on'] !== '') {
        $md .= "- **Runner:** `" . $job['runs-on'] . "`\n";
    }

    if ($job['needs'] !== []) {
        $md .= "- **Dependências:** "
            . implode(', ', array_map(
                fn($item) => "`{$item}`",
                $job['needs']
            ))
            . "\n";
    } else {
        $md .= "- **Dependências:** nenhuma identificada\n";
    }

    $md .= "\n**Etapas:**\n\n";

    if ($job['steps'] === []) {
        $md .= "- Nenhuma etapa identificada pelo analisador.\n\n";
        continue;
    }

    foreach ($job['steps'] as $indice => $etapa) {
        $nome = $etapa['name'] !== ''
            ? $etapa['name']
            : ($etapa['uses'] !== '' ? $etapa['uses'] : 'Etapa ' . ($indice + 1));

        $md .= ($indice + 1) . ". **" . $nome . "**";

        if ($etapa['uses'] !== '') {
            $md .= " — usa `" . $etapa['uses'] . "`";
        }

        if ($etapa['run'] !== '') {
            $md .= " — executa comandos de shell";
        }

        $md .= "\n";
    }

    $md .= "\n";
}

if (!is_dir($pastaSaida) && !mkdir($pastaSaida, 0775, true) && !is_dir($pastaSaida)) {
    fwrite(STDERR, "Erro: não foi possível criar a pasta pesquisas/.\n");
    exit(1);
}

if (file_put_contents($saida, $md) === false) {
    fwrite(STDERR, "Erro: não foi possível gravar pesquisas/workflow.md.\n");
    exit(1);
}

echo "Documentação gerada: pesquisas/workflow.md\n";
echo "Jobs identificados: " . count($jobs) . "\n";
