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
