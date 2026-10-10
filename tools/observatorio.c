#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <time.h>
#include <sys/stat.h>
#include <sys/types.h>

#define WIDTH 1600
#define HEIGHT 1000

#define BG_R 5
#define BG_G 7
#define BG_B 12

typedef struct {
    unsigned char r, g, b, a;
} Pixel;

typedef struct {
    float x, y;
    float radius;
    unsigned char r, g, b;
    char name[64];
} Node;

static Pixel image[HEIGHT][WIDTH];

/* =========================================================
   PIXEL
   ========================================================= */

static void pixel(int x, int y,
                  unsigned char r,
                  unsigned char g,
                  unsigned char b,
                  unsigned char a)
{
    if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT)
        return;

    image[y][x].r = r;
    image[y][x].g = g;
    image[y][x].b = b;
    image[y][x].a = a;
}

/* =========================================================
   BLEND
   ========================================================= */

static void blend_pixel(int x, int y,
                        unsigned char r,
                        unsigned char g,
                        unsigned char b,
                        unsigned char a)
{
    if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT)
        return;

    Pixel *p = &image[y][x];

    float alpha = a / 255.0f;

    p->r = (unsigned char)(p->r * (1.0f - alpha) + r * alpha);
    p->g = (unsigned char)(p->g * (1.0f - alpha) + g * alpha);
    p->b = (unsigned char)(p->b * (1.0f - alpha) + b * alpha);
}

/* =========================================================
   CLEAR
   ========================================================= */

static void clear_image(void)
{
    for (int y = 0; y < HEIGHT; y++) {
        for (int x = 0; x < WIDTH; x++) {
            image[y][x].r = BG_R;
            image[y][x].g = BG_G;
            image[y][x].b = BG_B;
            image[y][x].a = 255;
        }
    }
}

/* =========================================================
   CIRCLE
   ========================================================= */

static void circle(float cx, float cy, float radius,
                   unsigned char r,
                   unsigned char g,
                   unsigned char b)
{
    int minx = (int)(cx - radius);
    int maxx = (int)(cx + radius);
    int miny = (int)(cy - radius);
    int maxy = (int)(cy + radius);

    float rr = radius * radius;

    for (int y = miny; y <= maxy; y++) {
        for (int x = minx; x <= maxx; x++) {

            float dx = x - cx;
            float dy = y - cy;

            if (dx * dx + dy * dy <= rr)
                pixel(x, y, r, g, b, 255);
        }
    }
}

/* =========================================================
   GLOW
   ========================================================= */

static void glow(float cx, float cy, float radius,
                 unsigned char r,
                 unsigned char g,
                 unsigned char b)
{
    int minx = (int)(cx - radius);
    int maxx = (int)(cx + radius);
    int miny = (int)(cy - radius);
    int maxy = (int)(cy + radius);

    float rr = radius * radius;

    for (int y = miny; y <= maxy; y++) {
        for (int x = minx; x <= maxx; x++) {

            float dx = x - cx;
            float dy = y - cy;
            float d2 = dx * dx + dy * dy;

            if (d2 > rr)
                continue;

            float d = sqrtf(d2);
            float a = 1.0f - d / radius;

            a = a * a * 0.35f;

            blend_pixel(
                x,
                y,
                r,
                g,
                b,
                (unsigned char)(a * 255)
            );
        }
    }
}

/* =========================================================
   LINE
   ========================================================= */

static void line(float x1, float y1,
                 float x2, float y2,
                 unsigned char r,
                 unsigned char g,
                 unsigned char b,
                 unsigned char a)
{
    int steps = (int)fmaxf(fabsf(x2 - x1), fabsf(y2 - y1));

    if (steps <= 0)
        steps = 1;

    for (int i = 0; i <= steps; i++) {

        float t = (float)i / steps;

        int x = (int)(x1 + (x2 - x1) * t);
        int y = (int)(y1 + (y2 - y1) * t);

        blend_pixel(x, y, r, g, b, a);
    }
}

