"use strict";

/*
 * Relatório de linguagens do repositório GitHub.
 */
const REPORT_URL =
  "https://raw.githubusercontent.com/mulher-amparada/mulher-amparada-app/main/relatorios/linguagens.json";

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
  blank: null,
  comment: null,
  code: null,
  bytes: null
};

let reportType = "linguist";
let requestController = null;

/*
 * Formatação e utilitários.
 */

function formatNumber(value) {
  if (value === null || value === undefined || value === "") {
    return "—";
  }

  const number = Number(value);

  return Number.isFinite(number)
    ? numberFormat.format(number)
    : "—";
}

function formatPercentage(value) {
  const number = Number(value);

  if (!Number.isFinite(number)) {
    return "0%";
  }

  return number.toLocaleString("pt-BR", {
    minimumFractionDigits: 1,
    maximumFractionDigits: 2
  }) + "%";
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

function slugify(name) {
  return String(name)
    .normalize("NFKD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-zA-Z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .toLowerCase() || "linguagem";
}

function applyLanguageClass(element, name) {
  if (!element || !name) {
    return;
  }

  element.classList.add("language-" + slugify(name));
  element.dataset.language = name;
}

function getLanguageColor(name, index = 0) {
  if (LANGUAGE_COLORS[name]) {
    return LANGUAGE_COLORS[name];
  }

  const position = Math.abs(
    [...String(name)].reduce(
      (total, character) => total + character.charCodeAt(0),
      0
    )
  ) % FALLBACK_COLORS.length;

  return FALLBACK_COLORS[position] || FALLBACK_COLORS[index % FALLBACK_COLORS.length];
}

function parsePercentage(value) {
  if (typeof value === "number") {
    return Number.isFinite(value) ? value : 0;
  }

  const normalized = String(value ?? "")
    .replace("%", "")
    .replace(/\s/g, "")
    .replace(",", ".");

  const number = Number(normalized);

  return Number.isFinite(number) ? number : 0;
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
 * Converte o JSON do GitHub Linguist.
 *
 * Estrutura:
 * {
 *   "total_linguagens": 12,
 *   "total_arquivos_classificados": 96,
 *   "total_bytes_classificados": 1801488,
 *   "linguagens": [
 *     {
 *       "nome": "Kotlin",
 *       "tamanho_bytes": 12345,
 *       "percentual": "50.00",
 *       "arquivos": ["arquivo.kt"]
 *     }
 *   ]
 * }
 */

function parseLinguistReport(report) {
  if (!Array.isArray(report.linguagens)) {
    throw new Error(
      "O JSON não contém uma lista válida de linguagens."
    );
  }

  const parsedLanguages = report.linguagens
    .filter(item => item && typeof item === "object")
    .map((item, index) => {
      const paths = Array.isArray(item.arquivos)
        ? item.arquivos
        : [];

      return {
        name: String(item.nome || "Desconhecida"),
        files: Array.isArray(item.arquivos)
          ? paths.length
          : Number(item.quantidade_arquivos) || 0,
        blank: null,
        comment: null,
        code: null,
        bytes: Number(item.tamanho_bytes) || 0,
        percentage: parsePercentage(item.percentual),
        paths,
        color: getLanguageColor(item.nome, index)
      };
    });

  const computedFiles = parsedLanguages.reduce(
    (sum, language) => sum + language.files,
    0
  );

  const computedBytes = parsedLanguages.reduce(
    (sum, language) => sum + language.bytes,
    0
  );

  /*
   * Usa os totais do próprio relatório quando disponíveis.
   */

  const totalFiles =
    Number(report.total_arquivos_classificados);

  const totalBytes =
    Number(report.total_bytes_classificados);

  const totalLanguages =
    Number(report.total_linguagens);

  return {
    type: "linguist",
    languages: parsedLanguages,
    totals: {
      files: Number.isFinite(totalFiles)
        ? totalFiles
        : computedFiles,
      blank: null,
      comment: null,
      code: null,
      bytes: Number.isFinite(totalBytes)
        ? totalBytes
        : computedBytes
    },
    version: report.fonte_classificacao || "GitHub Linguist"
  };
}

/*
 * Converte relatórios produzidos pelo cloc.
 */

function parseClocReport(report) {
  const parsedLanguages = Object.entries(report)
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
      bytes: null,
      percentage: 0,
      paths: [],
      color: getLanguageColor(name, index)
    }));

  if (parsedLanguages.length === 0) {
    throw new Error(
      "Nenhuma linguagem foi encontrada no formato Linguist ou cloc."
    );
  }

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

  const sum = report.SUM || {};

  const resultTotals = {
    files: Number(sum.nFiles ?? computedTotals.files),
    blank: Number(sum.blank ?? computedTotals.blank),
    comment: Number(sum.comment ?? computedTotals.comment),
    code: Number(sum.code ?? computedTotals.code),
    bytes: null
  };

  const totalCode = resultTotals.code;

  parsedLanguages.forEach(language => {
    language.percentage = totalCode > 0
      ? language.code / totalCode * 100
      : 0;
  });

  parsedLanguages.sort((a, b) => b.code - a.code);

  return {
    type: "cloc",
    languages: parsedLanguages,
    totals: resultTotals,
    version: getVersion(report.header)
  };
}

