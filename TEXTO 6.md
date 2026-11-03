# 👨🏾‍💻Safe Woman Operational System

O **Safe Woman Operational System (Safe Woman OS)** é um projeto experimental de personalização do Android baseado no **Android Open Source Project (AOSP)**. A proposta é integrar o aplicativo **Mulher Amparada** à imagem do sistema e desenvolver uma configuração de produto própria para execução no **Cuttlefish**.

O projeto não é um sistema operacional escrito do zero. Ele utiliza código e mecanismos do Android, acrescentando arquivos de produto, integração de aplicativos, permissões e provisionamento de perfil gerenciado.

## Auditoria da estrutura atual no GitHub

A estrutura foi conferida diretamente na árvore recursiva da pasta `Safe Woman Operational System` da branch `main`.

**Resultado da inspeção:**
- 970 entradas entre arquivos e diretórios.
- Somente três diretórios de primeiro nível: `build/`, `device/` e `packages/`.
- O arquivo AOSP `build/envsetup.sh` está presente.
- Em `device/`, existe a configuração própria `device/safe_woman/`.
- Em `packages/apps/`, existem `ManagedProvisioning/`, `MulherAmparada/` e `MulherAmparadaProfileAdmin/`.

A contagem de 970 entradas inclui os arquivos internos desses projetos; ela **não representa 970 projetos AOSP**.

### O que está presente

```text
Safe Woman Operational System/
├── build/
│   └── envsetup.sh
│
├── device/
│   └── safe_woman/
│       ├── AndroidProducts.mk
│       ├── BoardConfig.mk
│       ├── safe_woman.mk
│       ├── init.safe_woman.rc
│       ├── privapp-permissions-mulher-amparada.xml
│       └── provision-mulher-amparada-profile.sh
│
└── packages/
    └── apps/
        ├── ManagedProvisioning/
        │   └── código-fonte do componente Android
        ├── MulherAmparada/
        │   ├── Android.bp
        │   └── app/
        │       └── projeto Android do aplicativo
        └── MulherAmparadaProfileAdmin/
            ├── Android.bp
            ├── AndroidManifest.xml
            ├── res/
            └── src/
```

A representação acima resume os diretórios e arquivos relevantes, sem enumerar todos os arquivos internos.

### O que não está presente nessa pasta

Na árvore atual inspecionada, não foram encontrados diretórios de primeiro nível para:
- `frameworks/`
- `system/`
- `vendor/`
- `kernel/` ou `common/`
- `hardware/`
- `external/`
- `prebuilts/`
- `bootable/`
- `bionic/`
- `art/`
- `libcore/`
- `dalvik/`
- `cts/`

Também não foi encontrada, dentro dessa pasta, a árvore `device/google/cuttlefish/` que o produto referencia.

Isso significa que o conteúdo atual é uma **estrutura parcial de integração e personalização**, não uma árvore AOSP completa. O componente `ManagedProvisioning` é código-fonte do Android, mas sua presença isolada não substitui os demais projetos e dependências da plataforma.

## Configuração própria do produto

O diretório `device/safe_woman/` contém os arquivos centrais da configuração personalizada.

### AndroidProducts.mk

Registra `safe_woman.mk` e declara a opção:

```text
safe_woman-userdebug
```

A declaração disponibiliza o nome do produto para o sistema de build quando a árvore e suas dependências estão corretamente integradas. Ela não comprova que o alvo já possa ser compilado na pasta atual.

### BoardConfig.mk

Define:
- `TARGET_ARCH := x86_64`
- `TARGET_ARCH_VARIANT := x86-64`
- `TARGET_USES_64_BIT_BINDER := true`

Portanto, a configuração atual está direcionada a uma plataforma x86_64, coerente com a intenção de testar um dispositivo virtual Cuttlefish. Ela não é, por si só, uma configuração de hardware para o Galaxy A16 5G.

### safe_woman.mk

Define a identidade do produto:
- Produto: `safe_woman`
- Dispositivo: `safe_woman`
- Marca: `SafeWoman`
- Modelo: `Safe Woman Operational System`
- Fabricante: `SafeWoman`

O arquivo tenta herdar:

```text
device/google/cuttlefish/vsoc_x86_64/phone/aosp_cf.mk
```

Esse caminho não está presente na árvore atual da pasta. Portanto, a herança só poderá ser resolvida quando o projeto Cuttlefish correspondente for incorporado a uma árvore AOSP compatível.

O produto também declara os módulos `MulherAmparada` e `MulherAmparadaProfileAdmin` em `PRODUCT_PACKAGES` e copia os arquivos de permissões e provisionamento para as partições `product` e `vendor`.

## Aplicativo Mulher Amparada integrado

O aplicativo está em:

```text
packages/apps/MulherAmparada/
```

O módulo Soong declarado em `Android.bp` chama-se:

```text
MulherAmparada
```

O arquivo declara fontes Kotlin e Java, recursos em `app/src/main/res`, `sdk_version: "current"`, alvo JVM 17 e `privileged: true`.

A configuração `privileged: true` solicita que o módulo seja instalado como aplicativo privilegiado na imagem. Isso não concede automaticamente qualquer permissão solicitada pelo aplicativo. Permissões privilegiadas precisam ser compatíveis com a plataforma e atender às regras de allowlist; permissões de runtime continuam sujeitas ao modelo normal de concessão do Android.

O pacote do aplicativo principal é `com.mulheres`.

## Aplicativo auxiliar de administração do perfil

O projeto contém também:

```text
packages/apps/MulherAmparadaProfileAdmin/
```

O módulo Soong é `MulherAmparadaProfileAdmin`, com pacote declarado como `com.mulheres.profileadmin`.

Seus arquivos incluem:
- `Android.bp`
- `AndroidManifest.xml`
- `res/xml/device_admin.xml`
- `MulherAmparadaProfileAdminReceiver.java`
- `ProfileProvisioningActivity.java`

O receiver usa o mecanismo `DeviceAdminReceiver`, e a atividade auxilia no provisionamento. O objetivo é administrar um perfil gerenciado Android dedicado ao Mulher Amparada.

A presença do módulo e de seu manifesto comprova que existe uma implementação de suporte no repositório; não comprova que o perfil já tenha sido criado com sucesso em um dispositivo.

## Provisionamento do perfil gerenciado

O arquivo:

```text
device/safe_woman/provision-mulher-amparada-profile.sh
```

implementa um fluxo experimental que:
1. Procura um usuário chamado `Mulher Amparada`.
2. Se não encontrar, tenta criar um perfil gerenciado associado ao usuário principal.
3. Tenta iniciar o perfil.
4. Tenta disponibilizar nele os pacotes `com.mulheres.profileadmin` e `com.mulheres`.
5. Verifica se o administrador já aparece como Profile Owner.
6. Caso ainda não apareça, tenta executar `dpm set-profile-owner`.
7. Quando define o Profile Owner nessa execução, tenta abrir a atividade de provisionamento.

O arquivo `init.safe_woman.rc` declara o serviço `safe_woman_managed_profile`, acionado após `sys.boot_completed=1`. O serviço executa o script de provisionamento como root e está marcado como `oneshot`.

**Ponto de atenção técnico:** o script contém chamadas que ignoram falhas, por exemplo nas tentativas de iniciar o perfil e disponibilizar os aplicativos. Além disso, o comando de criação do perfil e a atribuição de Profile Owner estão sujeitos às restrições do Android e ao estado de provisionamento do dispositivo. O log final do script, isoladamente, não é prova suficiente de que todas as etapas foram concluídas. É necessário verificar os usuários, os pacotes instalados no perfil e o estado do Device Policy Manager após a inicialização.

## Allowlist de permissões privilegiadas

O arquivo:

```text
device/safe_woman/privapp-permissions-mulher-amparada.xml
```

declara permissões para o pacote `com.mulheres`, incluindo localização, contatos, chamadas, microfone, serviços em primeiro plano, bloqueio de suspensão e acesso a armazenamento.

**A configuração precisa de revisão antes de uma compilação de produção.** A allowlist de permissões privilegiadas não é um mecanismo para conceder permissões comuns de runtime. Algumas permissões listadas, como localização, contatos, microfone e chamadas, normalmente dependem de concessão em runtime ou de outros mecanismos específicos. Permissões obsoletas ou restritas por versão também precisam ser avaliadas. A lista deve conter somente permissões realmente classificadas como privilegiadas e permitidas para o pacote na versão de Android escolhida.