/* =========================================================
   GRID
   ========================================================= */

static void draw_grid(int commits)
{
    int cells = (int)sqrt((double)(commits > 0 ? commits : 1));

    if (cells < 8)
        cells = 8;

    if (cells > 100)
        cells = 100;

    float cell = 6.0f;

    float total = cells * cell;

    float start_x = WIDTH / 2.0f - total / 2.0f;
    float start_y = HEIGHT / 2.0f - total / 2.0f;

    for (int gy = 0; gy < cells; gy++) {

        for (int gx = 0; gx < cells; gx++) {

            int index = gy * cells + gx;

            if (index >= commits)
                continue;

            /*
             * Distribuição determinística.
             * Em uma execução real, cada célula pode ser
             * associada diretamente ao timestamp do commit.
             */

            float phase = index * 0.137f;

            float wave =
                (sinf(phase) + 1.0f) * 0.5f;

            unsigned char r =
                (unsigned char)(25 + wave * 70);

            unsigned char g =
                (unsigned char)(45 + wave * 120);

            unsigned char b =
                (unsigned char)(100 + wave * 130);

            int x = (int)(start_x + gx * cell);
            int y = (int)(start_y + gy * cell);

            for (int py = 0; py < 4; py++) {
                for (int px = 0; px < 4; px++) {
                    blend_pixel(
                        x + px,
                        y + py,
                        r,
                        g,
                        b,
                        180
                    );
                }
            }
        }
    }
}

/* =========================================================
   ESCUDO
   ========================================================= */

static void draw_shield(float cx, float cy)
{
    glow(cx, cy, 180, 100, 255, 180);
    glow(cx, cy, 90, 80, 255, 200);

    /*
     * Escudo geométrico.
     */

    for (int y = -110; y <= 110; y++) {

        float yy = y / 110.0f;

        float half =
            95.0f * (1.0f - fabsf(yy) * 0.45f);

        if (y > 20)
            half *= 0.85f;

        line(
            cx - half,
            cy + y,
            cx + half,
            cy + y,
            70,
            255,
            190,
            20
        );
    }

    /*
     * Contorno do escudo.
     */

    for (int i = 0; i < 360; i++) {

        float a = i * (float)M_PI / 180.0f;

        float x =
            cx + cosf(a) * 105.0f;

        float y =
            cy + sinf(a) * 125.0f;

        blend_pixel(
            (int)x,
            (int)y,
            120,
            255,
            210,
            230
        );
    }

    /*
     * Onda sonora.
     */

    float previous_x = cx - 80;
    float previous_y = cy;

    for (int i = 0; i <= 160; i++) {

        float x = cx - 80 + i;

        float t = i / 160.0f;

        float y =
            cy +
            sinf(t * 6.0f * (float)M_PI)
            * 22.0f *
            expf(-fabsf(t - 0.5f) * 1.5f);

        line(
            previous_x,
            previous_y,
            x,
            y,
            170,
            255,
            220,
            240
        );

        previous_x = x;
        previous_y = y;
    }
}

/* =========================================================
   NÓS
   ========================================================= */

static void draw_nodes(void)
{
    Node nodes[] = {

        {300, 270, 30, 90, 180, 255, "ANDROID"},
        {470, 180, 25, 150, 110, 255, "KOTLIN"},
        {650, 140, 22, 100, 220, 255, "JAVA"},
        {950, 140, 22, 180, 100, 255, "C/C++"},
        {1130, 190, 25, 255, 100, 180, "XML"},
        {1290, 300, 30, 255, 150, 100, "GITHUB"},
        {1340, 500, 28, 100, 220, 255, "SOS"},
        {1260, 690, 28, 120, 255, 180, "SENSORES"},
        {1080, 790, 25, 255, 100, 180, "LOCALIZAÇÃO"},
        {850, 840, 22, 120, 180, 255, "DOCS"},
        {600, 820, 24, 180, 100, 255, "CI/CD"},
        {390, 690, 28, 100, 255, 200, "SEGURANÇA"}
    };

    int count =
        sizeof(nodes) / sizeof(nodes[0]);

    float cx = WIDTH / 2.0f;
    float cy = HEIGHT / 2.0f;

    for (int i = 0; i < count; i++) {

        Node *n = &nodes[i];

        line(
            cx,
            cy,
            n->x,
            n->y,
            n->r,
            n->g,
            n->b,
            35
        );

        glow(
            n->x,
            n->y,
            n->radius * 4,
            n->r,
            n->g,
            n->b
        );

        circle(
            n->x,
            n->y,
            n->radius,
            n->r,
            n->g,
            n->b
        );

        circle(
            n->x,
            n->y,
            n->radius * 0.35f,
            235,
            245,
            255
        );
    }
}

