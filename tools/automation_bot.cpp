
#include <curl/curl.h>
#include <openssl/evp.h>
#include <openssl/pem.h>

#include <algorithm>
#include <cstdlib>
#include <filesystem>
#include <fstream>
#include <iostream>
#include <regex>
#include <stdexcept>
#include <string>
#include <vector>
#include <sys/stat.h>

namespace fs = std::filesystem;

struct Response {
    long status = 0;
    std::string body;
};

static size_t writeCallback(
    char* data, size_t size, size_t count, void* user
) {
    auto* output = static_cast<std::string*>(user);
    const size_t bytes = size * count;
    output->append(data, bytes);
    return bytes;
}

static std::string env(const char* name) {
    const char* value = std::getenv(name);
    if (!value || !*value) {
        throw std::runtime_error(
            std::string("Variável ausente: ") + name
        );
    }
    return value;
}

static std::string b64(const unsigned char* data, size_t size) {
    std::string out(4 * ((size + 2) / 3), '\0');
    int length = EVP_EncodeBlock(
        reinterpret_cast<unsigned char*>(out.data()),
        data,
        static_cast<int>(size)
    );
    out.resize(length);
    return out;
}

static std::string b64url(const std::string& input) {
    std::string out = b64(
        reinterpret_cast<const unsigned char*>(input.data()),
        input.size()
    );

    std::replace(out.begin(), out.end(), '+', '-');
    std::replace(out.begin(), out.end(), '/', '_');

    while (!out.empty() && out.back() == '=') {
        out.pop_back();
    }
    return out;
}

static std::string decodeKey(std::string input) {
    input.erase(
        std::remove_if(
            input.begin(), input.end(),
            [](unsigned char c) {
                return c == '\n' || c == '\r' ||
                       c == ' ' || c == '\t';
            }
        ),
        input.end()
    );

    while (input.size() % 4) input.push_back('=');

    std::string output(input.size() / 4 * 3, '\0');

    int length = EVP_DecodeBlock(
        reinterpret_cast<unsigned char*>(output.data()),
        reinterpret_cast<const unsigned char*>(input.data()),
        static_cast<int>(input.size())
    );

    if (length < 0) {
        throw std::runtime_error("Chave Base64 inválida.");
    }

    if (!input.empty() && input.back() == '=') --length;
    if (input.size() > 1 && input[input.size() - 2] == '=') {
        --length;
    }

    output.resize(length);
    return output;
}

static std::string makeJwt(
    const std::string& appId,
    const std::string& pem
) {
    const long long now = static_cast<long long>(time(nullptr));

    const std::string header =
        R"({"alg":"RS256","typ":"JWT"})";

    const std::string payload =
        "{\"iat\":" + std::to_string(now - 60) +
        ",\"exp\":" + std::to_string(now + 540) +
        ",\"iss\":\"" + appId + "\"}";

    const std::string message =
        b64url(header) + "." + b64url(payload);

    BIO* bio = BIO_new_mem_buf(
        pem.data(), static_cast<int>(pem.size())
    );

    if (!bio) throw std::runtime_error("Falha ao abrir PEM.");

    EVP_PKEY* key = PEM_read_bio_PrivateKey(
        bio, nullptr, nullptr, nullptr
    );
    BIO_free(bio);

    if (!key) throw std::runtime_error("PEM inválido.");

    EVP_MD_CTX* ctx = EVP_MD_CTX_new();
    if (!ctx) {
        EVP_PKEY_free(key);
        throw std::runtime_error("Falha no contexto criptográfico.");
    }

    size_t length = 0;
    bool ok =
        EVP_DigestSignInit(
            ctx, nullptr, EVP_sha256(), nullptr, key
        ) == 1 &&
        EVP_DigestSignUpdate(
            ctx, message.data(), message.size()
        ) == 1 &&
        EVP_DigestSignFinal(ctx, nullptr, &length) == 1;

    std::string signature;

    if (ok) {
        signature.resize(length);
        ok = EVP_DigestSignFinal(
            ctx,
            reinterpret_cast<unsigned char*>(signature.data()),
            &length
        ) == 1;
        signature.resize(length);
    }

    EVP_MD_CTX_free(ctx);
    EVP_PKEY_free(key);

    if (!ok) throw std::runtime_error("Falha ao assinar JWT.");

    return message + "." + b64url(signature);
}

