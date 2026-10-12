#include <curl/curl.h>
#include <openssl/evp.h>
#include <openssl/pem.h>
#include <algorithm>
#include <cstdlib>
#include <ctime>
#include <iostream>
#include <stdexcept>
#include <string>

static size_t writeCallback(char* data, size_t size, size_t count, void* user) {
    auto* out = static_cast<std::string*>(user);
    size_t bytes = size * count;
    out->append(data, bytes);
    return bytes;
}

static std::string env(const char* name) {
    const char* v = std::getenv(name);
    if (!v || !*v) throw std::runtime_error(std::string("Variável ausente: ") + name);
    return v;
}

static std::string b64url(const std::string& in) {
    std::string out(4 * ((in.size() + 2) / 3), '\0');
    int n = EVP_EncodeBlock(reinterpret_cast<unsigned char*>(out.data()),
                            reinterpret_cast<const unsigned char*>(in.data()),
                            static_cast<int>(in.size()));
    if (n < 0) throw std::runtime_error("Falha no Base64.");
    out.resize(static_cast<size_t>(n));
    std::replace(out.begin(), out.end(), '+', '-');
    std::replace(out.begin(), out.end(), '/', '_');
    while (!out.empty() && out.back() == '=') out.pop_back();
    return out;
}

static std::string makeJwt(const std::string& appId, const std::string& pem) {
    const long long now = std::time(nullptr);
    const std::string header = R"({"alg":"RS256","typ":"JWT"})";
    const std::string payload = "{\"iat\":" + std::to_string(now - 60) +
        ",\"exp\":" + std::to_string(now + 540) +
        ",\"iss\":\"" + appId + "\"}";
    const std::string msg = b64url(header) + "." + b64url(payload);

    BIO* bio = BIO_new_mem_buf(pem.data(), static_cast<int>(pem.size()));
    if (!bio) throw std::runtime_error("Falha ao abrir chave PEM.");
    EVP_PKEY* key = PEM_read_bio_PrivateKey(bio, nullptr, nullptr, nullptr);
    BIO_free(bio);
    if (!key) throw std::runtime_error("Chave privada inválida. Use o conteúdo PEM original.");

    EVP_MD_CTX* ctx = EVP_MD_CTX_new();
    size_t sigLen = 0;
    bool ok = ctx &&
        EVP_DigestSignInit(ctx, nullptr, EVP_sha256(), nullptr, key) == 1 &&
        EVP_DigestSignUpdate(ctx, msg.data(), msg.size()) == 1 &&
        EVP_DigestSignFinal(ctx, nullptr, &sigLen) == 1;
    std::string sig;
    if (ok) {
        sig.resize(sigLen);
        ok = EVP_DigestSignFinal(ctx,
            reinterpret_cast<unsigned char*>(sig.data()), &sigLen) == 1;
        if (ok) sig.resize(sigLen);
    }
    if (ctx) EVP_MD_CTX_free(ctx);
    EVP_PKEY_free(key);
    if (!ok) throw std::runtime_error("Falha ao assinar JWT.");
    return msg + "." + b64url(sig);
}

static std::string request(const std::string& url, const std::string& auth,
                           const std::string& method, const std::string& body) {
    CURL* curl = curl_easy_init();
    if (!curl) throw std::runtime_error("Falha ao iniciar curl.");
    std::string response;
    curl_slist* headers = nullptr;
    headers = curl_slist_append(headers, ("Authorization: " + auth).c_str());
    headers = curl_slist_append(headers, "Accept: application/vnd.github+json");
    headers = curl_slist_append(headers, "X-GitHub-Api-Version: 2022-11-28");
    headers = curl_slist_append(headers, "User-Agent: mulher-amparada-agent");
    headers = curl_slist_append(headers, "Content-Type: application/json");

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
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, body.c_str());
        curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, static_cast<long>(body.size()));
    }
    CURLcode rc = curl_easy_perform(curl);
    long status = 0;
    if (rc == CURLE_OK) curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &status);
    curl_slist_free_all(headers);
    curl_easy_cleanup(curl);
    if (rc != CURLE_OK) throw std::runtime_error(curl_easy_strerror(rc));
    if (status < 200 || status >= 300)
        throw std::runtime_error("GitHub API HTTP " + std::to_string(status) + ": " + response);
    return response;
}

