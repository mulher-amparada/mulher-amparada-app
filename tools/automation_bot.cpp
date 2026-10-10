#include <curl/curl.h>
#include <openssl/evp.h>
#include <openssl/pem.h>
#include <openssl/rsa.h>
#include <openssl/bio.h>
#include <openssl/buffer.h>

#include <chrono>
#include <cstdlib>
#include <ctime>
#include <iostream>
#include <sstream>
#include <string>

static std::string base64Url(const unsigned char* data, size_t len) {
    BIO* b64 = BIO_new(BIO_f_base64());
    BIO* bio = BIO_new(BIO_s_mem());

    BIO_set_flags(b64, BIO_FLAGS_BASE64_NO_NL);
    b64 = BIO_push(b64, bio);

    BIO_write(b64, data, static_cast<int>(len));
    BIO_flush(b64);

    BUF_MEM* buffer = nullptr;
    BIO_get_mem_ptr(b64, &buffer);

    std::string result(buffer->data, buffer->length);

    BIO_free_all(b64);

    for (char& c : result) {
        if (c == '+') c = '-';
        else if (c == '/') c = '_';
    }

    while (!result.empty() && result.back() == '=') {
        result.pop_back();
    }

    return result;
}

static std::string base64Decode(const std::string& input) {
    std::string data = input;

    while (data.size() % 4 != 0) {
        data += '=';
    }

    for (char& c : data) {
        if (c == '-') c = '+';
        else if (c == '_') c = '/';
    }

    BIO* b64 = BIO_new(BIO_f_base64());
    BIO* bio = BIO_new_mem_buf(
        data.data(),
        static_cast<int>(data.size())
    );

    BIO_set_flags(b64, BIO_FLAGS_BASE64_NO_NL);
    bio = BIO_push(b64, bio);

    std::string output(data.size(), '\0');

    int length = BIO_read(
        bio,
        output.data(),
        static_cast<int>(output.size())
    );

    BIO_free_all(bio);

    if (length <= 0) {
        return {};
    }

    output.resize(length);
    return output;
}

static std::string jsonEscape(const std::string& value) {
    std::string result;

    for (char c : value) {
        switch (c) {
            case '"':
                result += "\\\"";
                break;

            case '\\':
                result += "\\\\";
                break;

            case '\n':
                result += "\\n";
                break;

            case '\r':
                result += "\\r";
                break;

            case '\t':
                result += "\\t";
                break;

            default:
                result += c;
        }
    }

    return result;
}

static size_t writeCallback(
    void* contents,
    size_t size,
    size_t nmemb,
    void* userData
) {
    size_t total = size * nmemb;

    std::string* output =
        static_cast<std::string*>(userData);

    output->append(
        static_cast<char*>(contents),
        total
    );

    return total;
}

static std::string httpRequest(
    const std::string& url,
    const std::string& method,
    const std::string& body,
    const std::string& authorization
) {
    CURL* curl = curl_easy_init();

    if (!curl) {
        return {};
    }

    std::string response;

    struct curl_slist* headers = nullptr;

    headers = curl_slist_append(
        headers,
        "Accept: application/vnd.github+json"
    );

    headers = curl_slist_append(
        headers,
        "User-Agent: mulher-amparada-bot"
    );

    headers = curl_slist_append(
        headers,
        "X-GitHub-Api-Version: 2022-11-28"
    );

    if (!authorization.empty()) {
        headers = curl_slist_append(
            headers,
            authorization.c_str()
        );
    }

    if (method == "POST") {
        headers = curl_slist_append(
            headers,
            "Content-Type: application/json"
        );

        curl_easy_setopt(
            curl,
            CURLOPT_POST,
            1L
        );

        curl_easy_setopt(
            curl,
            CURLOPT_POSTFIELDS,
            body.c_str()
        );
    }

    curl_easy_setopt(
        curl,
        CURLOPT_URL,
        url.c_str()
    );

    curl_easy_setopt(
        curl,
        CURLOPT_HTTPHEADER,
        headers
    );

    curl_easy_setopt(
        curl,
        CURLOPT_WRITEFUNCTION,
        writeCallback
    );

    curl_easy_setopt(
        curl,
        CURLOPT_WRITEDATA,
        &response
    );

    curl_easy_setopt(
        curl,
        CURLOPT_USERAGENT,
        "mulher-amparada-bot"
    );

    CURLcode result = curl_easy_perform(curl);

    if (result != CURLE_OK) {
        std::cerr
            << "Erro CURL: "
            << curl_easy_strerror(result)
            << '\n';
    }

    long status = 0;

    curl_easy_getinfo(
        curl,
        CURLINFO_RESPONSE_CODE,
        &status
    );

    std::cout
        << "HTTP "
        << status
        << '\n';

    if (!response.empty()) {
        std::cout
            << response
            << '\n';
    }

    curl_slist_free_all(headers);
    curl_easy_cleanup(curl);

    return response;
}

static std::string extractToken(
    const std::string& json
) {
    const std::string key = "\"token\":\"";

    size_t start = json.find(key);

    if (start == std::string::npos) {
        return {};
    }

    start += key.length();

    size_t end = json.find(
        '"',
        start
    );

    if (end == std::string::npos) {
        return {};
    }

    return json.substr(
        start,
        end - start
    );
}

