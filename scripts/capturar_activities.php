
<?php

declare(strict_types=1);

function executar(string $comando): array
{
    $processo = proc_open(
        $comando,
        [
            0 => ['pipe', 'r'],
            1 => ['pipe', 'w'],
            2 => ['pipe', 'w'],
        ],
        $pipes
    );

    if (!is_resource($processo)) {
        return [
            'codigo' => 1,
            'saida' => '',
            'erro' => 'Não foi possível iniciar o processo.',
        ];
    }

    fclose($pipes[0]);

    $saida = stream_get_contents($pipes[1]);
    fclose($pipes[1]);

    $erro = stream_get_contents($pipes[2]);
    fclose($pipes[2]);

    $codigo = proc_close($processo);

    return [
        'codigo' => $codigo,
        'saida' => trim($saida),
        'erro' => trim($erro),
    ];
}

function registrar(string $mensagem): void
{
    echo '[' . date('H:i:s') . '] ' . $mensagem . PHP_EOL;
}

function nomeArquivo(string $nome): string
{
    $nome = preg_replace('/[^a-zA-Z0-9_.-]/', '_', $nome) ?? 'activity';
    return trim($nome, '._-') ?: 'activity';
}

$pacote = $argv[1] ?? 'com.mulheres';
$manifesto = $argv[2] ?? 'Código-fonte do app = Mulher Amparada/app/src/main/AndroidManifest.xml';

$runnerTemp = getenv('RUNNER_TEMP');

if ($runnerTemp === false || $runnerTemp === '') {
    fwrite(STDERR, "Erro: a variável RUNNER_TEMP não está definida.\n");
    exit(1);
}

$pastaCapturas = $runnerTemp . DIRECTORY_SEPARATOR . 'screenshots';

if (!is_dir($pastaCapturas) && !mkdir($pastaCapturas, 0777, true) && !is_dir($pastaCapturas)) {
    fwrite(STDERR, "Erro: não foi possível criar a pasta temporária de capturas.\n");
    exit(1);
}

if (!is_file($manifesto)) {
    fwrite(STDERR, "Erro: AndroidManifest.xml não encontrado: {$manifesto}\n");
    exit(1);
}

if (!class_exists(DOMDocument::class)) {
    fwrite(STDERR, "Erro: a extensão PHP DOM não está instalada.\n");
    exit(1);
}

$xml = new DOMDocument();

if (!$xml->load($manifesto)) {
    fwrite(STDERR, "Erro: não foi possível ler o AndroidManifest.xml.\n");
    exit(1);
}

$xpath = new DOMXPath($xml);
$nos = $xpath->query('//activity | //activity-alias');

if ($nos === false || $nos->length === 0) {
    fwrite(STDERR, "Erro: nenhuma Activity encontrada no manifesto.\n");
    exit(1);
}

$activities = [];

foreach ($nos as $no) {
    if (!$no instanceof DOMElement) {
        continue;
    }

    $nome = trim($no->getAttribute('android:name'));

    if ($nome === '') {
        continue;
    }

    if (str_starts_with($nome, '.')) {
        $nome = $pacote . $nome;
    } elseif (!str_contains($nome, '.')) {
        $nome = $pacote . '.' . $nome;
    }

    $activities[$nome] = true;
}

$activities = array_keys($activities);

if (count($activities) === 0) {
    fwrite(STDERR, "Erro: não foi possível resolver os nomes das Activities.\n");
    exit(1);
}

registrar('Verificando conexão com o emulador...');

$dispositivos = executar('adb devices');

if ($dispositivos['codigo'] !== 0 || !preg_match('/\tdevice(?:\r?$)/m', $dispositivos['saida'])) {
    fwrite(STDERR, "Erro: nenhum dispositivo Android conectado e autorizado.\n");
    fwrite(STDERR, $dispositivos['saida'] . PHP_EOL . $dispositivos['erro'] . PHP_EOL);
    exit(1);
}

registrar('Activities encontradas: ' . count($activities));
registrar('Capturas temporárias: ' . $pastaCapturas);

$csv = fopen($pastaCapturas . DIRECTORY_SEPARATOR . 'relatorio.csv', 'w');

if ($csv === false) {
    fwrite(STDERR, "Erro: não foi possível criar o relatório.\n");
    exit(1);
}

fputcsv($csv, ['Activity', 'Resultado', 'Arquivo', 'Detalhes']);

$totalSucesso = 0;
$totalFalha = 0;

foreach ($activities as $activity) {
    $arquivo = nomeArquivo($activity) . '.png';
    $caminho = $pastaCapturas . DIRECTORY_SEPARATOR . $arquivo;

    registrar("Abrindo {$activity}");

    executar('adb shell am force-stop ' . escapeshellarg($pacote));

    $inicio = executar(
        'adb shell am start -W -n ' .
        escapeshellarg($pacote . '/' . $activity)
    );

    if ($inicio['codigo'] !== 0 || str_contains($inicio['saida'] . $inicio['erro'], 'Error:')) {
        $detalhes = trim($inicio['saida'] . ' ' . $inicio['erro']);

        registrar("FALHA ao abrir {$activity}");

        fputcsv($csv, [
            $activity,
            'FALHA AO ABRIR',
            '',
            $detalhes,
        ]);

        $totalFalha++;
        continue;
    }

    sleep(3);

    $captura = executar(
        'adb exec-out screencap -p'
    );

    if ($captura['codigo'] !== 0 || $captura['saida'] === '') {
        registrar("FALHA ao capturar {$activity}");

        fputcsv($csv, [
            $activity,
            'FALHA NA CAPTURA',
            '',
            $captura['erro'] ?: $captura['saida'],
        ]);

        $totalFalha++;
        continue;
    }

    $processo = proc_open(
        'adb exec-out screencap -p',
        [
            0 => ['pipe', 'r'],
            1 => ['file', $caminho, 'wb'],
            2 => ['pipe', 'w'],
        ],
        $pipes
    );

    if (!is_resource($processo)) {
        registrar("FALHA ao salvar {$activity}");

        fputcsv($csv, [
            $activity,
            'FALHA AO SALVAR',
            '',
            'Não foi possível executar a captura.',
        ]);

        $totalFalha++;
        continue;
    }

    fclose($pipes[0]);
    $erro = stream_get_contents($pipes[2]);
    fclose($pipes[2]);
    $codigo = proc_close($processo);

    $conteudo = is_file($caminho) ? file_get_contents($caminho) : false;

    if (
        $codigo !== 0 ||
        $conteudo === false ||
        !str_starts_with($conteudo, "\x89PNG\r\n\x1a\n")
    ) {
        @unlink($caminho);

        registrar("FALHA ao validar o print de {$activity}");

        fputcsv($csv, [
            $activity,
            'FALHA NA CAPTURA',
            '',
            trim($erro) ?: 'A imagem PNG não foi gerada corretamente.',
        ]);

        $totalFalha++;
        continue;
    }

    registrar("Captura salva: {$arquivo}");

    fputcsv($csv, [
        $activity,
        'SUCESSO',
        $arquivo,
        '',
    ]);

    $totalSucesso++;
}

fclose($csv);

registrar('Processo concluído.');
registrar("Capturas realizadas: {$totalSucesso}");
registrar("Falhas: {$totalFalha}");
registrar("Diretório temporário: {$pastaCapturas}");

if ($totalSucesso === 0) {
    exit(1);
}
