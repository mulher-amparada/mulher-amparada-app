
require "open3"
require "time"

def git(*args)
  output, status = Open3.capture2("git", *args)
  abort "Erro ao executar git #{args.join(" ")}" unless status.success?
  output
end

total = git("rev-list", "--all", "--count").strip
autores = git("log", "--all", "--format=%an").lines
  .map(&:strip).tally.sort_by { |_, n| -n }

historico = git(
  "log", "--all",
  "--date=iso-strict",
  "--format=%h | %an | %ad | %s"
)

File.open("dados_commits.txt", "w") do |f|
  f.puts "RELATÓRIO DE COMMITS — MULHER AMPARADA"
  f.puts "Gerado em: #{Time.now.iso8601}"
  f.puts "Total de commits: #{total}"
  f.puts
  f.puts "COMMITS POR AUTOR"
  autores.each { |nome, quantidade| f.puts "#{nome}: #{quantidade}" }
  f.puts
  f.puts "HISTÓRICO COMPLETO"
  f.puts historico
end

puts "dados_commits.txt gerado."
