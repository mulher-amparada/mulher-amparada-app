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

historico = git("log", "--all", "--reverse", "--format=%H\t%an\t%aI\t%s").lines.map(&:chomp)

total = historico.size
tamanho_lote = 150

arquivos = Dir.glob("#{pasta}/commits_*.md").filter_map do |caminho|
  match = File.basename(caminho).match(/commits_(\d+)-(\d+)\.md/)
  next unless match

  { caminho: caminho, inicio: match[1].to_i, fim: match[2].to_i }
end.sort_by { |arquivo| arquivo[:inicio] }

if arquivos.empty?
  inicio = 1
else
  ultimo = arquivos.max_by { |arquivo| arquivo[:fim] }

  if ultimo[:fim] >= total
    puts "Todos os commits já estão registrados."
    exit
  end

  inicio = ultimo[:inicio]
  FileUtils.rm_f(ultimo[:caminho])
end

while inicio <= total
  fim = [inicio + tamanho_lote - 1, total].min
  nome = format("%s/commits_%05d-%05d.md", pasta, inicio, fim)
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
        valor.to_s.gsub("\\", "\\\\\\").gsub("|", "\\|").gsub("\n", " ")
      end

      arquivo.puts "| #{valores.join(' | ')} |"
    end
  end

  puts "Gerado: #{nome} (#{lote.size} commits)"
  inicio = fim + 1
end

puts "Total: #{total} commits em #{Dir.glob("#{pasta}/commits_*.md").size} arquivos."
