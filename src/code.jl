
# =========================================================
# MULHER AMPARADA — LIGUE 180 + JULIA
# Fonte: Ministério dos Direitos Humanos e da Cidadania
# Período dos dados: 2019
# =========================================================

using Downloads
using CSV
using DataFrames
using Plots
using Dates

const RAIZ = normpath(joinpath(@__DIR__, ".."))
const BRUTOS = joinpath(RAIZ, "dados", "brutos")
const PROCESSADOS = joinpath(RAIZ, "dados", "processados")
const RELATORIOS = joinpath(RAIZ, "relatorios")
const GRAFICOS = joinpath(RELATORIOS, "graficos")

const FONTE =
    "https://www.gov.br/mdh/pt-br/acesso-a-informacao/dados-abertos/ligue180/2019/balanco-2019-ligue180-tipo-de-violacao.csv/@@download/file"

for pasta in (BRUTOS, PROCESSADOS, RELATORIOS, GRAFICOS)
    mkpath(pasta)
end

function executar()
    println("========================================")
    println(" MULHER AMPARADA — DADOS OFICIAIS")
    println("========================================")
    println("Início: ", Dates.now())

    arquivo = joinpath(BRUTOS, "ligue180-2019.csv")

    # -----------------------------------------------------
    # 1. BAIXAR O ARQUIVO ORIGINAL
    # -----------------------------------------------------

    println("\n[1/4] Baixando CSV oficial...")

    Downloads.download(FONTE, arquivo)

    tamanho = filesize(arquivo)

    tamanho > 100 ||
        error("O arquivo baixado está vazio ou incompleto.")

    println("Arquivo recebido: ", tamanho, " bytes")

    # -----------------------------------------------------
    # 2. LOCALIZAR E VALIDAR A TABELA
    # -----------------------------------------------------

    println("\n[2/4] Lendo e validando os dados...")

    linhas = readlines(arquivo)

    cabecalho = findfirst(
        linha -> occursin(",UF,Jan,Fev,Mar,Abr", linha),
        linhas
    )

    isnothing(cabecalho) &&
        error(
            "Cabeçalho esperado não encontrado. " *
            "A fonte pode ter mudado ou o download não contém CSV."
        )

    tabela = CSV.read(
        IOBuffer(join(linhas[cabecalho:end], "\n")),
        DataFrame;
        on_error = :collect
    )

    println("Colunas encontradas:")
    println(names(tabela))

    coluna_categoria = findfirst(
        nome -> strip(nome) == "UF",
        names(tabela)
    )

    isnothing(coluna_categoria) &&
        error("A coluna de categorias 'UF' não foi encontrada.")

    coluna_total = findfirst(
        nome -> strip(nome) == "Total 2019",
        names(tabela)
    )

    isnothing(coluna_total) &&
        error("A coluna de totais 'Total 2019' não foi encontrada.")

    # -----------------------------------------------------
    # 3. PROCESSAR E EXPORTAR OS DADOS
    # -----------------------------------------------------

    categorias = String[]
    totais = Int[]

    for linha in eachrow(tabela)
        valor_categoria = linha[coluna_categoria]
        valor_total = linha[coluna_total]

        if ismissing(valor_categoria) || ismissing(valor_total)
            continue
        end

        categoria = strip(string(valor_categoria))
        valor = strip(string(valor_total))

        if isempty(categoria) || isempty(valor)
            continue
        end

        if uppercase(valor) in ("N/A", "NA", "NULL", "-")
            continue
        end

        # Interpretar números com separador de milhares.
        valor_limpo = replace(valor, "," => "")

        total = tryparse(Int, valor_limpo)

        isnothing(total) && continue

        # Ignorar linhas que sejam rótulos ou totais gerais
        # repetidos, caso estejam presentes na fonte.
        push!(categorias, categoria)
        push!(totais, total)
    end

    isempty(categorias) &&
        error(
            "Nenhuma linha válida foi encontrada. " *
            "Confira o formato e os cabeçalhos do CSV."
        )

    resultado = DataFrame(
        categoria = categorias,
        total_2019 = totais
    )

    arquivo_processado = joinpath(
        PROCESSADOS,
        "ligue180-2019-processado.csv"
    )

    CSV.write(arquivo_processado, resultado)

    println("CSV processado: ", arquivo_processado)
    println("Linhas processadas: ", nrow(resultado))

    # -----------------------------------------------------
    # 4. GERAR O GRÁFICO
    # -----------------------------------------------------

    println("\n[4/4] Gerando gráfico...")

    ordem = sortperm(resultado.total_2019; rev = true)
    quantidade = min(15, length(ordem))

    selecionados = resultado[ordem[1:quantidade], :]

    grafico = bar(
        selecionados.categoria,
        selecionados.total_2019;
        title = "Ligue 180 — categorias publicadas em 2019",
        xlabel = "Categoria",
        ylabel = "Total informado na fonte",
        legend = false,
        size = (1200, 700),
        xrotation = 45,
        bottom_margin = 12 * Plots.mm
    )

    arquivo_grafico = joinpath(
        GRAFICOS,
        "ligue180-2019.png"
    )

    savefig(grafico, arquivo_grafico)

    println("Gráfico salvo: ", arquivo_grafico)

    # -----------------------------------------------------
    # RESUMO FINAL
    # -----------------------------------------------------

    println("\n========================================")
    println(" PROCESSAMENTO CONCLUÍDO")
    println("========================================")
    println("Fonte: Ligue 180")
    println("Período: 2019")
    println("Categorias processadas: ", nrow(resultado))
    println("CSV: dados/processados/ligue180-2019-processado.csv")
    println("Gráfico: relatorios/graficos/ligue180-2019.png")
    println("Fim: ", Dates.now())
end

# =========================================================
# EXECUÇÃO E TRATAMENTO DE ERROS
# =========================================================

try
    executar()
catch erro
    println(stderr, "\nERRO NO PROCESSAMENTO DOS DADOS:")

    showerror(stderr, erro, catch_backtrace())
    println(stderr)

    exit(1)
end
