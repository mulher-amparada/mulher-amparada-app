# 🔐 Política de Segurança

## Mulher Amparada

A segurança do **Mulher Amparada** é uma prioridade.

Por se tratar de um aplicativo que possui recursos relacionados à proteção, privacidade e segurança pessoal, qualquer vulnerabilidade identificada no projeto deve ser tratada com responsabilidade e comunicada aos mantenedores de forma adequada.

Esta política explica como reportar vulnerabilidades de segurança, quais informações podem ser fornecidas e como esses relatos serão tratados.

---

## 📋 Índice

- [Versões suportadas](#-versões-suportadas)
- [O que é uma vulnerabilidade](#-o-que-é-uma-vulnerabilidade)
- [Como reportar uma vulnerabilidade](#-como-reportar-uma-vulnerabilidade)
- [Informações que devem ser incluídas](#-informações-que-devem-ser-incluídas)
- [Divulgação responsável](#-divulgação-responsável)
- [Processo de análise](#-processo-de-análise)
- [Classificação de severidade](#-classificação-de-severidade)
- [Testes de segurança](#-testes-de-segurança)
- [Proteção de dados](#-proteção-de-dados)
- [Escopo](#-escopo)
- [Reconhecimento](#-reconhecimento)
- [Problemas comuns](#-problemas-comuns)
- [Contato](#-contato)

---

## 🛡️ Versões suportadas

Recomendamos utilizar sempre a versão mais recente do **Mulher Amparada** disponibilizada pelo projeto.

| Versão | Status |
|---|---|
| Versão mais recente | ✅ Suportada |
| Versões anteriores | ⚠️ Suporte limitado |
| Versões descontinuadas | ❌ Não suportadas |

Uma vulnerabilidade encontrada em uma versão antiga poderá ser analisada, especialmente quando também estiver presente na versão atualmente suportada.

---

## ⚠️ O que é uma vulnerabilidade?

Consideramos uma vulnerabilidade qualquer falha que possa comprometer a segurança, privacidade, integridade ou funcionamento esperado do aplicativo.

Entre os exemplos estão:

- Bypass de mecanismos de proteção;
- Falhas de autenticação ou autorização;
- Acesso não autorizado a informações;
- Exposição de dados que deveriam permanecer protegidos;
- Armazenamento inadequado de informações sensíveis;
- Falhas na implementação de mecanismos criptográficos;
- Vazamento de credenciais, tokens ou chaves;
- Acesso indevido a arquivos;
- Execução não autorizada de funcionalidades;
- Falhas relacionadas ao Android, Kotlin, JavaScript ou WebView;
- Falhas que possam comprometer recursos de segurança do aplicativo.

Esta lista não é exaustiva.

Um comportamento inesperado não significa necessariamente que exista uma vulnerabilidade. Cada relatório será analisado individualmente.

---

## 🚨 Como reportar uma vulnerabilidade

### ⚠️ Não utilize Issues públicas

**Não publique vulnerabilidades de segurança em Issues, Pull Requests ou discussões públicas.**

A divulgação pública antes da existência de uma correção pode permitir que outras pessoas explorem o problema e aumentar o risco para os usuários.

Utilize um canal privado de contato com os responsáveis pelo projeto.

Caso o GitHub disponibilize um mecanismo privado de reporte de vulnerabilidades para o repositório, ele deverá ser utilizado preferencialmente.

---

## 📝 Informações que devem ser incluídas

Um bom relatório deve conter o máximo possível de informações relevantes.

Você pode utilizar o seguinte modelo:

Título:
[Resumo curto da vulnerabilidade]

Versão afetada:
[Versão do Mulher Amparada]

Componente afetado:
[Componente ou funcionalidade]

Descrição:
[Explique claramente o problema]

Impacto:
[Explique quais consequências podem ocorrer]

Passos para reprodução:
[Descreva como reproduzir o problema]

Ambiente:
[Modelo do dispositivo e versão do Android, quando relevante]

Evidências:
[Capturas de tela, registros ou outras evidências]

Sugestão de correção:
[Opcional]

Não é necessário possuir todas essas informações para realizar um reporte.

Um relatório incompleto ainda poderá ser analisado.

---

## 🔍 Processo de análise

Após o recebimento de um relatório, os responsáveis pelo projeto poderão:

1. Confirmar o recebimento;
2. Analisar as informações fornecidas;
3. Tentar reproduzir o comportamento;
4. Identificar os componentes afetados;
5. Determinar as versões afetadas;
6. Avaliar o impacto e a severidade;
7. Desenvolver ou avaliar uma correção;
8. Testar a correção;
9. Publicar uma atualização, quando necessário;
10. Documentar a resolução de forma responsável.

O tempo necessário para cada etapa depende da complexidade e do impacto da vulnerabilidade.

---

## 📊 Classificação de severidade

As vulnerabilidades poderão ser classificadas de acordo com seu impacto:

| Severidade | Descrição |
|---|---|
| 🔴 Crítica | Vulnerabilidade com potencial de causar comprometimento grave da segurança, exposição significativa de dados ou controle não autorizado de recursos importantes. |
| 🟠 Alta | Vulnerabilidade com impacto significativo que pode permitir ações não autorizadas ou comprometer recursos importantes. |
| 🟡 Média | Vulnerabilidade com impacto relevante, normalmente dependente de determinadas condições para ser explorada. |
| 🟢 Baixa | Vulnerabilidade com impacto limitado ou com condições de exploração mais restritivas. |
| ⚪ Informativa | Problema ou comportamento que não representa um risco significativo à segurança. |

A classificação definitiva será determinada após a análise técnica.

---

## 🧪 Testes de segurança

Pesquisas e testes de segurança devem ser realizados de forma responsável e somente em ambientes para os quais o pesquisador possui autorização.

Não realize testes que possam:

- Acessar dados de terceiros;
- Utilizar contas de terceiros;
- Comprometer dispositivos de outras pessoas;
- Alterar ou excluir dados sem autorização;
- Interromper deliberadamente serviços;
- Causar danos ao aplicativo ou aos usuários;
- Expor informações privadas;
- Afetar sistemas externos sem autorização.

Sempre que possível, utilize dispositivos, contas e dados especificamente destinados a testes.

---

## 🔒 Proteção de dados

Relatórios de segurança podem conter informações potencialmente sensíveis.

**Nunca envie dados pessoais ou confidenciais desnecessários.**

Utilize dados fictícios ou anonimizados sempre que possível.

Não inclua no relatório:

- Senhas;
- Tokens de autenticação;
- Chaves privadas;
- Credenciais;
- Dados pessoais;
- Arquivos pessoais;
- Informações de terceiros;
- Informações que não sejam necessárias para reproduzir o problema.

Caso uma evidência contenha informações sensíveis, remova essas informações antes de enviá-la.

---

## 🎯 Escopo

Esta política se aplica ao código e às funcionalidades desenvolvidas e disponibilizadas pelo projeto **Mulher Amparada** neste repositório:

https://github.com/mulher-amparada/mulher-amparada-app

O escopo pode incluir, entre outros:

- Código Android;
- Kotlin;
- JavaScript;
- HTML;
- CSS;
- WebView;
- Armazenamento local;
- Autenticação;
- Sistemas de proteção;
- Criptografia;
- Gerenciamento de arquivos;
- Comunicação entre componentes;
- Configurações de segurança;
- Recursos desenvolvidos especificamente para o aplicativo.

Bibliotecas, sistemas operacionais, APIs, serviços externos e plataformas de terceiros podem possuir suas próprias políticas de segurança e devem ser reportados aos respectivos responsáveis quando apropriado.

---

## 🏆 Reconhecimento

Agradecemos pesquisadores, desenvolvedores e colaboradores que contribuam de forma responsável para a segurança do projeto.

Quando apropriado, o projeto poderá reconhecer publicamente pesquisadores que tenham reportado vulnerabilidades de maneira responsável.

Qualquer reconhecimento público poderá ser realizado somente quando isso não comprometer a privacidade ou a segurança do pesquisador.

---

## 📢 Problemas comuns

Issues públicas podem ser utilizadas para relatar:

- Bugs que não representem riscos de segurança;
- Problemas de funcionamento;
- Sugestões;
- Melhorias;
- Problemas de interface;
- Outras questões que não contenham informações sensíveis.

Se houver dúvida sobre a natureza de um problema, prefira tratá-lo inicialmente como um possível problema de segurança e utilize um canal privado.

---

## 📬 Contato

Para reportar uma vulnerabilidade de segurança, utilize um canal privado disponibilizado pelos responsáveis pelo projeto.

### Repositório oficial

https://github.com/mulher-amparada/mulher-amparada-app

Caso o repositório disponibilize o recurso **Security Advisories / Report a vulnerability**, utilize esse mecanismo preferencialmente para reportes relacionados à segurança.

---

## ❤️ Obrigado

Agradecemos a todos que dedicam tempo para identificar e comunicar problemas de segurança de forma responsável.

Cada reporte válido pode ajudar a:

- Melhorar a segurança do aplicativo;
- Corrigir vulnerabilidades;
- Proteger os usuários;
- Aumentar a qualidade do código;
- Tornar o projeto mais confiável.

> Segurança, privacidade e responsabilidade são compromissos contínuos do projeto Mulher Amparada.