/*
 * Detecta automaticamente qual formato foi recebido.
 */

function parseReport(report) {
  if (!report || typeof report !== "object" || Array.isArray(report)) {
    throw new Error("O relatório JSON não é válido.");
  }

  if (Array.isArray(report.linguagens)) {
    return parseLinguistReport(report);
  }

  return parseClocReport(report);
}

/*
 * Atualiza os rótulos para não confundir bytes com linhas.
 */

function updateReportLabels() {
  const linguist = reportType === "linguist";

  const table = elements.rows?.closest("table");
  const headers = table
    ? table.querySelectorAll("thead th")
    : [];

  if (headers.length >= 5) {
    headers[1].textContent = "Arquivos";
    headers[2].textContent = linguist
      ? "Linhas em branco"
      : "Linhas em branco";
    headers[3].textContent = linguist
      ? "Comentários"
      : "Comentários";
    headers[4].textContent = linguist
      ? "Tamanho (bytes)"
      : "Linhas de código";
  }

  /*
   * Atualiza rótulos de métricas quando o HTML utiliza
   * label[for], data-label-for ou classes comuns de rótulos.
   */

  const updateLabel = (id, text) => {
    const label = document.querySelector(
      `label[for="${id}"], [data-label-for="${id}"]`
    );

    if (label) {
      label.textContent = text;
      return;
    }

    const valueElement = document.getElementById(id);

    if (!valueElement?.parentElement) {
      return;
    }

    const parent = valueElement.parentElement;

    const candidate = parent.querySelector(
      ".metric-label, .stat-label, .card-label, [data-metric-label]"
    );

    if (candidate && candidate !== valueElement) {
      candidate.textContent = text;
    }
  };

  updateLabel(
    "code-total",
    linguist ? "Tamanho total (bytes)" : "Linhas de código"
  );

  updateLabel(
    "total-code",
    linguist ? "Tamanho total (bytes)" : "Linhas de código"
  );

  updateLabel(
    "total-blank",
    linguist ? "Linhas em branco (indisponível)" : "Linhas em branco"
  );

  updateLabel(
    "total-comments",
    linguist ? "Comentários (indisponível)" : "Comentários"
  );
}

/*
 * Retorna a métrica usada para ordenar e desenhar o gráfico.
 */

function getMetric(language) {
  return reportType === "linguist"
    ? language.bytes
    : language.code;
}

function getTotalMetric() {
  return reportType === "linguist"
    ? totals.bytes
    : totals.code;
}

/*
 * Resumo.
 */

