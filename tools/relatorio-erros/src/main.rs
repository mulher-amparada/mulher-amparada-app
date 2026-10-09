
use chrono::Utc;
use reqwest::blocking::Client;
use std::env;
use std::error::Error;
use std::fs;
use std::io::{Cursor, Read};
use zip::ZipArchive;

fn traduzir(texto: &str) -> String {
    let traducoes = [
        ("Process completed with exit code 1", "O processo terminou com código de saída 1"),
        ("Process completed with exit code 127", "Comando não encontrado"),
        ("Process completed with exit code 137", "Processo encerrado pelo sistema"),
        ("Permission denied", "Permissão negada"),
        ("Operation not permitted", "Operação não permitida"),
        ("No such file or directory", "Arquivo ou diretório não encontrado"),
        ("command not found", "Comando não encontrado"),
        ("Connection timed out", "Tempo limite da conexão excedido"),
        ("Operation timed out", "Tempo limite da operação excedido"),
        ("Request timed out", "A solicitação excedeu o tempo limite"),
        ("Network is unreachable", "Rede inacessível"),
        ("Connection refused", "Conexão recusada"),
        ("Could not resolve host", "Não foi possível localizar o servidor"),
        ("Could not resolve hostname", "Não foi possível localizar o servidor"),
        ("Resource not accessible by integration", "Recurso inacessível para o token do GitHub Actions"),
        ("Bad credentials", "Credenciais inválidas"),
        ("Not Found", "Recurso não encontrado"),
        ("Rate limit exceeded", "Limite de solicitações excedido"),
        ("API rate limit exceeded", "Limite de solicitações da API excedido"),
        ("manifest path", "Caminho do arquivo de manifesto"),
        ("could not compile", "Não foi possível compilar"),
        ("failed to compile", "Falha na compilação"),
        ("error[E", "Erro do compilador Rust"),
        ("warning:", "Aviso:"),
        ("error:", "Erro:"),
        ("fatal:", "Erro fatal:"),
        ("failed:", "Falha:"),
        ("Failure:", "Falha:"),
        ("Error:", "Erro:"),
        ("Timed out", "Tempo limite excedido"),
        ("timed out", "tempo limite excedido"),
        ("Cancelled", "Cancelado"),
        ("cancelled", "cancelado"),
        ("Canceled", "Cancelado"),
        ("canceled", "cancelado"),
        ("Skipping", "Ignorando"),
        ("Uploading artifact", "Enviando artefato"),
        ("Downloading artifact", "Baixando artefato"),
        ("Cache not found", "Cache não encontrado"),
        ("Process completed successfully", "Processo concluído com sucesso"),
    ];

    let mut resultado = texto.to_string();

    for (original, traducao) in traducoes {
        resultado = resultado.replace(original, traducao);
    }

    resultado
}

fn criar_cliente() -> Result<Client, Box<dyn Error>> {
    Ok(Client::builder()
        .user_agent("Mulher-Amparada-Workflow-Error-Report")
        .build()?)
}

fn consultar_execucao(
    cliente: &Client,
    token: &str,
    repositorio: &str,
    run_id: &str,
) -> Result<(String, String), Box<dyn Error>> {
    let url = format!(
        "https://api.github.com/repos/{}/actions/runs/{}",
        repositorio, run_id
    );

    let resposta = cliente
        .get(&url)
        .bearer_auth(token)
        .header("Accept", "application/vnd.github+json")
        .header("X-GitHub-Api-Version", "2022-11-28")
        .send()?;

    let status_http = resposta.status();
    let corpo = resposta.text()?;

    if !status_http.is_success() {
        return Err(format!(
            "Não foi possível consultar a execução.\nHTTP {}.\nResposta da API: {}",
            status_http, corpo
        )
        .into());
    }

    let dados: serde_json::Value = serde_json::from_str(&corpo)?;

    let status = dados["status"]
        .as_str()
        .unwrap_or("desconhecido")
        .to_string();

    let conclusao = dados["conclusion"]
        .as_str()
        .unwrap_or("ainda não concluída")
        .to_string();

    Ok((status, conclusao))
}

fn baixar_logs(
    cliente: &Client,
    token: &str,
    repositorio: &str,
    run_id: &str,
) -> Result<Vec<u8>, Box<dyn Error>> {
    let url = format!(
        "https://api.github.com/repos/{}/actions/runs/{}/logs",
        repositorio, run_id
    );

    let resposta = cliente
        .get(&url)
        .bearer_auth(token)
        .header("Accept", "application/vnd.github+json")
        .header("X-GitHub-Api-Version", "2022-11-28")
        .send()?;

    let status = resposta.status();
    let corpo = resposta.bytes()?;

    if !status.is_success() {
        let mensagem = String::from_utf8_lossy(&corpo);

        return Err(format!(
            "Não foi possível baixar os logs.\nHTTP {}.\nURL: {}\nResposta da API: {}",
            status, url, mensagem
        )
        .into());
    }

    Ok(corpo.to_vec())
}

