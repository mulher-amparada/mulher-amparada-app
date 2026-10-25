
/*
 * Mulher Amparada
 * Observatorio — Mapa de Calor Consciente
 *
 * Cada celula representa um commit real do Git.
 *
 * Recursos:
 * - Commits em ordem cronologica
 * - Cores baseadas no horario local do commit
 * - Intensidade baseada no intervalo entre commits
 * - Exatamente 20.000 commits por pagina, exceto a ultima
 * - Geracao de PNGs sequenciais
 * - Memoria dinamica, sem limite fixo de commits
 * - Fundo, grade, cabecalho, moldura e rodape preservados
 *
 * Removidos:
 * - Escudo central
 * - Contorno do escudo
 * - Iluminacao adicional das celulas
 * - Circulos e nucleos artificiais
 *
 * Arquivos gerados:
 * docs/observatorio/observatorio-001.png
 * docs/observatorio/observatorio-002.png
 * docs/observatorio/observatorio-003.png
 *
 * Este programa nao gera observatorio.png.
 *
 * Compilacao:
 * clang -O2 -Wall -Wextra tools/observatorio.c \
 *     -o observatorio \
 *     $(pkg-config --cflags --libs cairo) -lm
 *
 * Execucao:
 * ./observatorio
 */

#define _POSIX_C_SOURCE 200809L

#include <cairo/cairo.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <time.h>
#include <stdint.h>
#include <errno.h>
#include <limits.h>
#include <sys/stat.h>

#define WIDTH  1800
#define HEIGHT 1100

#define MAP_X  90
#define MAP_Y  150
#define MAP_W  1620
#define MAP_H  760

#define COMMITS_PER_IMAGE 20000

#define OUTPUT_DIR "docs/observatorio"

typedef struct {
    time_t timestamp;
    double heat;
    int hour;
} Commit;

static Commit *commits = NULL;
static size_t commit_count = 0;
static size_t commit_capacity = 0;


/* ============================================================
 * UTILITARIOS
 * ============================================================ */

static double clamp01(double value)
{
    if (value < 0.0)
        return 0.0;

    if (value > 1.0)
        return 1.0;

    return value;
}


static double lerp(double a, double b, double t)
{
    return a + (b - a) * t;
}


/* ============================================================
 * ARMAZENAMENTO DINAMICO
 * ============================================================ */

static int reserve_commits(size_t needed)
{
    if (needed <= commit_capacity)
        return 1;

    size_t capacity =
        commit_capacity ? commit_capacity : 4096;

    while (capacity < needed) {
        if (capacity > SIZE_MAX / 2) {
            capacity = needed;
            break;
        }

        capacity *= 2;
    }

    if (capacity > SIZE_MAX / sizeof(*commits)) {
        fprintf(
            stderr,
            "Erro: historico grande demais para a memoria.\n"
        );
        return 0;
    }

    Commit *new_commits = realloc(
        commits,
        capacity * sizeof(*commits)
    );

    if (!new_commits) {
        fprintf(
            stderr,
            "Erro: memoria insuficiente para os commits.\n"
        );
        return 0;
    }

    commits = new_commits;
    commit_capacity = capacity;

    return 1;
}


/* ============================================================
 * CARREGAR HISTORICO DO GIT
 *
 * git log --all considera commits alcancaveis pelas
 * referencias locais do repositorio.
 *
 * --reverse apresenta o historico em ordem cronologica.
 * ============================================================ */

