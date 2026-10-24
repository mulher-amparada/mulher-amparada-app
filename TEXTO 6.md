# 👨🏾‍💻Safe Woman Operational System:

O **Safe Woman Operational System** é um projeto experimental de sistema operacional baseado no Android Open Source Project (AOSP).

O objetivo é criar uma versão personalizada do Android voltada para integrar o projeto **Mulher Amparada** diretamente ao sistema.

## Importante:

Este repositório **não contém o AOSP completo**.

A estrutura presente aqui representa apenas **uma pequena parte de todo o AOSP**. O AOSP completo possui centenas de componentes, incluindo framework, bibliotecas, ferramentas de compilação, HALs, componentes do sistema, kernel, Cuttlefish, aplicações e diversos outros projetos.

O Safe Woman OS contém somente os arquivos específicos necessários para definir a personalização do sistema.

## Estrutura:

    Safe Woman Operational System/
    ├── device/
    │   └── safe_woman/
    │       ├── AndroidProducts.mk
    │       ├── BoardConfig.mk
    │       ├── safe_woman.mk
    │       └── privapp-permissions-mulher-amparada.xml
    │
    └── packages/
        └── apps/
            └── MulherAmparada/
                ├── Android.bp
                └── app/
                    └── src/
                        └── main/

## Device:

O diretório `device/safe_woman` contém a configuração específica do dispositivo/produto Safe Woman OS.

### AndroidProducts.mk:

Define os arquivos de produto disponíveis e o alvo:

    safe_woman-userdebug

### BoardConfig.mk:

Define as características básicas da plataforma utilizada para o sistema experimental, atualmente preparada para arquitetura x86_64 e execução em ambiente virtual.

### safe_woman.mk:

Define o produto Safe Woman OS e integra componentes específicos ao sistema.

Entre eles está o aplicativo:

    MulherAmparada

## Mulher Amparada:

O aplicativo Mulher Amparada é integrado ao sistema em:

    packages/apps/MulherAmparada

Seu pacote Android é:

    com.mulheres

O objetivo dessa integração é permitir que o aplicativo faça parte da imagem do sistema em vez de existir somente como um aplicativo instalado posteriormente pelo usuário.

## Android.bp:

O arquivo `Android.bp` define o módulo do aplicativo para o sistema de build do AOSP, o Soong.

O módulo é chamado:

    MulherAmparada

A configuração também define o aplicativo como privilegiado:

    privileged: true

Isso significa que o aplicativo pode ser tratado como um aplicativo privilegiado do sistema.

Isso não significa que todas as permissões do aplicativo sejam concedidas automaticamente. Permissões perigosas/runtime continuam seguindo o modelo de permissões do Android.

## Privapp permissions:

O arquivo:

    privapp-permissions-mulher-amparada.xml

é utilizado para definir permissões privilegiadas que podem ser concedidas ao pacote:

    com.mulheres

Somente permissões compatíveis com o mecanismo de allowlist de permissões privilegiadas devem ser adicionadas nesse arquivo.

Permissões comuns de runtime, como localização, contatos ou microfone, não se tornam automaticamente privilegiadas apenas por serem colocadas nesse XML.

## Cuttlefish:

O Safe Woman OS foi planejado inicialmente para execução em um dispositivo Android virtual utilizando o **Cuttlefish**.

Isso permite testar a estrutura do sistema sem depender de um aparelho físico.

O objetivo experimental é chegar a uma configuração semelhante a:

    AOSP completo
          +
    Safe Woman OS
          +
    Mulher Amparada
          ↓
    safe_woman-userdebug
          ↓
    imagem do sistema
          ↓
    Cuttlefish

## Relação com o AOSP:

O Safe Woman OS não substitui o AOSP.

Ele funciona como uma camada de personalização sobre uma árvore AOSP completa.

A árvore completa do AOSP possui componentes como:

- Android Framework
- Android Runtime (ART)
- Bionic
- System
- Hardware Abstraction Layers (HALs)
- Kernel
- Cuttlefish
- bibliotecas
- ferramentas de build
- componentes externos
- aplicações do sistema
- configurações de dispositivos
- prebuilts
- ferramentas de desenvolvimento

O Safe Woman OS contém apenas uma pequena fração dessa estrutura.

## Estado atual:

O projeto é experimental.

A estrutura atual foi organizada para que seus arquivos específicos possam ser colocados dentro de uma árvore AOSP completa.

A existência desses arquivos, por si só, não significa que o sistema completo já possa ser compilado.

Ainda são necessários todos os componentes correspondentes da árvore AOSP e as dependências utilizadas pelo aplicativo.

## Objetivo futuro:

A ideia é utilizar uma árvore AOSP completa como base e adicionar o Safe Woman OS como uma configuração personalizada, integrando o Mulher Amparada diretamente ao sistema.

O resultado esperado é um Android personalizado executável em ambiente virtual, inicialmente através do Cuttlefish.

## Natureza do projeto:

O Safe Woman Operational System é um experimento de desenvolvimento de sistema operacional baseado em AOSP.

O projeto não pretende reproduzir o AOSP inteiro dentro deste repositório.

Ele mantém somente os arquivos próprios do Safe Woman OS e a integração planejada com o aplicativo Mulher Amparada

Link:
https://github.com/mulher-amparada/mulher-amparada-app/tree/main/Safe%20Woman%20Operational%20System

# simular-os.yml:

O workflow `simular-os.yml` automatiza a compilação, a inicialização de um ambiente Android virtual e a verificação do aplicativo Safe Woman OS por meio do GitHub Actions.