static std::string jsonString(const std::string& json, const std::string& key) {
    const std::string needle = "\"" + key + "\"";
    size_t p = json.find(needle);
    if (p == std::string::npos) throw std::runtime_error("Campo ausente na resposta: " + key);
    p = json.find(':', p + needle.size());
    if (p == std::string::npos) throw std::runtime_error("JSON inválido.");
    p = json.find('"', p + 1);
    if (p == std::string::npos) throw std::runtime_error("JSON inválido.");
    std::string out;
    for (size_t i = p + 1; i < json.size(); ++i) {
        if (json[i] == '\\' && i + 1 < json.size()) {
            char c = json[++i];
            if (c == '"' || c == '\\' || c == '/') out.push_back(c);
            else if (c == 'n') out.push_back('\n');
            else if (c == 'r') out.push_back('\r');
            else if (c == 't') out.push_back('\t');
            else { out.push_back('\\'); out.push_back(c); }
        } else if (json[i] == '"') return out;
        else out.push_back(json[i]);
    }
    throw std::runtime_error("JSON incompleto.");
}

static std::string urlEncode(const std::string& value) {
    CURL* curl = curl_easy_init();
    if (!curl) throw std::runtime_error("Falha no curl.");
    char* p = curl_easy_escape(curl, value.c_str(), static_cast<int>(value.size()));
    if (!p) { curl_easy_cleanup(curl); throw std::runtime_error("Falha ao codificar URL."); }
    std::string out(p);
    curl_free(p);
    curl_easy_cleanup(curl);
    return out;
}

int main(int argc, char** argv) {
    bool curlReady = false;
    try {
        if (argc < 2) {
            std::cerr << "Uso:\n"
                      << "  github_agent workflow <arquivo.yml> <branch>\n"
                      << "  github_agent dependabot <branch>\n";
            return 2;
        }
        if (curl_global_init(CURL_GLOBAL_DEFAULT) != CURLE_OK)
            throw std::runtime_error("Falha ao iniciar libcurl.");
        curlReady = true;

        const std::string appId = env("MULHER_AMPARADA_APP_ID");
        const std::string installationId = env("MULHER_AMPARADA_INSTALLATION_ID");
        const std::string privateKey = env("MULHER_AMPARADA_PRIVATE_KEY");
        const std::string repository = env("GITHUB_REPOSITORY");
        if (repository.find('/') == std::string::npos ||
            repository.find("..") != std::string::npos)
            throw std::runtime_error("GITHUB_REPOSITORY inválido.");

        const std::string jwt = makeJwt(appId, privateKey);
        const std::string tokenResponse = request(
            "https://api.github.com/app/installations/" + installationId + "/access_tokens",
            "Bearer " + jwt, "POST", "{}");
        const std::string token = jsonString(tokenResponse, "token");
        const std::string api = "https://api.github.com/repos/" + repository;

        const std::string command = argv[1];
        if (command == "workflow") {
            if (argc != 4) throw std::runtime_error("Informe arquivo YAML e branch.");
            const std::string workflow = argv[2];
            const std::string branch = argv[3];
            if (workflow.empty() || workflow.find('/') != std::string::npos ||
                workflow.find('\\') != std::string::npos ||
                workflow.find("..") != std::string::npos)
                throw std::runtime_error("Informe apenas o nome do workflow, por exemplo automations.yml.");
            if (branch.empty() || branch.find('"') != std::string::npos ||
                branch.find('\\') != std::string::npos)
                throw std::runtime_error("Branch inválida.");
            request(api + "/actions/workflows/" + urlEncode(workflow) + "/dispatches",
                    "Bearer " + token, "POST",
                    "{\"ref\":\"" + branch + "\"}");
            std::cout << "Workflow solicitado: " << workflow << " (branch " << branch << ").\n";
        } else if (command == "dependabot") {
            if (argc != 3) throw std::runtime_error("Informe a branch.");
            const std::string branch = argv[2];
            if (branch.empty() || branch.find('"') != std::string::npos ||
                branch.find('\\') != std::string::npos)
                throw std::runtime_error("Branch inválida.");
            request(api + "/actions/workflows/dependabot-maintenance.yml/dispatches",
                    "Bearer " + token, "POST",
                    "{\"ref\":\"" + branch + "\",\"inputs\":{\"action\":\"rebase\"}}");
            std::cout << "Solicitação de manutenção do Dependabot enviada.\n";
        } else {
            throw std::runtime_error("Comando desconhecido.");
        }
        curl_global_cleanup();
        return 0;
    } catch (const std::exception& e) {
        std::cerr << "Erro: " << e.what() << '\n';
        if (curlReady) curl_global_cleanup();
        return 1;
    }
}
