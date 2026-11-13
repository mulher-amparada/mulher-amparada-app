const messages = document.querySelector("#messages");
const form = document.querySelector("#chat-form");
const promptInput = document.querySelector("#prompt");
const sendButton = document.querySelector("#send-button");
const sendLabel = document.querySelector("#send-label");
const statusLabel = document.querySelector("#status-label");
const statusDot = document.querySelector("#status-dot");
const clearButton = document.querySelector("#clear-chat");
const suggestions = document.querySelectorAll(".suggestion");

const initialMessage = messages.innerHTML;
let isSending = false;

function scrollToLatest() {
  messages.scrollTop = messages.scrollHeight;
}

function addMessage(content, author, extraClass = "") {
  const message = document.createElement("div");
  message.className = `message ${author} ${extraClass}`.trim();
  message.textContent = content;
  message.setAttribute("role", "listitem");

  messages.appendChild(message);
  scrollToLatest();

  return message;
}

function setStatus(label, state = "") {
  statusLabel.textContent = label;
  statusDot.className = "status-dot";

  if (state) {
    statusDot.classList.add(state);
  }
}

function setSending(sending) {
  isSending = sending;
  sendButton.disabled = sending;
  sendLabel.textContent = sending ? "Enviando" : "Enviar";
  promptInput.disabled = sending;
}

async function checkModelStatus() {
  try {
    const response = await fetch("/api/status", {
      headers: { Accept: "application/json" },
      cache: "no-store"
    });

    if (!response.ok) {
      throw new Error("Não foi possível consultar o servidor.");
    }

    const data = await response.json();

    if (data.ready) {
      const ragStatus = data.rag_ready
        ? `RAG local ativo · ${data.rag_files} arquivos`
        : "RAG local sem dados indexáveis";
      setStatus(`Modelo pronto · ${ragStatus}`, data.rag_ready ? "ready" : "error");
    } else {
      setStatus(data.message || "Modelo indisponível", "error");
    }
  } catch {
    setStatus("Servidor local desconectado", "error");
  }
}

async function sendMessage(event) {
  event.preventDefault();

  const message = promptInput.value.trim();

  if (!message || isSending) {
    return;
  }

  addMessage(message, "user");
  promptInput.value = "";
  setSending(true);

  const pendingMessage = addMessage(
    "A Amparada IA está processando…",
    "assistant",
    "pending"
  );

  try {
    const response = await fetch("/api/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json"
      },
      body: JSON.stringify({ message })
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.error || data.message || "Não foi possível gerar uma resposta.");
    }

    pendingMessage.remove();
    addMessage(
      data.answer || "O modelo não gerou uma continuação.",
      "assistant"
    );

    if (Array.isArray(data.sources) && data.sources.length) {
      const sourceNames = [...new Set(data.sources.map((source) => source.file))];
      addMessage(
        `Fontes locais consultadas: ${sourceNames.join(", ")}`,
        "assistant",
        "sources"
      );
    }
  } catch (error) {
    pendingMessage.remove();
    addMessage(
      error.message || "Falha ao conectar com o modelo.",
      "assistant"
    );
  } finally {
    setSending(false);
    promptInput.focus();
  }
}

function clearConversation() {
  if (isSending) {
    return;
  }

  messages.innerHTML = initialMessage;
  promptInput.value = "";
  promptInput.focus();
}

function useSuggestion(event) {
  const button = event.currentTarget;
  promptInput.value = button.dataset.prompt || "";
  promptInput.focus();
  promptInput.dispatchEvent(new Event("input", { bubbles: true }));
}

form.addEventListener("submit", sendMessage);
if (clearButton) {
  clearButton.addEventListener("click", clearConversation);
}

suggestions.forEach((button) => {
  button.addEventListener("click", useSuggestion);
});

promptInput.addEventListener("keydown", (event) => {
  if (event.key === "Enter" && !event.shiftKey) {
    event.preventDefault();
    form.requestSubmit();
  }
});

checkModelStatus();