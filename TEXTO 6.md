# 👨🏾‍💻 Safe Woman Operational System:

O **Safe Woman Operational System (Safe Woman OS)** é um projeto experimental de personalização do Android, baseado no **Android Open Source Project (AOSP)**. Seu objetivo é integrar o aplicativo **Mulher Amparada** à imagem do sistema e explorar uma configuração própria para execução no **Cuttlefish**, ambiente virtual de dispositivos Android.

O projeto não é um sistema operacional desenvolvido do zero. Ele utiliza componentes do AOSP e acrescenta configurações, aplicativos e scripts próprios.

## Estado da estrutura no repositório

A pasta `Safe Woman Operational System` não contém uma árvore AOSP completa. Porém, ela também não é mais composta somente pelos arquivos de personalização: atualmente reúne uma seleção de componentes de código-fonte do Android 17, importados de repositórios oficiais do AOSP, além dos arquivos específicos do Safe Woman OS.

A automação `.github/workflows/importar-cuttlefish-android17.yml` importa componentes da branch `android17-release` e copia seus arquivos para essa pasta, sem incluir os diretórios Git internos dos clones.

Os repositórios/componentes definidos nessa automação são:

- `device/google/cuttlefish`
- `frameworks/base`
- `packages/apps/ManagedProvisioning`
- `packages/apps/Settings`
- `packages/apps/Launcher3`
- `packages/modules/Permission`
- `packages/modules/NetworkStack`
- `packages/modules/HealthFitness`
- `system/sepolicy`
- `system/core`

Portanto, a pasta contém uma **seleção de componentes do AOSP**, e não todos os projetos necessários para formar uma árvore Android completa. A presença desses diretórios não significa que todos os componentes e dependências de uma compilação estejam armazenados nela.

## Organização dos componentes próprios

Os arquivos específicos do Safe Woman OS ficam principalmente nestes diretórios:

```text
Safe Woman Operational System/
├── device/
│   ├── google/
│   │   └── cuttlefish/
│   │       └── componentes importados do Cuttlefish
│   └── safe_woman/
│       ├── AndroidProducts.mk
│       ├── BoardConfig.mk
│       ├── safe_woman.mk
│       ├── init.safe_woman.rc
│       ├── provision-mulher-amparada-profile.sh
│       └── privapp-permissions-mulher-amparada.xml
│
├── frameworks/
│   └── base/
│       └── componentes importados do Android Framework
│
├── packages/
│   ├── apps/
│   │   ├── Launcher3/
│   │   ├── ManagedProvisioning/
│   │   ├── Settings/
│   │   ├── MulherAmparada/
│   │   │   ├── Android.bp
│   │   │   └── app/
│   │   │       └── src/main/
│   │   └── MulherAmparadaProfileAdmin/
│   │       ├── Android.bp
│   │       ├── AndroidManifest.xml
│   │       ├── res/
│   │       └── src/
│   └── modules/
│       ├── HealthFitness/
│       ├── NetworkStack/
│       └── Permission/
│
├── system/
│   ├── core/
│   └── sepolicy/
│
└── preparar-cuttlefish-android17.sh
```

A organização acima apresenta os principais componentes identificados e não pretende enumerar todos os arquivos internos dos projetos importados.

## Configuração do produto Safe Woman OS

O diretório `device/safe_woman` contém a configuração própria do produto.

### AndroidProducts.mk

Registra o arquivo de produto e disponibiliza a opção de compilação:

```text
safe_woman-userdebug
```

### BoardConfig.mk

Define a arquitetura de destino:

- `TARGET_ARCH := x86_64`
- `TARGET_ARCH_VARIANT := x86-64`
- `TARGET_USES_64_BIT_BINDER := true`

### safe_woman.mk

Define a identidade do produto:

- Nome: `Safe Woman Operational System`
- Produto: `safe_woman`
- Dispositivo: `safe_woman`
- Marca: `SafeWoman`
- Fabricante: `SafeWoman`

O arquivo herda a configuração de telefone virtual x86_64 do Cuttlefish:

```text
device/google/cuttlefish/vsoc_x86_64/phone/aosp_cf.mk
```

Também declara os módulos `MulherAmparada` e `MulherAmparadaProfileAdmin` em `PRODUCT_PACKAGES` e copia arquivos de configuração e provisionamento para as partições correspondentes.

## Integração do aplicativo Mulher Amparada

O aplicativo é mantido em:

```text
packages/apps/MulherAmparada
```

Seu identificador de pacote é:

```text
com.mulheres
```

O arquivo `Android.bp` declara o módulo Soong:

```text
MulherAmparada
```

