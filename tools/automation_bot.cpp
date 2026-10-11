#include <curl/curl.h>
#include <openssl/evp.h>
#include <openssl/pem.h>

#include <algorithm>
#include <cctype>
#include <cstdlib>
#include <ctime>
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
    std::string location;
};

static size_t writeCallback(
    char* data,
    size_t size,
    size_t count,
    void* user
) {
    if (!user || (!data && size * count != 0)) {
        return 0;
    }

    const size_t bytes = size * count;

    try {
        auto* output = static_cast<std::string*>(user);
        output->append(data, bytes);
    } catch (...) {
        return 0;
    }

    return bytes;
}

static size_t headerCallback(
    char* data,
    size_t size,
    size_t count,
    void* user
) {
    if (!user || (!data && size * count != 0)) {
        return 0;
    }

    const size_t bytes = size * count;
    auto* response = static_cast<Response*>(user);

    try {
        std::string line(data, bytes);
        const std::string prefix = "location:";

        if (line.size() >= prefix.size()) {
            std::string key = line.substr(0, prefix.size());

            std::transform(
                key.begin(),
                key.end(),
                key.begin(),
                [](unsigned char c) {
                    return static_cast<char>(std::tolower(c));
                }
            );

            if (key == prefix) {
                std::string value = line.substr(prefix.size());

                const auto first =
                    value.find_first_not_of(" \t\r\n");

                if (first == std::string::npos) {
                    response->location.clear();
                } else {
                    const auto last =
                        value.find_last_not_of(" \t\r\n");

                    response->location =
                        value.substr(first, last - first + 1);
                }
            }
        }
    } catch (...) {
        return 0;
    }

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

static std::string b64(
    const unsigned char* data,
    size_t size
) {
    if (size > static_cast<size_t>(INT_MAX)) {
        throw std::runtime_error(
            "Dados grandes demais para Base64."
        );
    }

    if (size != 0 && !data) {
        throw std::runtime_error(
            "Dados inválidos para Base64."
        );
    }

    const size_t capacity = 4 * ((size + 2) / 3);

    std::string output(capacity, '\0');

    int length = EVP_EncodeBlock(
        reinterpret_cast<unsigned char*>(output.data()),
        data,
        static_cast<int>(size)
    );

    if (length < 0) {
        throw std::runtime_error(
            "Falha ao codificar Base64."
        );
    }

    output.resize(static_cast<size_t>(length));
    return output;
}

static std::string b64url(const std::string& input) {
    std::string output = b64(
        reinterpret_cast<const unsigned char*>(input.data()),
        input.size()
    );

    std::replace(output.begin(), output.end(), '+', '-');
    std::replace(output.begin(), output.end(), '/', '_');

    while (!output.empty() && output.back() == '=') {
        output.pop_back();
    }

    return output;
}

static std::string decodeKey(std::string input) {
    input.erase(
        std::remove_if(
            input.begin(),
            input.end(),
            [](unsigned char c) {
                return std::isspace(c);
            }
        ),
        input.end()
    );

    if (input.empty()) {
        throw std::runtime_error(
            "A chave privada Base64 está vazia."
        );
    }

    while (input.size() % 4) {
        input.push_back('=');
    }

    if (input.size() > static_cast<size_t>(INT_MAX)) {
        throw std::runtime_error(
            "Chave privada grande demais."
        );
    }

    std::string output(input.size() / 4 * 3, '\0');

    int length = EVP_DecodeBlock(
        reinterpret_cast<unsigned char*>(output.data()),
        reinterpret_cast<const unsigned char*>(input.data()),
        static_cast<int>(input.size())
    );

    if (length < 0) {
        throw std::runtime_error(
            "Chave Base64 inválida."
        );
    }

    if (!input.empty() && input.back() == '=') {
        --length;
    }

    if (input.size() > 1 && input[input.size() - 2] == '=') {
        --length;
    }

    if (length < 0) {
        throw std::runtime_error(
            "Tamanho inválido da chave Base64."
        );
    }

    output.resize(static_cast<size_t>(length));
    return output;
}

static std::string makeJwt(
    const std::string& appId,
    const std::string& pem
) {
    const long long now =
        static_cast<long long>(std::time(nullptr));

    const std::string header =
        R"({"alg":"RS256","typ":"JWT"})";

    const std::string payload =
        "{\"iat\":" + std::to_string(now - 60) +
        ",\"exp\":" + std::to_string(now + 540) +
        ",\"iss\":\"" + appId + "\"}";

    const std::string message =
        b64url(header) + "." + b64url(payload);

    if (pem.empty()) {
        throw std::runtime_error(
            "A chave privada PEM está vazia."
        );
    }

    if (pem.size() > static_cast<size_t>(INT_MAX)) {
        throw std::runtime_error(
            "Chave PEM grande demais."
        );
    }

    BIO* bio = BIO_new_mem_buf(
        pem.data(),
        static_cast<int>(pem.size())
    );

    if (!bio) {
        throw std::runtime_error(
            "Falha ao abrir PEM."
        );
    }

    EVP_PKEY* key = PEM_read_bio_PrivateKey(
        bio,
        nullptr,
        nullptr,
        nullptr
    );

    BIO_free(bio);

    if (!key) {
        throw std::runtime_error(
            "PEM inválido ou incompatível."
        );
    }

    EVP_MD_CTX* context = EVP_MD_CTX_new();

    if (!context) {
        EVP_PKEY_free(key);

        throw std::runtime_error(
            "Falha no contexto criptográfico."
        );
    }

    size_t length = 0;

    bool ok =
        EVP_DigestSignInit(
            context,
            nullptr,
            EVP_sha256(),
            nullptr,
            key
        ) == 1 &&
        EVP_DigestSignUpdate(
            context,
            message.data(),
            message.size()
        ) == 1 &&
        EVP_DigestSignFinal(
            context,
            nullptr,
            &length
        ) == 1;

    std::string signature;

    if (ok) {
        signature.resize(length);

        ok = EVP_DigestSignFinal(
            context,
            reinterpret_cast<unsigned char*>(signature.data()),
            &length
        ) == 1;

        if (ok) {
            signature.resize(length);
        }
    }

    EVP_MD_CTX_free(context);
    EVP_PKEY_free(key);

    if (!ok) {
        throw std::runtime_error(
            "Falha ao assinar JWT."
        );
    }

    return message + "." + b64url(signature);
}

static Response request(
    const std::string& url,
    const std::string& authorization = "",
    const std::string& method = "GET",
    const std::string& body = "",
    const std::string& accept =
        "application/vnd.github+json"
) {
    CURL* curl = curl_easy_init();

    if (!curl) {
        throw std::runtime_error(
            "Falha ao inicializar curl."
        );
    }

    Response response;
    curl_slist* headers = nullptr;

    auto addHeader = [&](const std::string& value) {
        curl_slist* updated =
            curl_slist_append(headers, value.c_str());

        if (!updated) {
            throw std::runtime_error(
                "Falha ao alocar cabeçalhos HTTP."
            );
        }

        headers = updated;
    };

    try {
        if (!authorization.empty()) {
            addHeader("Authorization: " + authorization);
        }

        addHeader("Accept: " + accept);
        addHeader("X-GitHub-Api-Version: 2022-11-28");
        addHeader("User-Agent: mulher-amparada-bot");

        curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, writeCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response.body);
        curl_easy_setopt(curl, CURLOPT_HEADERFUNCTION, headerCallback);
        curl_easy_setopt(curl, CURLOPT_HEADERDATA, &response);

        curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 0L);
        curl_easy_setopt(curl, CURLOPT_CONNECTTIMEOUT, 30L);
        curl_easy_setopt(curl, CURLOPT_TIMEOUT, 300L);
        curl_easy_setopt(curl, CURLOPT_NOSIGNAL, 1L);

        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 1L);
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 2L);

        if (method == "POST") {
            curl_easy_setopt(curl, CURLOPT_POST, 1L);
            curl_easy_setopt(
                curl,
                CURLOPT_POSTFIELDS,
                body.data()
            );

            curl_easy_setopt(
                curl,
                CURLOPT_POSTFIELDSIZE_LARGE,
                static_cast<curl_off_t>(body.size())
            );
        } else if (method != "GET") {
            curl_easy_setopt(
                curl,
                CURLOPT_CUSTOMREQUEST,
                method.c_str()
            );

            if (!body.empty()) {
                curl_easy_setopt(
                    curl,
                    CURLOPT_POSTFIELDS,
                    body.data()
                );

                curl_easy_setopt(
                    curl,
                    CURLOPT_POSTFIELDSIZE_LARGE,
                    static_cast<curl_off_t>(body.size())
                );
            }
        }

        const CURLcode result = curl_easy_perform(curl);

        if (result != CURLE_OK) {
            throw std::runtime_error(
                std::string("HTTP: ") +
                curl_easy_strerror(result)
            );
        }

        curl_easy_getinfo(
            curl,
            CURLINFO_RESPONSE_CODE,
            &response.status
        );

        curl_slist_free_all(headers);
        curl_easy_cleanup(curl);

        return response;
    } catch (...) {
        curl_slist_free_all(headers);
        curl_easy_cleanup(curl);
        throw;
    }
}