static Response request(
    const std::string& url,
    const std::string& authorization,
    const std::string& method = "GET",
    const std::string& body = "",
    const std::string& accept =
        "application/vnd.github+json"
) {
    CURL* curl = curl_easy_init();
    if (!curl) throw std::runtime_error("Falha ao inicializar curl.");

    Response response;
    curl_slist* headers = nullptr;

    headers = curl_slist_append(
        headers, ("Authorization: " + authorization).c_str()
    );
    headers = curl_slist_append(
        headers, ("Accept: " + accept).c_str()
    );
    headers = curl_slist_append(
        headers, "X-GitHub-Api-Version: 2022-11-28"
    );
    headers = curl_slist_append(
        headers, "User-Agent: mulher-amparada-bot"
    );

    curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, writeCallback);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response.body);
    curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 0L);
    curl_easy_setopt(curl, CURLOPT_CONNECTTIMEOUT, 30L);
    curl_easy_setopt(curl, CURLOPT_TIMEOUT, 300L);

    if (method == "POST") {
        curl_easy_setopt(curl, CURLOPT_POST, 1L);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, body.c_str());
        curl_easy_setopt(
            curl, CURLOPT_POSTFIELDSIZE,
            static_cast<long>(body.size())
        );
    }

    CURLcode result = curl_easy_perform(curl);

    if (result != CURLE_OK) {
        std::string error = curl_easy_strerror(result);
        curl_slist_free_all(headers);
        curl_easy_cleanup(curl);
        throw std::runtime_error("HTTP: " + error);
    }

    curl_easy_getinfo(
        curl, CURLINFO_RESPONSE_CODE, &response.status
    );

    curl_slist_free_all(headers);
    curl_easy_cleanup(curl);
    return response;
}

static void require2xx(const Response& r, const std::string& action) {
    if (r.status < 200 || r.status >= 300) {
        throw std::runtime_error(
            action + " falhou (HTTP " +
            std::to_string(r.status) + "): " + r.body
        );
    }
}

static std::string jsonField(
    const std::string& json,
    const std::string& field
) {
    std::regex pattern(
        "\"" + field + "\"\\s*:\\s*\"([^\"]+)\""
    );
    std::smatch match;

    if (!std::regex_search(json, match, pattern)) {
        throw std::runtime_error(
            "Campo JSON ausente: " + field
        );
    }
    return match[1].str();
}

static long long jsonNumber(
    const std::string& json,
    const std::string& field
) {
    std::regex pattern(
        "\"" + field + "\"\\s*:\\s*([0-9]+)"
    );
    std::smatch match;

    if (!std::regex_search(json, match, pattern)) {
        throw std::runtime_error(
            "Campo numérico ausente: " + field
        );
    }
    return std::stoll(match[1].str());
}

static std::string shellQuote(const std::string& value) {
    std::string out = "'";
    for (char c : value) {
        if (c == '\'') out += "'\\''";
        else out += c;
    }
    return out + "'";
}

static void run(const std::string& command) {
    std::cout << "$ " << command << '\n';
    int result = std::system(command.c_str());

    if (result != 0) {
        throw std::runtime_error(
            "Comando falhou: " + command
        );
    }
}

static std::string apiUrl(
    const std::string& repo,
    const std::string& path
) {
    return "https://api.github.com/repos/" + repo + path;
}

