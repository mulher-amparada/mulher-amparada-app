#include <curl/curl.h>
#include <openssl/evp.h>

#include <algorithm>
#include <climits>
#include <cstdlib>
#include <filesystem>
#include <fstream>
#include <iostream>
#include <regex>
#include <stdexcept>
#include <string>
#include <vector>
#include <sys/stat.h>
#include <sys/wait.h>

namespace fs = std::filesystem;

struct Response {
    long status = 0;
    std::string body;
};

static size_t writeCallback(
    char* data,
    size_t size,
    size_t count,
    void* user
) {
    if (size != 0 && count > SIZE_MAX / size) {
        return 0;
    }

    const size_t bytes = size * count;
    auto* output = static_cast<std::string*>(user);

    try {
        output->append(data, bytes);
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

static std::string shellQuote(const std::string& value) {
    std::string out = "'";

    for (char c : value) {
        if (c == '\'') {
            out += "'\\''";
        } else {
            out += c;
        }
    }

    return out + "'";
}

static void run(const std::string& command) {
    std::cout << "$ " << command << '\n';

    const int result = std::system(command.c_str());

    if (result == -1 ||
        !WIFEXITED(result) ||
        WEXITSTATUS(result) != 0) {
        throw std::runtime_error(
            "Falha ao executar comando."
        );
    }
}

static Response request(
    const std::string& url,
    const std::string& token,
    const std::string& method = "GET",
    const std::string& body = "",
    const std::string& accept =
        "application/vnd.github+json"
) {
    if (url.rfind("https://", 0) != 0) {
        throw std::runtime_error(
            "Somente URLs HTTPS são permitidas."
        );
    }

    CURL* curl = curl_easy_init();

    if (!curl) {
        throw std::runtime_error(
            "Falha ao inicializar curl."
        );
    }

    Response response;
    curl_slist* headers = nullptr;

    auto addHeader = [&](const std::string& header) {
        curl_slist* updated =
            curl_slist_append(headers, header.c_str());

        if (!updated) {
            throw std::runtime_error(
                "Falha ao criar cabeçalhos HTTP."
            );
        }

        headers = updated;
    };

    try {
        addHeader("Authorization: Bearer " + token);
        addHeader("Accept: " + accept);
        addHeader(
            "X-GitHub-Api-Version: 2022-11-28"
        );
        addHeader(
            "User-Agent: mulher-amparada-bot"
        );

        curl_easy_setopt(
            curl, CURLOPT_URL, url.c_str()
        );

        curl_easy_setopt(
            curl, CURLOPT_HTTPHEADER, headers
        );

        curl_easy_setopt(
            curl, CURLOPT_WRITEFUNCTION, writeCallback
        );

        curl_easy_setopt(
            curl, CURLOPT_WRITEDATA, &response.body
        );

        curl_easy_setopt(
            curl, CURLOPT_FOLLOWLOCATION, 1L
        );

        curl_easy_setopt(
            curl, CURLOPT_MAXREDIRS, 5L
        );

        curl_easy_setopt(
            curl, CURLOPT_CONNECTTIMEOUT, 30L
        );

        curl_easy_setopt(
            curl, CURLOPT_TIMEOUT, 300L
        );

        curl_easy_setopt(
            curl, CURLOPT_SSL_VERIFYPEER, 1L
        );

        curl_easy_setopt(
            curl, CURLOPT_SSL_VERIFYHOST, 2L
        );

        curl_easy_setopt(
            curl, CURLOPT_PROTOCOLS_STR, "https"
        );

        curl_easy_setopt(
            curl, CURLOPT_REDIR_PROTOCOLS_STR, "https"
        );

        if (method == "POST") {
            curl_easy_setopt(
                curl, CURLOPT_POST, 1L
            );

            curl_easy_setopt(
                curl, CURLOPT_POSTFIELDS, body.c_str()
            );

            curl_easy_setopt(
                curl, CURLOPT_POSTFIELDSIZE,
                static_cast<long>(body.size())
            );
        }

        const CURLcode result =
            curl_easy_perform(curl);

        if (result != CURLE_OK) {
            throw std::runtime_error(
                std::string("Erro HTTP: ") +
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
    if (response.status < 200 ||
        response.status >= 300) {
        throw std::runtime_error(
            action + " falhou (HTTP " +
            std::to_string(response.status) + ")."
        );
    }
}

static std::string jsonStringField(
    const std::string& object,
    const std::string& field
) {
    const std::regex pattern(
        "\"" + field +
        "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
    );

    std::smatch match;

    if (!std::regex_search(
            object, match, pattern)) {
        throw std::runtime_error(
            "Campo JSON ausente: " + field
        );
    }

    std::string value = match[1].str();

    std::string decoded;
    decoded.reserve(value.size());

    for (size_t i = 0; i < value.size(); ++i) {
        if (value[i] == '\\' &&
            i + 1 < value.size()) {
            ++i;

            switch (value[i]) {
                case '"':
                    decoded += '"';
                    break;
                case '\\':
                    decoded += '\\';
                    break;
                case '/':
                    decoded += '/';
                    break;
                case 'n':
                    decoded += '\n';
                    break;
                case 'r':
                    decoded += '\r';
                    break;
                case 't':
                    decoded += '\t';
                    break;
                default:
                    decoded += value[i];
                    break;
            }
        } else {
            decoded += value[i];
        }
    }

    return decoded;
}

static std::vector<std::string> jsonObjects(
    const std::string& json,
    const std::string& arrayName
) {
    const std::regex arrayPattern(
        "\"" + arrayName +
        "\"\\s*:\\s*\\["
    );

    std::smatch match;

    if (!std::regex_search(
            json, match, arrayPattern)) {
        throw std::runtime_error(
            "Lista JSON ausente: " + arrayName
        );
    }

    size_t pos =
        static_cast<size_t>(match.position() +
                            match.length());

    std::vector<std::string> objects;

    while (pos < json.size()) {
        while (pos < json.size() &&
               (json[pos] == ' ' ||
                json[pos] == '\n' ||
                json[pos] == '\r' ||
                json[pos] == '\t' ||
                json[pos] == ',')) {
            ++pos;
        }

        if (pos >= json.size() ||
            json[pos] == ']') {
            break;
        }

        if (json[pos] != '{') {
            throw std::runtime_error(
                "Estrutura JSON inesperada."
            );
        }

        const size_t start = pos;
        int depth = 0;
        bool inString = false;
        bool escaped = false;

        for (; pos < json.size(); ++pos) {
            const char c = json[pos];

            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }

                continue;
            }

            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                ++depth;
            } else if (c == '}') {
                --depth;

                if (depth == 0) {
                    ++pos;
                    break;
                }
            }
        }

        if (depth != 0) {
            throw std::runtime_error(
                "Objeto JSON incompleto."
            );
        }

        objects.push_back(
            json.substr(start, pos - start)
        );
    }

    return objects;
}

static std::string apiUrl(
    const std::string& repository,
    const std::string& path
) {
    return "https://api.github.com/repos/" +
           repository + path;
}

static bool validArtifactName(
    const std::string& name
) {
    if (name.empty() ||
        name == "." ||
        name == "..") {
        return false;
    }

    return std::all_of(
        name.begin(),
        name.end(),
        [](unsigned char c) {
            return (c >= 'a' && c <= 'z') ||
                   (c >= 'A' && c <= 'Z') ||
                   (c >= '0' && c <= '9') ||
                   c == '-' || c == '_' || c == '.';
        }
    );
}

static fs::path safeDestination(
    const fs::path& root,
    const std::string& relative
) {
    const fs::path target(relative);

    if (target.empty() || target.is_absolute()) {
        throw std::runtime_error(
            "Destino inválido no mapeamento."
        );
    }

    for (const auto& part : target) {
        if (part == "..") {
            throw std::runtime_error(
                "O destino não pode sair do repositório."
            );
        }
    }

    const fs::path normalized =
        (root / target).lexically_normal();

    const fs::path relativeCheck =
        normalized.lexically_relative(root);

    if (relativeCheck.empty() ||
        relativeCheck.is_absolute()) {
        throw std::runtime_error(
            "Destino fora do repositório."
        );
    }

    for (const auto& part : relativeCheck) {
        if (part == "..") {
            throw std::runtime_error(
                "Destino fora do repositório."
            );
        }
    }

    return normalized;
}

static void decodeWorkflowBase64(
    std::string input,
    const fs::path& destination
) {
    input.erase(
        std::remove_if(
            input.begin(),
            input.end(),
            [](unsigned char c) {
                return c == '\n' ||
                       c == '\r' ||
                       c == ' ' ||
                       c == '\t';
            }
        ),
        input.end()
    );

    if (input.size() >
        static_cast<size_t>(INT_MAX)) {
        throw std::runtime_error(
            "Workflow grande demais."
        );
    }

    while (input.size() % 4) {
        input.push_back('=');
    }

    std::string output(
        input.size() / 4 * 3, '\0'
    );

    const int length = EVP_DecodeBlock(
        reinterpret_cast<unsigned char*>(
            output.data()
        ),
        reinterpret_cast<const unsigned char*>(
            input.data()
        ),
        static_cast<int>(input.size())
    );

    if (length < 0) {
        throw std::runtime_error(
            "Base64 do workflow inválido."
        );
    }

    int actualLength = length;

    if (!input.empty() &&
        input.back() == '=') {
        --actualLength;
    }

    if (input.size() > 1 &&
        input[input.size() - 2] == '=') {
        --actualLength;
    }

    if (actualLength < 0) {
        throw std::runtime_error(
            "Tamanho Base64 inválido."
        );
    }

    output.resize(
        static_cast<size_t>(actualLength)
    );

    fs::create_directories(
        destination.parent_path()
    );

    std::ofstream file(
        destination, std::ios::binary
    );

    if (!file) {
        throw std::runtime_error(
            "Não foi possível salvar o workflow."
        );
    }

    file.write(
        output.data(),
        static_cast<std::streamsize>(
            output.size()
        )
    );

    if (!file) {
        throw std::runtime_error(
            "Falha ao gravar o workflow."
        );
    }
}

int main() {
    bool curlInitialized = false;

    try {
        if (curl_global_init(
                CURL_GLOBAL_DEFAULT) != CURLE_OK) {
            throw std::runtime_error(
                "Falha ao inicializar libcurl."
            );
        }

        curlInitialized = true;

        const std::string token = env("GH_TOKEN");
        const std::string repository =
            env("GITHUB_REPOSITORY");
        const std::string runId =
            env("GITHUB_RUN_ID");
        const fs::path root =
            fs::canonical(env("GITHUB_WORKSPACE"));

        std::cout
            << "Mulher Amparada Bot iniciado.\n";

        std::cout
            << "Consultando artifacts da execução "
            << runId << "...\n";

        Response artifactsResponse = request(
            apiUrl(
                repository,
                "/actions/runs/" + runId +
                "/artifacts?per_page=100"
            ),
            token
        );

        require2xx(
            artifactsResponse,
            "Consulta de artifacts"
        );

        const auto objects = jsonObjects(
            artifactsResponse.body,
            "artifacts"
        );

        const fs::path temp =
            fs::temp_directory_path() /
            ("mulher-amparada-" + runId);

        const fs::path artifactsDir =
            temp / "artifacts";

        fs::create_directories(artifactsDir);

        std::vector<std::string> artifactNames;

        for (const auto& object : objects) {
            const std::string name =
                jsonStringField(object, "name");

            const std::string url =
                jsonStringField(
                    object,
                    "archive_download_url"
                );

            if (!validArtifactName(name)) {
                throw std::runtime_error(
                    "Nome de artifact inválido."
                );
            }

            if (url.rfind(
                    "https://api.github.com/", 0) != 0) {
                throw std::runtime_error(
                    "URL de artifact inesperada."
                );
            }

            std::cout
                << "Baixando artifact: "
                << name << '\n';

            Response archive = request(
                url,
                token,
                "GET",
                "",
                "application/vnd.github+json"
            );

            require2xx(
                archive,
                "Download do artifact " + name
            );

            if (archive.body.empty()) {
                throw std::runtime_error(
                    "Artifact vazio: " + name
                );
            }

            const fs::path zip =
                artifactsDir / (name + ".zip");

            {
                std::ofstream file(
                    zip, std::ios::binary
                );

                if (!file) {
                    throw std::runtime_error(
                        "Não foi possível criar o ZIP."
                    );
                }

                file.write(
                    archive.body.data(),
                    static_cast<std::streamsize>(
                        archive.body.size()
                    )
                );

                if (!file) {
                    throw std::runtime_error(
                        "Falha ao gravar o ZIP."
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

            artifactNames.push_back(name);
        }

        if (artifactNames.empty()) {
            std::cout
                << "Nenhum artifact encontrado.\n";
        }

        const char* mapEnv =
            std::getenv(
                "MULHER_AMPARADA_ARTIFACT_MAP"
            );

        std::vector<fs::path> changedTargets;

        if (mapEnv && *mapEnv) {
            const std::string mappings(mapEnv);
            size_t start = 0;

            while (start < mappings.size()) {
                const size_t end =
                    mappings.find(';', start);

                const std::string entry =
                    mappings.substr(
                        start,
                        end == std::string::npos
                            ? std::string::npos
                            : end - start
                    );

                const size_t colon =
                    entry.find(':');

                if (colon == std::string::npos) {
                    throw std::runtime_error(
                        "Mapeamento inválido: "
                        "use artifact:destino."
                    );
                }

                const std::string name =
                    entry.substr(0, colon);

                const std::string target =
                    entry.substr(colon + 1);

                if (!validArtifactName(name)) {
                    throw std::runtime_error(
                        "Nome inválido no mapeamento."
                    );
                }

                if (target.empty()) {
                    throw std::runtime_error(
                        "Destino vazio no mapeamento."
                    );
                }

                const fs::path source =
                    artifactsDir / name;

                const fs::path destination =
                    safeDestination(root, target);

                if (!fs::exists(source) ||
                    !fs::is_directory(source)) {
                    std::cerr
                        << "Artifact não encontrado: "
                        << name << '\n';
                } else {
                    fs::create_directories(
                        destination
                    );

                    for (
                        const auto& item :
                        fs::recursive_directory_iterator(
                            source
                        )
                    ) {
                        if (item.is_symlink()) {
                            throw std::runtime_error(
                                "Symlink não permitido "
                                "no artifact."
                            );
                        }

                        if (!item.is_regular_file()) {
                            continue;
                        }

                        const fs::path relative =
                            fs::relative(
                                item.path(), source
                            );

                        const fs::path output =
                            safeDestination(
                                destination,
                                relative.string()
                            );

                        fs::create_directories(
                            output.parent_path()
                        );

                        fs::copy_file(
                            item.path(),
                            output,
                            fs::copy_options::
                                overwrite_existing
                        );

                        changedTargets.push_back(
                            output
                        );
                    }
                }

                if (end == std::string::npos) {
                    break;
                }

                start = end + 1;
            }
        }

        const fs::path workflowPath =
            root /
            ".github/workflows/automations.yml";

        if (!fs::exists(workflowPath)) {
            std::cerr
                << "Aviso: workflow não encontrado "
                << "no checkout.\n";
        }

        const fs::path askpass =
            temp / "git-askpass.sh";

        fs::create_directories(temp);

        {
            std::ofstream script(askpass);

            if (!script) {
                throw std::runtime_error(
                    "Não foi possível criar Git AskPass."
                );
            }

            script
                << "#!/bin/sh\n"
                << "case \"$1\" in\n"
                << "  *Username*) "
                << "printf '%s\\n' 'x-access-token' ;;\n"
                << "  *Password*) "
                << "printf '%s\\n' \"$GH_TOKEN\" ;;\n"
                << "  *) exit 1 ;;\n"
                << "esac\n";
        }

        if (chmod(askpass.c_str(), 0700) != 0) {
            throw std::runtime_error(
                "Não foi possível proteger Git AskPass."
            );
        }

        setenv(
            "GIT_ASKPASS",
            askpass.c_str(),
            1
        );

        setenv(
            "GIT_TERMINAL_PROMPT",
            "0",
            1
        );

        run(
            "git -C " +
            shellQuote(root.string()) +
            " config user.name " +
            shellQuote("mulher-amparada-bot[bot]")
        );

        run(
            "git -C " +
            shellQuote(root.string()) +
            " config user.email " +
            shellQuote(
                "mulher-amparada-bot[bot]"
                "@users.noreply.github.com"
            )
        );

        for (const auto& target : changedTargets) {
            const fs::path relative =
                target.lexically_relative(root);

            if (relative.empty() ||
                relative.is_absolute()) {
                throw std::runtime_error(
                    "Arquivo fora do repositório."
                );
            }

            run(
                "git -C " +
                shellQuote(root.string()) +
                " add -- " +
                shellQuote(relative.generic_string())
            );
        }

        const std::string git =
            "git -C " + shellQuote(root.string());

        const int staged = std::system(
            (git + " diff --cached --quiet").c_str()
        );

        if (staged == -1 ||
            (!WIFEXITED(staged)) ||
            (WEXITSTATUS(staged) != 0 &&
             WEXITSTATUS(staged) != 1)) {
            throw std::runtime_error(
                "Falha ao verificar alterações staged."
            );
        }

        if (WEXITSTATUS(staged) == 0) {
            std::cout
                << "Nenhuma alteração para publicar.\n";

            curl_global_cleanup();
            return 0;
        }

        run(
            git + " commit -m " +
            shellQuote(
                "Atualização automática pelo "
                "mulher-amparada-bot [skip ci]"
            )
        );

        run(git + " fetch origin main");

        run(git + " rebase origin/main");

        run(git + " push origin HEAD:main");

        std::cout
            << "Publicação na main concluída.\n";

        curl_global_cleanup();
        return 0;

    } catch (const std::exception& error) {
        std::cerr
            << "ERRO: " << error.what() << '\n';

        if (curlInitialized) {
            curl_global_cleanup();
        }

        return 1;
    }
}