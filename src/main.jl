
# =========================================================
# MULHER AMPARADA — ANÁLISE DE DADOS EM JULIA
# =========================================================

using Pkg
using CSV
using DataFrames
using Plots
using Dates

# Diretórios do projeto
const DIRETORIO = normpath(joinpath(@__DIR__, ".."))
const DADOS_DIR = joinpath(DIRETORIO, "dados", "processados")
const RELATORIOS_DIR = joinpath(DIRETORIO, "relatorios")
const GRAFICOS_DIR = joinpath(RELATORIOS_DIR, "graficos")

# Criar diretórios necessários
mkpath(DADOS_DIR)
mkpath(RELATORIOS_DIR)
mkpath(GRAFICOS_DIR)

println("========================================")
println(" MULHER AMPARADA — JULIA")
println("========================================")
println("Início: ", Dates.now())

# Exemplo demonstrativo: NÃO são dados reais.
# Substituir por dados oficiais validados antes
# de publicar resultados.

dados = DataFrame(
    mes = ["Jan", "Fev", "Mar", "Abr", "Mai", "Jun"],
    registros = [0, 0, 0, 0, 0, 0]
)

# Exportar tabela de exemplo
arquivo_csv = joinpath(RELATORIOS_DIR, "estatisticas.csv")
CSV.write(arquivo_csv, dados)

# Gerar gráfico demonstrativo
grafico = plot(
    dados.mes,
    dados.registros;
    title = "Registros por mês — dados demonstrativos",
    xlabel = "Mês",
    ylabel = "Quantidade",
    marker = :circle,
    legend = false
)

arquivo_grafico = joinpath(GRAFICOS_DIR, "registros_mensais.png")
savefig(grafico, arquivo_grafico)

println("CSV criado: ", arquivo_csv)
println("Gráfico criado: ", arquivo_grafico)
println("Concluído: ", Dates.now())
