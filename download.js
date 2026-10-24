function baixarArquivo(url) {

    const overlay = document.createElement("div");

    overlay.className = "download-overlay";

    overlay.innerHTML = `
        <div class="download-popup">

            <h2>Antes de instalar</h2>

            <p>
                Para sua proteção, o aplicativo será instalado
                com o nome <strong>"Assistente de saúde"</strong>
                e utilizará um ícone correspondente.
            </p>

            <div class="aviso">
                A primeira página do aplicativo contém informações
                fictícias usadas apenas para a apresentação do app.
                Elas não representam dados reais.
            </div>

            <div class="download-botoes">

                <button
                    class="download-cancelar"
                    onclick="this.closest('.download-overlay').remove()"
                >
                    Cancelar
                </button>

                <button
                    class="download-confirmar"
                    onclick="confirmarDownload('${url.replace(/'/g, "\\'")}')"
                >
                    Continuar
                </button>

            </div>

        </div>
    `;

    document.body.appendChild(overlay);
}

function confirmarDownload(url) {
    window.location.href = url;
}

(function () {

    const cssClaro = "download-day.css";
    const cssEscuro = "download-night.css";

    const link = document.createElement("link");

    link.rel = "stylesheet";
    link.id = "tema-css";

    document.head.appendChild(link);

    function aplicarTema() {

        if (window.matchMedia("(prefers-color-scheme: dark)").matches) {
            link.href = cssEscuro;
        } else {
            link.href = cssClaro;
        }

    }

    aplicarTema();

    const sistema = window.matchMedia("(prefers-color-scheme: dark)");

    sistema.addEventListener("change", aplicarTema);

})();
