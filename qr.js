/* =========================================================
   ESTADO
========================================================= */

let qr = null;

const input =
    document.getElementById("text");

const qrBox =
    document.getElementById("qrBox");

const qrActions =
    document.getElementById("qrActions");

const historyBox =
    document.getElementById("history");

const historyCount =
    document.getElementById("historyCount");

const toast =
    document.getElementById("toast");


/* =========================================================
   GERAR
========================================================= */

function generate() {

    const text =
        input.value.trim();

    if (!text) {

        showToast("Digite algum conteúdo.");

        input.focus();

        return;
    }


    qrBox.innerHTML = "";

    qrBox.classList.add("visible");


    qr = new QRCode(qrBox, {

        text: text,

        width: 200,

        height: 200,

        colorDark: "#000000",

        colorLight: "#ffffff",

        correctLevel:
            QRCode.CorrectLevel.M

    });


    qrActions.classList.add("active");


    saveHistory(text);

    showToast("QR Code gerado.");
}


/* =========================================================
   ENTER
========================================================= */

function handleKey(event) {

    if (event.key === "Enter") {

        generate();
    }
}


/* =========================================================
   HISTÓRICO
========================================================= */

function getHistory() {

    try {

        return JSON.parse(
            localStorage.getItem("qrHistory") || "[]"
        );

    } catch {

        return [];
    }
}


function saveHistory(text) {

    let data =
        getHistory();


    data =
        data.filter(item => item !== text);


    data.unshift(text);


    data =
        data.slice(0, 20);


    localStorage.setItem(
        "qrHistory",
        JSON.stringify(data)
    );


    renderHistory();
}


function renderHistory() {

    const data =
        getHistory();


    historyBox.innerHTML = "";


    historyCount.textContent =
        `${data.length} ${
            data.length === 1
                ? "item"
                : "itens"
        }`;


    if (!data.length) {

        const empty =
            document.createElement("div");

        empty.className =
            "history-empty";

        empty.textContent =
            "Os QR Codes gerados aparecerão aqui.";

        historyBox.appendChild(empty);

        return;
    }


    data.forEach(text => {

        const item =
            document.createElement("div");

        item.className =
            "item";


        const span =
            document.createElement("span");

        span.className =
            "item-text";

        span.textContent =
            text;


        const button =
            document.createElement("button");

        button.className =
            "smallBtn";

        button.textContent =
            "Usar";


        button.addEventListener(
            "click",
            () => {

                input.value = text;

                generate();

            }
        );


        item.appendChild(span);

        item.appendChild(button);

        historyBox.appendChild(item);

    });
}


/* =========================================================
   DOWNLOAD
========================================================= */

function downloadQR() {

    const canvas =
        qrBox.querySelector("canvas");

    const img =
        qrBox.querySelector("img");


    if (!canvas && !img) {

        showToast("Gere um QR Code primeiro.");

        return;
    }


    const link =
        document.createElement("a");


    if (canvas) {

        link.href =
            canvas.toDataURL("image/png");

    } else {

        link.href =
            img.src;
    }


    link.download =
        "qr-code.png";


    document.body.appendChild(link);

    link.click();

    link.remove();


    showToast("QR Code salvo.");
}


/* =========================================================
   COPIAR
========================================================= */

async function copyText() {

    const text =
        input.value.trim();


    if (!text) {

        showToast("Não há texto para copiar.");

        return;
    }


    try {

        await navigator.clipboard.writeText(text);

        showToast("Texto copiado.");

    } catch {

        showToast(
            "Não foi possível copiar automaticamente."
        );
    }
}


/* =========================================================
   LIMPAR
========================================================= */

function clearAll() {

    input.value = "";

    qrBox.innerHTML = "";

    qrBox.classList.remove("visible");

    qrActions.classList.remove("active");

    localStorage.removeItem(
        "qrHistory"
    );

    renderHistory();

    input.focus();

    showToast("Tudo foi limpo.");
}


/* =========================================================
   TOAST
========================================================= */

let toastTimer;


function showToast(message) {

    clearTimeout(toastTimer);


    toast.textContent =
        message;


    toast.classList.add("show");


    toastTimer =
        setTimeout(() => {

            toast.classList.remove("show");

        }, 2200);
}


/* =========================================================
   INICIALIZAÇÃO
========================================================= */

renderHistory();