static void downloadFile(
    const std::string& url,
    const std::string& token,
    const fs::path& destination
) {
    Response r = request(
        url,
        "Bearer " + token,
        "GET",
        "",
        "application/vnd.github+json"
    );

    /*
     * A API de artifacts pode responder com redirecionamento.
     * Para evitar seguir redirecionamentos com o token do GitHub
     * enviado a outro host, o download usa uma segunda requisição
     * sem Authorization após validar o endereço de destino.
     */
    if (r.status >= 300 && r.status < 400) {
        throw std::runtime_error(
            "Download redirecionado inesperadamente. "
            "O endpoint precisa fornecer uma URL de download."
        );
    }

    require2xx(r, "Download");
    fs::create_directories(destination.parent_path());

    std::ofstream file(destination, std::ios::binary);
    file.write(r.body.data(), static_cast<std::streamsize>(r.body.size()));

    if (!file) throw std::runtime_error("Falha ao salvar download.");
}

int main() {
    try {
        curl_global_init(CURL_GLOBAL_DEFAULT);

        const std::string appId =
            env("MULHER_AMPARADA_APP_ID");

        const std::string installationId =
            env("MULHER_AMPARADA_INSTALLATION_ID");

        const std::string privateKey =
            decodeKey(env("MULHER_AMPARADA_PRIVATE_KEY"));

        const std::string repository =
            env("GITHUB_REPOSITORY");

        const std::string runId =
            env("GITHUB_RUN_ID");

        const std::string workspace =
            env("GITHUB_WORKSPACE");

        const std::string jwt = makeJwt(appId, privateKey);

        std::cout << "Obtendo token de instalação do GitHub App...\n";

        Response tokenResponse = request(
            "https://api.github.com/app/installations/" +
            installationId + "/access_tokens",
            "Bearer " + jwt,
            "POST",
            "{}"
        );

        require2xx(tokenResponse, "Token de instalação");

        const std::string token =
            jsonField(tokenResponse.body, "token");

        const std::string apiRoot =
            apiUrl(repository, "/actions/runs/" + runId + "/artifacts");

        std::cout << "Consultando artifacts da execução "
                  << runId << "...\n";

        Response artifactsResponse = request(
            apiRoot + "?per_page=100",
            "Bearer " + token
        );

        require2xx(artifactsResponse, "Consulta de artifacts");

        const std::string artifactsJson =
            artifactsResponse.body;

        const fs::path temp =
            fs::temp_directory_path() /
            ("mulher-amparada-" + runId);

        fs::create_directories(temp);

        /*
         * Consulta o workflow atual pela API e grava uma cópia
         * local para auditoria. O arquivo do workflow não é
         * executado como código C++.
         */
        std::cout << "Consultando o workflow atual...\n";

        Response workflow = request(
            apiUrl(
                repository,
                "/contents/.github/workflows/automations.yml?ref=main"
            ),
            "Bearer " + token
        );

        require2xx(workflow, "Consulta do workflow");

        std::string workflowContent =
            jsonField(workflow.body, "content");

        workflowContent.erase(
            std::remove_if(
                workflowContent.begin(),
                workflowContent.end(),
                [](unsigned char c) {
                    return c == '\n' || c == '\r' ||
                           c == ' ' || c == '\t';
                }
            ),
            workflowContent.end()
        );

        while (workflowContent.size() % 4) {
            workflowContent.push_back('=');
        }

        std::string decodedWorkflow(
            workflowContent.size() / 4 * 3, '\0'
        );

        int decodedLength = EVP_DecodeBlock(
            reinterpret_cast<unsigned char*>(decodedWorkflow.data()),
            reinterpret_cast<const unsigned char*>(
                workflowContent.data()
            ),
            static_cast<int>(workflowContent.size())
        );

        if (decodedLength < 0) {
            throw std::runtime_error(
                "Não foi possível decodificar o workflow."
            );
        }

        if (!workflowContent.empty() &&
            workflowContent.back() == '=') {
            --decodedLength;
        }

        if (workflowContent.size() > 1 &&
            workflowContent[workflowContent.size() - 2] == '=') {
            --decodedLength;
        }

        decodedWorkflow.resize(decodedLength);

        fs::create_directories(temp / "workflow");

        std::ofstream workflowFile(
            temp / "workflow" / "automations.yml"
        );

        workflowFile << decodedWorkflow;
        workflowFile.close();

        /*
         * Lista os artifacts. A extração é feita com unzip,
         * portanto o runner precisa ter unzip instalado.
         */
        std::regex artifactObject(
            R"re(\{[^{}]*"id"\s*:\s*([0-9]+)[^{}]*"name"\s*:\s*"([^"]+)"[^{}]*"archive_download_url"\s*:\s*"([^"]+)"[^{}]*\})re"
        );

        std::vector<std::pair<std::string, std::string>> artifacts;

        for (
            std::sregex_iterator it(
                artifactsJson.begin(),
                artifactsJson.end(),
                artifactObject
            ),
            end;
            it != end;
            ++it
        ) {
            artifacts.emplace_back(
                (*it)[2].str(),
                (*it)[3].str()
            );
        }

        if (artifacts.empty()) {
            throw std::runtime_error(
                "Nenhum artifact encontrado na execução. "
                "Verifique se o job roda após os produtores."
            );
        }

        const fs::path artifactsDir = temp / "artifacts";
        fs::create_directories(artifactsDir);

        for (const auto& artifact : artifacts) {
            const std::string name = artifact.first;
            const std::string url = artifact.second;

            std::cout << "Baixando artifact: " << name << '\n';

            /*
             * A URL archive_download_url requer autenticação
             * e pode responder com redirect. O fluxo abaixo
             * solicita o endpoint pelo curl e exige resposta
             * direta, evitando enviar o token a outro host.
             */
            Response archive = request(
                url,
                "Bearer " + token
            );

            if (archive.status >= 300 && archive.status < 400) {
                std::cerr
                    << "O GitHub redirecionou o download de "
                    << name
                    << ". O CPP precisa resolver a URL assinada "
                    << "sem encaminhar o token para outro host.\n";
                continue;
            }

            require2xx(archive, "Download do artifact " + name);

            const fs::path zip = artifactsDir / (name + ".zip");

            std::ofstream zipFile(zip, std::ios::binary);
            zipFile.write(
                archive.body.data(),
                static_cast<std::streamsize>(archive.body.size())
            );
            zipFile.close();

            const fs::path output = artifactsDir / name;
            fs::create_directories(output);

            run(
                "unzip -o " + shellQuote(zip.string()) +
                " -d " + shellQuote(output.string())
            );
        }

        /*
         * A execução do workflow não deve publicar artifacts
         * de outros runs. A lista consultada acima está limitada
         * ao GITHUB_RUN_ID atual.
         *
         * Copie somente os arquivos de artifacts conhecidos
         * para o checkout antes de executar o bot publicador.
         * Os artifacts ficam preservados em $RUNNER_TEMP.
         */
        const fs::path root = fs::canonical(workspace);

        const fs::path workflowPath =
            root / ".github/workflows/automations.yml";

        if (!fs::exists(workflowPath)) {
            throw std::runtime_error(
                "O checkout não contém .github/workflows/automations.yml."
            );
        }

        /*
         * Copia o workflow consultado para o checkout, sem
         * sobrescrever a versão do checkout com uma versão
         * diferente da main silenciosamente.
         */
        const fs::path savedWorkflow =
            root / "automations-workflow-consultado.yml";

        fs::copy_file(
            temp / "workflow" / "automations.yml",
            savedWorkflow,
            fs::copy_options::overwrite_existing
        );

        /*
         * Para publicar arquivos, os artifacts precisam ter
         * caminhos conhecidos no repositório. Os arquivos
         * baixados permanecem em:
         *
         * $RUNNER_TEMP/mulher-amparada-<run>/artifacts/
         *
         * Este programa não joga arquivos arbitrários na raiz
         * do projeto: isso poderia sobrescrever código-fonte,
         * configurações ou arquivos de segurança.
         *
         * O job pode definir MULHER_AMPARADA_ARTIFACT_MAP com
         * mapeamentos "artifact:subdiretório" separados por
         * ponto e vírgula, por exemplo:
         *
         * APK-Kotlin-Assinado:.
         * arquivo-readme:.
         * sitemap:sitemap
         */
        const char* mapEnv =
            std::getenv("MULHER_AMPARADA_ARTIFACT_MAP");

        if (mapEnv && *mapEnv) {
            std::string mappings(mapEnv);
            size_t start = 0;

            while (start < mappings.size()) {
                size_t end = mappings.find(';', start);

                std::string entry = mappings.substr(
                    start,
                    end == std::string::npos
                        ? std::string::npos
                        : end - start
                );

                size_t colon = entry.find(':');

                if (colon != std::string::npos) {
                    std::string artifactName =
                        entry.substr(0, colon);

                    std::string target =
                        entry.substr(colon + 1);

                    fs::path source = artifactsDir / artifactName;
                    fs::path destination = root / target;

                    if (fs::exists(source)) {
                        fs::create_directories(destination);

                        for (
                            const auto& item :
                            fs::recursive_directory_iterator(source)
                        ) {
                            if (!item.is_regular_file()) continue;

                            fs::path relative =
                                fs::relative(item.path(), source);

                            fs::path output =
                                destination / relative;

                            fs::create_directories(output.parent_path());

                            fs::copy_file(
                                item.path(),
                                output,
                                fs::copy_options::overwrite_existing
                            );
                        }
                    }
                }

                if (end == std::string::npos) break;
                start = end + 1;
            }
        }

        /*
         * Prepara autenticação Git via GIT_ASKPASS. O token
         * não é inserido na URL do remote nem impresso no log.
         */
        const fs::path askpass =
            temp / "git-askpass.sh";

        {
            std::ofstream script(askpass);
            script
                << "#!/bin/sh\n"
                << "case \"$1\" in\n"
                << "  *Username*) printf '%s\\n' 'x-access-token' ;;\n"
                << "  *Password*) printf '%s\\n' \"$MULHER_AMPARADA_GIT_TOKEN\" ;;\n"
                << "  *) exit 1 ;;\n"
                << "esac\n";
        }

        chmod(askpass.c_str(), 0700);

        setenv("GIT_ASKPASS", askpass.c_str(), 1);
        setenv("GIT_TERMINAL_PROMPT", "0", 1);
        setenv("MULHER_AMPARADA_GIT_TOKEN", token.c_str(), 1);

        run("git -C " + shellQuote(root.string()) +
            " config user.name " +
            shellQuote("mulher-amparada-bot[bot]"));

        run("git -C " + shellQuote(root.string()) +
            " config user.email " +
            shellQuote("mulher-amparada-bot[bot]@users.noreply.github.com"));

        run("git -C " + shellQuote(root.string()) + " add -A");

        int staged = std::system(
            ("git -C " + shellQuote(root.string()) +
             " diff --cached --quiet").c_str()
        );

        if (staged == 0) {
            std::cout << "Nenhuma alteração para publicar.\n";
            curl_global_cleanup();
            return 0;
        }

        run(
            "git -C " + shellQuote(root.string()) +
            " commit -m " +
            shellQuote("Atualização automática pelo mulher-amparada-bot [skip ci]")
        );

        run("git -C " + shellQuote(root.string()) + " fetch origin main");

        run(
            "git -C " + shellQuote(root.string()) +
            " rebase origin/main"
        );

        run(
            "git -C " + shellQuote(root.string()) +
            " push origin HEAD:main"
        );

        std::cout << "Publicação na main concluída pelo bot.\n";

        curl_global_cleanup();
        return 0;

    } catch (const std::exception& e) {
        std::cerr << "ERRO: " << e.what() << '\n';
        curl_global_cleanup();
        return 1;
    }
}
