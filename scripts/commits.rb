require "open3"
require "time"
require "fileutils"

def git(*args)
  output, status = Open3.capture2("git", *args)

  abort "Erro ao executar git #{args.join(' ')}" unless status.success?

  output
end

pasta = "commits"
FileUtils.mkdir_p(pasta)

historico = git(
  "log",
  "--all",
  "--reverse",
  "--format=%H\t%an\t%aI\t%s"
).lines.map(&:chomp)

total = historico.size
tamanho_lote = 150

arquivos = Dir.glob("#{pasta}/commits_*.md").filter_map do |caminho|
  match = File.basename(caminho).match(/commits_(\d+)-(\d+)\.md/)
  next unless match

  {
    caminho: caminho,
    inicio: match[1].to_i,
    fim: match[2].to_i
  }
end

ultimo_fim = arquivos.map { |arquivo| arquivo[:fim] }.max || 0

if ultimo_fim >= total
  puts "Nenhum commit novo."
  puts "Último commit registrado: #{ultimo_fim}"
  puts "Total atual: #{total}"
  exit
end

inicio = ultimo_fim + 1

while inicio <= total
  fim = [inicio + tamanho_lote - 1, total].min

  nome = format(
    "%s/commits_%05d-%05d.md",
    pasta,
    inicio,
    fim
  )

  lote = historico[(inicio - 1)...fim]

  File.open(nome, "w:UTF-8") do |arquivo|
    arquivo.puts "# Relatório de Commits — Mulher Amparada"
    arquivo.puts
    arquivo.puts "- **Intervalo:** #{inicio} a #{fim}"
    arquivo.puts "- **Quantidade neste arquivo:** #{lote.size}"
    arquivo.puts "- **Total de commits encontrados:** #{total}"
    arquivo.puts "- **Gerado em:** #{Time.now.iso8601}"
    arquivo.puts
    arquivo.puts "| Hash | Autor | Data | Mensagem |"
    arquivo.puts "|---|---|---|---|"

    lote.each do |linha|
      hash, autor, data, mensagem = linha.split("\t", 4)

      valores = [hash, autor, data, mensagem].map do |valor|
        valor.to_s
          .gsub("\\", "\\\\\\")
          .gsub("|", "\\|")
          .gsub("\n", " ")
      end

      arquivo.puts "| #{valores.join(' | ')} |"
    end
  end

  puts "Gerado: #{nome} (#{lote.size} commits)"

  inicio = fim + 1
end

puts
puts "Total: #{total} commits."
puts "Arquivos: #{Dir.glob("#{pasta}/commits_*.md").size}"