/* =========================================================
   CONTAGEM DE COMMITS
   ========================================================= */

static int count_commits(void)
{
    FILE *pipe =
        popen("git rev-list --count HEAD 2>/dev/null", "r");

    if (!pipe)
        return 0;

    int commits = 0;

    fscanf(pipe, "%d", &commits);

    pclose(pipe);

    return commits;
}

/* =========================================================
   ÚLTIMO COMMIT
   ========================================================= */

static void get_commit_hash(char *buffer, size_t size)
{
    FILE *pipe =
        popen("git rev-parse --short HEAD 2>/dev/null", "r");

    if (!pipe) {
        snprintf(buffer, size, "unknown");
        return;
    }

    if (!fgets(buffer, size, pipe))
        snprintf(buffer, size, "unknown");

    pclose(pipe);

    buffer[strcspn(buffer, "\r\n")] = '\0';
}

/* =========================================================
   PNG
   ========================================================= */

/*
 * Para evitar dependências externas no código principal,
 * esta função escreve um PPM temporário.
 *
 * O workflow converte o PPM para PNG.
 */

static int write_ppm(const char *filename)
{
    FILE *f = fopen(filename, "wb");

    if (!f)
        return 0;

    fprintf(
        f,
        "P6\n%d %d\n255\n",
        WIDTH,
        HEIGHT
    );

    for (int y = 0; y < HEIGHT; y++) {

        for (int x = 0; x < WIDTH; x++) {

            fputc(image[y][x].r, f);
            fputc(image[y][x].g, f);
            fputc(image[y][x].b, f);
        }
    }

    fclose(f);

    return 1;
}

/* =========================================================
   MAIN
   ========================================================= */

int main(void)
{
    mkdir("docs", 0755);
    mkdir("docs/observatorio", 0755);

    clear_image();

    int commits = count_commits();

    char hash[64];

    get_commit_hash(hash, sizeof(hash));

    /*
     * Fundo generativo
     */

    draw_grid(commits);

    /*
     * Constelação
     */

    draw_nodes();

    /*
     * Vigilante digital
     */

    draw_shield(
        WIDTH / 2.0f,
        HEIGHT / 2.0f
    );

    /*
     * Pequenos pontos atmosféricos
     */

    for (int i = 0; i < 180; i++) {

        float x =
            fmodf(
                i * 97.31f,
                WIDTH
            );

        float y =
            fmodf(
                i * 53.17f,
                HEIGHT
            );

        float glow_size =
            1.0f +
            fmodf(i * 0.73f, 3.0f);

        glow(
            x,
            y,
            glow_size * 5,
            90,
            180,
            255
        );

        circle(
            x,
            y,
            glow_size,
            150,
            220,
            255
        );
    }

    /*
     * PPM.
     */

    if (!write_ppm(
            "docs/observatorio/observatorio.ppm"))
    {
        fprintf(
            stderr,
            "Erro ao gerar observatorio.ppm\n"
        );

        return 1;
    }

    printf(
        "Observatório gerado.\n"
        "Commits: %d\n"
        "Build: %s\n",
        commits,
        hash
    );

    return 0;
}