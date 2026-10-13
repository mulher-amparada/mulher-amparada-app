#include <curl/curl.h>
#include <openssl/evp.h>
#include <openssl/pem.h>
#include <openssl/bio.h>
#include <openssl/err.h>

#include <algorithm>
#include <cctype>
#include <cstdio>
#include <cstdlib>
#include <ctime>
#include <fstream>
#include <iostream>
#include <iterator>
#include <stdexcept>
#include <string>
#include <vector>

static size_t writeCallback(
    char* data, size_t size, size_t count, void* user
) {
    auto* out = static_cast<std::string*>(user);
    const size_t bytes = size * count;
    out->append(data, bytes);
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

static std::string readFile(const std::string& path) {
    std::ifstream file(path, std::ios::binary);

    if (!file) {
        throw std::runtime_error(
            "Não foi possível abrir o arquivo da chave privada."
        );
    }

    return std::string(
        std::istreambuf_iterator<char>(file),
        std::istreambuf_iterator<char>()
    );
}

static std::string normalizeBase64(std::string input) {
    std::string output;

    for (unsigned char c : input) {
        if (std::isspace(c)) {
            continue;
        }

        if (c == '-' ) {
            output.push_back('+');
        } else if (c == '_') {
            output.push_back('/');
        } else {
            output.push_back(static_cast<char>(c));
        }
    }

    return output;
}

static std::string decodeBase64(const std::string& input) {
    const std::string encoded = normalizeBase64(input);

    if (encoded.empty() || encoded.size() % 4 == 1) {
        throw std::runtime_error(
            "Valor Base64 da chave privada inválido."
        );
    }

    std::string padded = encoded;

    while (padded.size() % 4 != 0) {
        padded.push_back('=');
    }

    std::vector<unsigned char> buffer(
        (padded.size() / 4) * 3 + 4
    );

    const int decodedLength = EVP_DecodeBlock(
        buffer.data(),
        reinterpret_cast<const unsigned char*>(padded.data()),
        static_cast<int>(padded.size())
    );

    if (decodedLength < 0) {
        throw std::runtime_error(
            "Não foi possível decodificar a chave Base64."
        );
    }

    int actualLength = decodedLength;

    if (!padded.empty() && padded.back() == '=') {
        --actualLength;
    }

    if (padded.size() >= 2 &&
        padded[padded.size() - 2] == '=') {
        --actualLength;
    }

    if (actualLength < 0) {
        throw std::runtime_error(
            "Comprimento Base64 inválido."
        );
    }

    return std::string(
        reinterpret_cast<const char*>(buffer.data()),
        static_cast<size_t>(actualLength)
    );
}

static std::string loadPrivateKeyText() {
    const char* filePath = std::getenv(
        "MULHER_AMPARADA_PRIVATE_KEY_FILE"
    );

    if (filePath && *filePath) {
        std::string contents = readFile(filePath);

        if (contents.find("-----BEGIN") != std::string::npos) {
            return contents;
        }
    }

    const std::string value = env(
        "MULHER_AMPARADA_PRIVATE_KEY"
    );

    if (value.find("-----BEGIN") != std::string::npos) {
        std::string pem = value;

        std::string normalized;
        normalized.reserve(pem.size());

        for (size_t i = 0; i < pem.size(); ++i) {
            if (pem[i] == '\\' &&
                i + 1 < pem.size() &&
                pem[i + 1] == 'n') {
                normalized.push_back('\n');
                ++i;
            } else {
                normalized.push_back(pem[i]);
            }
        }

        return normalized;
    }

    return decodeBase64(value);
}

static std::string b64url(const std::string& input) {
    std::string output(
        4 * ((input.size() + 2) / 3), '\0'
    );

    const int length = EVP_EncodeBlock(
        reinterpret_cast<unsigned char*>(output.data()),
        reinterpret_cast<const unsigned char*>(input.data()),
        static_cast<int>(input.size())
    );

    if (length < 0) {
        throw std::runtime_error("Falha no Base64.");
    }

    output.resize(static_cast<size_t>(length));

    std::replace(output.begin(), output.end(), '+', '-');
    std::replace(output.begin(), output.end(), '/', '_');

    while (!output.empty() && output.back() == '=') {
        output.pop_back();
    }

    return output;
}

static std::string makeJwt(
    const std::string& appId,
    const std::string& pem
) {
    const long long now = std::time(nullptr);

    const std::string header =
        R"({"alg":"RS256","typ":"JWT"})";

    const std::string payload =
        "{\"iat\":" + std::to_string(now - 60) +
        ",\"exp\":" + std::to_string(now + 540) +
        ",\"iss\":\"" + appId + "\"}";

    const std::string message =
        b64url(header) + "." + b64url(payload);

    BIO* bio = BIO_new_mem_buf(
        pem.data(),
        static_cast<int>(pem.size())
    );

    if (!bio) {
        throw std::runtime_error(
            "Falha ao abrir a chave privada."
        );
    }

    EVP_PKEY* key = PEM_read_bio_PrivateKey(
        bio, nullptr, nullptr, nullptr
    );

    BIO_free(bio);

    if (!key) {
        ERR_clear_error();

        throw std::runtime_error(
            "Chave privada inválida. Verifique o Base64 e o PEM original."
        );
    }

    EVP_MD_CTX* context = EVP_MD_CTX_new();

    if (!context) {
        EVP_PKEY_free(key);
        throw std::runtime_error(
            "Falha ao criar contexto de assinatura."
        );
    }

    size_t signatureLength = 0;

    bool ok =
        EVP_PKEY_base_id(key) == EVP_PKEY_RSA &&
        EVP_DigestSignInit(
            context, nullptr, EVP_sha256(), nullptr, key
        ) == 1 &&
        EVP_DigestSignUpdate(
            context, message.data(), message.size()
        ) == 1 &&
        EVP_DigestSignFinal(
            context, nullptr, &signatureLength
        ) == 1;

    std::string signature;

    if (ok) {
        signature.resize(signatureLength);

        ok = EVP_DigestSignFinal(
            context,
            reinterpret_cast<unsigned char*>(signature.data()),
            &signatureLength
        ) == 1;

        if (ok) {
            signature.resize(signatureLength);
        }
    }

    EVP_MD_CTX_free(context);
    EVP_PKEY_free(key);

    if (!ok) {
        throw std::runtime_error(
            "Falha ao assinar JWT RSA-SHA256."
        );
    }

    return message + "." + b64url(signature);
}

static std::string request(
    const std::string& url,
    const std::string& auth,
    const std::string& method,
    const std::string& body
) {
    CURL* curl = curl_easy_init();

    if (!curl) {
        throw std::runtime_error(
            "Falha ao iniciar curl."
        );
    }

    std::string response;
    curl_slist* headers = nullptr;

    headers = curl_slist_append(
        headers, ("Authorization: " + auth).c_str()
    );
    headers = curl_slist_append(
        headers, "Accept: application/vnd.github+json"
    );
    headers = curl_slist_append(
        headers, "X-GitHub-Api-Version: 2022-11-28"
    );
    headers = curl_slist_append(
        headers, "User-Agent: mulher-amparada-agent"
    );
    headers = curl_slist_append(
        headers, "Content-Type: application/json"
    );

    curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, writeCallback);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
    curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 1L);
    curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 2L);
    curl_easy_setopt(curl, CURLOPT_CONNECTTIMEOUT, 30L);
    curl_easy_setopt(curl, CURLOPT_TIMEOUT, 120L);

    if (method == "POST") {
        curl_easy_setopt(curl, CURLOPT_POST, 1L);
        curl_easy_setopt(
            curl, CURLOPT_POSTFIELDS, body.c_str()
        );
        curl_easy_setopt(
            curl, CURLOPT_POSTFIELDSIZE,
            static_cast<long>(body.size())
        );
    }

    const CURLcode result = curl_easy_perform(curl);

    long status = 0;

    if (result == CURLE_OK) {
        curl_easy_getinfo(
            curl, CURLINFO_RESPONSE_CODE, &status
        );
    }

    curl_slist_free_all(headers);
    curl_easy_cleanup(curl);

    if (result != CURLE_OK) {
        throw std::runtime_error(curl_easy_strerror(result));
    }

    if (status < 200 || status >= 300) {
        throw std::runtime_error(
            "GitHub API HTTP " + std::to_string(status) +
            ": " + response
        );
    }

    return response;
}

