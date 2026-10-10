#include <algorithm>
#include <cstdio>
#include <cstdlib>
#include <ctime>
#include <fstream>
#include <iostream>
#include <regex>
#include <sstream>
#include <string>
#include <vector>

struct Commit {
    std::time_t timestamp;
};

std::string executarGit(const std::string& comando) {
    FILE* pipe = popen(comando.c_str(), "r");

    if (!pipe) {
        std::cerr << "Erro ao executar Git.\n";
        std::exit(1);
    }

    char buffer[4096];
    std::string resultado;

    while (fgets(buffer, sizeof(buffer), pipe)) {
        resultado += buffer;
    }

    pclose(pipe);
    return resultado;
}

std::time_t converterTimestamp(const std::string& valor) {
    return static_cast<std::time_t>(
        std::stoll(valor)
    );
}

int main() {

    /*
     * Busca todos os commits da história.
     *
     * %at = timestamp Unix do commit
     */
    std::string saida = executarGit(
        "git log --all --format=%at --reverse"
    );

    std::istringstream stream(saida);

    std::vector<Commit> commits;

    std::string linha;

    while (std::getline(stream, linha)) {

        if (linha.empty())
            continue;

        try {
            commits.push_back({
                converterTimestamp(linha)
            });
        }
        catch (...) {
            continue;
        }
    }

    if (commits.empty()) {
        std::cerr << "Nenhum commit encontrado.\n";
        return 1;
    }

    /*
     * Ordenar os commits por data.
     */
    std::sort(
        commits.begin(),
        commits.end(),
        [](const Commit& a, const Commit& b) {
            return a.timestamp < b.timestamp;
        }
    );

    /*
     * Calculamos o tempo entre commits consecutivos.
     *
     * Para evitar considerar grandes períodos em que
     * provavelmente não houve trabalho, cada intervalo
     * é limitado a 8 horas.
     */
    long long segundos_trabalhados = 0;

    for (std::size_t i = 1; i < commits.size(); ++i) {

        long long intervalo =
            static_cast<long long>(
                commits[i].timestamp -
                commits[i - 1].timestamp
            );

        /*
         * Ignora intervalos negativos.
         */
        if (intervalo <= 0)
            continue;

        /*
         * Limita cada intervalo a 8 horas.
         */
        const long long oito_horas =
            8LL * 60LL * 60LL;

        segundos_trabalhados +=
            std::min(intervalo, oito_horas);
    }

    /*
     * Converte para horas.
     */
    double horas =
        static_cast<double>(segundos_trabalhados) /
        3600.0;

    long long minutos =
        segundos_trabalhados / 60;

    long long horas_inteiras =
        minutos / 60;

    long long minutos_restantes =
        minutos % 60;

    std::cout
        << "Commits analisados: "
        << commits.size()
        << "\n";

    std::cout
        << "Horas estimadas: "
        << horas_inteiras
        << "h "
        << minutos_restantes
        << "min\n";

    /*
     * -------------------------------------------------------
     * ATUALIZAR README.md
     * -------------------------------------------------------
     */

    const std::string caminho =
        "README.md";

    std::ifstream arquivoEntrada(
        caminho
    );

    if (!arquivoEntrada) {
        std::cerr
            << "ERRO: README.md não encontrado.\n";

        return 1;
    }

    std::stringstream buffer;

    buffer
        << arquivoEntrada.rdbuf();

    arquivoEntrada.close();

    std::string texto =
        buffer.str();

    /*
     * Procuramos uma linha como:
     *
     * **Horas de trabalho no projeto = 123h 45min**
     *
     * e substituímos o valor.
     */

    std::regex padrao(
        R"(\*\*Horas de trabalho no projeto\s*=\s*[0-9]+h\s*[0-9]+min\s*\*\*)"
    );

    std::string novoTexto =
        "**Horas de trabalho no projeto = " +
        std::to_string(horas_inteiras) +
        "h " +
        std::to_string(minutos_restantes) +
        "min**";

    if (!std::regex_search(texto, padrao)) {

        /*
         * Se ainda não existir, adiciona no final.
         */
        if (!texto.empty() &&
            texto.back() != '\n') {
            texto += "\n";
        }

        texto += "\n";
        texto += novoTexto;
        texto += "\n";

        std::cout
            << "Campo de horas adicionado ao README.\n";
    }
    else {

        texto =
            std::regex_replace(
                texto,
                padrao,
                novoTexto,
                std::regex_constants::format_first_only
            );

        std::cout
            << "Horas atualizadas no README.\n";
    }

    /*
     * Salvar README.
     */
    std::ofstream arquivoSaida(
        caminho
    );

    if (!arquivoSaida) {
        std::cerr
            << "ERRO: não foi possível salvar README.md.\n";

        return 1;
    }

    arquivoSaida << texto;

    arquivoSaida.close();

    std::cout
        << "README.md atualizado com sucesso.\n";

    return 0;
}