static int load_commits(void)
{
    FILE *pipe = popen(
        "git log --format=%ct --reverse --all",
        "r"
    );

    if (!pipe) {
        perror("Erro ao executar git log");
        return 0;
    }

    char line[128];
    int allocation_error = 0;

    while (fgets(line, sizeof(line), pipe)) {
        char *end = NULL;

        errno = 0;

        long long value = strtoll(line, &end, 10);

        if (end == line || errno == ERANGE || value <= 0)
            continue;

        if ((long long)(time_t)value != value)
            continue;

        if (commit_count == SIZE_MAX ||
            !reserve_commits(commit_count + 1)) {
            allocation_error = 1;
            break;
        }

        Commit *commit = &commits[commit_count];

        commit->timestamp = (time_t)value;
        commit->heat = 0.0;
        commit->hour = 12;

        struct tm local_tm;

        if (localtime_r(&commit->timestamp, &local_tm))
            commit->hour = local_tm.tm_hour;

        commit_count++;
    }

    int pipe_status = pclose(pipe);

    if (allocation_error)
        return 0;

    if (pipe_status != 0) {
        fprintf(
            stderr,
            "Erro: git log terminou com status %d.\n",
            pipe_status
        );

        fprintf(
            stderr,
            "O historico pode estar incompleto; nenhuma geracao sera iniciada.\n"
        );

        return 0;
    }

    if (commit_count == 0) {
        fprintf(
            stderr,
            "Nenhum commit encontrado nas referencias locais.\n"
        );
        return 0;
    }

    return 1;
}


/* ============================================================
 * CALCULAR INTENSIDADE
 *
 * O calculo considera o historico inteiro, e nao apenas
 * os commits pertencentes a cada imagem.
 * ============================================================ */

static void calculate_heat(void)
{
    if (commit_count == 0)
        return;

    if (commit_count == 1) {
        commits[0].heat = 1.0;
        return;
    }

    double max_gap = 0.0;

    for (size_t i = 1; i < commit_count; i++) {
        double gap = difftime(
            commits[i].timestamp,
            commits[i - 1].timestamp
        );

        if (gap < 0.0)
            gap = 0.0;

        if (gap > max_gap)
            max_gap = gap;
    }

    if (max_gap <= 0.0)
        max_gap = 1.0;

    double denominator = log1p(max_gap);

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

        if (gap < 0.0)
            gap = 0.0;

        double normalized =
            1.0 - log1p(gap) / denominator;

        normalized = clamp01(normalized);

        commits[i].heat =
            0.18 + normalized * 0.82;
    }
}


/* ============================================================
 * COR BASEADA NO HORARIO
 *
 * Madrugada: azul/ciano
 * Dia:       azul/roxo
 * Noite:     roxo/rosa
 *
 * O calor modifica a luminosidade da propria celula.
 * Nenhuma segunda pintura e aplicada.
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
        double t = (hour - 6) / 12.0;

        rr = lerp(0.20, 0.65, t);
        gg = lerp(0.30, 0.08, t);
        bb = lerp(0.95, 0.85, t);

    } else {
        double t = (hour - 18) / 6.0;

        rr = lerp(0.65, 1.00, t);
        gg = 0.08;
        bb = lerp(0.85, 0.55, t);
    }

    double brightness = 0.55 + heat * 0.45;

    *r = clamp01(rr * brightness);
    *g = clamp01(gg * brightness);
    *b = clamp01(bb * brightness);
}


/* ============================================================
 * FUNDO E GRADE
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
 *
 * Cada commit do intervalo produz exatamente uma celula.
 *
 * Sem escudo, mascara central, circulos, nucleos,
 * segunda camada ou iluminacao adicional.
 *
 * A cor e opaca: nao existe transparencia variavel
 * capaz de criar um destaque adicional.
 * ============================================================ */

static void draw_heatmap(
    cairo_t *cr,
    size_t start,
    size_t count
)
{
    if (count == 0)
        return;

    size_t columns_size = (size_t)ceil(sqrt((double)count));

    if (columns_size == 0)
        columns_size = 1;

    int cols = (int)columns_size;

    size_t rows_size =
        count / columns_size +
        (count % columns_size != 0);

    int rows = (int)rows_size;

    double cell_w = (double)MAP_W / cols;
    double cell_h = (double)MAP_H / rows;

    for (size_t j = 0; j < count; j++) {
        size_t i = start + j;

        int col = (int)(j % columns_size);
        int row = (int)(j / columns_size);

        double x = MAP_X + col * cell_w;
        double y = MAP_Y + row * cell_h;

        double r, g, b;

        hour_color(
            commits[i].hour,
            commits[i].heat,
            &r,
            &g,
            &b
        );

        /*
         * Cor opaca: uma unica celula por commit.
         * Nenhum brilho, circulo ou destaque central.
         */
        cairo_set_source_rgb(cr, r, g, b);

        cairo_rectangle(
            cr,
            x + 1.0,
            y + 1.0,
            fmax(1.0, cell_w - 2.0),
            fmax(1.0, cell_h - 2.0)
        );

        cairo_fill(cr);
    }
}


