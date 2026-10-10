/*
 * Mulher Amparada
 * Observatório — Mapa de Calor Consciente
 *
 * Cada célula representa um commit real do Git.
 *
 * - Ordem cronológica dos commits
 * - Cor baseada no horário do commit
 * - Intensidade baseada na proximidade entre commits
 * - Grid cresce conforme o número de commits
 * - Escudo central emerge do próprio mapa
 * - Onda sonora matemática no centro
 *
 * Compilação:
 *   gcc -O2 -Wall -Wextra tools/observatorio.c \
 *       $(pkg-config --cflags --libs cairo) -lm \
 *       -o observatorio
 */

#include <cairo/cairo.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <time.h>
#include <stdint.h>

#define WIDTH  1800
#define HEIGHT 1100

#define MAP_X  90
#define MAP_Y  150
#define MAP_W  1620
#define MAP_H  760

#define MAX_COMMITS 200000

typedef struct {
    time_t timestamp;
    float heat;
    int hour;
} Commit;

static Commit commits[MAX_COMMITS];
static size_t commit_count = 0;


/* ============================================================
 * UTILITÁRIOS
 * ============================================================ */

static double clamp01(double x)
{
    if (x < 0.0) return 0.0;
    if (x > 1.0) return 1.0;
    return x;
}

static double lerp(double a, double b, double t)
{
    return a + (b - a) * t;
}


/* ============================================================
 * CARREGAR COMMITS REAIS DO GIT
 * ============================================================ */

static int load_commits(void)
{
    FILE *pipe = popen(
        "git log --format=%ct --reverse --all",
        "r"
    );

    if (!pipe) {
        fprintf(stderr, "Erro ao executar git log.\n");
        return 0;
    }

    char line[128];

    while (fgets(line, sizeof(line), pipe)) {

        if (commit_count >= MAX_COMMITS)
            break;

        long long value = atoll(line);

        if (value <= 0)
            continue;

        commits[commit_count].timestamp = (time_t)value;

        struct tm local_tm;

        if (localtime_r(
                &commits[commit_count].timestamp,
                &local_tm
            )) {

            commits[commit_count].hour =
                local_tm.tm_hour;
        } else {
            commits[commit_count].hour = 12;
        }

        commits[commit_count].heat = 0.0f;

        commit_count++;
    }

    pclose(pipe);

    return commit_count > 0;
}


/* ============================================================
 * CALCULAR INTENSIDADE DO CALOR
 *
 * Commits muito próximos no tempo ficam mais "quentes".
 * ============================================================ */

static void calculate_heat(void)
{
    if (commit_count == 0)
        return;

    if (commit_count == 1) {
        commits[0].heat = 1.0f;
        return;
    }

    double min_gap = 1e30;
    double max_gap = 0.0;

    for (size_t i = 1; i < commit_count; i++) {

        double gap = difftime(
            commits[i].timestamp,
            commits[i - 1].timestamp
        );

        if (gap < 0)
            gap = 0;

        if (gap < min_gap)
            min_gap = gap;

        if (gap > max_gap)
            max_gap = gap;
    }

    if (max_gap <= 0)
        max_gap = 1.0;

    for (size_t i = 0; i < commit_count; i++) {

        double gap;

        if (i == 0) {
            gap = difftime(
                commits[1].timestamp,
                commits[0].timestamp
            );
        } else {
            gap = difftime(
                commits[i].timestamp,
                commits[i - 1].timestamp
            );
        }

        if (gap < 0)
            gap = 0;

        /*
         * Quanto menor o intervalo,
         * maior a intensidade.
         */
        double normalized =
            1.0 -
            (log(1.0 + gap) /
             log(1.0 + max_gap));

        normalized = clamp01(normalized);

        /*
         * Mantém alguma luminosidade mesmo
         * nos commits mais isolados.
         */
        commits[i].heat =
            (float)(0.18 + normalized * 0.82);
    }
}


/* ============================================================
 * COR DO HORÁRIO
 *
 * Madrugada -> azul
 * Dia       -> roxo
 * Noite     -> rosa
 * ============================================================ */