## Objetivo:

- Compilar o APK do projeto Android.
- Criar um dispositivo virtual com Android 15 (API 35) e arquitetura x86_64.
- Iniciar o emulador com suporte à escrita na imagem do sistema.
- Tentar instalar o APK `com.mulheres` como aplicativo privilegiado (`priv-app`).
- Criar o arquivo XML de configuração de permissões privilegiadas.
- Reiniciar os serviços do Android para solicitar a releitura dos pacotes.
- Verificar o registro do aplicativo e o caminho de instalação.
- Registrar informações sobre partições, permissões e estado do sistema.
- Salvar os resultados na branch `main` e publicar um artefato do GitHub Actions.

## Funcionamento:

### 1. Preparação do ambiente:

Executa o workflow no Ubuntu 24.04 e configura o Android SDK, as ferramentas de plataforma e o emulador.

### 2. Compilação do APK:

Localiza o `gradlew` dentro da pasta `Safe Woman Operational System` e executa:

`./gradlew assembleDebug --stacktrace`

Depois, procura o APK gerado nos diretórios de saída do Gradle.

### 3. Criação do emulador:

Cria um dispositivo virtual chamado `SafeWomanOS`, utilizando a imagem `aosp_atd` do Android 15 para x86_64.

### 4. Preparação para alterações no sistema:

Instala ferramentas de manipulação de imagens Android, configura a aceleração KVM quando disponível e inicia o emulador com a opção `-writable-system`.

O workflow tenta desativar o verity e remontar o sistema com permissão de escrita.

### 5. Injeção do APK:

Tenta criar o diretório `/system/priv-app/MulherAmparada` e copia o APK para `/system/priv-app/MulherAmparada/MulherAmparada.apk`.

Também cria o arquivo `privapp-permissions-com.mulheres.xml` em `/system/etc/permissions/`, destinado à configuração de permissões privilegiadas do pacote `com.mulheres`.

A cópia do APK e a criação desse XML não garantem, por si só, que o aplicativo seja reconhecido como privilegiado ou receba permissões especiais. Isso depende da configuração e das políticas de segurança da imagem Android.

### 6. Reinicialização dos serviços:

Executa `stop` e `start` para solicitar a reinicialização dos serviços do Android e aguarda a conclusão da inicialização.

### 7. Verificação do pacote:

Consulta `pm path com.mulheres` para verificar se o Android registrou o pacote e se o caminho retornado contém `/priv-app/`.

Se o pacote não estiver registrado ou o caminho não corresponder ao esperado, a etapa falha.

### 8. Verificação de permissões:

Obtém informações do pacote com `dumpsys package com.mulheres` e registra dados relacionados às permissões solicitadas e concedidas.

### 9. Informações finais:

Registra informações sobre o pacote, seu caminho de instalação, o estado de inicialização do Android, o tipo de compilação e o estado do Verified Boot.

### 10. Publicação dos resultados:

Copia os arquivos disponíveis em `simulation/` para a pasta `Resultados da compilação do Safe Woman OS` na branch `main`.

Se a pasta não existir, ela será criada. Se já existir, os novos arquivos serão mesclados aos existentes, sem apagar os demais arquivos da pasta.

Quando houver alterações, o workflow cria um commit e tenta enviá-lo para a `code`.

Além disso, publica os arquivos de `simulation/` como um artefato chamado `Safe-Woman-OS-Simulacao` no GitHub Actions.

## Arquivos gerados:

- `emulator.log`: saída do emulador.
- `emulator.pid`: PID registrado do processo do emulador.
- `mount.txt`: sistemas de arquivos montados.
- `df.txt`: utilização de espaço.
- `system-list.txt`: conteúdo de `/system`.
- `priv-app-list.txt`: conteúdo de `/priv-app`.
- `partitions.txt`: entradas de `/dev/block/by-name/`.
- `dynamic-partitions.txt`: propriedade de partições dinâmicas.
- `slot.txt`: sufixo do slot de inicialização.
- `product.txt`: nome do produto Android.
- `build-type.txt`: tipo de compilação.
- `debuggable.txt`: propriedade de depuração.
- `system-search.txt`: resultados da busca pelo aplicativo.
- `expected-directory.txt`: conteúdo do diretório esperado.
- `pm-path.txt`: caminho registrado pelo Package Manager.
- `permissions.txt`: informações do pacote e permissões.

## Permissões do workflow:

- `contents: write`: permite gravar alterações no repositório.
- `actions: write`: permite operações autorizadas relacionadas às execuções e aos artefatos do GitHub Actions.

A gravação na branch `main` também depende das regras de proteção e das permissões efetivas do repositório.

## Limitações:

- O ambiente é um emulador, não um dispositivo físico.
- A opção `-writable-system` não garante que todas as partições possam ser modificadas.
- `adb disable-verity`, `adb remount` e a reinicialização dos serviços podem falhar dependendo da imagem.
- O XML criado contém a estrutura da lista de permissões, mas não declara permissões individuais.
- O sucesso da compilação não comprova o funcionamento integral do aplicativo.
- A instalação em `/system/priv-app/` não garante a concessão de permissões privilegiadas.
- Como a publicação utiliza `if: always()`, ela é tentada mesmo quando etapas anteriores falham; os resultados podem representar uma execução incompleta.

Link:
https://github.com/mulher-amparada/mulher-amparada-app/tree/main/Resultados%20da%20compila%C3%A7%C3%A3o%20do%20Safe%20Woman%20OS
