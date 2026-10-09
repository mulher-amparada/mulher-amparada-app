<?php

declare(strict_types=1);

$raiz = dirname(__DIR__);
$arquivoYml = $raiz . '/.github/workflows/automations.yml';
$pasta = $raiz . '/pesquisas';
$arquivoMd = $pasta . '/workflow.md';

if (!is_file($arquivoYml)) {
    exit("Erro: .github/workflows/automations.yml não encontrado.\n");
}

if (!is_dir($pasta) && !mkdir($pasta, 0775, true) && !is_dir($pasta)) {
    exit("Erro ao criar a pasta pesquisas.\n");
}

$conteudo = file_get_contents($arquivoYml);

if ($conteudo === false || trim($conteudo) === '') {
    exit("Erro: não foi possível ler o YAML ou ele está vazio.\n");
}

if (!preg_match('/^jobs:\s*$/m', $conteudo)) {
    exit("Erro: seção jobs: não encontrada no YAML.\n");
}

function escaparTabela(string $texto): string
{
    return str_replace(
        ["|", "\r", "\n"],
        ["\\|", '', ' '],
        trim($texto)
    );
}

function escaparMermaid(string $texto): string
{
    return str_replace(
        ['"', "\r", "\n", '[', ']', '(', ')'],
        ["'", '', ' ', '(', ')', '', ''],
        trim($texto)
    );
}

function indentacao(string $linha): int
{
    return strlen($linha) - strlen(ltrim($linha, ' '));
}

function extrairEscalar(string $valor): string
{
    $valor = trim($valor);

    if (
        strlen($valor) >= 2 &&
        (
            ($valor[0] === '"' && str_ends_with($valor, '"')) ||
            ($valor[0] === "'" && str_ends_with($valor, "'"))
        )
    ) {
        return substr($valor, 1, -1);
    }

    return $valor;
}

$linhas = preg_split('/\r\n|\r|\n/', $conteudo);
$jobs = [];
$jobAtual = null;
$secaoAtual = '';
$stepAtual = null;
$indentJobs = null;
$indentJob = null;
$indentSteps = null;
$indentStep = null;
$indentNeeds = null;
$indentNome = null;
$indentRun = null;
$emBloco = false;

foreach ($linhas as $linha) {
    if (trim($linha) === '' || preg_match('/^\s*#/', $linha)) {
        continue;
    }

    $nivel = indentacao($linha);
    $texto = trim($linha);

    if ($nivel === 0) {
        $emBloco = false;
        continue;
    }

    if ($emBloco) {
        if ($nivel > $indentRun) {
            continue;
        }

        $emBloco = false;
    }

    if (
        $nivel === 2 &&
        preg_match('/^([A-Za-z0-9_-]+):\s*$/', $texto, $m)
    ) {
        $secaoAtual = $m[1];

        if ($secaoAtual === 'jobs') {
            $indentJobs = $nivel;
        }

        continue;
    }

    if (
        $secaoAtual !== 'jobs' ||
        $indentJobs === null
    ) {
        continue;
    }

    if (
        $nivel === 4 &&
        preg_match('/^([A-Za-z0-9_-]+):\s*$/', $texto, $m)
    ) {
        $jobAtual = $m[1];

        $jobs[$jobAtual] = [
            'id' => $jobAtual,
            'nome' => $jobAtual,
            'descricao' => '',
            'needs' => [],
            'steps' => [],
            'runs_on' => '',
            'if' => '',
        ];

        $indentJob = $nivel;
        $indentSteps = null;
        $indentStep = null;
        $indentNeeds = null;
        $indentNome = null;
        $indentRun = null;

        continue;
    }

    if ($jobAtual === null) {
        continue;
    }

    if (
        $nivel === 6 &&
        preg_match('/^name:\s*(.*)$/', $texto, $m)
    ) {
        $jobs[$jobAtual]['nome'] = extrairEscalar($m[1]);
        $indentNome = $nivel;
        continue;
    }

    if (
        $nivel === 6 &&
        preg_match('/^runs-on:\s*(.*)$/', $texto, $m)
    ) {
        $jobs[$jobAtual]['runs_on'] = extrairEscalar($m[1]);
        continue;
    }

    if (
        $nivel === 6 &&
        preg_match('/^if:\s*(.*)$/', $texto, $m)
    ) {
        $jobs[$jobAtual]['if'] = extrairEscalar($m[1]);
        continue;
    }

    if (
        $nivel === 6 &&
        preg_match('/^needs:\s*(.*)$/', $texto, $m)
    ) {
        $indentNeeds = $nivel;
        $valor = trim($m[1]);

        if ($valor !== '') {
            $valor = trim($valor, '[]');

            foreach (explode(',', $valor) as $dependencia) {
                $dependencia = trim($dependencia, " \t\"'");

                if ($dependencia !== '') {
                    $jobs[$jobAtual]['needs'][] = $dependencia;
                }
            }
        }

        continue;
    }

    if (
        $indentNeeds !== null &&
        $nivel > $indentNeeds &&
        preg_match('/^-\s*(.+)$/', $texto, $m)
    ) {
        $dependencia = trim($m[1], " \t\"'");

        if ($dependencia !== '') {
            $jobs[$jobAtual]['needs'][] = $dependencia;
        }

        continue;
    }

    if (
        $nivel === 6 &&
        preg_match('/^steps:\s*$/', $texto)
    ) {
        $indentSteps = $nivel;
        $indentStep = null;
        continue;
    }

    if (
        $indentSteps !== null &&
        $nivel === 8 &&
        preg_match('/^-\s*name:\s*(.*)$/', $texto, $m)
    ) {
        $jobs[$jobAtual]['steps'][] = [
            'nome' => extrairEscalar($m[1]),
            'uses' => '',
            'run' => '',
        ];

        $indentStep = $nivel;
        $indentRun = null;
        continue;
    }

    if (
        $indentSteps !== null &&
        $indentStep !== null &&
        $nivel >= 10 &&
        preg_match('/^uses:\s*(.*)$/', $texto, $m)
    ) {
        $ultimo = count($jobs[$jobAtual]['steps']) - 1;

        if ($ultimo >= 0) {
            $jobs[$jobAtual]['steps'][$ultimo]['uses'] =
                extrairEscalar($m[1]);
        }

        continue;
    }

    if (
        $indentSteps !== null &&
        $indentStep !== null &&
        $nivel >= 10 &&
        preg_match('/^run:\s*(.*)$/', $texto, $m)
    ) {
        $ultimo = count($jobs[$jobAtual]['steps']) - 1;

        if ($ultimo >= 0) {
            $jobs[$jobAtual]['steps'][$ultimo]['run'] =
                extrairEscalar($m[1]);
        }

        if (in_array(trim($m[1]), ['|', '>', '|-', '>-'], true)) {
            $indentRun = $nivel;
            $emBloco = true;
        }

        continue;
    }

    if (
        $indentSteps !== null &&
        $indentStep !== null &&
        $nivel >= 10 &&
        preg_match('/^uses:\s*(.*)$/', $texto, $m)
    ) {
        continue;
    }

    if (
        $nivel === 6 &&
        preg_match('/^description:\s*(.*)$/', $texto, $m)
    ) {
        $jobs[$jobAtual]['descricao'] = extrairEscalar($m[1]);
    }
}