static std::string jsonString(
    const std::string& json,
    const std::string& key
) {
    const std::string needle = "\"" + key + "\"";

    size_t position = json.find(needle);

    if (position == std::string::npos) {
        throw std::runtime_error(
            "Campo ausente na resposta: " + key
        );
    }

    position = json.find(':', position + needle.size());

    if (position == std::string::npos) {
        throw std::runtime_error("JSON inválido.");
    }

    position = json.find('"', position + 1);

    if (position == std::string::npos) {
        throw std::runtime_error("JSON inválido.");
    }

    std::string output;

    for (size_t i = position + 1; i < json.size(); ++i) {
        if (json[i] == '\\' && i + 1 < json.size()) {
            const char c = json[++i];

            if (c == '"' || c == '\\' || c == '/') {
                output.push_back(c);
            } else if (c == 'n') {
                output.push_back('\n');
            } else if (c == 'r') {
                output.push_back('\r');
            } else if (c == 't') {
                output.push_back('\t');
            } else {
                output.push_back('\\');
                output.push_back(c);
            }
        } else if (json[i] == '"') {
            return output;
        } else {
            output.push_back(json[i]);
        }
    }

    throw std::runtime_error("JSON incompleto.");
}

static std::string urlEncode(const std::string& value) {
    CURL* curl = curl_easy_init();

    if (!curl) {
        throw std::runtime_error("Falha no curl.");
    }

    char* encoded = curl_easy_escape(
        curl, value.c_str(), static_cast<int>(value.size())
    );

    if (!encoded) {
        curl_easy_cleanup(curl);

        throw std::runtime_error(
            "Falha ao codificar URL."
        );
    }

    std::string output(encoded);

    curl_free(encoded);
    curl_easy_cleanup(curl);

    return output;
}