A configuração define `privileged: true`, para que o aplicativo seja incluído como aplicativo privilegiado na imagem do sistema, conforme a configuração de build.

Isso, por si só, **não concede automaticamente todas as permissões**. As permissões de runtime continuam sujeitas às regras do Android, e permissões privilegiadas dependem de compatibilidade, allowlist e demais requisitos da plataforma.

O código-fonte do aplicativo é sincronizado para essa pasta pela automação `.github/workflows/sincronizar-app.yml`, que utiliza como origem o diretório:

```text
Código-fonte do app = Mulher Amparada
```

e como destino:

```text
Safe Woman Operational System/packages/apps/MulherAmparada
```

## Aplicativo auxiliar de perfil gerenciado

O projeto também contém o módulo:

```text
MulherAmparadaProfileAdmin
```

Ele possui um receiver baseado em `DeviceAdminReceiver` e uma atividade de provisionamento. O objetivo é dar suporte à configuração de um perfil gerenciado chamado **Mulher Amparada**.

Os arquivos relacionados incluem:

- `packages/apps/MulherAmparadaProfileAdmin/Android.bp`
- `packages/apps/MulherAmparadaProfileAdmin/AndroidManifest.xml`
- `packages/apps/MulherAmparadaProfileAdmin/src/`
- `device/safe_woman/init.safe_woman.rc`
- `device/safe_woman/provision-mulher-amparada-profile.sh`

O serviço definido no arquivo init é iniciado após a inicialização do Android e executa o script de provisionamento. O script procura ou tenta criar o perfil, disponibiliza os aplicativos e tenta configurar o administrador do perfil.

Esse fluxo é experimental: a criação do perfil e a definição de Profile Owner dependem das condições e restrições do Android durante o provisionamento. A existência dos arquivos não comprova que o procedimento já funcione em todas as inicializações ou configurações.

## Allowlist de permissões privilegiadas

O arquivo:

```text
device/safe_woman/privapp-permissions-mulher-amparada.xml
```

declara permissões associadas ao pacote `com.mulheres`.

A allowlist não transforma permissões comuns de runtime — como localização, contatos e microfone — em permissões automaticamente concedidas. Somente permissões compatíveis com o mecanismo de permissões privilegiadas devem ser mantidas nessa configuração, respeitando também as regras da versão do Android utilizada.

## Importação dos componentes Android 17

A automação:

```text
.github/workflows/importar-cuttlefish-android17.yml
```

é executada manualmente por `workflow_dispatch`.

Ela:

1. Baixa o repositório do projeto.
2. Clona os dez componentes definidos na automação, na branch `android17-release`.
3. Copia os arquivos para `Safe Woman Operational System`, excluindo os metadados `.git` dos clones.
4. Configura Git LFS para arquivos acima de 90 MB.
5. Registra as alterações e envia os componentes para a branch `main`.

Essa automação importa os componentes selecionados. Ela não sincroniza, por si só, todos os repositórios do manifesto AOSP.

## Preparação de uma árvore AOSP completa

O script:

```text
preparar-cuttlefish-android17.sh
```

tem uma função diferente da automação de importação de componentes.

Ele prepara um diretório de trabalho separado, inicializa o cliente Repo com o manifesto oficial do AOSP na branch `android17-release` e executa `repo sync`.

O script também verifica requisitos mínimos definidos nele próprio:

- Linux de 64 bits.
- Arquitetura de host x86_64.
- Pelo menos 400 GB de espaço livre.
- Pelo menos 64 GB de RAM.
- Disponibilidade de Git, cURL e Python 3.

Caso o comando `repo` não esteja instalado, o script tenta baixá-lo para `$HOME/bin`.

**Limitação importante da configuração atual:** depois da sincronização, o script executa:

```text
lunch aosp_cf_x86_64_only_phone-aosp_current-userdebug
m
```

Esse alvo é o produto AOSP Cuttlefish genérico. O script, na versão atualmente armazenada, ainda não aplica automaticamente os arquivos próprios do Safe Woman OS à árvore sincronizada nem seleciona `safe_woman-userdebug`.

Assim, ele prepara e compila uma base AOSP/Cuttlefish, mas essa execução isolada não deve ser apresentada como uma compilação do Safe Woman OS.

## Como as partes se relacionam

A arquitetura pretendida é:

```text
Árvore AOSP completa (Android 17)
             +
Componentes do Cuttlefish
             +
Configuração device/safe_woman
             +
Aplicativo Mulher Amparada
             +
Aplicativo auxiliar de perfil gerenciado
             ↓
       safe_woman-userdebug
             ↓
       Imagem personalizada
             ↓
          Cuttlefish
```

