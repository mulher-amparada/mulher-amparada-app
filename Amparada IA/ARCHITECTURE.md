# 🏗Arquitetura da Amparada IA:

## Objetivo desta etapa:

Construir uma prova de conceito pequena e auditável para estudar os fundamentos de modelos de linguagem. O código desta pasta inicializa os parâmetros aleatoriamente e aprende apenas com o corpus explicitamente fornecido.

## Fluxo de dados:

1. O corpus de treinamento é lido como UTF-8.
2. O tokenizador por caractere cria um vocabulário determinístico.
3. Cada caractere vira um ID inteiro.
4. Janelas de tamanho fixo formam pares de entrada e alvo deslocados em um token.
5. O Transformer calcula logits para o próximo caractere.
6. A entropia cruzada mede o erro e o otimizador AdamW atualiza os pesos.
7. O checkpoint guarda os pesos, a configuração e o vocabulário para inferência posterior.

## Componentes:

- Embedding de tokens: vetor aprendido para cada ID.
- Embedding posicional: representa a posição de cada caractere no contexto.
- Atenção multi-cabeças causal: impede que uma posição consulte tokens futuros.
- Rede feed-forward: transforma as representações de cada posição.
- Conexões residuais e LayerNorm: ajudam a estabilizar o treinamento.
- Cabeça de linguagem: converte a representação em pontuações para o próximo caractere.

## Por que começar por caracteres?

É simples de implementar e permite visualizar o pipeline completo. Porém, sequências ficam longas, palavras não são unidades atômicas e a eficiência é baixa. Um tokenizador por subpalavras poderá ser pesquisado posteriormente.

## Divisão de dados:

Treinamento e validação devem ser arquivos distintos. A validação deve conter texto compatível com o vocabulário construído no treinamento. Não use o conjunto de validação para atualizar pesos.

## Métricas:

A perda mede o erro de previsão no conjunto avaliado. A perplexidade é a exponencial da perda média e dá uma noção da incerteza do modelo. Nenhuma dessas métricas, isoladamente, comprova compreensão, factualidade, segurança ou qualidade de acolhimento.

## Limites:

O corpus de demonstração é pequeno demais para produzir um assistente útil. O modelo pode reproduzir padrões sem compreender contexto, inventar informações ou gerar conteúdo inadequado. Não deve ser conectado a usuárias nem utilizado para aconselhamento, triagem de risco ou atendimento de emergência.

## Segurança de dados:

Não inserir dados pessoais, relatos de violência, conversas privadas, documentos, endereços ou identificadores de usuárias no corpus. Antes de considerar dados reais, realizar avaliação jurídica, ética, de privacidade e de segurança com orientação especializada.
