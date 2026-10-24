"use strict";

    /*
     * Caminho do JSON gerado pelo GitHub Actions.
     *
     * Estrutura esperada:
     * relatorios/linguagens.json
     *
     * O JSON deve ser produzido pelo cloc com --json.
     */

    const REPORT_URL =
  "https://raw.githubusercontent.com/mulher-amparada/mulher-amparada-app/main/relatorios/linguagens.json";

    /*
     * Cores conhecidas das linguagens no GitHub Linguist.
     * Linguagens sem uma cor oficial específica usam
     * uma cor aproximada para manter a identificação visual.
     */

    const LANGUAGE_COLORS = {
      "Kotlin": "#A97BFF",
      "Markdown": "#083FA1",
      "CSS": "#663399",
      "HTML": "#E34C26",
      "XML": "#0060AC",
      "YAML": "#CB171E",
      "Bourne Shell": "#89E051",
      "Shell": "#89E051",
      "Bash": "#89E051",
      "C": "#555555",
      "C++": "#F34B7D",
      "PHP": "#4F5D95",
      "Julia": "#A270BA",
      "Gradle": "#02303A",
      "Ruby": "#701516",
      "DOS Batch": "#C1F12E",
      "TOML": "#9C4221",
      "CSV": "#237346",
      "Python": "#3572A5",
      "JSON": "#292929",
      "Properties": "#2A6277",
      "Java": "#B07219",
      "JavaScript": "#F1E05A",
      "TypeScript": "#3178C6",
      "Dart": "#00B4AB",
      "Go": "#00ADD8",
      "Rust": "#DEA584",
      "Swift": "#F05138",
      "Kotlin Script": "#A97BFF",
      "Gradle Kotlin DSL": "#02303A",
      "Makefile": "#427819",
      "Dockerfile": "#384D54",
      "SQL": "#E38C00",
      "PowerShell": "#012456",
      "R": "#198CE7",
      "Lua": "#000080",
      "Perl": "#0298C3",
      "HCL": "#844FBA",
      "Markdown Extra": "#083FA1",
      "Text": "#777777"
    };

    const FALLBACK_COLORS = [
      "#0969DA",
      "#8250DF",
      "#1A7F37",
      "#BF3989",
      "#BC4C00",
      "#096B72",
      "#CF222E",
      "#6639BA"
    ];

    const numberFormat = new Intl.NumberFormat("pt-BR");

    const elements = {
      languageTotal: document.getElementById("language-total"),
      fileTotal: document.getElementById("file-total"),
      codeTotal: document.getElementById("code-total"),

      version: document.getElementById("cloc-version"),
      footerVersion: document.getElementById("footer-cloc-version"),

      status: document.getElementById("update-status"),
      footerUpdated: document.getElementById("footer-updated"),

      tableLanguageCount: document.getElementById(
        "table-language-count"
      ),

      rows: document.getElementById("language-rows"),
      bar: document.getElementById("distribution-bar"),
      legend: document.getElementById("legend"),

      search: document.getElementById("search"),
      refreshButton: document.getElementById("refresh-button"),

      loading: document.getElementById("loading-state"),
      error: document.getElementById("error-state"),
      empty: document.getElementById("empty-state"),

      totalFiles: document.getElementById("total-files"),
      totalBlank: document.getElementById("total-blank"),
      totalComments: document.getElementById("total-comments"),
      totalCode: document.getElementById("total-code")
    };

    let languages = [];
    let totals = {
      files: 0,
      blank: 0,
      comment: 0,
      code: 0
    };

    let requestController = null;

    function formatNumber(value) {
      return numberFormat.format(Number(value) || 0);
    }

    function escapeHTML(value) {
      return String(value).replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
      })[character]);
    }

    function getLanguageColor(name, index) {
      if (LANGUAGE_COLORS[name]) {
        return LANGUAGE_COLORS[name];
      }

      return FALLBACK_COLORS[index % FALLBACK_COLORS.length];
    }

    function getVersion(header) {
      if (!Array.isArray(header)) {
        return "cloc";
      }

      const text = header.join(" ");

      const match = text.match(/cloc\s+v?([\d.]+)/i);

      return match ? `cloc v${match[1]}` : "cloc";
    }

    /*
     * Converte a estrutura JSON do cloc para uma lista.
     *
     * Campos do cloc:
     * nFiles = arquivos
     * blank = linhas em branco
     * comment = comentários
     * code = linhas de código
     */

    function parseReport(report) {
      if (!report || typeof report !== "object") {
        throw new Error("O relatório JSON não é válido.");
      }

      const entries = Object.entries(report);

      const parsedLanguages = entries
        .filter(([name, value]) => {
          return (
            name !== "header" &&
            name !== "SUM" &&
            value !== null &&
            typeof value === "object" &&
            !Array.isArray(value) &&
            (
              "nFiles" in value ||
              "blank" in value ||
              "comment" in value ||
              "code" in value
            )
          );
        })
        .map(([name, value], index) => ({
          name,
          files: Number(value.nFiles) || 0,
          blank: Number(value.blank) || 0,
          comment: Number(value.comment) || 0,
          code: Number(value.code) || 0,
          color: getLanguageColor(name, index)
        }))
        .sort((a, b) => b.code - a.code);

      if (parsedLanguages.length === 0) {
        throw new Error(
          "Nenhuma linguagem foi encontrada no JSON do cloc."
        );
      }

      const sum = report.SUM || {};

      const computedTotals = parsedLanguages.reduce(
        (result, language) => {
          result.files += language.files;
          result.blank += language.blank;
          result.comment += language.comment;
          result.code += language.code;

          return result;
        },
        {
          files: 0,
          blank: 0,
          comment: 0,
          code: 0
        }
      );

      /*
       * Usa o SUM oficial do cloc quando disponível.
       * Se não estiver disponível, calcula os totais.
       */

      const reportTotals = {
        files: Number(sum.nFiles ?? computedTotals.files),
        blank: Number(sum.blank ?? computedTotals.blank),
        comment: Number(sum.comment ?? computedTotals.comment),
        code: Number(sum.code ?? computedTotals.code)
      };

      return {
        languages: parsedLanguages,
        totals: reportTotals,
        version: getVersion(report.header)
      };
    }

    function renderSummary() {
      elements.languageTotal.textContent = formatNumber(
        languages.length
      );

      elements.fileTotal.textContent = formatNumber(
        totals.files
      );

      elements.codeTotal.textContent = formatNumber(
        totals.code
      );

      elements.tableLanguageCount.textContent =
        `${formatNumber(languages.length)} linguagens`;

      elements.totalFiles.textContent = formatNumber(
        totals.files
      );

      elements.totalBlank.textContent = formatNumber(
        totals.blank
      );

      elements.totalComments.textContent = formatNumber(
        totals.comment
      );

      elements.totalCode.textContent = formatNumber(
        totals.code
      );
    }

    function renderTable(filter = "") {
      const query = filter
        .trim()
        .toLocaleLowerCase("pt-BR");

      const filtered = languages.filter(language =>
        language.name
          .toLocaleLowerCase("pt-BR")
          .includes(query)
      );

      elements.rows.innerHTML = filtered.map(language => `
        <tr>
          <td>
            <span class="language">
              <span
                class="dot"
                style="background:${language.color}"
                aria-hidden="true"
              ></span>
              ${escapeHTML(language.name)}
            </span>
          </td>

          <td class="number">
            ${formatNumber(language.files)}
          </td>

          <td class="number">
            ${formatNumber(language.blank)}
          </td>

          <td class="number">
            ${formatNumber(language.comment)}
          </td>

          <td class="code-number">
            ${formatNumber(language.code)}
          </td>
        </tr>
      `).join("");

      elements.empty.hidden = filtered.length > 0;
    }

    function renderDistribution() {
      const totalCode = totals.code;

      elements.bar.innerHTML = "";
      elements.legend.innerHTML = "";

      if (totalCode <= 0) {
        elements.bar.setAttribute(
          "aria-label",
          "Não há linhas de código para exibir."
        );

        return;
      }

      elements.bar.setAttribute(
        "aria-label",
        `Distribuição de ${formatNumber(totalCode)} linhas de código entre ${languages.length} linguagens.`
      );

      const barFragment = document.createDocumentFragment();

      languages
        .filter(language => language.code > 0)
        .forEach(language => {
          const percentage = language.code / totalCode * 100;
          const segment = document.createElement("div");

          segment.className = "bar-segment";
          segment.style.width = `${percentage}%`;
          segment.style.backgroundColor = language.color;
          segment.title =
            `${language.name}: ${formatNumber(language.code)} linhas ` +
            `(${percentage.toLocaleString("pt-BR", {
              maximumFractionDigits: 2
            })}%)`;

          segment.setAttribute("aria-hidden", "true");

          barFragment.appendChild(segment);
        });

      elements.bar.appendChild(barFragment);

      const legendFragment = document.createDocumentFragment();

      languages.slice(0, 6).forEach(language => {
        const percentage = language.code / totalCode * 100;

        const item = document.createElement("div");
        item.className = "legend-item";

        const dot = document.createElement("span");
        dot.className = "dot";
        dot.style.backgroundColor = language.color;
        dot.setAttribute("aria-hidden", "true");

        const name = document.createElement("strong");
        name.textContent = language.name;
        name.title = language.name;

        const percent = document.createElement("span");
        percent.textContent =
          percentage.toLocaleString("pt-BR", {
            minimumFractionDigits: 1,
            maximumFractionDigits: 1
          }) + "%";

        item.append(dot, name, percent);
        legendFragment.appendChild(item);
      });

      elements.legend.appendChild(legendFragment);
    }

    function updateVersion(version) {
      elements.version.textContent = version;
      elements.footerVersion.textContent = version;
    }

    function setLoading(isLoading) {
      elements.refreshButton.disabled = isLoading;
      elements.loading.hidden = !isLoading;

      if (isLoading) {
        elements.status.textContent = "Atualizando dados...";
      }
    }

    function showError(message) {
      elements.error.hidden = false;
      elements.error.textContent = message;

      elements.status.textContent = "Falha ao carregar o relatório.";
      elements.loading.hidden = true;
    }

    async function loadReport() {
      if (requestController) {
        requestController.abort();
      }

      requestController = new AbortController();

      setLoading(true);
      elements.error.hidden = true;

      try {
        /*
         * O parâmetro _ impede que o navegador reutilize
         * uma versão antiga do JSON em cache.
         */

        const url = new URL(REPORT_URL, window.location.href);
        url.searchParams.set("_", Date.now().toString());

        const response = await fetch(url, {
          method: "GET",
          cache: "no-store",
          signal: requestController.signal,
          headers: {
            "Accept": "application/json"
          }
        });

        if (!response.ok) {
          throw new Error(
            `O relatório respondeu com HTTP ${response.status}.`
          );
        }

        const report = await response.json();
        const result = parseReport(report);

        languages = result.languages;
        totals = result.totals;

        renderSummary();
        renderTable(elements.search.value);
        renderDistribution();
        updateVersion(result.version);

        const now = new Date();

        const formattedDate = now.toLocaleString("pt-BR", {
          dateStyle: "short",
          timeStyle: "short"
        });

        elements.status.textContent =
          `Dados carregados · ${formattedDate}`;

        elements.footerUpdated.textContent =
          `Última consulta: ${formattedDate}`;

        elements.error.hidden = true;
        elements.loading.hidden = true;

      } catch (error) {
        if (error.name === "AbortError") {
          return;
        }

        console.error(
          "Erro ao carregar o relatório de linguagens:",
          error
        );

        showError(
          "Não foi possível carregar os dados. " +
          "Confirme se relatorios/linguagens.json existe " +
          "e está publicado no GitHub Pages. " +
          error.message
        );

      } finally {
        elements.refreshButton.disabled = false;
        elements.loading.hidden = true;
      }
    }

    /*
     * Pesquisa local, sem precisar consultar o GitHub
     * a cada caractere digitado.
     */

    elements.search.addEventListener("input", event => {
      renderTable(event.target.value);
    });

    /*
     * Botão de atualização manual.
     */

    elements.refreshButton.addEventListener("click", () => {
      loadReport();
    });

    /*
     * Carrega os dados ao abrir a página.
     */

    loadReport();

    /*
     * Consulta novamente a cada cinco minutos enquanto
     * a página estiver aberta.
     */

    window.setInterval(() => {
      if (!document.hidden) {
        loadReport();
      }
    }, 5 * 60 * 1000);

    /*
     * Atualiza os dados quando o usuário retorna
     * à aba do navegador.
     */

    document.addEventListener("visibilitychange", () => {
      if (!document.hidden) {
        loadReport();
      }
    });

