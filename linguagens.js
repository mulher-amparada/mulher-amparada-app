"use strict";

/*
 * Inventário de extensões do repositório GitHub.
 */

const REPORT_URL =
  "https://raw.githubusercontent.com/mulher-amparada/mulher-amparada-app/main/relatorios/extensoes.json";

const REQUEST_TIMEOUT = 15000;
const REFRESH_INTERVAL = 5 * 60 * 1000;

const LANGUAGE_COLORS = {
  Kotlin: "#A97BFF",
  Markdown: "#083FA1",
  CSS: "#663399",
  HTML: "#E34C26",
  XML: "#0060AC",
  YAML: "#CB171E",
  "Bourne Shell": "#89E051",
  Shell: "#89E051",
  Bash: "#89E051",
  C: "#555555",
  "C++": "#F34B7D",
  PHP: "#4F5D95",
  Julia: "#A270BA",
  Gradle: "#02303A",
  Ruby: "#701516",
  "DOS Batch": "#C1F12E",
  TOML: "#9C4221",
  CSV: "#237346",
  Python: "#3572A5",
  JSON: "#292929",
  Properties: "#2A6277",
  Java: "#B07219",
  JavaScript: "#F1E05A",
  TypeScript: "#3178C6",
  Dart: "#00B4AB",
  Go: "#00ADD8",
  Rust: "#DEA584",
  Swift: "#F05138",
  "Kotlin Script": "#A97BFF",
  "Gradle Kotlin DSL": "#02303A",
  Makefile: "#427819",
  Dockerfile: "#384D54",
  SQL: "#E38C00",
  PowerShell: "#012456",
  R: "#198CE7",
  Lua: "#000080",
  Perl: "#0298C3",
  HCL: "#844FBA",
  "Markdown Extra": "#083FA1",
  Text: "#777777",
  "[sem extensão]": "#777777"
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

  tableLanguageCount: document.getElementById("table-language-count"),
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
  bytes: 0
};

let reportType = "extensions";
let requestController = null;
let refreshTimer = null;

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

  return (
    number.toLocaleString("pt-BR", {
      minimumFractionDigits: 1,
      maximumFractionDigits: 2
    }) + "%"
  );
}

function escapeHTML(value) {
  const entities = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;"
  };

  return String(value).replace(/[&<>"']/g, character => {
    return entities[character];
  });
}

function slugify(name) {
  return (
    String(name)
      .normalize("NFKD")
      .replace(/[\u0300-\u036f]/g, "")
      .replace(/[^a-zA-Z0-9]+/g, "-")
      .replace(/^-+|-+$/g, "")
      .toLowerCase() || "extensao"
  );
}