static void require2xx(
    const Response& response,
    const std::string& action
) {
    if (response.status < 200 || response.status >= 300) {
        throw std::runtime_error(
            action + " falhou (HTTP " +
            std::to_string(response.status) +
            "): " + response.body
        );
    }
}

static std::string jsonUnescape(const std::string& input) {
    std::string output;
    output.reserve(input.size());

    for (size_t i = 0; i < input.size(); ++i) {
        if (input[i] != '\\' || i + 1 >= input.size()) {
            output.push_back(input[i]);
            continue;
        }

        const char next = input[++i];

        switch (next) {
            case '"':
                output.push_back('"');
                break;

            case '\\':
                output.push_back('\\');
                break;

            case '/':
                output.push_back('/');
                break;

            case 'b':
                output.push_back('\b');
                break;

            case 'f':
                output.push_back('\f');
                break;

            case 'n':
                output.push_back('\n');
                break;

            case 'r':
                output.push_back('\r');
                break;

            case 't':
                output.push_back('\t');
                break;

            default:
                output.push_back('\\');
                output.push_back(next);
                break;
        }
    }

    return output;
}

static std::string jsonField(
    const std::string& json,
    const std::string& field
) {
    const std::regex pattern(
        "\"" + field +
        R"re("\s*:\s*"((?:\\.|[^"\\])*)")re"
    );

    std::smatch match;

    if (!std::regex_search(json, match, pattern)) {
        throw std::runtime_error(
            "Campo JSON ausente: " + field
        );
    }

    return jsonUnescape(match[1].str());
}