static std::string createJwt(
    const std::string& appId,
    const std::string& privateKey
) {
    BIO* bio = BIO_new_mem_buf(
        privateKey.data(),
        static_cast<int>(privateKey.size())
    );

    if (!bio) {
        return {};
    }

    EVP_PKEY* privateKeyObject =
        PEM_read_bio_PrivateKey(
            bio,
            nullptr,
            nullptr,
            nullptr
        );

    BIO_free(bio);

    if (!privateKeyObject) {
        return {};
    }

    const long now =
        static_cast<long>(std::time(nullptr));

    const long issuedAt = now - 60;
    const long expiration = now + 540;

    std::string header =
        "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";

    std::string payload =
        "{\"iat\":" +
        std::to_string(issuedAt) +
        ",\"exp\":" +
        std::to_string(expiration) +
        ",\"iss\":\"" +
        jsonEscape(appId) +
        "\"}";

    std::string encodedHeader =
        base64Url(
            reinterpret_cast<const unsigned char*>(
                header.data()
            ),
            header.size()
        );

    std::string encodedPayload =
        base64Url(
            reinterpret_cast<const unsigned char*>(
                payload.data()
            ),
            payload.size()
        );

    std::string message =
        encodedHeader +
        "." +
        encodedPayload;

    EVP_MD_CTX* context =
        EVP_MD_CTX_new();

    if (!context) {
        EVP_PKEY_free(privateKeyObject);
        return {};
    }

    if (EVP_DigestSignInit(
            context,
            nullptr,
            EVP_sha256(),
            nullptr,
            privateKeyObject
        ) != 1) {
        EVP_MD_CTX_free(context);
        EVP_PKEY_free(privateKeyObject);
        return {};
    }

    if (EVP_DigestSignUpdate(
            context,
            message.data(),
            message.size()
        ) != 1) {
        EVP_MD_CTX_free(context);
        EVP_PKEY_free(privateKeyObject);
        return {};
    }

    size_t signatureLength = 0;

    if (EVP_DigestSignFinal(
            context,
            nullptr,
            &signatureLength
        ) != 1) {
        EVP_MD_CTX_free(context);
        EVP_PKEY_free(privateKeyObject);
        return {};
    }

    std::string signature(
        signatureLength,
        '\0'
    );

    if (EVP_DigestSignFinal(
            context,
            reinterpret_cast<unsigned char*>(
                signature.data()
            ),
            &signatureLength
        ) != 1) {
        EVP_MD_CTX_free(context);
        EVP_PKEY_free(privateKeyObject);
        return {};
    }

    signature.resize(signatureLength);

    EVP_MD_CTX_free(context);
    EVP_PKEY_free(privateKeyObject);

    return message +
           "." +
           base64Url(
               reinterpret_cast<const unsigned char*>(
                   signature.data()
               ),
               signature.size()
           );
}

int main() {
    curl_global_init(CURL_GLOBAL_DEFAULT);

    const char* appIdEnv =
        std::getenv(
            "MULHER_AMPARADA_APP_ID"
        );

    const char* installationIdEnv =
        std::getenv(
            "MULHER_AMPARADA_INSTALLATION_ID"
        );

    const char* privateKeyEnv =
        std::getenv(
            "MULHER_AMPARADA_PRIVATE_KEY"
        );

    const char* repositoryEnv =
        std::getenv(
            "GITHUB_REPOSITORY"
        );

    if (!appIdEnv ||
        !installationIdEnv ||
        !privateKeyEnv ||
        !repositoryEnv) {

        std::cerr
            << "❌ Variáveis de ambiente ausentes."
            << '\n';

        curl_global_cleanup();

        return 1;
    }

    const std::string appId =
        appIdEnv;

    const std::string installationId =
        installationIdEnv;

    const std::string repository =
        repositoryEnv;

    std::cout
        << "🤖 Mulher Amparada Bot"
        << '\n';

    std::cout
        << "App ID: "
        << appId
        << '\n';

    std::cout
        << "Installation ID: "
        << installationId
        << '\n';

    std::string privateKey =
        base64Decode(
            privateKeyEnv
        );

    if (privateKey.empty()) {
        std::cerr
            << "❌ Não foi possível decodificar a Private Key."
            << '\n';

        curl_global_cleanup();

        return 1;
    }

    std::string jwt =
        createJwt(
            appId,
            privateKey
        );

    if (jwt.empty()) {
        std::cerr
            << "❌ Não foi possível gerar o JWT."
            << '\n';

        curl_global_cleanup();

        return 1;
    }

    std::cout
        << "✅ JWT gerado."
        << '\n';

    const std::string tokenUrl =
        "https://api.github.com/app/installations/" +
        installationId +
        "/access_tokens";

    std::string tokenResponse =
        httpRequest(
            tokenUrl,
            "POST",
            "",
            "Authorization: Bearer " + jwt
        );

    std::string token =
        extractToken(
            tokenResponse
        );

    if (token.empty()) {
        std::cerr
            << "❌ Não foi possível obter o Installation Access Token."
            << '\n';

        curl_global_cleanup();

        return 1;
    }

    std::cout
        << "✅ Installation Access Token obtido."
        << '\n';

    const std::string dispatchUrl =
        "https://api.github.com/repos/" +
        repository +
        "/actions/workflows/automations.yml/dispatches";

    const std::string body =
        "{\"ref\":\"main\"}";

    std::string dispatchResponse =
        httpRequest(
            dispatchUrl,
            "POST",
            body,
            "Authorization: Bearer " + token
        );

    std::cout
        << "🚀 Workflow automations.yml acionado pelo Mulher Amparada Bot."
        << '\n';

    curl_global_cleanup();

    return 0;
}