function getLanguageColor(name, index = 0) {
  if (LANGUAGE_COLORS[name]) {
    return LANGUAGE_COLORS[name];
  }

  const position =
    [...String(name)].reduce((total, character) => {
      return total + character.charCodeAt(0);
    }, 0) % FALLBACK_COLORS.length;

  return (
    FALLBACK_COLORS[position] ||
    FALLBACK_COLORS[index % FALLBACK_COLORS.length]
  );
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

function getNumericValue(value, fallback = 0) {
  if (value === null || value === undefined || value === "") {
    return fallback;
  }

  const number = Number(value);

  return Number.isFinite(number) ? number : fallback;
}

/*
 * Converte o inventário de extensões.
 */

function parseExtensionsReport(report) {
  if (!Array.isArray(report.extensoes)) {
    throw new Error(
      "O JSON não contém uma lista válida de extensões."
    );
  }

  const parsedLanguages = report.extensoes
    .filter(item => item && typeof item === "object")
    .map((item, index) => {
      const name = String(item.extensao || "[sem extensão]");

      return {
        name,
        files: getNumericValue(item.quantidade_arquivos),
        blank: null,
        comment: null,
        code: null,
        bytes: getNumericValue(item.tamanho_bytes),
        percentage: parsePercentage(item.percentual_bytes),
        filePercentage: parsePercentage(item.percentual_arquivos),
        paths: [],
        color: getLanguageColor(name, index)
      };
    });

  const computedFiles = parsedLanguages.reduce((sum, item) => {
    return sum + item.files;
  }, 0);

  const computedBytes = parsedLanguages.reduce((sum, item) => {
    return sum + item.bytes;
  }, 0);

  const totalFiles = Number(report.total_arquivos);
  const totalBytes = Number(report.total_bytes);
  const totalExtensions = Number(report.total_extensoes);

  return {
    type: "extensions",
    languages: parsedLanguages,
    totals: {
      files: Number.isFinite(totalFiles) ? totalFiles : computedFiles,
      blank: null,
      comment: null,
      code: null,
      bytes: Number.isFinite(totalBytes) ? totalBytes : computedBytes
    },
    version: "Inventário de extensões",
    totalExtensions: Number.isFinite(totalExtensions)
      ? totalExtensions
      : parsedLanguages.length,
    generatedAt: report.gerado_em_utc || null
  };
}

/*
 * Compatibilidade com relatórios antigos do Linguist.
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

      const name = String(item.nome || "Desconhecida");

      return {
        name,
        files: Array.isArray(item.arquivos)
          ? paths.length
          : getNumericValue(item.quantidade_arquivos),
        blank: null,
        comment: null,
        code: null,
        bytes: getNumericValue(item.tamanho_bytes),
        percentage: parsePercentage(item.percentual),
        paths,
        color: getLanguageColor(name, index)
      };
    });

  const computedFiles = parsedLanguages.reduce((sum, item) => {
    return sum + item.files;
  }, 0);

  const computedBytes = parsedLanguages.reduce((sum, item) => {
    return sum + item.bytes;
  }, 0);

  const totalFiles = Number(report.total_arquivos_classificados);
  const totalBytes = Number(report.total_bytes_classificados);

  return {
    type: "linguist",
    languages: parsedLanguages,
    totals: {
      files: Number.isFinite(totalFiles) ? totalFiles : computedFiles,
      blank: null,
      comment: null,
      code: null,
      bytes: Number.isFinite(totalBytes) ? totalBytes : computedBytes
    },
    version: report.fonte_classificacao || "GitHub Linguist",
    generatedAt: report.gerado_em_utc || null
  };
}

/*
 * Compatibilidade com relatórios produzidos pelo cloc.
 */

function getVersion(header) {
  if (!Array.isArray(header)) {
    return "cloc";
  }

  const match = header.join(" ").match(/cloc\s+v?([\d.]+)/i);

  return match ? `cloc v${match[1]}` : "cloc";
}

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
      files: getNumericValue(value.nFiles),
      blank: getNumericValue(value.blank),
      comment: getNumericValue(value.comment),
      code: getNumericValue(value.code),
      bytes: null,
      percentage: 0,
      paths: [],
      color: getLanguageColor(name, index)
    }));

  if (parsedLanguages.length === 0) {
    throw new Error(
      "O JSON não possui um formato válido de extensões, Linguist ou cloc."
    );
  }

  const computedTotals = parsedLanguages.reduce(
    (result, item) => {
      result.files += item.files;
      result.blank += item.blank;
      result.comment += item.comment;
      result.code += item.code;

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

  const totals = {
    files: getNumericValue(sum.nFiles, computedTotals.files),
    blank: getNumericValue(sum.blank, computedTotals.blank),
    comment: getNumericValue(sum.comment, computedTotals.comment),
    code: getNumericValue(sum.code, computedTotals.code),
    bytes: null
  };

  parsedLanguages.forEach(item => {
    item.percentage = totals.code > 0
      ? (item.code / totals.code) * 100
      : 0;
  });

  parsedLanguages.sort((a, b) => b.code - a.code);

  return {
    type: "cloc",
    languages: parsedLanguages,
    totals,
    version: getVersion(report.header),
    generatedAt: null
  };
}

/*
 * Detecta automaticamente o formato do JSON.
 */

function parseReport(report) {
  if (
    !report ||
    typeof report !== "object" ||
    Array.isArray(report)
  ) {
    throw new Error("O relatório JSON não é válido.");
  }

  if (Array.isArray(report.extensoes)) {
    return parseExtensionsReport(report);
  }

  if (Array.isArray(report.linguagens)) {
    return parseLinguistReport(report);
  }

  return parseClocReport(report);
}

/*
 * Métrica principal do relatório.
 */

function getMetric(item) {
  if (
    reportType === "extensions" ||
    reportType === "linguist"
  ) {
    return item.bytes;
  }

  return item.code;
}

function getTotalMetric() {
  if (
    reportType === "extensions" ||
    reportType === "linguist"
  ) {
    return totals.bytes;
  }

  return totals.code;
}

function getMetricLabel() {
  if (
    reportType === "extensions" ||
    reportType === "linguist"
  ) {
    return "bytes";
  }

  return "linhas de código";
}

/*
 * Atualiza os rótulos da interface.
 */

function updateReportLabels() {
  const isExtensions = reportType === "extensions";
  const isLinguist = reportType === "linguist";
  const isByteReport = isExtensions || isLinguist;

  const table = elements.rows?.closest("table");
  const headers = table
    ? table.querySelectorAll("thead th")
    : [];

  if (headers.length >= 5) {
    headers[1].textContent = "Arquivos";
    headers[2].textContent = isByteReport
      ? "Linhas em branco"
      : "Linhas em branco";
    headers[3].textContent = "Comentários";
    headers[4].textContent = isByteReport
      ? "Tamanho (bytes)"
      : "Linhas de código";
  }

  const updateLabel = (id, labelText) => {
    const label = document.querySelector(
      `label[for="${id}"], [data-label-for="${id}"]`
    );

    if (label) {
      label.textContent = labelText;
      return;
    }

    const value = document.getElementById(id);

    if (!value?.parentElement) {
      return;
    }

    const candidate = value.parentElement.querySelector(
      ".metric-label, .stat-label, .card-label, [data-metric-label]"
    );

    if (candidate && candidate !== value) {
      candidate.textContent = labelText;
    }
  };

  const metricLabel = isByteReport
    ? "Tamanho total (bytes)"
    : "Linhas de código";

  updateLabel("code-total", metricLabel);
  updateLabel("total-code", metricLabel);

  updateLabel(
    "total-blank",
    isByteReport
      ? "Linhas em branco (indisponível)"
      : "Linhas em branco"
  );

  updateLabel(
    "total-comments",
    isByteReport
      ? "Comentários (indisponível)"
      : "Comentários"
  );
}

/*
 * Resumo do inventário.
 */

function renderSummary() {
  const metricTotal = getTotalMetric();

  if (elements.languageTotal) {
    elements.languageTotal.textContent = formatNumber(
      languages.length
    );

    elements.languageTotal.title = reportType === "extensions"
      ? "Quantidade de extensões identificadas"
      : "Quantidade de linguagens identificadas";
  }

  if (elements.fileTotal) {
    elements.fileTotal.textContent = formatNumber(totals.files);
  }

  if (elements.codeTotal) {
    elements.codeTotal.textContent = formatNumber(metricTotal);
    elements.codeTotal.title = reportType === "cloc"
      ? "Total de linhas de código"
      : "Tamanho total em bytes";
  }

  if (elements.tableLanguageCount) {
    const label = reportType === "extensions"
      ? "extensões"
      : "linguagens";

    elements.tableLanguageCount.textContent =
      `${formatNumber(languages.length)} ${label}`;
  }

  if (elements.totalFiles) {
    elements.totalFiles.textContent = formatNumber(totals.files);
  }

  if (elements.totalBlank) {
    elements.totalBlank.textContent = formatNumber(totals.blank);
  }

  if (elements.totalComments) {
    elements.totalComments.textContent = formatNumber(
      totals.comment
    );
  }

  if (elements.totalCode) {
    elements.totalCode.textContent = formatNumber(metricTotal);
    elements.totalCode.title = reportType === "cloc"
      ? "Total de linhas de código"
      : "Tamanho total em bytes";
  }

  updateReportLabels();
}

/*
 * Renderiza a tabela.
 */

function renderTable(filter = "") {
  if (!elements.rows) {
    return;
  }

  const query = String(filter)
    .trim()
    .toLocaleLowerCase("pt-BR");

  const filtered = [...languages]
    .filter(item =>
      item.name.toLocaleLowerCase("pt-BR").includes(query)
    )
    .sort((a, b) => getMetric(b) - getMetric(a));

  elements.rows.innerHTML = filtered.map(item => {
    const metric = getMetric(item);
    const name = escapeHTML(item.name);
    const slug = slugify(item.name);

    return `
      <tr class="language-${slug}" data-language="${name}">
        <td>
          <span
            class="language language-${slug}"
            data-language="${name}"
          >
            <span
              class="dot language-${slug}"
              data-language="${name}"
              style="background:${item.color}"
              aria-hidden="true"
            ></span>
            ${name}
          </span>
        </td>

        <td class="number">${formatNumber(item.files)}</td>
        <td class="number">${formatNumber(item.blank)}</td>
        <td class="number">${formatNumber(item.comment)}</td>
        <td class="code-number">${formatNumber(metric)}</td>
      </tr>
    `;
  }).join("");

  if (elements.empty) {
    elements.empty.hidden = filtered.length > 0;
  }
}

/*
 * Gráfico de distribuição por bytes ou linhas.
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

  const metricName = getMetricLabel();

  elements.bar.setAttribute(
    "aria-label",
    `Distribuição de ${formatNumber(totalMetric)} ${metricName} entre ${languages.length} itens.`
  );

  const sortedLanguages = [...languages]
    .filter(item => getMetric(item) > 0)
    .sort((a, b) => getMetric(b) - getMetric(a));

  const barFragment = document.createDocumentFragment();

  sortedLanguages.forEach(item => {
    const metric = getMetric(item);
    const percentage = (metric / totalMetric) * 100;

    const segment = document.createElement("div");

    segment.className =
      `bar-segment language-${slugify(item.name)}`;

    segment.dataset.language = item.name;
    segment.style.width = `${percentage}%`;
    segment.style.backgroundColor = item.color;

    segment.title =
      `${item.name}: ${formatNumber(metric)} ${metricName} ` +
      `(${formatPercentage(percentage)})`;

    segment.setAttribute("aria-label", segment.title);

    barFragment.appendChild(segment);
  });

  elements.bar.appendChild(barFragment);

  const legendFragment = document.createDocumentFragment();

  sortedLanguages.slice(0, 6).forEach(item => {
    const metric = getMetric(item);
    const percentage = (metric / totalMetric) * 100;

    const legendItem = document.createElement("div");

    legendItem.className =
      `legend-item language-${slugify(item.name)}`;

    legendItem.dataset.language = item.name;

    const dot = document.createElement("span");

    dot.className = `dot language-${slugify(item.name)}`;
    dot.dataset.language = item.name;
    dot.style.backgroundColor = item.color;
    dot.setAttribute("aria-hidden", "true");

    const name = document.createElement("strong");

    name.textContent = item.name;
    name.title = item.name;

    const percent = document.createElement("span");

    percent.textContent = formatPercentage(percentage);
    percent.title = `${formatNumber(metric)} ${metricName}`;

    legendItem.append(dot, name, percent);
    legendFragment.appendChild(legendItem);
  });

  elements.legend.appendChild(legendFragment);
}

/*
 * Atualiza a versão exibida.
 */

function updateVersion(version) {
  if (elements.version) {
    elements.version.textContent = version;
  }

  if (elements.footerVersion) {
    elements.footerVersion.textContent = version;
  }
}

/*
 * Estados de carregamento e erro.
 */

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
 * Busca o JSON publicado no GitHub.
 *
 * O tempo limite evita uma requisição presa indefinidamente.
 */

async function loadReport() {
  if (requestController) {
    requestController.abort();
  }

  const controller = new AbortController();
  requestController = controller;

  let timeoutId = null;

  setLoading(true);

  if (elements.error) {
    elements.error.hidden = true;
  }

  try {
    const url = new URL(REPORT_URL);

    url.searchParams.set("_", Date.now().toString());

    timeoutId = window.setTimeout(() => {
      controller.abort();
    }, REQUEST_TIMEOUT);

    const response = await fetch(url.toString(), {
      method: "GET",
      cache: "no-store",
      signal: controller.signal,
      headers: {
        Accept: "application/json"
      }
    });

    if (!response.ok) {
      throw new Error(
        `Erro HTTP ${response.status} ao consultar o JSON.`
      );
    }

    const report = await response.json();
    const result = parseReport(report);

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
      if (result.generatedAt) {
        const date = new Date(result.generatedAt);

        elements.footerUpdated.textContent =
          `Relatório gerado em: ${
            Number.isNaN(date.getTime())
              ? result.generatedAt
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
    if (requestController !== controller) {
      return;
    }

    console.error(
      "Erro ao carregar o inventário do repositório:",
      error
    );

    let message;

    if (error.name === "AbortError") {
      message =
        "A requisição demorou demais ou foi cancelada. " +
        "Verifique a conexão e tente novamente.";
    } else {
      message =
        "Não foi possível carregar o relatório. " +
        "Verifique se o arquivo existe, se o repositório é público " +
        "e se o JSON possui uma estrutura válida. " +
        error.message;
    }

    showError(message);
  } finally {
    if (timeoutId !== null) {
      window.clearTimeout(timeoutId);
    }

    if (requestController === controller) {
      requestController = null;
      setLoading(false);
    }
  }
}

/*
 * Pesquisa local.
 */

if (elements.search) {
  elements.search.addEventListener("input", event => {
    renderTable(event.target.value);
  });
}

/*
 * Atualização manual.
 */

if (elements.refreshButton) {
  elements.refreshButton.addEventListener("click", () => {
    loadReport();
  });
}

/*
 * Carregamento inicial.
 */

loadReport();

/*
 * Atualização automática a cada cinco minutos.
 */

refreshTimer = window.setInterval(() => {
  if (!document.hidden) {
    loadReport();
  }
}, REFRESH_INTERVAL);

/*
 * Atualização ao voltar para a página.
 */

document.addEventListener("visibilitychange", () => {
  if (!document.hidden) {
    loadReport();
  }
});

/*
 * Evita manter o temporizador ativo em páginas descartadas.
 */

window.addEventListener("pagehide", () => {
  if (requestController) {
    requestController.abort();
    requestController = null;
  }

  if (refreshTimer !== null) {
    window.clearInterval(refreshTimer);
    refreshTimer = null;
  }
});