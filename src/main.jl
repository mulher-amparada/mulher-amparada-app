
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

    # 1. Baixar o arquivo original
    println("\n[1/4] Baixando CSV oficial...")
    Downloads.download(FONTE, arquivo)

    tamanho = filesize(arquivo)
    tamanho > 100 || error("O arquivo baixado está vazio ou incompleto.")

    println("Arquivo recebido: ", tamanho, " bytes")

    # 2. Localizar a tabela após o cabeçalho explicativo
    println("\n[2/4] Lendo e validando os dados...")

    linhas = readlines(arquivo)
    cabecalho = findfirst(
        linha -> occursin(",UF,Jan,Fev,Mar,Abr", linha),
        linhas
    )

    isnothing(cabecalho) &&
        error("Não foi possível localizar o cabeçalho esperado.")

    tabela = CSV.read(
        IOBuffer(join(linhas[cabecalho:end], "\n")),
        DataFrame;
        silencewarnings = true
    )

    coluna_categoria = findfirst(==("UF"), names(tabela))
    isnothing(coluna_categoria) &&
        error("A coluna de categorias não foi encontrada.")

    coluna_total = findfirst(==("Total 2019"), names(tabela))
    isnothing(coluna_total) &&
        error("A coluna de totais não foi encontrada.")

    categorias = String[]
    totais = Int[]

    for linha in eachrow(tabela)
        categoria = strip(string(linha[coluna_categoria]))
        valor = strip(string(linha[coluna_total]))

        if isempty(categoria) || ismissing(linha[coluna_categoria])
            continue
        end

        if isempty(valor) || valor == "N/A" ||
           ismissing(linha[coluna_total])
            continue
        end

        # Os valores publicados usam vírgula como separador
        # de milhares, por exemplo: "3,256".
        valor_limpo = replace(valor, "," => "")

        total = tryparse(Int, valor_limpo)
        isnothing(total) && continue

        push!(categorias, categoria)
        push!(totais, total)
    end

    isempty(categorias) &&
        error("Nenhuma categoria válida foi encontrada.")

    resultado = DataFrame(
        categoria = categorias,
        total_2019 = totais
    )

    # 3. Exportar dados processados
    println("\n[3/4] Salvando CSV processado...")

    CSV.write(
        joinpath(PROCESSADOS, "ligue180-2019-processado.csv"),
        resultado
    )

    # 4. Criar gráfico
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
        bottom_margin = 12Plots.mm
    )

    savefig(
        grafico,
        joinpath(GRAFICOS, "ligue180-2019.png")
    )

    println("\nConcluído!")
    println("Categorias processadas: ", nrow(resultado))
    println("CSV: dados/processados/ligue180-2019-processado.csv")
    println("Gráfico: relatorios/graficos/ligue180-2019.png")
    println("Fim: ", Dates.now())
end

try
    executar()
catch erro
    showerror(stderr, erro, catch_backtrace())
    println(stderr)
    exit(1)
end
