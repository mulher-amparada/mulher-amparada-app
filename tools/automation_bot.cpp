#include <curl/curl.h>

#include <cstdlib>
#include <iostream>
#include <string>

int main() {
    const char* token = std::getenv("GH_TOKEN");
    const char* repo = std::getenv("GITHUB_REPOSITORY");

    if (!token || !repo) {
        std::cerr
            << "ERRO: GH_TOKEN ou GITHUB_REPOSITORY não definido.\n";

        return 1;
    }

    std::string url =
        "https://api.github.com/repos/" +
        std::string(repo) +
        "/actions/workflows/automations.yml/dispatches";

    CURL* curl = curl_easy_init();

    if (!curl) {
        std::cerr
            << "ERRO: não foi possível iniciar libcurl.\n";

        return 1;
    }

    struct curl_slist* headers = nullptr;

    headers = curl_slist_append(
        headers,
        "Accept: application/vnd.github+json"
    );

    headers = curl_slist_append(
        headers,
        "X-GitHub-Api-Version: 2022-11-28"
    );

    headers = curl_slist_append(
        headers,
        "User-Agent: mulher-amparada-automation-bot"
    );

    std::string autorizacao =
        "Authorization: Bearer " +
        std::string(token);

    headers = curl_slist_append(
        headers,
        autorizacao.c_str()
    );

    headers = curl_slist_append(
        headers,
        "Content-Type: application/json"
    );

    const std::string body =
        R"({"ref":"main"})";

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
        CURLOPT_CUSTOMREQUEST,
        "POST"
    );

    curl_easy_setopt(
        curl,
        CURLOPT_POSTFIELDS,
        body.c_str()
    );

    CURLcode resultado =
        curl_easy_perform(curl);

    long status = 0;

    curl_easy_getinfo(
        curl,
        CURLINFO_RESPONSE_CODE,
        &status
    );

    curl_slist_free_all(headers);
    curl_easy_cleanup(curl);

    if (resultado != CURLE_OK) {
        std::cerr
            << "ERRO na comunicação com GitHub: "
            << curl_easy_strerror(resultado)
            << "\n";

        return 1;
    }

    if (status < 200 || status >= 300) {
        std::cerr
            << "GitHub API retornou HTTP "
            << status
            << ".\n";

        return 1;
    }

    std::cout
        << "========================================\n"
        << "BOT DE AUTOMAÇÕES\n"
        << "========================================\n\n"
        << "Repositório: "
        << repo
        << "\n"
        << "Workflow: automations.yml\n"
        << "Branch: main\n"
        << "Status: disparado com sucesso\n";

    return 0;
}