static void hour_color(
    int hour,
    double heat,
    double *r,
    double *g,
    double *b
)
{
    double rr;
    double gg;
    double bb;

    if (hour < 6) {

        /*
         * Azul/ciano — madrugada
         */
        double t = hour / 6.0;

        rr = lerp(0.05, 0.20, t);
        gg = lerp(0.55, 0.30, t);
        bb = lerp(1.00, 0.95, t);

    } else if (hour < 18) {

        /*
         * Azul -> roxo
         */
        double t = (hour - 6) / 12.0;

        rr = lerp(0.20, 0.65, t);
        gg = lerp(0.30, 0.08, t);
        bb = lerp(0.95, 0.85, t);

    } else {

        /*
         * Roxo -> rosa
         */
        double t = (hour - 18) / 6.0;

        rr = lerp(0.65, 1.00, t);
        gg = lerp(0.08, 0.08, t);
        bb = lerp(0.85, 0.55, t);
    }

    /*
     * Intensidade do calor.
     */
    double brightness =
        0.55 + heat * 0.45;

    *r = clamp01(rr * brightness);
    *g = clamp01(gg * brightness);
    *b = clamp01(bb * brightness);
}


/* ============================================================
 * FUNDO
 * ============================================================ */

static void draw_background(cairo_t *cr)
{
    cairo_set_source_rgb(
        cr,
        0.005,
        0.006,
        0.012
    );

    cairo_paint(cr);

    /*
     * Grade muito discreta.
     */
    cairo_set_line_width(cr, 1.0);

    cairo_set_source_rgba(
        cr,
        0.15,
        0.20,
        0.30,
        0.13
    );

    for (int x = MAP_X; x <= MAP_X + MAP_W; x += 24) {

        cairo_move_to(cr, x, MAP_Y);
        cairo_line_to(cr, x, MAP_Y + MAP_H);
    }

    for (int y = MAP_Y; y <= MAP_Y + MAP_H; y += 24) {

        cairo_move_to(cr, MAP_X, y);
        cairo_line_to(cr, MAP_X + MAP_W, y);
    }

    cairo_stroke(cr);
}


/* ============================================================
 * MAPA DE CALOR
 * ============================================================ */

static void draw_heatmap(cairo_t *cr)
{
    if (commit_count == 0)
        return;

    /*
     * Grid quadrado baseado na quantidade de commits.
     *
     * 4920 commits:
     *
     * sqrt(4920) ≈ 70
     *
     * => aproximadamente 70 x 70
     */
    int cols =
        (int)ceil(sqrt((double)commit_count));

    if (cols < 1)
        cols = 1;

    int rows =
        (int)ceil(
            (double)commit_count /
            cols
        );

    double cell_w =
        (double)MAP_W / cols;

    double cell_h =
        (double)MAP_H / rows;

    for (size_t i = 0; i < commit_count; i++) {

        int col = (int)(i % cols);
        int row = (int)(i / cols);

        double x =
            MAP_X + col * cell_w;

        double y =
            MAP_Y + row * cell_h;

        double r, g, b;

        hour_color(
            commits[i].hour,
            commits[i].heat,
            &r,
            &g,
            &b
        );

        double heat =
            commits[i].heat;

        /*
         * Pequeno brilho retangular.
         *
         * Não é uma bolinha:
         * continua sendo a própria célula.
         */
        if (heat > 0.65) {

            cairo_set_source_rgba(
                cr,
                r,
                g,
                b,
                0.08 * heat
            );

            cairo_rectangle(
                cr,
                x - 2,
                y - 2,
                cell_w + 4,
                cell_h + 4
            );

            cairo_fill(cr);
        }

        /*
         * Célula principal.
         */
        cairo_set_source_rgba(
            cr,
            r,
            g,
            b,
            0.30 + heat * 0.70
        );

        cairo_rectangle(
            cr,
            x + 1,
            y + 1,
            fmax(1.0, cell_w - 2),
            fmax(1.0, cell_h - 2)
        );

        cairo_fill(cr);

        /*
         * Núcleo de commits muito intensos.
         */
        if (heat > 0.82) {

            cairo_set_source_rgba(
                cr,
                0.85,
                0.95,
                1.0,
                0.22 * heat
            );

            cairo_rectangle(
                cr,
                x + cell_w * 0.25,
                y + cell_h * 0.25,
                cell_w * 0.50,
                cell_h * 0.50
            );

            cairo_fill(cr);
        }
    }
}


