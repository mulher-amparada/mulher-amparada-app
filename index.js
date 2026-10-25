/* =========================================================
   CONTROLE DE NAVEGAÇÃO
========================================================= */

let navegando =
    false;


/* =========================================================
   VIBRAÇÃO
========================================================= */

function vibrar(
    duracao = 40
) {

    try {

        if (
            "vibrate" in navigator &&
            typeof navigator.vibrate === "function"
        ) {

            navigator.vibrate(
                duracao
            );

        }

    } catch (erro) {

        console.warn(
            "Vibração indisponível.",
            erro
        );

    }

}


/* =========================================================
   NAVEGAÇÃO
========================================================= */

function ir(
    page
) {

    if (
        typeof page !== "string" ||
        !page.trim()
    ) {

        return;

    }

    sairDaPagina(
        page
    );

}


function navigate(
    page
) {

    if (
        typeof page !== "string" ||
        !page.trim()
    ) {

        return;

    }

    sairDaPagina(
        page
    );

}


/* =========================================================
   SAÍDA COM FADE
========================================================= */

function sairDaPagina(
    destino
) {

    if (
        navegando ||
        typeof destino !== "string" ||
        !destino.trim()
    ) {

        return;

    }

    navegando =
        true;

    vibrar(
        40
    );
    window.location.href = destino;

}


/* =========================================================
   LIGAÇÕES
========================================================= */

function ligar(
    numero
) {

    if (
        typeof numero !== "string" ||
        !numero.trim()
    ) {

        return;

    }

    vibrar(
        50
    );

    try {

        window.location.href =
            "tel:" + numero;

    } catch (erro) {

        console.warn(
            "Não foi possível iniciar a ligação.",
            erro
        );

    }

}


/* =========================================================
   CARDS DOS APLICATIVOS
========================================================= */

document
    .querySelectorAll(".app-card")
    .forEach(
        function(card) {

            const destino =
                card.dataset.page ||
                card.getAttribute("href");

            card.addEventListener(
                "click",
                function(event) {

                    event.preventDefault();

                    sairDaPagina(
                        destino
                    );

                }
            );

        }
    );


/* =========================================================
   REPOSITÓRIO
========================================================= */

const repoButton =
    document.querySelector(
        ".repo-button"
    );

if (repoButton) {

    repoButton.addEventListener(
        "click",
        function() {

            vibrar(
                40
            );

        }
    );

}


/* =========================================================
   RESTAURAÇÃO
========================================================= */

window.addEventListener(
    "pageshow",
    function() {

        navegando =
            false;

        document.body.classList.remove(
            "fade-out"
        );

        document.body.style.pointerEvents =
            "auto";

    }
);