static long long jsonNumber(
    const std::string& json,
    const std::string& field
) {
    const std::regex pattern(
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
    std::string output = "'";

    for (char c : value) {
        if (c == '\'') {
            output += "'\\''";
        } else {
            output += c;
        }
    }

    return output + "'";
}

static void run(const std::string& command) {
    std::cout << "$ " << command << '\n' << std::flush;

    const int result = std::system(command.c_str());

    if (result == -1) {
        throw std::runtime_error(
            "Não foi possível iniciar o comando."
        );
    }

    if (result != 0) {
        throw std::runtime_error(
            "Comando falhou com código " +
            std::to_string(result) + ": " + command
        );
    }
}

static std::string apiUrl(
    const std::string& repository,
    const std::string& path
) {
    return "https://api.github.com/repos/" +
        repository + path;
}

static bool isHttpsUrl(const std::string& url) {
    return url.rfind("https://", 0) == 0;
}

static Response downloadArtifact(
    const std::string& url,
    const std::string& token
) {
    Response response = request(
        url,
        "Bearer " + token,
        "GET",
        "",
        "application/vnd.github+json"
    );

    for (
        int redirects = 0;
        response.status >= 300 && response.status < 400;
        ++redirects
    ) {
        if (redirects >= 5) {
            throw std::runtime_error(
                "Limite de redirecionamentos excedido."
            );
        }

        const std::string location = response.location;

        if (location.empty() || !isHttpsUrl(location)) {
            throw std::runtime_error(
                "URL de redirecionamento inválida."
            );
        }

        response = request(
            location,
            "",
            "GET",
            "",
            "application/octet-stream"
        );
    }

    require2xx(response, "Download do artifact");
    return response;
}

static std::string decodeBase64(std::string input) {
    input.erase(
        std::remove_if(
            input.begin(),
            input.end(),
            [](unsigned char c) {
                return std::isspace(c);
            }
        ),
        input.end()
    );

    if (input.empty()) {
        return {};
    }

    if (input.size() > static_cast<size_t>(INT_MAX)) {
        throw std::runtime_error(
            "Conteúdo Base64 grande demais."
        );
    }

    while (input.size() % 4) {
        input.push_back('=');
    }

    std::string output(input.size() / 4 * 3, '\0');

    const int decodedLength = EVP_DecodeBlock(
        reinterpret_cast<unsigned char*>(output.data()),
        reinterpret_cast<const unsigned char*>(input.data()),
        static_cast<int>(input.size())
    );

    if (decodedLength < 0) {
        throw std::runtime_error(
            "Não foi possível decodificar Base64."
        );
    }

    int length = decodedLength;

    if (!input.empty() && input.back() == '=') {
        --length;
    }

    if (input.size() > 1 &&
        input[input.size() - 2] == '=') {
        --length;
    }

    if (length < 0) {
        throw std::runtime_error(
            "Tamanho inválido após decodificação Base64."
        );
    }

    output.resize(static_cast<size_t>(length));
    return output;
}

static void mergeArtifact(
    const fs::path& source,
    const fs::path& destination
) {
    if (!fs::exists(source) || !fs::is_directory(source)) {
        std::cerr
            << "Artifact não encontrado ou inválido: "
            << source << '\n';

        return;
    }

    fs::create_directories(destination);

    for (
        const auto& item :
        fs::recursive_directory_iterator(source)
    ) {
        if (!item.is_regular_file()) {
            continue;
        }

        const fs::path relative =
            fs::relative(item.path(), source);

        const fs::path output =
            destination / relative;

        fs::create_directories(output.parent_path());

        fs::copy_file(
            item.path(),
            output,
            fs::copy_options::overwrite_existing
        );

        std::cout
            << "Arquivo atualizado: "
            << relative.string()
            << '\n';
    }
}

static std::string trim(std::string value) {
    const size_t begin =
        value.find_first_not_of(" \t\r\n");

    if (begin == std::string::npos) {
        return {};
    }

    const size_t finish =
        value.find_last_not_of(" \t\r\n");

    return value.substr(begin, finish - begin + 1);
}

static bool validArtifactName(const std::string& name) {
    return !name.empty() &&
        name != "." &&
        name != ".." &&
        name.find('/') == std::string::npos &&
        name.find('\\') == std::string::npos;
}

/*
 * A API retorna um array de objetos JSON.
 * A extração abaixo não depende da ordem das propriedades
 * e não usa uma única expressão regular para o objeto inteiro.
 */
static std::vector<std::pair<std::string, std::string>>
parseArtifacts(const std::string& json) {
    std::vector<std::pair<std::string, std::string>> artifacts;

    const std::regex namePattern(
        R"re("name"\s*:\s*"((?:\\.|[^"\\])*)")re"
    );

    const std::regex urlPattern(
        R"re("archive_download_url"\s*:\s*"((?:\\.|[^"\\])*)")re"
    );

    size_t position = 0;

    while (position < json.size()) {
        const size_t idPosition =
            json.find("\"id\"", position);

        if (idPosition == std::string::npos) {
            break;
        }

        const size_t begin =
            json.rfind('{', idPosition);

        const size_t end =
            json.find('}', idPosition);

        if (
            begin == std::string::npos ||
            end == std::string::npos ||
            end < idPosition
        ) {
            position = idPosition + 4;
            continue;
        }

        const std::string object =
            json.substr(begin, end - begin + 1);

        std::smatch nameMatch;
        std::smatch urlMatch;

        if (
            std::regex_search(
                object,
                nameMatch,
                namePattern
            ) &&
            std::regex_search(
                object,
                urlMatch,
                urlPattern
            )
        ) {
            const std::string name =
                jsonUnescape(nameMatch[1].str());

            const std::string url =
                jsonUnescape(urlMatch[1].str());

            if (validArtifactName(name) &&
                isHttpsUrl(url)) {
                artifacts.emplace_back(name, url);
            }
        }

        position = end + 1;
    }

    return artifacts;
}

int main() {
    bool curlInitialized = false;

    try {
        const CURLcode initResult =
            curl_global_init(CURL_GLOBAL_DEFAULT);

        if (initResult != CURLE_OK) {
            throw std::runtime_error(
                "Falha ao inicializar libcurl."
            );
        }

        curlInitialized = true;

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

        const fs::path root =
            fs::canonical(env("GITHUB_WORKSPACE"));

        if (
            repository.find('/') == std::string::npos ||
            runId.empty() ||
            !std::all_of(
                runId.begin(),
                runId.end(),
                [](unsigned char c) {
                    return std::isdigit(c);
                }
            )
        ) {
            throw std::runtime_error(
                "GITHUB_REPOSITORY ou GITHUB_RUN_ID inválido."
            );
        }

        std::cout
            << "Criando JWT do GitHub App...\n"
            << std::flush;

        const std::string jwt =
            makeJwt(appId, privateKey);

        std::cout
            << "Obtendo token de instalação...\n"
            << std::flush;

        Response tokenResponse = request(
            "https://api.github.com/app/installations/" +
                installationId + "/access_tokens",
            "Bearer " + jwt,
            "POST",
            "{}"
        );

        require2xx(
            tokenResponse,
            "Token de instalação"
        );

        const std::string token =
            jsonField(tokenResponse.body, "token");

        std::cout
            << "Consultando artifacts da execução "
            << runId << "...\n"
            << std::flush;

        Response artifactsResponse = request(
            apiUrl(
                repository,
                "/actions/runs/" + runId +
                    "/artifacts?per_page=100"
            ),
            "Bearer " + token
        );

        require2xx(
            artifactsResponse,
            "Consulta de artifacts"
        );

        const fs::path temp =
            fs::temp_directory_path() /
            ("mulher-amparada-" + runId);

        fs::create_directories(temp);

        std::cout
            << "Consultando workflow da branch main...\n"
            << std::flush;

        Response workflow = request(
            apiUrl(
                repository,
                "/contents/.github/workflows/automations.yml?ref=main"
            ),
            "Bearer " + token
        );

        require2xx(
            workflow,
            "Consulta do workflow"
        );

        const std::string workflowContent =
            decodeBase64(
                jsonField(workflow.body, "content")
            );

        const fs::path workflowTemp =
            temp / "workflow" / "automations.yml";

        fs::create_directories(
            workflowTemp.parent_path()
        );

        {
            std::ofstream file(
                workflowTemp,
                std::ios::binary
            );

            if (!file) {
                throw std::runtime_error(
                    "Não foi possível salvar o workflow temporário."
                );
            }

            file.write(
                workflowContent.data(),
                static_cast<std::streamsize>(
                    workflowContent.size()
                )
            );

            if (!file) {
                throw std::runtime_error(
                    "Falha ao gravar o workflow temporário."
                );
            }
        }

        const fs::path workflowPath =
            root / ".github/workflows/automations.yml";

        if (!fs::exists(workflowPath)) {
            throw std::runtime_error(
                "O checkout não contém "
                ".github/workflows/automations.yml."
            );
        }

        fs::copy_file(
            workflowTemp,
            root / "automations-workflow-consultado.yml",
            fs::copy_options::overwrite_existing
        );

        std::vector<std::pair<std::string, std::string>>
            artifacts = parseArtifacts(
                artifactsResponse.body
            );

        if (artifacts.empty()) {
            throw std::runtime_error(
                "Nenhum artifact válido encontrado na execução " +
                runId + ". Resposta: " +
                artifactsResponse.body
            );
        }

        const fs::path artifactsDir =
            temp / "artifacts";

        fs::create_directories(artifactsDir);

        for (const auto& artifact : artifacts) {
            const std::string& name = artifact.first;
            const std::string& url = artifact.second;

            if (!validArtifactName(name)) {
                throw std::runtime_error(
                    "Nome de artifact inválido."
                );
            }

            std::cout
                << "Baixando artifact: "
                << name << '\n'
                << std::flush;

            Response archive =
                downloadArtifact(url, token);

            const fs::path zip =
                artifactsDir / (name + ".zip");

            {
                std::ofstream zipFile(
                    zip,
                    std::ios::binary
                );

                if (!zipFile) {
                    throw std::runtime_error(
                        "Não foi possível criar: " +
                        zip.string()
                    );
                }

                zipFile.write(
                    archive.body.data(),
                    static_cast<std::streamsize>(
                        archive.body.size()
                    )
                );

                if (!zipFile) {
                    throw std::runtime_error(
                        "Falha ao gravar: " +
                        zip.string()
                    );
                }
            }

            const fs::path output =
                artifactsDir / name;

            fs::create_directories(output);

            run(
                "unzip -o " +
                shellQuote(zip.string()) +
                " -d " +
                shellQuote(output.string())
            );
        }

        const char* mapEnv =
            std::getenv("MULHER_AMPARADA_ARTIFACT_MAP");

        if (mapEnv && *mapEnv) {
            const std::string mappings(mapEnv);
            size_t start = 0;

            while (start < mappings.size()) {
                const size_t end =
                    mappings.find(';', start);

                std::string entry = mappings.substr(
                    start,
                    end == std::string::npos
                        ? std::string::npos
                        : end - start
                );

                entry = trim(entry);

                if (!entry.empty()) {
                    const size_t colon =
                        entry.find(':');

                    if (colon == std::string::npos) {
                        std::cerr
                            << "Aviso: mapeamento inválido: "
                            << entry << '\n';
                    } else {
                        std::string artifactName =
                            trim(entry.substr(0, colon));

                        std::string target =
                            trim(entry.substr(colon + 1));

                        if (artifactName.empty()) {
                            throw std::runtime_error(
                                "Nome de artifact vazio no mapeamento."
                            );
                        }

                        if (!validArtifactName(artifactName)) {
                            throw std::runtime_error(
                                "Nome de artifact inválido no mapeamento: " +
                                artifactName
                            );
                        }

                        fs::path relativeTarget =
                            target.empty() || target == "."
                                ? fs::path(".")
                                : fs::path(target);

                        if (relativeTarget.is_absolute()) {
                            throw std::runtime_error(
                                "Destino absoluto não permitido: " +
                                target
                            );
                        }

                        relativeTarget =
                            relativeTarget.lexically_normal();

                        for (const auto& part : relativeTarget) {
                            if (part == "..") {
                                throw std::runtime_error(
                                    "Destino fora do repositório não permitido: " +
                                    target
                                );
                            }
                        }

                        const fs::path source =
                            artifactsDir / artifactName;

                        const fs::path destination =
                            relativeTarget == fs::path(".")
                                ? root
                                : root / relativeTarget;

                        if (
                            fs::exists(source) &&
                            fs::is_directory(source)
                        ) {
                            std::cout
                                << "Mesclando artifact "
                                << artifactName
                                << " em "
                                << destination.string()
                                << "...\n"
                                << std::flush;

                            mergeArtifact(
                                source,
                                destination
                            );
                        } else {
                            std::cerr
                                << "Aviso: artifact do mapeamento não encontrado: "
                                << artifactName << '\n';
                        }
                    }
                }

                if (end == std::string::npos) {
                    break;
                }

                start = end + 1;
            }
        } else {
            throw std::runtime_error(
                "Variável MULHER_AMPARADA_ARTIFACT_MAP ausente."
            );
        }

        const fs::path askpass =
            temp / "git-askpass.sh";

        {
            std::ofstream script(askpass);

            if (!script) {
                throw std::runtime_error(
                    "Não foi possível criar o script GIT_ASKPASS."
                );
            }

            script
                << "#!/bin/sh\n"
                << "case \"$1\" in\n"
                << "  *Username*) printf '%s\\n' 'x-access-token' ;;\n"
                << "  *Password*) printf '%s\\n' \"$MULHER_AMPARADA_GIT_TOKEN\" ;;\n"
                << "  *) exit 1 ;;\n"
                << "esac\n";
        }

        if (chmod(askpass.c_str(), 0700) != 0) {
            throw std::runtime_error(
                "Não foi possível configurar permissões do GIT_ASKPASS."
            );
        }

        if (
            setenv("GIT_ASKPASS", askpass.c_str(), 1) != 0 ||
            setenv("GIT_TERMINAL_PROMPT", "0", 1) != 0 ||
            setenv(
                "MULHER_AMPARADA_GIT_TOKEN",
                token.c_str(),
                1
            ) != 0
        ) {
            throw std::runtime_error(
                "Não foi possível configurar a autenticação Git."
            );
        }

        const std::string git =
            "git -C " + shellQuote(root.string()) + " ";

        run(
            git + "config user.name " +
            shellQuote("mulher-amparada-bot[bot]")
        );

        run(
            git + "config user.email " +
            shellQuote(
                "mulher-amparada-bot[bot]@users.noreply.github.com"
            )
        );

        run(git + " fetch origin main");
        run(git + " rebase origin/main");
        run(git + " add -A");

        const int staged = std::system(
            (
                git +
                "diff --cached --quiet"
            ).c_str()
        );

        if (staged == 0) {
            std::cout
                << "Nenhuma alteração para publicar.\n";

            unsetenv("MULHER_AMPARADA_GIT_TOKEN");
            curl_global_cleanup();
            return 0;
        }

        if (staged != 256) {
            throw std::runtime_error(
                "Falha ao verificar alterações staged."
            );
        }

        run(
            git +
            " commit -m " +
            shellQuote(
                "Atualização automática dos artifacts"
            )
        );

        run(git + " fetch origin main");
        run(git + " rebase origin/main");
        run(git + " push origin HEAD:main");

        unsetenv("MULHER_AMPARADA_GIT_TOKEN");

        std::cout
            << "Artifacts publicados na branch main.\n";

        curl_global_cleanup();
        return 0;

    } catch (const std::exception& error) {
        std::cerr
            << "::error::"
            << error.what()
            << '\n';

        if (curlInitialized) {
            curl_global_cleanup();
        }

        return 1;
    } catch (...) {
        std::cerr
            << "::error::Erro desconhecido no automation_bot.\n";

        if (curlInitialized) {
            curl_global_cleanup();
        }

        return 1;
    }
}