/* ============================================================
 * ESCUDO CENTRAL
 *
 * O escudo NÃO é uma figura sólida.
 * Ele aumenta o brilho das células que já existem.
 * ============================================================ */

static int inside_shield(
    double x,
    double y,
    double cx,
    double cy,
    double width,
    double height
)
{
    double nx =
        (x - cx) / width;

    double ny =
        (y - cy) / height;

    if (fabs(nx) > 1.0)
        return 0;

    if (ny < -1.0 || ny > 1.0)
        return 0;

    /*
     * Curva superior + ponta inferior.
     */
    double top =
        -0.80 +
        0.45 * nx * nx;

    if (ny < top)
        return 0;

    double bottom =
        0.85 -
        0.25 * fabs(nx);

    if (ny > bottom)
        return 0;

    return 1;
}


static void illuminate_shield(cairo_t *cr)
{
    if (commit_count == 0)
        return;

    int cols =
        (int)ceil(sqrt((double)commit_count));

    int rows =
        (int)ceil(
            (double)commit_count /
            cols
        );

    double cell_w =
        (double)MAP_W / cols;

    double cell_h =
        (double)MAP_H / rows;

    double cx =
        MAP_X + MAP_W * 0.50;

    double cy =
        MAP_Y + MAP_H * 0.50;

    double shield_w =
        MAP_W * 0.25;

    double shield_h =
        MAP_H * 0.36;

    for (size_t i = 0; i < commit_count; i++) {

        int col = (int)(i % cols);
        int row = (int)(i / cols);

        double x =
            MAP_X +
            col * cell_w +
            cell_w * 0.5;

        double y =
            MAP_Y +
            row * cell_h +
            cell_h * 0.5;

        if (!inside_shield(
                x,
                y,
                cx,
                cy,
                shield_w,
                shield_h
            ))
            continue;

        double intensity =
            commits[i].heat;

        /*
         * Verde-limão + azul neon.
         */
        double r =
            lerp(0.10, 0.65, intensity);

        double g =
            lerp(0.70, 1.00, intensity);

        double b =
            lerp(0.35, 0.95, intensity);

        cairo_set_source_rgba(
            cr,
            r,
            g,
            b,
            0.25 + intensity * 0.60
        );

        cairo_rectangle(
            cr,
            MAP_X + col * cell_w + 1,
            MAP_Y + row * cell_h + 1,
            fmax(1.0, cell_w - 2),
            fmax(1.0, cell_h - 2)
        );

        cairo_fill(cr);
    }
}


/* ============================================================
 * ONDA SONORA
 * ============================================================ */

static void draw_wave(cairo_t *cr)
{
    double cx =
        MAP_X + MAP_W * 0.50;

    double cy =
        MAP_Y + MAP_H * 0.50;

    cairo_set_line_width(cr, 4.0);

    cairo_set_source_rgba(
        cr,
        0.55,
        1.00,
        0.35,
        0.95
    );

    cairo_new_path(cr);

    int points = 260;

    for (int i = 0; i < points; i++) {

        double t =
            (double)i /
            (points - 1);

        double x =
            cx -
            260.0 +
            t * 520.0;

        double envelope =
            sin(M_PI * t);

        double amplitude =
            72.0 * envelope;

        double y =
            cy +
            sin(t * M_PI * 10.0) *
            amplitude;

        if (i == 0)
            cairo_move_to(cr, x, y);
        else
            cairo_line_to(cr, x, y);
    }

    cairo_stroke(cr);

    /*
     * Segunda camada azul.
     */
    cairo_set_line_width(cr, 1.5);

    cairo_set_source_rgba(
        cr,
        0.20,
        0.70,
        1.00,
        0.85
    );

    cairo_new_path(cr);

    for (int i = 0; i < points; i++) {

        double t =
            (double)i /
            (points - 1);

        double x =
            cx -
            260.0 +
            t * 520.0;

        double amplitude =
            82.0 * sin(M_PI * t);

        double y =
            cy +
            sin(t * M_PI * 10.0) *
            amplitude;

        if (i == 0)
            cairo_move_to(cr, x, y);
        else
            cairo_line_to(cr, x, y);
    }

    cairo_stroke(cr);
}