/* ============================================================
 * TEXTO
 * ============================================================ */

static void draw_text(
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

    cairo_move_to(cr, x, y);
    cairo_show_text(cr, value);
}


/* ============================================================
 * CABECALHO
 * ============================================================ */

static void draw_header(
    cairo_t *cr,
    size_t page,
    size_t total_pages,
    size_t start,
    size_t count
)
{
    draw_text(
        cr,
        "MULHER AMPARADA",
        90, 65, 28,
        0.75, 1.00, 0.80, 1.0
    );

    draw_text(
        cr,
        "MAPA DE CALOR CONSCIENTE",
        90, 105, 17,
        0.35, 0.75, 1.00, 0.95
    );

    size_t columns_size =
        (size_t)ceil(sqrt((double)count));

    if (columns_size == 0)
        columns_size = 1;

    size_t rows_size =
        count / columns_size +
        (count % columns_size != 0);

    char info[256];

    snprintf(
        info,
        sizeof(info),
        "PAGINA %zu/%zu | %zu COMMITS",
        page,
        total_pages,
        count
    );

    draw_text(
        cr,
        info,
        1050, 65, 15,
        0.65, 0.70, 0.80, 0.95
    );

    snprintf(
        info,
        sizeof(info),
        "GRID %zux%zu | COMMITS %zu-%zu",
        columns_size,
        rows_size,
        start + 1,
        start + count
    );

    draw_text(
        cr,
        info,
        1050, 95, 13,
        0.40, 0.65, 0.80, 0.90
    );
}


/* ============================================================
 * RODAPE
 * ============================================================ */

