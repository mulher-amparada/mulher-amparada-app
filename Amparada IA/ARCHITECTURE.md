# ⚙️Arquitetura da Amparada IA:

## Objetivo e estado atual:

A Amparada IA é uma prova de conceito para estudar os fundamentos de modelos de linguagem. O modelo é inicializado com parâmetros aleatórios e aprende padrões estatísticos a partir do corpus fornecido.

Implementação atual:
- Transformer Decoder-only.
- Tokenizador BPE simples, implementado no projeto.
- Previsão autorregressiva do próximo token.
- Treinamento com PyTorch e otimizador AdamW.
- Checkpoint com pesos, configuração e estado do tokenizador.

## Fluxo de dados:

1. O programa lê `data/treino.txt` e `data/validacao.txt` como UTF-8.
2. O tokenizador BPE é aprendido exclusivamente a partir do texto de treino.
3. O texto é inicialmente separado em palavras, sequências de espaços e grupos de pontuação; depois são aprendidas fusões de pares frequentes.
4. Cada token vira um ID inteiro. Símbolos desconhecidos são convertidos para `<unk>`.
5. O treinamento seleciona janelas aleatórias de `block_size` tokens. A entrada contém a sequência e o alvo contém a mesma sequência deslocada uma posição à frente.
6. O modelo produz logits para cada posição, isto é, pontuações para o próximo token.
7. A entropia cruzada calcula a perda entre os logits e os tokens-alvo.
8. AdamW atualiza os parâmetros. Os gradientes são limitados por norma máxima de 1,0.
9. Em intervalos definidos, a perda é estimada separadamente nos dados de treino e validação. A validação não atualiza os pesos.
10. Ao final, o checkpoint salva os pesos, a configuração, o tokenizador (vocabulário e fusões) e informações básicas do treinamento.

## Componentes do modelo:

### Embeddings
- **Embedding de tokens:** transforma cada ID em um vetor de dimensão `n_embd`.
- **Embedding posicional aprendido:** adiciona informação sobre a posição de cada token, até o limite de `block_size`.

### Blocos causais:

Cada bloco contém:
- **LayerNorm antes da atenção (Pre-LN).**
- **Atenção multi-head causal:** uma máscara triangular impede que uma posição consulte tokens futuros.
- **Conexão residual:** soma a saída da atenção à representação de entrada.
- **LayerNorm antes da rede feed-forward.**
- **Rede feed-forward:** expande a dimensão para `4 × n_embd`, aplica GELU, projeta de volta para `n_embd` e aplica dropout.
- **Conexão residual:** soma a saída feed-forward à representação anterior.

O número de blocos é controlado por `n_layer`; o número de cabeças, por `n_head`.

### Saída e geração:

Uma LayerNorm final prepara as representações. A cabeça de linguagem é uma camada linear que produz pontuações para cada token do vocabulário em cada posição. A função de perda treina a previsão do próximo token.

Na geração, o modelo aplica softmax às pontuações da última posição, ajustadas pela temperatura, e amostra o próximo token. O processo se repete de forma autorregressiva, mantendo o contexto dentro de `block_size`.

## Tokenizador BPE:

O tokenizador começa com caracteres dentro de unidades separadas por uma expressão regular e aprende fusões de pares adjacentes ponderadas pela frequência das unidades no corpus. A ordem das fusões é armazenada e reutilizada durante a codificação.

O vocabulário e as fusões são serializados no checkpoint, garantindo que geração e avaliação usem o mesmo mapeamento de IDs do treinamento.

`--vocab-size` funciona como meta. O conjunto inicial de caracteres pode, por si só, exceder essa meta; portanto, o tamanho final não é necessariamente limitado ao valor informado. Caracteres ausentes no vocabulário são representados por `<unk>`.

## Dados e validação:

Treino e validação devem ser arquivos distintos. O tokenizador é aprendido somente no treino, evitando aprender fusões a partir da validação. A validação acompanha a perda em dados que não atualizam os pesos. Se o texto de validação contiver caracteres ausentes no vocabulário de treino, eles serão convertidos para `<unk>`; muitos desconhecidos tornam a avaliação menos representativa.

## Métricas e interpretação:

A perda é a entropia cruzada média da previsão do próximo token. A perplexidade é a exponencial da perda média. Nenhuma dessas métricas, isoladamente, comprova compreensão, factualidade, segurança, utilidade ou qualidade de acolhimento.

## Limitações e segurança:

O corpus, a capacidade reduzida e o treinamento limitado restringem a qualidade da geração. O modelo pode produzir texto sem sentido, repetir padrões, inventar informações ou gerar conteúdo inadequado. Prever sequências prováveis não equivale a compreender seu significado.

Não use este protótipo para aconselhamento, triagem de risco, atendimento de emergência ou decisões sobre pessoas. Não o conecte a usuárias em situações reais sem avaliação humana e revisão especializada de segurança, privacidade, acessibilidade e proteção de dados.

Não inclua no corpus dados pessoais, relatos privados de violência, conversas, documentos, endereços ou identificadores de usuárias. Use textos próprios, sintéticos ou com origem e licença verificadas, preservando as atribuições e condições aplicáveis.
