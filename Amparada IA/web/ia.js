const messages = document.querySelector("#messages");
const form = document.querySelector("#chat-form");
const promptInput = document.querySelector("#prompt");
const sendButton = document.querySelector("#send-button");
const statusLabel = document.querySelector("#status-label");
const statusDot = document.querySelector("#status-dot");

function addMessage(content, author, extraClass = "") {
  const message = document.createElement("div");
  message.className = `message ${author} ${extraClass}`.trim();
  message.textContent = content;

  messages.appendChild(message);
  messages.scrollTop = messages.scrollHeight;

  return message;
}

function setStatus(label, state) {
  statusLabel.textContent = label;
  statusDot.className = "status-dot";

  if (state) {
    statusDot.classList.add(state);
  }
}

async function checkModelStatus() {
  try {
    const response = await fetch("/api/status");
    const data = await response.json();

    if (data.ready) {
      setStatus("Modelo carregado", "ready");
    } else {
      setStatus("Modelo indisponível", "error");
    }
  } catch {
    setStatus("Servidor local desconectado", "error");
  }
}

async function sendMessage(event) {
  event.preventDefault();

  const message = promptInput.value.trim();

  if (!message || sendButton.disabled) {
    return;
  }

  addMessage(message, "user");
  promptInput.value = "";
  sendButton.disabled = true;
  sendButton.textContent = "…";

  const pendingMessage = addMessage(
    "A IA está processando…",
    "assistant",
    "pending"
  );

  try {
    const response = await fetch("/api/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ message })
    });

    const data = await response.json();
    pendingMessage.remove();

    if (!response.ok) {
      throw new Error(data.error || "Não foi possível gerar uma resposta.");
    }

    addMessage(
      data.answer || "O modelo não gerou uma resposta.",
      "assistant"
    );
  } catch (error) {
    pendingMessage.remove();
    addMessage(
      error.message || "Falha ao conectar com o modelo.",
      "assistant"
    );
  } finally {
    sendButton.disabled = false;
    sendButton.textContent = "Enviar";
    promptInput.focus();
  }
}

form.addEventListener("submit", sendMessage);
checkModelStatus();