## Estado de completude da árvore

| Área | Estado verificado na pasta |
|---|---|
| Configuração do produto `safe_woman` | Presente |
| Configuração x86_64 | Presente |
| `build/envsetup.sh` | Presente |
| Aplicativo Mulher Amparada | Presente |
| Módulo auxiliar Profile Admin | Presente |
| Código-fonte ManagedProvisioning | Presente |
| Projeto Cuttlefish referenciado | Não encontrado nessa pasta |
| Android Framework (`frameworks/base`) | Não encontrado |
| System (`system/core`, `system/sepolicy`) | Não encontrado |
| Vendor | Não encontrado |
| Kernel | Não encontrado |
| Hardware e HALs | Não encontrados |
| Prebuilts e demais dependências de build | Não encontrados como árvore própria |
| Imagem Safe Woman OS compilada e testada | Não comprovada pela inspeção do repositório |

A ausência desses diretórios nesta pasta não significa que não existam em outro local ou em outro ambiente de trabalho. Significa apenas que eles não fazem parte da árvore atualmente armazenada nesse caminho do repositório.

## O que falta para compilar o produto

Para transformar essa estrutura em um produto Android compilável, ainda é necessário:

1. Sincronizar uma árvore AOSP completa e compatível, usando um manifesto e uma branch consistentes.
2. Incorporar a configuração `device/safe_woman` à árvore sincronizada.
3. Disponibilizar o projeto Cuttlefish exigido pelo `safe_woman.mk` e resolver suas dependências.
4. Confirmar que os módulos dos dois aplicativos são aceitos pelo Soong e corrigir dependências, APIs e recursos incompatíveis.
5. Revisar a allowlist de permissões privilegiadas.
6. Validar os arquivos init e o script de provisionamento no ambiente de destino.
7. Executar `lunch safe_woman-userdebug` na árvore integrada.
8. Compilar com `m` e corrigir os erros encontrados.
9. Inicializar a imagem no Cuttlefish e testar separadamente o aplicativo, o perfil gerenciado, o Profile Owner e as permissões.

A existência de `AndroidProducts.mk` e `safe_woman.mk` não significa que o produto já compile. Da mesma forma, compilar um produto AOSP Cuttlefish genérico não equivale a compilar o Safe Woman OS.

## Relação entre os componentes

A arquitetura pretendida é:

```text
Árvore AOSP completa e compatível
              +
      Projeto Cuttlefish
              +
     device/safe_woman
              +
      Mulher Amparada
              +
    MulherAmparadaProfileAdmin
              ↓
      safe_woman-userdebug
              ↓
      Imagem personalizada
              ↓
          Cuttlefish
              ↓
      Testes de inicialização,
      aplicativo e perfil gerenciado
```

Esse fluxo descreve a arquitetura planejada, não um resultado de compilação já confirmado.

## Conclusão: estado atual do Safe Woman OS

O Safe Woman OS já possui uma base própria de personalização Android: configuração de produto x86_64, declaração de módulos Soong para o aplicativo principal e o administrador do perfil, integração do código-fonte de ManagedProvisioning, allowlist de permissões e um fluxo experimental de provisionamento de perfil gerenciado.

Entretanto, a inspeção da pasta no GitHub mostra que ela **não contém uma árvore AOSP completa**. Atualmente, seus diretórios de primeiro nível são apenas `build/`, `device/` e `packages/`. Não foram encontrados nela o Framework, System, Vendor, Kernel, Hardware, Prebuilts nem o projeto Cuttlefish referenciado pela configuração.

Assim, a classificação correta é: **projeto de sistema Android personalizado em desenvolvimento, com estrutura parcial de produto e integração de aplicativos**. Ainda não há comprovação, apenas pela árvore do repositório, de compilação bem-sucedida, imagem inicializável ou funcionamento do perfil gerenciado.

🔗 **Diretório do projeto:**  
https://github.com/mulher-amparada/mulher-amparada-app/tree/main/Safe%20Woman%20Operational%20System
