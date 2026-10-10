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
 * - Escudo central emerge das próprias células
 *
 * Sem:
 * - contorno externo do escudo
 * - segundo contorno
 * - núcleo dentro das células
 * - brilho externo das células
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
    if (x < 0.0)
        return 0.0;

    if (x > 1.0)
        return 1.0;

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
        fprintf(
            stderr,
            "Erro ao executar git log.\n"
        );

        return 0;
    }

    char line[128];

    while (fgets(line, sizeof(line), pipe)) {

        if (commit_count >= MAX_COMMITS)
            break;

        long long value = atoll(line);

        if (value <= 0)
            continue;

        commits[commit_count].timestamp =
            (time_t)value;

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
 * ============================================================ */

static void calculate_heat(void)
{
    if (commit_count == 0)
        return;

    if (commit_count == 1) {
        commits[0].heat = 1.0f;
        return;
    }

    double max_gap = 0.0;

    for (size_t i = 1; i < commit_count; i++) {

        double gap = difftime(
            commits[i].timestamp,
            commits[i - 1].timestamp
        );

        if (gap < 0)
            gap = 0;

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

        double normalized =
            1.0 -
            (
                log(1.0 + gap) /
                log(1.0 + max_gap)
            );

        normalized = clamp01(normalized);

        commits[i].heat =
            (float)(
                0.18 +
                normalized * 0.82
            );
    }
}


/* ============================================================
 * COR DO HORÁRIO
 *
 * Madrugada -> azul/ciano
 * Dia       -> azul/roxo
 * Noite     -> roxo/rosa
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

        double t = hour / 6.0;

        rr = lerp(0.05, 0.20, t);
        gg = lerp(0.55, 0.30, t);
        bb = lerp(1.00, 0.95, t);

    } else if (hour < 18) {

        double t =
            (hour - 6) / 12.0;

        rr = lerp(0.20, 0.65, t);
        gg = lerp(0.30, 0.08, t);
        bb = lerp(0.95, 0.85, t);

    } else {

        double t =
            (hour - 18) / 6.0;

        rr = lerp(0.65, 1.00, t);
        gg = lerp(0.08, 0.08, t);
        bb = lerp(0.85, 0.55, t);
    }

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

    cairo_set_line_width(cr, 1.0);

    cairo_set_source_rgba(
        cr,
        0.15,
        0.20,
        0.30,
        0.13
    );

    for (
        int x = MAP_X;
        x <= MAP_X + MAP_W;
        x += 24
    ) {

        cairo_move_to(
            cr,
            x,
            MAP_Y
        );

        cairo_line_to(
            cr,
            x,
            MAP_Y + MAP_H
        );
    }

    for (
        int y = MAP_Y;
        y <= MAP_Y + MAP_H;
        y += 24
    ) {

        cairo_move_to(
            cr,
            MAP_X,
            y
        );

        cairo_line_to(
            cr,
            MAP_X + MAP_W,
            y
        );
    }

    cairo_stroke(cr);
}


/* ============================================================
 * MAPA DE CALOR
 *
 * Cada célula = exatamente um commit.
 *
 * Não existe:
 * - núcleo interno
 * - ponto
 * - bola
 * - brilho externo
 * - célula adicional
 * ============================================================ */

static void draw_heatmap(cairo_t *cr)
{
    if (commit_count == 0)
        return;

    int cols =
        (int)ceil(
            sqrt((double)commit_count)
        );

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

        int col =
            (int)(i % cols);

        int row =
            (int)(i / cols);

        double x =
            MAP_X +
            col * cell_w;

        double y =
            MAP_Y +
            row * cell_h;

        double r;
        double g;
        double b;

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
         * UMA ÚNICA CÉLULA POR COMMIT.
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
    }
}


