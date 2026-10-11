# =========================================================
# MULHER AMPARADA — LIGUE 180 + JULIA
# Busca anual automática + gráfico em notação científica
# Fonte histórica: Ministério dos Direitos Humanos
# =========================================================

using Downloads
using CSV
using DataFrames
using Plots
using Dates
using Printf

const RAIZ = normpath(joinpath(@__DIR__, ".."))
const BRUTOS = joinpath(RAIZ, "dados", "brutos")
const PROCESSADOS = joinpath(RAIZ, "dados", "processados")
const RELATORIOS = joinpath(RAIZ, "relatorios")
const GRAFICOS = joinpath(RELATORIOS, "graficos")

const ANO_INICIAL = 2019
const ANO_ATUAL = year(today())

const ARQUIVO_CSV = joinpath(
    PROCESSADOS,
    "ligue180-mais-recente-processado.csv"
)

const ARQUIVO_GRAFICO = joinpath(
    GRAFICOS,
    "ligue180-mais-recente.png"
)

for pasta in (BRUTOS, PROCESSADOS, RELATORIOS, GRAFICOS)
    mkpath(pasta)
end

# ---------------------------------------------------------
# Localizar e validar o CSV anual mais recente disponível
# ---------------------------------------------------------

function buscar_dados()
    println("\n[1/4] Procurando dados anuais disponíveis...")

    for ano in ANO_ATUAL:-1:ANO_INICIAL
        url = "https://www.gov.br/mdh/pt-br/acesso-a-informacao/" *
              "dados-abertos/ligue180/$ano/" *
              "balanco-$ano-ligue180-tipo-de-violacao.csv/@@download/file"

        temporario = joinpath(
            BRUTOS,
            "tentativa-ligue180-$ano.csv"
        )

        println("Verificando ano: ", ano)

        try
            Downloads.download(url, temporario)

            filesize(temporario) > 100 ||
                error("Arquivo vazio ou incompleto.")

            linhas = readlines(temporario)

            cabecalho = findfirst(
                linha -> occursin(
                    r"UF.*Jan.*Fev.*Mar.*Abr"i,
                    linha
                ),
                linhas
            )

            isnothing(cabecalho) &&
                error("O CSV não tem o cabeçalho esperado.")

            tabela = CSV.read(
                IOBuffer(join(linhas[cabecalho:end], "\n")),
                DataFrame;
                silencewarnings = true
            )

            nomes = strip.(string.(names(tabela)))

            indice_categoria = findfirst(
                nome -> uppercase(nome) == "UF",
                nomes
            )

            isnothing(indice_categoria) &&
                error("Coluna de categorias não encontrada.")

            indice_total = findfirst(
                nome -> occursin(
                    Regex("^Total\\s+$ano\$"),
                    nome
                ),
                nomes
            )

            isnothing(indice_total) &&
                error("Coluna Total $ano não encontrada.")

            println("CSV válido encontrado para ", ano)

            return (
                tabela = tabela,
                ano = ano,
                indice_categoria = indice_categoria,
                indice_total = indice_total,
                origem = url
            )

        catch erro
            println(
                "Ano $ano indisponível neste formato: ",
                sprint(showerror, erro)
            )
        end
    end

    error(
        "Nenhum CSV anual compatível foi encontrado. " *
        "Consulte a fonte oficial para identificar o arquivo " *
        "mais recente publicado em outro formato."
    )
end

# ---------------------------------------------------------
# Converter valores numéricos
# ---------------------------------------------------------

function converter_total(valor)
    ismissing(valor) && return nothing

    texto = strip(string(valor))

    isempty(texto) && return nothing

    uppercase(texto) in ("N/A", "NA", "NULL", "-") &&
        return nothing

    texto = replace(texto, "," => "")
    numero = tryparse(Float64, texto)

    isnothing(numero) && return nothing
    isfinite(numero) || return nothing

    return round(Int, numero)
end

# ---------------------------------------------------------
# Processar e exportar dados
# ---------------------------------------------------------

function processar(dados)
    println("\n[2/4] Processando dados de ", dados.ano, "...")

    tabela = dados.tabela
    nomes = strip.(string.(names(tabela)))

    categorias = String[]
    totais = Int[]

    for linha in eachrow(tabela)
        categoria_original = linha[dados.indice_categoria]
        valor_original = linha[dados.indice_total]

        ismissing(categoria_original) && continue

        categoria = strip(string(categoria_original))
        isempty(categoria) && continue

        total = converter_total(valor_original)
        isnothing(total) && continue

        total >= 0 || continue

        push!(categorias, categoria)
        push!(totais, total)
    end

    isempty(categorias) &&
        error("Nenhuma categoria válida foi encontrada.")

    resultado = DataFrame(
        categoria = categorias,
        total = totais
    )

    # Remover categorias duplicadas somando seus valores.
    resultado = combine(
        groupby(resultado, :categoria),
        :total => sum => :total
    )

    sort!(resultado, :total; rev = true)

    CSV.write(ARQUIVO_CSV, resultado)

    println("Ano dos dados: ", dados.ano)
    println("Categorias: ", nrow(resultado))
    println("CSV salvo: ", ARQUIVO_CSV)

    return resultado
end

# ---------------------------------------------------------
# Gráfico aprimorado com notação científica
# ---------------------------------------------------------

function gerar_grafico(resultado, ano)
    println("\n[3/4] Criando gráfico...")

    quantidade = min(15, nrow(resultado))
    selecionados = resultado[1:quantidade, :]

    # Inverter a ordem para mostrar a maior barra no topo.
    categorias = reverse(selecionados.categoria)
    valores = reverse(selecionados.total)

    grafico = bar(
        valores,
        categorias;
        orientation = :h,
        title = "Ligue 180 | Dados oficiais de $ano",
        subtitle = "Principais categorias por total registrado",
        xlabel = "Total registrado (notação científica)",
        ylabel = "",
        label = false,
        color = :steelblue,
        linecolor = :white,
        linewidth = 0.8,
        xformatter = :scientific,
        ytickfontsize = 9,
        xtickfontsize = 9,
        titlefontsize = 15,
        guidefontsize = 11,
        legend = false,
        grid = true,
        gridalpha = 0.18,
        background_color = :white,
        foreground_color = :black,
        size = (1500, 900),
        left_margin = 12 * Plots.mm,
        right_margin = 10 * Plots.mm,
        bottom_margin = 10 * Plots.mm,
        top_margin = 12 * Plots.mm,
        dpi = 200
    )

    savefig(grafico, ARQUIVO_GRAFICO)

    filesize(ARQUIVO_GRAFICO) > 0 ||
        error("O gráfico não foi salvo corretamente.")

    println("Gráfico salvo: ", ARQUIVO_GRAFICO)
end

# ---------------------------------------------------------
# Execução principal
# ---------------------------------------------------------

function executar()
    println("========================================")
    println(" MULHER AMPARADA — LIGUE 180")
    println("========================================")
    println("Início: ", Dates.now())

    dados = buscar_dados()
    resultado = processar(dados)
    gerar_grafico(resultado, dados.ano)

    println("\n[4/4] PROCESSAMENTO CONCLUÍDO")
    println("Ano encontrado: ", dados.ano)
    println("Fonte consultada: ", dados.origem)
    println("CSV: ", ARQUIVO_CSV)
    println("Gráfico: ", ARQUIVO_GRAFICO)
    println("Fim: ", Dates.now())
end

try
    executar()
catch erro
    println(stderr, "\nERRO NO PROCESSAMENTO:")
    showerror(stderr, erro, catch_backtrace())
    println(stderr)
    exit(1)
end