int main(int argc, char** argv) {
    bool curlReady = false;

    try {
        if (argc < 2) {
            std::cerr
                << "Uso:\n"
                << "  github_agent workflow <arquivo.yml> <branch>\n"
                << "  github_agent dependabot <branch>\n";

            return 2;
        }

        if (curl_global_init(CURL_GLOBAL_DEFAULT) != CURLE_OK) {
            throw std::runtime_error(
                "Falha ao iniciar libcurl."
            );
        }

        curlReady = true;

        const std::string appId =
            env("MULHER_AMPARADA_APP_ID");

        const std::string installationId =
            env("MULHER_AMPARADA_INSTALLATION_ID");

        const std::string privateKey =
            loadPrivateKeyText();

        const std::string repository =
            env("GITHUB_REPOSITORY");

        if (repository.find('/') == std::string::npos ||
            repository.find("..") != std::string::npos) {
            throw std::runtime_error(
                "GITHUB_REPOSITORY inválido."
            );
        }

        const std::string jwt = makeJwt(appId, privateKey);

        const std::string tokenResponse = request(
            "https://api.github.com/app/installations/" +
                installationId + "/access_tokens",
            "Bearer " + jwt,
            "POST",
            "{}"
        );

        const std::string token =
            jsonString(tokenResponse, "token");

        const std::string api =
            "https://api.github.com/repos/" + repository;

        const std::string command = argv[1];

        if (command == "workflow") {
            if (argc != 4) {
                throw std::runtime_error(
                    "Informe arquivo YAML e branch."
                );
            }

            const std::string workflow = argv[2];
            const std::string branch = argv[3];

            if (workflow.empty() ||
                workflow.find('/') != std::string::npos ||
                workflow.find('\\') != std::string::npos ||
                workflow.find("..") != std::string::npos) {
                throw std::runtime_error(
                    "Informe apenas o nome do workflow, por exemplo automations.yml."
                );
            }

            if (branch.empty() ||
                branch.find('"') != std::string::npos ||
                branch.find('\\') != std::string::npos) {
                throw std::runtime_error("Branch inválida.");
            }

            request(
                api + "/actions/workflows/" +
                    urlEncode(workflow) + "/dispatches",
                "Bearer " + token,
                "POST",
                "{\"ref\":\"" + branch + "\"}"
            );

            std::cout
                << "Workflow solicitado: " << workflow
                << " (branch " << branch << ").\n";

          } else if (command == "dependabot") {
            if (argc != 3) {
                throw std::runtime_error(
                    "Informe a branch."
                );
            }

            const std::string branch = argv[2];

            if (branch.empty() ||
                branch.find('"') != std::string::npos ||
                branch.find('\\') != std::string::npos) {
                throw std::runtime_error("Branch inválida.");
            }

            const std::string workflow =
                "dependabot-maintenance.yml";

            request(
                api + "/actions/workflows/" +
                    urlEncode(workflow) + "/dispatches",
                "Bearer " + token,
                "POST",
                "{\"ref\":\"" + branch + "\",\"inputs\":{\"action\":\"rebase\"}}"
            );

            std::cout
                << "Solicitação de manutenção do Dependabot enviada.\n";

        } else {
            throw std::runtime_error("Comando desconhecido.");
        }

        curl_global_cleanup();
        return 0;

    } catch (const std::exception& error) {
        std::cerr << "Erro: " << error.what() << '\n';

        if (curlReady) {
            curl_global_cleanup();
        }

        return 1;
    }
}