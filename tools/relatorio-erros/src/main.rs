
use reqwest::blocking::Client;
use std::env;
use std::fs;
use std::io::{Cursor, Read};
use std::path::Path;
use zip::ZipArchive;

fn traduzir(texto: &str) -> String {
    let substituicoes = [
        ("Process completed with exit code 1", "O processo terminou com código de erro 1."),
        ("Process completed with exit code 101", "O processo Rust/Cargo terminou com código de erro 101."),
        ("Resource not accessible by integration", "O recurso não está acessível com as permissões atuais do GitHub Actions."),
        ("Not Found", "O recurso solicitado não foi encontrado."),
        ("Permission denied", "Permissão negada."),
        ("timed out", "A operação excedeu o tempo limite."),
        ("Connection refused", "A conexão foi recusada."),
        ("Could not resolve host", "Não foi possível resolver o endereço do servidor."),
        ("No files were found with the provided path", "Nenhum arquivo foi encontrado no caminho informado."),
        ("failed to parse manifest", "O Cargo não conseguiu interpretar o arquivo Cargo.toml."),
        ("no targets specified in the manifest", "O Cargo não encontrou src/main.rs, src/lib.rs ou um destino configurado no Cargo.toml."),
        ("error:", "Erro:"),
        ("warning:", "Aviso:"),
    ];

    let mut resultado = texto.to_string();

    for (original, traducao) in substituicoes {
        resultado = resultado.replace(original, traducao);
    }

    resultado
}

fn executar() -> Result<String, Box<dyn std::error::Error>> {
    let token = env::var("GITHUB_TOKEN")?;
    let repositorio = env::var("GITHUB_REPOSITORY")?;
    let run_id = env::var("GITHUB_RUN_ID")?;

    let cliente = Client::builder()
        .user_agent("Mulher-Amparada-Workflow-Error-Report")
        .build()?;

    let url = format!(
        "https://api.github.com/repos/{}/actions/runs/{}/logs",
        repositorio, run_id
    );

    let resposta = cliente
        .get(&url)
        .bearer_auth(token)
        .header("Accept", "application/vnd.github+json")
        .header("X-GitHub-Api-Version", "2022-11-28")
        .send()?
        .error_for_status()?
        .bytes()?;

    let cursor = Cursor::new(resposta);
    let mut arquivo_zip = ZipArchive::new(cursor)?;
    let mut erros = Vec::new();

    for indice in 0..arquivo_zip.len() {
        let mut arquivo = arquivo_zip.by_index(indice)?;

        if !arquivo.name().ends_with(".txt") {
            continue;
        }

        let nome = arquivo.name().to_string();
        let mut conteudo = String::new();

        arquivo.read_to_string(&mut conteudo)?;

        let linhas: Vec<&str> = conteudo.lines().collect();

        for (indice_linha, linha) in linhas.iter().enumerate() {
            let minúsculo = linha.to_lowercase();

            let relevante = minúsculo.contains("error")
                || minúsculo.contains("failed")
                || minúsculo.contains("failure")
                || minúsculo.contains("fatal")
                || minúsculo.contains("exception")
                || minúsculo.contains("exit code")
                || minúsculo.contains("timed out")
                || minúsculo.contains("not found")
                || minúsculo.contains("denied")
                || minúsculo.contains("panic");

            if relevante {
                let trecho = linhas
                    .iter()
                    .skip(indice_linha.saturating_sub(2))
                    .take(5)
                    .copied()
                    .collect::<Vec<_>>()
                    .join("\n");

                erros.push(format!(
                    "### Log: `{}`\n\n```text\n{}\n```",
                    nome,
                    traduzir(&trecho)
                ));
            }
        }
    }

    let mut relatorio = String::new();

    relatorio.push_str("# Relatório de erros — Mulher Amparada\n\n");
    relatorio.push_str(&format!("- Repositório: `{}`\n", repositorio));
    relatorio.push_str(&format!("- Execução: `{}`\n", run_id));
    relatorio.push_str(&format!("- Data UTC: `{}`\n\n", chrono::Utc::now().to_rfc3339()));

    if erros.is_empty() {
        relatorio.push_str(
            "Nenhuma linha com indicadores conhecidos de erro foi encontrada nos logs coletados.\n\n"
        );
        relatorio.push_str(
            "Isso não garante que a execução esteja livre de falhas; consulte os logs completos no GitHub Actions.\n"
        );
    } else {
        relatorio.push_str(&format!(
            "Foram encontrados {} trechos que podem indicar erros ou avisos.\n\n",
            erros.len()
        ));

        for erro in &erros {
            relatorio.push_str(erro);
            relatorio.push_str("\n\n");
        }
    }

    Ok(relatorio)
}

fn main() {
    let caminho = Path::new("relatorio-erros.md");

    let resultado = executar();

    let relatorio = match resultado {
        Ok(conteudo) => conteudo,
        Err(erro) => {
            format!(
                "# Relatório de erros — Mulher Amparada\n\n\
                Não foi possível coletar os logs da execução.\n\n\
                ## Erro técnico\n\n\
                ```text\n{}\n```\n",
                erro
            )
        }
    };

    if let Err(erro) = fs::write(caminho, &relatorio) {
        eprintln!("Erro ao salvar relatorio-erros.md: {}", erro);
        std::process::exit(1);
    }

    println!("{}", relatorio);

    if env::var("GITHUB_STEP_SUMMARY").is_ok() {
        if let Ok(caminho_resumo) = env::var("GITHUB_STEP_SUMMARY") {
            if let Err(erro) = fs::write(caminho_resumo, &relatorio) {
                eprintln!("Não foi possível atualizar o resumo: {}", erro);
            }
        }
    }
}