fn analisar_logs(bytes: Vec<u8>) -> Result<(usize, String), Box<dyn Error>> {
    let cursor = Cursor::new(bytes);
    let mut arquivo_zip = ZipArchive::new(cursor)?;

    let indicadores = [
        "error",
        "erro",
        "failed",
        "failure",
        "fatal",
        "exception",
        "panic",
        "timed out",
        "permission denied",
        "not found",
        "exit code",
    ];

    let mut quantidade = 0usize;
    let mut relatorio = String::new();
    let mut arquivos_analisados = 0usize;

    for indice in 0..arquivo_zip.len() {
        let mut arquivo = arquivo_zip.by_index(indice)?;

        if arquivo.is_dir() {
            continue;
        }

        let nome = arquivo.name().to_string();

        if !nome.ends_with(".txt") && !nome.ends_with(".log") {
            continue;
        }

        let mut bytes_arquivo = Vec::new();
        arquivo.read_to_end(&mut bytes_arquivo)?;

        let conteudo = String::from_utf8_lossy(&bytes_arquivo);
        arquivos_analisados += 1;

        let mut linhas_encontradas = Vec::new();

        for (indice_linha, linha) in conteudo.lines().enumerate() {
            let linha_minuscula = linha.to_lowercase();

            if indicadores
                .iter()
                .any(|indicador| linha_minuscula.contains(indicador))
            {
                linhas_encontradas.push(format!(
                    "- Linha {}: {}",
                    indice_linha + 1,
                    traduzir(linha.trim())
                ));

                quantidade += 1;

                if quantidade >= 500 {
                    break;
                }
            }
        }

        if !linhas_encontradas.is_empty() {
            relatorio.push_str(&format!("\n### Arquivo: `{}`\n\n", nome));

            for linha in linhas_encontradas {
                relatorio.push_str(&linha);
                relatorio.push('\n');
            }
        }

        if quantidade >= 500 {
            break;
        }
    }

    if arquivos_analisados == 0 {
        relatorio.push_str(
            "\nNenhum arquivo de log `.txt` ou `.log` foi encontrado no ZIP.\n",
        );
    } else if quantidade == 0 {
        relatorio.push_str(
            "\nNenhuma linha contendo os indicadores de erro configurados foi identificada.\n",
        );
    }

    Ok((quantidade, relatorio))
}

fn executar() -> Result<String, Box<dyn Error>> {
    let token = env::var("GITHUB_TOKEN")?;
    let repositorio = env::var("GITHUB_REPOSITORY")?;
    let run_id = env::var("GITHUB_RUN_ID")?;

    let data = Utc::now().format("%d/%m/%Y %H:%M:%S UTC");
    let cliente = criar_cliente()?;

    let (status, conclusao) =
        consultar_execucao(&cliente, &token, &repositorio, &run_id)?;

    let mut relatorio = format!(
        "# Relatório de erros — Mulher Amparada\n\n\
         - **Repositório:** `{}`\n\
         - **Execução:** `{}`\n\
         - **Status:** `{}`\n\
         - **Conclusão:** `{}`\n\
         - **Data da análise:** {}\n\n",
        repositorio, run_id, status, conclusao, data
    );

    if status != "completed" {
        relatorio.push_str(
            "## Logs ainda indisponíveis\n\n\
             A execução ainda não foi concluída. Os logs completos podem não estar disponíveis.\n",
        );

        return Ok(relatorio);
    }

    match baixar_logs(&cliente, &token, &repositorio, &run_id) {
        Ok(bytes) => match analisar_logs(bytes) {
            Ok((quantidade, detalhes)) => {
                relatorio.push_str(&format!(
                    "## Resultado da análise\n\n\
                     - **Ocorrências identificadas:** {}\n\n{}",
                    quantidade, detalhes
                ));
            }
            Err(erro) => {
                relatorio.push_str(&format!(
                    "## Falha ao analisar os logs\n\n```text\n{}\n```\n",
                    erro
                ));
            }
        },
        Err(erro) => {
            relatorio.push_str(&format!(
                "## Falha ao obter os logs\n\n```text\n{}\n```\n",
                erro
            ));
        }
    }

    Ok(relatorio)
}

fn salvar_relatorio(conteudo: &str) -> Result<(), Box<dyn Error>> {
    fs::write("relatorio-erros.md", conteudo)?;

    if let Ok(caminho) = env::var("GITHUB_STEP_SUMMARY") {
        fs::write(caminho, conteudo)?;
    }

    println!("{}", conteudo);
    Ok(())
}

fn main() {
    let resultado = executar();

    let relatorio = match resultado {
        Ok(conteudo) => conteudo,
        Err(erro) => format!(
            "# Relatório de erros — Mulher Amparada\n\n\
             Não foi possível concluir a coleta dos logs.\n\n\
             ```text\n{}\n```\n",
            erro
        ),
    };

    if let Err(erro) = salvar_relatorio(&relatorio) {
        eprintln!("Falha ao salvar o relatório: {}", erro);
        std::process::exit(1);
    }
}
