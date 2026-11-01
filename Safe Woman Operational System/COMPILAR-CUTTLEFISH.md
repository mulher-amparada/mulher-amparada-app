# Preparação de compilação — Android 17 Cuttlefish

Este diretório contém componentes AOSP selecionados que foram importados anteriormente. Eles **não formam, isoladamente, uma árvore AOSP compilável**.

## Alvo

- Manifesto oficial: https://android.googlesource.com/platform/manifest
- Branch: `android17-release`
- Produto: `aosp_cf_x86_64_only_phone`
- Configuração: `aosp_current-userdebug`

## Como preparar e compilar

Em uma estação Linux x86_64 que atenda aos requisitos oficiais:

```bash
sudo apt-get update
sudo apt-get install -y git curl python3
bash "Safe Woman Operational System/preparar-cuttlefish-android17.sh"
```

O script sincroniza os projetos declarados pelo manifesto oficial com Repo, configura o alvo Cuttlefish e executa a compilação. A sincronização usa o manifesto oficial completo; não é uma seleção artesanal de alguns repositórios, porque as dependências transitivas do build são resolvidas pelo conjunto oficial.

É possível escolher outra pasta e número de tarefas:

```bash
AOSP_DIR="$HOME/aosp-android17-cuttlefish" JOBS=8 bash "Safe Woman Operational System/preparar-cuttlefish-android17.sh"
```

## Requisitos e limitações

A documentação oficial do AOSP indica pelo menos **400 GB livres** (250 GB para sincronizar e 150 GB para compilar) e **64 GB de RAM**, em Linux x86_64. O primeiro sync e a primeira compilação podem levar bastante tempo.

O GitHub Actions hospedado comum não atende a esses requisitos de armazenamento e memória para uma compilação completa. Por isso, este repositório guarda o script e os componentes selecionados, não uma cópia de toda a árvore AOSP nem os artefatos de build.

## Importante

- Os dez componentes já importados são preservados.
- O script cria a árvore AOSP em uma pasta separada chamada `aosp-android17-cuttlefish` por padrão; não sobrescreve a pasta de componentes existente.
- A execução ainda não foi feita neste ambiente. A inclusão do script não significa que a ROM já foi compilada ou testada.