function renderSummary() {
  const metricTotal = getTotalMetric();

  if (elements.languageTotal) {
    elements.languageTotal.textContent = formatNumber(
      languages.length
    );
  }

  if (elements.fileTotal) {
    elements.fileTotal.textContent = formatNumber(
      totals.files
    );
  }

  if (elements.codeTotal) {
    elements.codeTotal.textContent = formatNumber(metricTotal);
    elements.codeTotal.title = reportType === "linguist"
      ? "Tamanho total classificado em bytes"
      : "Total de linhas de código";
  }

  if (elements.tableLanguageCount) {
    elements.tableLanguageCount.textContent =
      `${formatNumber(languages.length)} linguagens`;
  }

  if (elements.totalFiles) {
    elements.totalFiles.textContent = formatNumber(totals.files);
  }

  if (elements.totalBlank) {
    elements.totalBlank.textContent = formatNumber(totals.blank);
  }

  if (elements.totalComments) {
    elements.totalComments.textContent = formatNumber(totals.comment);
  }

  if (elements.totalCode) {
    elements.totalCode.textContent = formatNumber(metricTotal);
    elements.totalCode.title = reportType === "linguist"
      ? "Tamanho total classificado em bytes"
      : "Total de linhas de código";
  }

  updateReportLabels();
}

/*
 * Tabela de linguagens.
 */

function renderTable(filter = "") {
  if (!elements.rows) {
    return;
  }

  const query = String(filter)
    .trim()
    .toLocaleLowerCase("pt-BR");

  const filtered = [...languages]
    .filter(language =>
      language.name.toLocaleLowerCase("pt-BR").includes(query)
    )
    .sort((a, b) => getMetric(b) - getMetric(a));

  elements.rows.innerHTML = filtered.map(language => {
    const metric = getMetric(language);

    return `
      <tr class="language-${slugify(language.name)}"
          data-language="${escapeHTML(language.name)}">
        <td>
          <span class="language language-${slugify(language.name)}"
                data-language="${escapeHTML(language.name)}">
            <span
              class="dot language-${slugify(language.name)}"
              data-language="${escapeHTML(language.name)}"
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
          ${formatNumber(metric)}
        </td>
      </tr>
    `;
  }).join("");

  if (elements.empty) {
    elements.empty.hidden = filtered.length > 0;
  }
}

/*
 * Gráfico de distribuição.
 */

function renderDistribution() {
  if (!elements.bar || !elements.legend) {
    return;
  }

  const totalMetric = getTotalMetric();

  elements.bar.replaceChildren();
  elements.legend.replaceChildren();

  if (!(totalMetric > 0)) {
    elements.bar.setAttribute(
      "aria-label",
      "Não há dados disponíveis para exibir a distribuição."
    );

    return;
  }

  const metricName = reportType === "linguist"
    ? "bytes"
    : "linhas de código";

  elements.bar.setAttribute(
    "aria-label",
    `Distribuição de ${formatNumber(totalMetric)} ${metricName} entre ${languages.length} linguagens.`
  );

  const sortedLanguages = [...languages]
    .filter(language => getMetric(language) > 0)
    .sort((a, b) => getMetric(b) - getMetric(a));

  const barFragment = document.createDocumentFragment();

  sortedLanguages.forEach(language => {
    const metric = getMetric(language);
    const percentage = metric / totalMetric * 100;

    const segment = document.createElement("div");

    segment.className =
      `bar-segment language-${slugify(language.name)}`;

    segment.dataset.language = language.name;
    segment.style.width = `${percentage}%`;
    segment.style.backgroundColor = language.color;

    segment.title =
      `${language.name}: ${formatNumber(metric)} ${metricName} ` +
      `(${formatPercentage(percentage)})`;

    segment.setAttribute("aria-label", segment.title);

    barFragment.appendChild(segment);
  });

  elements.bar.appendChild(barFragment);

  const legendFragment = document.createDocumentFragment();

  sortedLanguages.slice(0, 6).forEach(language => {
    const metric = getMetric(language);
    const percentage = metric / totalMetric * 100;

    const item = document.createElement("div");

    item.className =
      `legend-item language-${slugify(language.name)}`;

    item.dataset.language = language.name;

    const dot = document.createElement("span");

    dot.className =
      `dot language-${slugify(language.name)}`;

    dot.dataset.language = language.name;
    dot.style.backgroundColor = language.color;
    dot.setAttribute("aria-hidden", "true");

    const name = document.createElement("strong");

    name.textContent = language.name;
    name.title = language.name;

    const percent = document.createElement("span");

    percent.textContent = formatPercentage(percentage);
    percent.title =
      `${formatNumber(metric)} ${metricName}`;

    item.append(dot, name, percent);
    legendFragment.appendChild(item);
  });

  elements.legend.appendChild(legendFragment);
}

