
<?php

$package = $argv[1] ?? '';
$manifest = $argv[2] ?? 'app/src/main/AndroidManifest.xml';
$outputDir = $argv[3] ?? 'screenshots';

if ($package === '' || !is_file($manifest)) {
    fwrite(STDERR, "Uso: php capturar_activities.php pacote manifest.xml [pasta]\n");
    exit(1);
}

if (!is_dir($outputDir)) {
    mkdir($outputDir, 0777, true);
}

$adb = 'adb';

function runCommand(string $command): array {
    exec($command . ' 2>&1', $output, $status);
    return [$status, implode("\n", $output)];
}

[$status, $devices] = runCommand(
    escapeshellarg('adb') . ' devices'
);

if ($status !== 0 || !str_contains($devices, "\tdevice")) {
    fwrite(STDERR, "Nenhum dispositivo ou emulador ADB conectado.\n");
    exit(1);
}

$xml = simplexml_load_file($manifest);

if ($xml === false) {
    fwrite(STDERR, "Não foi possível ler o AndroidManifest.xml.\n");
    exit(1);
}

$android = $xml->getNamespaces(true)['android']
    ?? 'http://schemas.android.com/apk/res/android';

$activities = [];

foreach ($xml->xpath('//activity | //activity-alias') as $node) {
    $attrs = $node->attributes($android);
    $name = (string) ($attrs['name'] ?? '');

    if ($name === '') {
        continue;
    }

    if (str_starts_with($name, '.')) {
        $name = $package . $name;
    } elseif (!str_contains($name, '.')) {
        $name = $package . '.' . $name;
    }

    $activities[] = $name;
}

$activities = array_values(array_unique($activities));

if (!$activities) {
    fwrite(STDERR, "Nenhuma Activity encontrada no manifest.\n");
    exit(1);
}

$csv = fopen("$outputDir/relatorio.csv", 'w');
fputcsv($csv, ['Activity', 'Resultado', 'Captura']);

foreach ($activities as $index => $activity) {
    $safeName = preg_replace('/[^A-Za-z0-9_.-]/', '_', $activity);
    $png = "$outputDir/" . sprintf('%03d_', $index + 1) . "$safeName.png";

    runCommand("$adb shell am force-stop " . escapeshellarg($package));

    [$status, $result] = runCommand(
        "$adb shell am start -W -n " .
        escapeshellarg("$package/$activity")
    );

    sleep(3);

    [$shotStatus, $shotResult] = runCommand(
        "$adb exec-out screencap -p > " . escapeshellarg($png)
    );

    $valid = $shotStatus === 0
        && is_file($png)
        && filesize($png) > 100;

    $opened = str_contains($result, 'Status: ok');

    $state = $opened && $valid
        ? 'OK'
        : (!$opened ? 'Falha ao abrir' : 'Falha na captura');

    if (!$valid && is_file($png)) {
        unlink($png);
    }

    fputcsv($csv, [
        $activity,
        $state,
        $valid ? basename($png) : ''
    ]);

    echo "$state: $activity\n";
}

fclose($csv);

echo "\nCapturas e relatório: $outputDir\n";