static void draw_footer(
    cairo_t *cr,
    size_t start,
    size_t count
)
{
    const char *footer =
        "TARGET_SDK: 37  |  GRADLE: 9.6  |  VERSION_CODE: 33";

    draw_text(
        cr,
        footer,
        90, 1015, 14,
        0.40, 0.65, 0.75, 0.90
    );

    if (count == 0)
        return;

    size_t last = start + count - 1;

    char date[64];
    struct tm tm_value;

    if (localtime_r(&commits[last].timestamp, &tm_value)) {
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

    draw_text(
        cr,
        latest,
        1080, 1015, 14,
        0.80, 0.35, 0.75, 0.95
    );
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
 * CRIAR DIRETORIOS DE SAIDA
 * ============================================================ */

static int ensure_directory(const char *path)
{
    if (mkdir(path, 0755) == 0)
        return 1;

    if (errno != EEXIST) {
        perror(path);
        return 0;
    }

    struct stat st;

    if (stat(path, &st) != 0 || !S_ISDIR(st.st_mode)) {
        fprintf(
            stderr,
            "Erro: %s existe, mas nao e um diretorio.\n",
            path
        );
        return 0;
    }

    return 1;
}


static int ensure_output_directory(void)
{
    if (!ensure_directory("docs"))
        return 0;

    if (!ensure_directory(OUTPUT_DIR))
        return 0;

    return 1;
}


/* ============================================================
 * GERAR UMA IMAGEM SEQUENCIAL
 *
 * IMPORTANTE:
 * O nome e sempre observatorio-NNN.png.
 * Nunca escreve observatorio.png.
 * ============================================================ */

static int generate_image(
    size_t page,
    size_t total_pages,
    size_t start,
    size_t count
)
{
    char path[256];

    int written = snprintf(
        path,
        sizeof(path),
        OUTPUT_DIR "/observatorio-%03zu.png",
        page
    );

    if (written < 0 || (size_t)written >= sizeof(path)) {
        fprintf(stderr, "Erro: caminho de arquivo muito longo.\n");
        return 0;
    }

    cairo_surface_t *surface =
        cairo_image_surface_create(
            CAIRO_FORMAT_ARGB32,
            WIDTH,
            HEIGHT
        );

    cairo_status_t surface_status =
        cairo_surface_status(surface);

    if (surface_status != CAIRO_STATUS_SUCCESS) {
        fprintf(
            stderr,
            "Erro ao criar superficie Cairo: %s\n",
            cairo_status_to_string(surface_status)
        );

        cairo_surface_destroy(surface);
        return 0;
    }

    cairo_t *cr = cairo_create(surface);

    cairo_status_t context_status = cairo_status(cr);

    if (context_status != CAIRO_STATUS_SUCCESS) {
        fprintf(
            stderr,
            "Erro ao criar contexto Cairo: %s\n",
            cairo_status_to_string(context_status)
        );

        cairo_destroy(cr);
        cairo_surface_destroy(surface);
        return 0;
    }

    draw_background(cr);

    draw_header(
        cr,
        page,
        total_pages,
        start,
        count
    );

    draw_heatmap(cr, start, count);

    draw_map_border(cr);

    draw_footer(cr, start, count);

    cairo_status_t status = cairo_status(cr);

    if (status == CAIRO_STATUS_SUCCESS)
        status = cairo_surface_write_to_png(surface, path);

    if (status != CAIRO_STATUS_SUCCESS) {
        fprintf(
            stderr,
            "Erro ao gerar %s: %s\n",
            path,
            cairo_status_to_string(status)
        );

        cairo_destroy(cr);
        cairo_surface_destroy(surface);
        return 0;
    }

    cairo_destroy(cr);
    cairo_surface_destroy(surface);

    printf(
        "[OK] %s | commits %zu-%zu (%zu)\n",
        path,
        start + 1,
        start + count,
        count
    );

    return 1;
}


/* ============================================================
 * MAIN
 * ============================================================ */

int main(void)
{
    if (!load_commits()) {
        free(commits);
        commits = NULL;
        return 1;
    }

    printf("Commits carregados: %zu\n", commit_count);

    calculate_heat();

    if (!ensure_output_directory()) {
        free(commits);
        commits = NULL;
        return 1;
    }

    size_t total_pages =
        commit_count / COMMITS_PER_IMAGE;

    if (commit_count % COMMITS_PER_IMAGE != 0)
        total_pages++;

    printf("Imagens previstas: %zu\n", total_pages);
    printf("Limite por imagem: %d commits\n", COMMITS_PER_IMAGE);

    size_t processed = 0;
    size_t generated = 0;

    for (size_t page = 1; page <= total_pages; page++) {
        size_t remaining = commit_count - processed;

        size_t count = remaining;

        if (count > COMMITS_PER_IMAGE)
            count = COMMITS_PER_IMAGE;

        if (!generate_image(
                page,
                total_pages,
                processed,
                count
            )) {
            fprintf(
                stderr,
                "Processamento interrompido na pagina %zu.\n",
                page
            );

            fprintf(
                stderr,
                "Commits desenhados anteriormente: %zu de %zu\n",
                processed,
                commit_count
            );

            free(commits);
            commits = NULL;
            return 1;
        }

        processed += count;
        generated++;
    }

    printf("\n");
    printf("Observatorio concluido.\n");
    printf("Commits carregados: %zu\n", commit_count);
    printf("Commits desenhados: %zu\n", processed);
    printf("Imagens geradas: %zu\n", generated);
    printf("Diretorio: %s\n", OUTPUT_DIR);
    printf("Nome antigo observatorio.png nao e gerado.\n");

    free(commits);
    commits = NULL;

    return processed == commit_count ? 0 : 1;
}