Essa é a arquitetura planejada para a integração. A compilação bem-sucedida do produto personalizado e sua execução precisam ser verificadas separadamente.

## Estado atual e limitações

O Safe Woman OS possui:

- Uma configuração própria de produto Android.
- Uma configuração de arquitetura x86_64.
- Componentes selecionados do AOSP Android 17.
- Componentes do Cuttlefish.
- Integração declarada do aplicativo Mulher Amparada pelo Soong.
- Um módulo auxiliar para administração de perfil gerenciado.
- Arquivos de provisionamento e configuração de permissões.
- Uma automação para importar componentes.
- Um script separado para sincronizar e compilar uma base AOSP/Cuttlefish.

Ainda é necessário validar a integração de todos esses elementos em uma árvore AOSP completa, incluindo dependências do aplicativo, regras de permissões, configuração do produto e compilação do alvo `safe_woman-userdebug`.

A presença dos arquivos no GitHub não equivale, por si só, a uma imagem final compilada, inicializada e testada.

## Objetivo futuro

O objetivo é consolidar os componentes próprios do Safe Woman OS sobre uma árvore AOSP Android 17 completa, resolver as dependências de build, compilar o produto personalizado e validar sua execução no Cuttlefish.

O resultado esperado é uma imagem Android personalizada que integre o Mulher Amparada ao sistema.

## Natureza do projeto

O Safe Woman Operational System é um projeto experimental de personalização e integração sobre o AOSP. Ele não é um sistema operacional criado do zero e não contém, nesta pasta, uma árvore AOSP completa.

A pasta reúne componentes selecionados do Android e os arquivos próprios necessários para desenvolver e testar a personalização.

🔗 **Diretório do projeto:**

https://github.com/mulher-amparada/mulher-amparada-app/tree/main/Safe%20Woman%20Operational%20System

## Nova etapa: perfil de usuário do Mulher Amparada

Além da integração do aplicativo à imagem do sistema, o projeto agora também contempla a criação de um **perfil de usuário gerenciado do Android chamado “Mulher Amparada”**.

A proposta é disponibilizar o aplicativo principal dentro desse perfil, separado do espaço principal do dispositivo. Para administrar esse ambiente, o projeto inclui o aplicativo auxiliar `MulherAmparadaProfileAdmin`, cujo pacote é `com.mulheres.profileadmin`.

O fluxo previsto nos arquivos atuais é:

1. Após a inicialização do Android, o serviço `safe_woman_managed_profile` chama o script de provisionamento.
2. O script procura um perfil gerenciado existente com o nome “Mulher Amparada” e, caso não encontre, tenta criar um associado ao usuário principal.
3. O script tenta disponibilizar os pacotes `com.mulheres.profileadmin` e `com.mulheres` dentro do perfil.
4. O componente `MulherAmparadaProfileAdminReceiver`, baseado em `DeviceAdminReceiver`, é indicado como administrador do perfil (Profile Owner).
5. A atividade `ProfileProvisioningActivity` verifica se o aplicativo é o Profile Owner, define o nome do perfil e solicita sua habilitação.

Os principais arquivos dessa implementação são:

- `device/safe_woman/init.safe_woman.rc`
- `device/safe_woman/provision-mulher-amparada-profile.sh`
- `packages/apps/MulherAmparadaProfileAdmin/AndroidManifest.xml`
- `packages/apps/MulherAmparadaProfileAdmin/src/com/mulheres/profileadmin/MulherAmparadaProfileAdminReceiver.java`
- `packages/apps/MulherAmparadaProfileAdmin/src/com/mulheres/profileadmin/ProfileProvisioningActivity.java`
- `device/safe_woman/safe_woman.mk`

O perfil gerenciado é uma funcionalidade do próprio Android, não apenas uma tela ou uma conta interna do aplicativo. Ele permite manter um espaço de usuário separado, sujeito às regras de gerenciamento de perfis da plataforma.

**Estado da implementação:** a estrutura de código e o fluxo de provisionamento estão presentes no repositório, mas ainda precisam ser validados em uma imagem compilada e inicializada do Safe Woman OS. A criação do perfil, a instalação dos pacotes e a atribuição do Profile Owner podem depender do estado de provisionamento do dispositivo e das restrições da versão do Android. Portanto, a documentação registra essa funcionalidade como implementação em desenvolvimento, e não como recurso já comprovadamente funcional.

Com isso, o escopo do Safe Woman OS passa a incluir não apenas a integração do Mulher Amparada ao sistema, mas também a proposta de um **perfil de usuário Android dedicado ao Mulher Amparada**, administrado por um componente próprio do projeto.
