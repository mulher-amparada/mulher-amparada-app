
require "open3"
require "time"
require "fileutils"

def git(*args)
  output, status = Open3.capture2("git", *args)
  abort "Erro ao executar git #{args.join(" ")}" unless status.success?
  output
end

pasta = "commits"
FileUtils.mkdir_p(pasta)

historico = git(
  "log", "--all",
  "--reverse",
  "--format=%H%x09%an%x09%aI%x09%s"
).lines.map(&:chomp)

total = historico.size
tamanho_lote = 150

Dir.glob("#{pasta}/*.txt").each { |arquivo| File.delete(arquivo) }

historico.each_slice(tamanho_lote).with_index do |lote, indice|
  inicio = indice * tamanho_lote + 1
  fim = inicio + lote.size - 1

  nome = format("%s/commits_%05d-%05d.txt", pasta, inicio, fim)

  File.open(nome, "w:UTF-8") do |arquivo|
    arquivo.puts "RELATÓRIO DE COMMITS — MULHER AMPARADA"
    arquivo.puts "Intervalo: #{inicio} a #{fim}"
    arquivo.puts "Quantidade neste arquivo: #{lote.size}"
    arquivo.puts "Total de commits encontrados: #{total}"
    arquivo.puts "Gerado em: #{Time.now.iso8601}"
    arquivo.puts
    arquivo.puts "HASH | AUTOR | DATA | MENSAGEM"
    arquivo.puts
    lote.each { |linha| arquivo.puts linha }
  end

  puts "Gerado: #{nome} (#{lote.size} commits)"
end

puts "Total: #{total} commits em #{(total.to_f / tamanho_lote).ceil} arquivos."