/* ============================================================
 * CONTORNO DO ESCUDO
 * ============================================================ */

static void draw_shield_outline(cairo_t *cr)
{
    double cx =
        MAP_X + MAP_W * 0.50;

    double cy =
        MAP_Y + MAP_H * 0.50;

    double w =
        MAP_W * 0.25;

    double h =
        MAP_H * 0.36;

    cairo_new_path(cr);

    cairo_move_to(
        cr,
        cx,
        cy - h
    );

    cairo_curve_to(
        cr,
        cx + w * 0.70,
        cy - h * 0.80,
        cx + w,
        cy - h * 0.55,
        cx + w,
        cy - h * 0.20
    );

    cairo_curve_to(
        cr,
        cx + w,
        cy + h * 0.35,
        cx + w * 0.45,
        cy + h * 0.75,
        cx,
        cy + h
    );

    cairo_curve_to(
        cr,
        cx - w * 0.45,
        cy + h * 0.75,
        cx - w,
        cy + h * 0.35,
        cx - w,
        cy - h * 0.20
    );

    cairo_curve_to(
        cr,
        cx - w,
        cy - h * 0.55,
        cx - w * 0.70,
        cy - h * 0.80,
        cx,
        cy - h
    );

    cairo_close_path(cr);

    cairo_set_line_width(cr, 3.0);

    cairo_set_source_rgba(
        cr,
        0.55,
        1.00,
        0.30,
        0.85
    );

    cairo_stroke(cr);

    /*
     * Segunda linha azul.
     */
    cairo_set_line_width(cr, 1.0);

    cairo_set_source_rgba(
        cr,
        0.15,
        0.65,
        1.00,
        0.75
    );

    cairo_stroke(cr);
}


/* ============================================================
 * TEXTO
 * ============================================================ */

static void text(
    cairo_t *cr,
    const char *value,
    double x,
    double y,
    double size,
    double r,
    double g,
    double b,
    double alpha
)
{
    cairo_select_font_face(
        cr,
        "DejaVu Sans Mono",
        CAIRO_FONT_SLANT_NORMAL,
        CAIRO_FONT_WEIGHT_NORMAL
    );

    cairo_set_font_size(cr, size);

    cairo_set_source_rgba(
        cr,
        r,
        g,
        b,
        alpha
    );

    cairo_move_to(
        cr,
        x,
        y
    );

    cairo_show_text(
        cr,
        value
    );
}


static void draw_header(cairo_t *cr)
{
    text(
        cr,
        "MULHER AMPARADA",
        90,
        65,
        28,
        0.75,
        1.00,
        0.80,
        1.0
    );

    text(
        cr,
        "MAPA DE CALOR CONSCIENTE",
        90,
        105,
        17,
        0.35,
        0.75,
        1.00,
        0.95
    );

    char info[256];

    snprintf(
        info,
        sizeof(info),
        "%zu COMMITS  |  GRID %dx%d",
        commit_count,
        (int)ceil(sqrt((double)commit_count)),
        (int)ceil(
            (double)commit_count /
            ceil(sqrt((double)commit_count))
        )
    );

    text(
        cr,
        info,
        1210,
        75,
        15,
        0.65,
        0.70,
        0.80,
        0.90
    );
}


static void draw_footer(cairo_t *cr)
{
    char footer[256];

    snprintf(
        footer,
        sizeof(footer),
        "TARGET_SDK: 37  |  GRADLE: 9.6  |  VERSION_CODE: 33"
    );

    text(
        cr,
        footer,
        90,
        1015,
        14,
        0.40,
        0.65,
        0.75,
        0.90
    );

    if (commit_count > 0) {

        char date[64];

        struct tm tm_value;

        if (localtime_r(
                &commits[commit_count - 1].timestamp,
                &tm_value
            )) {

            strftime(
                date,
                sizeof(date),
                "%Y-%m-%d %H:%M",
                &tm_value
            );
        } else {
            strcpy(date, "unknown");
        }

        char latest[256];

        snprintf(
            latest,
            sizeof(latest),
            "LATEST COMMIT: %s",
            date
        );

        text(
            cr,
            latest,
            1320,
            1015,
            14,
            0.80,
            0.35,
            0.75,
            0.95
        );
    }
}