if ($jobs === []) {
    exit("Nenhum job foi identificado no YAML.\n");
}

foreach ($jobs as &$job) {
    $job['needs'] = array_values(array_unique($job['needs']));
}
unset($job);

$md = "# Workflow — Automações do Mulher Amparada\n\n";
$md .= '- Arquivo analisado: `.github/workflows/automations.yml`' . "\n";
$md .= '- Data da geração: ' . date('d/m/Y H:i:s') . "\n";
$md .= '- Total de jobs identificados: ' . count($jobs) . "\n\n";

$md .= "## Tabela dos jobs\n\n";
$md .= "| Job | Nome | Dependências | Ambiente | Etapas |\n";
$md .= "|---|---|---|---|---:|\n";

foreach ($jobs as $job) {
    $dependencias = $job['needs'] === []
        ? 'Nenhuma declarada'
        : implode(', ', $job['needs']);

    $md .= '| `' . escaparTabela($job['id']) . '`'
        . ' | ' . escaparTabela($job['nome'])
        . ' | ' . escaparTabela($dependencias)
        . ' | ' . escaparTabela($job['runs_on'] ?: 'Não especificado')
        . ' | ' . count($job['steps'])
        . " |\n";
}

$md .= "\n## Diagrama de dependências\n\n";
$md .= "```mermaid\nflowchart TD\n";

foreach ($jobs as $job) {
    $id = $job['id'];
    $rotulo = escaparMermaid($job['nome']);

    $md .= '    ' . $id . '["' . $rotulo . "\"]\n";
}

foreach ($jobs as $job) {
    foreach ($job['needs'] as $dependencia) {
        if (isset($jobs[$dependencia])) {
            $md .= '    ' . $dependencia . ' --> ' . $job['id'] . "\n";
        }
    }
}

$md .= "```\n\n";
$md .= "> As setas indicam que o job de origem é uma dependência declarada do job de destino.\n\n";

$md .= "## Descrição dos jobs\n\n";

foreach ($jobs as $job) {
    $md .= '### ' . $job['nome'] . "\n\n";
    $md .= '- Identificador: `' . $job['id'] . "`\n";
    $md .= '- Ambiente: ' . ($job['runs_on'] ?: 'Não especificado') . "\n";

    if ($job['needs'] !== []) {
        $md .= '- Dependências: ' . implode(', ', array_map(
            static fn(string $item): string => '`' . $item . '`',
            $job['needs']
        )) . "\n";
    } else {
        $md .= "- Dependências: nenhuma declarada.\n";
    }

    if ($job['if'] !== '') {
        $md .= '- Condição de execução: `' . $job['if'] . "`\n";
    }

    if ($job['descricao'] !== '') {
        $md .= "\n" . $job['descricao'] . "\n";
    }

    if ($job['steps'] === []) {
        $md .= "\nNenhuma etapa identificada pelo analisador.\n\n";
        continue;
    }

    $md .= "\n**Etapas configuradas:**\n\n";

    foreach ($job['steps'] as $indice => $step) {
        $nome = $step['nome'] !== ''
            ? $step['nome']
            : 'Etapa ' . ($indice + 1);

        $md .= ($indice + 1) . '. **' . $nome . '**';

        if ($step['uses'] !== '') {
            $md .= ' — ação: `' . $step['uses'] . '`';
        }

        if ($step['run'] !== '') {
            $md .= ' — comando: `' . str_replace('`', '\`', $step['run']) . '`';
        }

        $md .= "\n";
    }

    $md .= "\n";
}

if (file_put_contents($arquivoMd, $md) === false) {
    exit("Erro ao salvar o relatório Markdown.\n");
}

echo "Workflow analisado com sucesso.\n";
echo 'Jobs identificados: ' . count($jobs) . "\n";
echo "Relatório: pesquisas/workflow.md\n";