/* ============================================================
 * ESCUDO CENTRAL
 *
 * O escudo não possui desenho externo.
 *
 * Ele é formado exclusivamente pelas próprias
 * células existentes no mapa.
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
     * Parte superior.
     */
    double top =
        -0.80 +
        0.45 * nx * nx;

    if (ny < top)
        return 0;

    /*
     * Parte inferior.
     */
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
        (int)ceil(
            sqrt((double)commit_count)
        );

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
        MAP_X +
        MAP_W * 0.50;

    double cy =
        MAP_Y +
        MAP_H * 0.50;

    double shield_w =
        MAP_W * 0.25;

    double shield_h =
        MAP_H * 0.36;

    for (size_t i = 0; i < commit_count; i++) {

        int col =
            (int)(i % cols);

        int row =
            (int)(i / cols);

        double cell_cx =
            MAP_X +
            col * cell_w +
            cell_w * 0.5;

        double cell_cy =
            MAP_Y +
            row * cell_h +
            cell_h * 0.5;

        if (!inside_shield(
                cell_cx,
                cell_cy,
                cx,
                cy,
                shield_w,
                shield_h
            ))
            continue;

        /*
         * Mantém a célula original,
         * apenas aumentando discretamente
         * sua presença.
         *
         * Não cria nenhuma forma nova.
         */
        double heat =
            commits[i].heat;

        double original_r;
        double original_g;
        double original_b;

        hour_color(
            commits[i].hour,
            heat,
            &original_r,
            &original_g,
            &original_b
        );

        /*
         * Mistura a cor original com
         * azul/ciano do escudo.
         */
        double r =
            lerp(
                original_r,
                0.25,
                0.42
            );

        double g =
            lerp(
                original_g,
                0.90,
                0.42
            );

        double b =
            lerp(
                original_b,
                1.00,
                0.42
            );

        cairo_set_source_rgba(
            cr,
            clamp01(r),
            clamp01(g),
            clamp01(b),
            0.55 + heat * 0.40
        );

        cairo_rectangle(
            cr,
            MAP_X +
                col * cell_w +
                1,
            MAP_Y +
                row * cell_h +
                1,
            fmax(
                1.0,
                cell_w - 2
            ),
            fmax(
                1.0,
                cell_h - 2
            )
        );

        cairo_fill(cr);
    }
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

    cairo_set_font_size(
        cr,
        size
    );

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

    int cols =
        (int)ceil(
            sqrt((double)commit_count)
        );

    int rows =
        (int)ceil(
            (double)commit_count /
            cols
        );

    snprintf(
        info,
        sizeof(info),
        "%zu COMMITS  |  GRID %dx%d",
        commit_count,
        cols,
        rows
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


/* ============================================================
 * RODAPÉ
 * ============================================================ */

static void draw_footer(cairo_t *cr)
{
    const char *footer =
        "TARGET_SDK: 37  |  GRADLE: 9.6  |  VERSION_CODE: 33";

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
                &commits[
                    commit_count - 1
                ].timestamp,
                &tm_value
            )) {

            strftime(
                date,
                sizeof(date),
                "%Y-%m-%d %H:%M",
                &tm_value
            );

        } else {

            strcpy(
                date,
                "unknown"
            );
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
    cairo_set_line_width(
        cr,
        1.0
    );

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
     * 3. Cada quadrado representa
     *    exatamente um commit.
     */
    draw_heatmap(cr);

    /*
     * 4. O escudo emerge das próprias
     *    células existentes.
     *
     *    Nenhum contorno é desenhado.
     */
    illuminate_shield(cr);

    /*
     * 5. Moldura do mapa.
     */
    draw_map_border(cr);

    /*
     * 6. Rodapé.
     */
    draw_footer(cr);

    cairo_status_t status =
        cairo_surface_write_to_png(
            surface,
            "docs/observatorio/observatorio.png"
        );

    if (status != CAIRO_STATUS_SUCCESS) {

        fprintf(
            stderr,
            "Erro ao salvar PNG: %s\n",
            cairo_status_to_string(status)
        );

        cairo_destroy(cr);
        cairo_surface_destroy(surface);

        return 1;
    }

    cairo_destroy(cr);
    cairo_surface_destroy(surface);

    printf(
        "Observatorio gerado com %zu commits.\n",
        commit_count
    );

    return 0;
}