/* ============================================================
 * BORDA DO MAPA
 * ============================================================ */

static void draw_map_border(cairo_t *cr)
{
    cairo_set_line_width(cr, 1.0);

    cairo_set_source_rgba(
        cr,
        0.25,
        0.55,
        0.75,
        0.30
    );

    cairo_rectangle(
        cr,
        MAP_X,
        MAP_Y,
        MAP_W,
        MAP_H
    );

    cairo_stroke(cr);
}


/* ============================================================
 * MAIN
 * ============================================================ */

int main(void)
{
    if (!load_commits()) {

        fprintf(
            stderr,
            "Nenhum commit encontrado.\n"
        );

        return 1;
    }

    calculate_heat();

    cairo_surface_t *surface =
        cairo_image_surface_create(
            CAIRO_FORMAT_ARGB32,
            WIDTH,
            HEIGHT
        );

    cairo_t *cr =
        cairo_create(surface);

    /*
     * 1. Fundo
     */
    draw_background(cr);

    /*
     * 2. Cabeçalho
     */
    draw_header(cr);

    /*
     * 3. MAPA DE CALOR REAL
     *
     * Cada quadrado = um commit.
     */
    draw_heatmap(cr);

    /*
     * 4. O próprio mapa ilumina
     *    o escudo central.
     */
    illuminate_shield(cr);

    /*
     * 5. Escudo
     */
    draw_shield_outline(cr);

    /*
     * 6. Onda sonora
     */
    draw_wave(cr);

    /*
     * 7. Moldura
     */
    draw_map_border(cr);

    /*
     * 8. Rodapé
     */
    draw_footer(cr);

    cairo_surface_write_to_png(
        surface,
        "docs/observatorio/observatorio.png"
    );

    cairo_destroy(cr);
    cairo_surface_destroy(surface);

    printf(
        "Observatorio gerado com %zu commits.\n",
        commit_count
    );

    return 0;
}

E troque o workflow anterior por este, porque agora o C gera PNG diretamente com Cairo, sem precisar do ImageMagick:

name: 🌌 Observatório

on:
  push:
    branches:
      - main

  workflow_dispatch:

permissions:
  contents: write

jobs:
  observatorio:
    name: 🌌 Gerar mapa de calor
    runs-on: ubuntu-24.04

    steps:
      - name: 📥 Checkout completo
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: 🔧 Instalar Cairo e GCC
        run: |
          sudo apt-get update
          sudo apt-get install -y \
            gcc \
            libcairo2-dev \
            pkg-config

      - name: 🧱 Compilar C
        run: |
          gcc \
            -O2 \
            -Wall \
            -Wextra \
            tools/observatorio.c \
            $(pkg-config --cflags --libs cairo) \
            -lm \
            -o observatorio

      - name: 🌌 Gerar mapa de calor
        run: |
          mkdir -p docs/observatorio
          ./observatorio

      - name: 💾 Publicar imagem
        run: |
          git config user.name "github-actions[bot]"
          git config user.email "41898282+github-actions[bot]@users.noreply.github.com"

          git add docs/observatorio/observatorio.png

          if git diff --cached --quiet; then
            echo "Nenhuma alteração na imagem."
            exit 0
          fi

          git commit -m "chore: atualizar mapa de calor"
          git push

No README:

## 🌌 Observatório do Projeto

O Mulher Amparada possui um observatório visual gerado diretamente em C a partir do histórico real do Git.

Cada célula representa um commit. A intensidade representa a concentração de atividade e as cores representam os períodos em que o projeto foi desenvolvido.

![Mapa de Calor Consciente](docs/observatorio/observatorio.png)

Agora não existem aquelas 180 bolinhas. O elemento principal é o grid: "1 commit = 1 célula". O escudo e a onda também usam o próprio grid, em vez de serem desenhos aleatórios colocados por cima.