(() => {
  const slug = name => name.normalize("NFKD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-zA-Z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .toLowerCase() || "linguagem";

  const aplicar = (elemento, nome) => {
    if (!elemento || !nome) return;
    elemento.classList.add("language-" + slug(nome));
    elemento.dataset.language = nome;
  };

  const legenda = [...document.querySelectorAll(".legend-item")];
  const nomes = [];

  legenda.forEach(item => {
    const strong = item.querySelector("strong");
    if (!strong) return;

    const nome = strong.textContent.trim();
    if (!nome) return;

    nomes.push(nome);
    aplicar(item, nome);
    aplicar(item.querySelector(".dot"), nome);
  });

  document.querySelectorAll("tbody tr").forEach(row => {
    const elemento = row.querySelector(".language");
    if (!elemento) return;

    const nome = elemento.textContent.trim();
    if (!nome) return;

    aplicar(row, nome);
    aplicar(elemento, nome);
    aplicar(elemento.querySelector(".dot"), nome);
  });

  const segmentos = [
    ...document.querySelectorAll(".bar-segment")
  ];

  segmentos.forEach((segmento, indice) => {
    const nome = segmento.dataset.language ||
      segmento.getAttribute("aria-label") ||
      segmento.getAttribute("title");

    if (nome) {
      aplicar(segmento, nome.trim());
    } else if (nomes[indice]) {
      aplicar(segmento, nomes[indice]);
    }
  });
})();