/*
 * Versão e estado da atualização.
 */

function updateVersion(version) {
  if (elements.version) {
    elements.version.textContent = version;
  }

  if (elements.footerVersion) {
    elements.footerVersion.textContent = version;
  }
}

function setLoading(isLoading) {
  if (elements.refreshButton) {
    elements.refreshButton.disabled = isLoading;
  }

  if (elements.loading) {
    elements.loading.hidden = !isLoading;
  }

  if (isLoading && elements.status) {
    elements.status.textContent = "Atualizando dados...";
  }
}

function showError(message) {
  if (elements.error) {
    elements.error.hidden = false;
    elements.error.textContent = message;
  }

  if (elements.status) {
    elements.status.textContent = "Falha ao carregar o relatório.";
  }

  if (elements.loading) {
    elements.loading.hidden = true;
  }
}

/*
 * Carrega o JSON diretamente do GitHub.
 */

async function loadReport() {
  if (requestController) {
    requestController.abort();
  }

  const controller = new AbortController();
  requestController = controller;

  setLoading(true);

  if (elements.error) {
    elements.error.hidden = true;
  }

  try {
    const url = new URL(REPORT_URL);

    /*
     * Evita reaproveitar uma resposta antiga em cache.
     */

    url.searchParams.set("_", Date.now().toString());

    const response = await fetch(url.toString(), {
      method: "GET",
      cache: "no-store",
      signal: controller.signal,
      headers: {
        "Accept": "application/json"
      }
    });

    if (!response.ok) {
      throw new Error(
        `Erro HTTP ${response.status} ao consultar o JSON.`
      );
    }

    const report = await response.json();
    const result = parseReport(report);

    /*
     * Não atualiza a interface se outra consulta já começou.
     */

    if (requestController !== controller) {
      return;
    }

    languages = result.languages;
    totals = result.totals;
    reportType = result.type;

    renderSummary();
    renderTable(elements.search?.value || "");
    renderDistribution();
    updateVersion(result.version);

    const now = new Date().toLocaleString("pt-BR", {
      dateStyle: "short",
      timeStyle: "short"
    });

    if (elements.status) {
      elements.status.textContent = `Dados carregados · ${now}`;
    }

    if (elements.footerUpdated) {
      const generatedAt = report.gerado_em_utc;

      if (generatedAt) {
        const date = new Date(generatedAt);

        elements.footerUpdated.textContent =
          `Relatório gerado em: ${
            Number.isNaN(date.getTime())
              ? generatedAt
              : date.toLocaleString("pt-BR")
          }`;
      } else {
        elements.footerUpdated.textContent =
          `Última consulta: ${now}`;
      }
    }

    if (elements.error) {
      elements.error.hidden = true;
    }

  } catch (error) {
    if (error.name === "AbortError") {
      return;
    }

    console.error(
      "Erro ao carregar o relatório de linguagens:",
      error
    );

    showError(
      "Não foi possível carregar o relatório. " +
      "Verifique se o arquivo existe, se o repositório é público " +
      "e se o JSON possui uma estrutura válida. " +
      error.message
    );

  } finally {
    if (requestController === controller) {
      requestController = null;
      setLoading(false);
    }
  }
}

/*
 * Pesquisa local na tabela.
 */

if (elements.search) {
  elements.search.addEventListener("input", event => {
    renderTable(event.target.value);
  });
}

/*
 * Botão para atualizar manualmente.
 */

if (elements.refreshButton) {
  elements.refreshButton.addEventListener("click", () => {
    loadReport();
  });
}

/*
 * Primeira consulta.
 */

loadReport();

/*
 * Atualiza a cada cinco minutos enquanto a página
 * estiver visível.
 */

window.setInterval(() => {
  if (!document.hidden) {
    loadReport();
  }
}, 5 * 60 * 1000);

/*
 * Atualiza quando o usuário volta à página.
 */

document.addEventListener("visibilitychange", () => {
  if (!document.hidden) {
    loadReport();
  }
});