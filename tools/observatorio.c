#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <sys/stat.h>

#define MAX_FILES 4000
#define MAX_NAME 512
#define MAX_TYPES 32
#define MAX_COMMITS 500
#define MAX_PERMISSIONS 300

typedef struct {
    char path[MAX_NAME];
    char type[64];
    long size;
    long lines;
} FileInfo;

typedef struct {
    char name[MAX_NAME];
    int count;
} Counter;

typedef struct {
    char hash[32];
    char date[32];
    char subject[256];
} Commit;

static FileInfo files[MAX_FILES];
static int file_count = 0;

static Counter types[MAX_TYPES];
static int type_count = 0;

static char permissions[MAX_PERMISSIONS][MAX_NAME];
static int permission_count = 0;

static Commit commits[MAX_COMMITS];
static int commit_count = 0;

static long total_lines = 0;
static long total_bytes = 0;


/* ============================================================
   UTILIDADES
   ============================================================ */

static void mkdir_safe(const char *p)
{
#ifdef _WIN32
    _mkdir(p);
#else
    mkdir(p, 0755);
#endif
}

static void prepare_dirs(void)
{
    mkdir_safe("docs");
    mkdir_safe("docs/observatorio");
}

static int has_suffix(const char *name, const char *suffix)
{
    size_t a = strlen(name);
    size_t b = strlen(suffix);

    return a >= b &&
           strcmp(name + a - b, suffix) == 0;
}

static void xml(FILE *f, const char *s)
{
    for (; *s; s++) {
        switch (*s) {
            case '&': fputs("&amp;", f); break;
            case '<': fputs("&lt;", f); break;
            case '>': fputs("&gt;", f); break;
            case '"': fputs("&quot;", f); break;
            default: fputc(*s, f);
        }
    }
}

static const char *file_type(const char *name)
{
    if (has_suffix(name, ".kt")) return "Kotlin";
    if (has_suffix(name, ".kts")) return "Kotlin Script";
    if (has_suffix(name, ".java")) return "Java";
    if (has_suffix(name, ".c")) return "C";
    if (has_suffix(name, ".h")) return "C Header";
    if (has_suffix(name, ".cpp")) return "C++";
    if (has_suffix(name, ".hpp")) return "C++ Header";
    if (has_suffix(name, ".xml")) return "XML";
    if (has_suffix(name, ".json")) return "JSON";
    if (has_suffix(name, ".yaml")) return "YAML";
    if (has_suffix(name, ".yml")) return "YAML";
    if (has_suffix(name, ".md")) return "Markdown";
    if (has_suffix(name, ".html")) return "HTML";
    if (has_suffix(name, ".css")) return "CSS";
    if (has_suffix(name, ".js")) return "JavaScript";
    if (has_suffix(name, ".properties")) return "Properties";
    if (has_suffix(name, ".toml")) return "TOML";
    if (has_suffix(name, ".gradle")) return "Gradle";
    if (has_suffix(name, ".sh")) return "Shell";
    return "Outro";
}

static void add_type(const char *type)
{
    for (int i = 0; i < type_count; i++) {
        if (strcmp(types[i].name, type) == 0) {
            types[i].count++;
            return;
        }
    }

    if (type_count < MAX_TYPES) {
        snprintf(types[type_count].name,
                 MAX_NAME,
                 "%s",
                 type);
        types[type_count].count = 1;
        type_count++;
    }
}


/* ============================================================
   LEITURA DE ARQUIVOS
   ============================================================ */

static void inspect_file(const char *path)
{
    if (file_count >= MAX_FILES)
        return;

    FILE *f = fopen(path, "rb");

    if (!f)
        return;

    FileInfo *info = &files[file_count];

    memset(info, 0, sizeof(*info));

    snprintf(info->path,
             sizeof(info->path),
             "%s",
             path);

    snprintf(info->type,
             sizeof(info->type),
             "%s",
             file_type(path));

    fseek(f, 0, SEEK_END);
    info->size = ftell(f);
    fseek(f, 0, SEEK_SET);

    char buffer[4096];
    size_t n;

    while ((n = fread(buffer, 1, sizeof(buffer), f)) > 0) {
        for (size_t i = 0; i < n; i++) {
            if (buffer[i] == '\n')
                info->lines++;
        }
    }

    fclose(f);

    total_bytes += info->size;
    total_lines += info->lines;

    add_type(info->type);

    file_count++;
}


/* ============================================================
   SCANNER DO REPOSITÓRIO
   ============================================================ */

static void scan(const char *dir)
{
    DIR *d = opendir(dir);

    if (!d)
        return;

    struct dirent *e;

    while ((e = readdir(d)) != NULL) {

        if (!strcmp(e->d_name, ".") ||
            !strcmp(e->d_name, ".."))
            continue;

        if (!strcmp(e->d_name, ".git") ||
            !strcmp(e->d_name, ".gradle") ||
            !strcmp(e->d_name, "build") ||
            !strcmp(e->d_name, ".idea"))
            continue;

        char path[MAX_NAME];

        snprintf(path,
                 sizeof(path),
                 "%s/%s",
                 dir,
                 e->d_name);

        struct stat st;

        if (stat(path, &st) != 0)
            continue;

        if (S_ISDIR(st.st_mode)) {
            scan(path);
        } else {
            inspect_file(path);
        }
    }

    closedir(d);
}


/* ============================================================
   MANIFEST / PERMISSÕES
   ============================================================ */

static void scan_manifest(void)
{
    const char *paths[] = {
        "app/src/main/AndroidManifest.xml",
        "AndroidManifest.xml"
    };

    FILE *f = NULL;

    for (int i = 0; i < 2; i++) {
        f = fopen(paths[i], "r");
        if (f)
            break;
    }

    if (!f)
        return;

    char line[4096];

    while (fgets(line, sizeof(line), f)) {

        char *p = strstr(line, "android.permission.");

        if (!p)
            continue;

        char permission[MAX_NAME];

        int i = 0;

        while (p[i] &&
               p[i] != '"' &&
               p[i] != '\'' &&
               p[i] != ' ' &&
               p[i] != '\n' &&
               i < MAX_NAME - 1) {

            permission[i] = p[i];
            i++;
        }

        permission[i] = '\0';

        if (permission_count < MAX_PERMISSIONS) {
            snprintf(
                permissions[permission_count],
                MAX_NAME,
                "%s",
                permission
            );

            permission_count++;
        }
    }

    fclose(f);
}


/* ============================================================
   GIT
   ============================================================ */

static void scan_git(void)
{
    FILE *p = popen(
        "git log --date=short "
        "--pretty=format:'%h|%ad|%s' "
        "-n 500",
        "r"
    );

    if (!p)
        return;

    char line[1024];

    while (fgets(line, sizeof(line), p) &&
           commit_count < MAX_COMMITS) {

        char *a = strchr(line, '|');

        if (!a)
            continue;

        *a = '\0';

        char *b = strchr(a + 1, '|');

        if (!b)
            continue;

        *b = '\0';

        snprintf(
            commits[commit_count].hash,
            sizeof(commits[commit_count].hash),
            "%s",
            line
        );

        snprintf(
            commits[commit_count].date,
            sizeof(commits[commit_count].date),
            "%s",
            a + 1
        );

        snprintf(
            commits[commit_count].subject,
            sizeof(commits[commit_count].subject),
            "%s",
            b + 1
        );

        commit_count++;
    }

    pclose(p);
}


/* ============================================================
   CABEÇALHO SVG
   ============================================================ */

static void svg_begin(FILE *f,
                      int width,
                      int height,
                      const char *title)
{
    fprintf(
        f,
        "<svg xmlns=\"http://www.w3.org/2000/svg\" "
        "width=\"%d\" height=\"%d\" "
        "viewBox=\"0 0 %d %d\">",
        width,
        height,
        width,
        height
    );

    fprintf(
        f,
        "<rect width=\"100%%\" height=\"100%%\" "
        "fill=\"#050505\"/>"
    );

    fprintf(
        f,
        "<text x=\"40\" y=\"55\" "
        "font-family=\"Arial\" "
        "font-size=\"30\" "
        "font-weight=\"bold\" "
        "fill=\"white\">"
    );

    xml(f, title);

    fprintf(f, "</text>");
}

static void svg_end(FILE *f)
{
    fprintf(f, "</svg>\n");
}


/* ============================================================
   🌌 UNIVERSO
   ============================================================ */

static void generate_universe(void)
{
    FILE *f = fopen(
        "docs/observatorio/universo.svg",
        "w"
    );

    if (!f)
        return;

    const int W = 1400;
    const int H = 900;

    svg_begin(
        f,
        W,
        H,
        "🌌 Universo do Repositório"
    );

    int max = file_count > 80 ? 80 : file_count;

    for (int i = 0; i < max; i++) {

        int x = 80 + (i * 137) % 1240;
        int y = 130 + (i * 83) % 680;

        int r = 3 + (files[i].lines % 9);

        fprintf(
            f,
            "<circle cx=\"%d\" cy=\"%d\" r=\"%d\" "
            "fill=\"#b86cff\" opacity=\"0.9\"/>",
            x, y, r
        );

        if (i > 0) {

            int px = 80 + ((i - 1) * 137) % 1240;
            int py = 130 + ((i - 1) * 83) % 680;

            fprintf(
                f,
                "<line x1=\"%d\" y1=\"%d\" "
                "x2=\"%d\" y2=\"%d\" "
                "stroke=\"#32184d\"/>",
                px, py, x, y
            );
        }
    }

    fprintf(
        f,
        "<text x=\"40\" y=\"850\" "
        "font-family=\"Arial\" font-size=\"18\" "
        "fill=\"#aaa\">"
        "Arquivos: %d | Linhas: %ld | Bytes: %ld"
        "</text>",
        file_count,
        total_lines,
        total_bytes
    );

    svg_end(f);
    fclose(f);
}


/* ============================================================
   🧬 DNA
   ============================================================ */

static void generate_dna(void)
{
    FILE *f = fopen(
        "docs/observatorio/dna.svg",
        "w"
    );

    if (!f)
        return;

    svg_begin(
        f,
        1400,
        900,
        "🧬 DNA do Projeto"
    );

    int max = file_count > 120 ? 120 : file_count;

    for (int i = 0; i < max; i++) {

        double angle = i * 0.35;

        int x1 = 700 + (int)(280 * sin(angle));
        int x2 = 700 + (int)(280 * sin(angle + 3.14));

        int y = 130 + i * 5;

        fprintf(
            f,
            "<line x1=\"%d\" y1=\"%d\" "
            "x2=\"%d\" y2=\"%d\" "
            "stroke=\"#8d4cff\" stroke-width=\"2\"/>",
            x1, y, x2, y
        );

        fprintf(
            f,
            "<circle cx=\"%d\" cy=\"%d\" r=\"7\" "
            "fill=\"#d98cff\"/>",
            x1, y
        );

        fprintf(
            f,
            "<circle cx=\"%d\" cy=\"%d\" r=\"7\" "
            "fill=\"#6cc7ff\"/>",
            x2, y
        );
    }

    fprintf(
        f,
        "<text x=\"40\" y=\"850\" "
        "font-family=\"Arial\" font-size=\"18\" "
        "fill=\"#aaa\">"
        "Tipos de arquivo: %d"
        "</text>",
        type_count
    );

    svg_end(f);
    fclose(f);
}


/* ============================================================
   ❤️ PULSAÇÃO
   ============================================================ */

static void generate_pulse(void)
{
    FILE *f = fopen(
        "docs/observatorio/pulsacao.svg",
        "w"
    );

    if (!f)
        return;

    svg_begin(
        f,
        1400,
        700,
        "❤️ Pulsação do Projeto"
    );

    fprintf(
        f,
        "<polyline fill=\"none\" "
        "stroke=\"#ff5c8a\" "
        "stroke-width=\"4\" points=\""
    );

    int max = commit_count > 150 ? 150 : commit_count;

    for (int i = 0; i < max; i++) {

        int x = 40 + i * 9;
        int y;

        if (i % 11 == 0)
            y = 220;
        else if (i % 7 == 0)
            y = 410;
        else
            y = 330 + (i % 5) * 5;

        fprintf(f, "%d,%d ", x, y);
    }

    fprintf(f, "\"/>");

    fprintf(
        f,
        "<text x=\"40\" y=\"620\" "
        "font-family=\"Arial\" font-size=\"18\" "
        "fill=\"#aaa\">"
        "Commits analisados: %d"
        "</text>",
        commit_count
    );

    svg_end(f);
    fclose(f);
}


/* ============================================================
   🕰️ EVOLUÇÃO
   ============================================================ */

static void generate_evolution(void)
{
    FILE *f = fopen(
        "docs/observatorio/evolucao.svg",
        "w"
    );

    if (!f)
        return;

    svg_begin(
        f,
        1400,
        800,
        "🕰️ Evolução do Projeto"
    );

    int max = commit_count > 30 ? 30 : commit_count;

    fprintf(
        f,
        "<line x1=\"70\" y1=\"400\" "
        "x2=\"1330\" y2=\"400\" "
        "stroke=\"#555\" stroke-width=\"3\"/>"
    );

    for (int i = 0; i < max; i++) {

        int x = 80 + i * 40;

        fprintf(
            f,
            "<circle cx=\"%d\" cy=\"400\" "
            "r=\"9\" fill=\"#8e44ad\"/>",
            x
        );

        fprintf(
            f,
            "<text x=\"%d\" y=\"370\" "
            "font-family=\"Arial\" "
            "font-size=\"11\" "
            "fill=\"#aaa\" "
            "transform=\"rotate(-45 %d 370)\">",
            x - 15,
            x
        );

        xml(f, commits[i].date);

        fprintf(f, "</text>");
    }

    fprintf(
        f,
        "<text x=\"40\" y=\"700\" "
        "font-family=\"Arial\" font-size=\"18\" "
        "fill=\"#aaa\">"
        "Histórico Git: %d commits analisados"
        "</text>",
        commit_count
    );

    svg_end(f);
    fclose(f);
}


/* ============================================================
   🛡️ SEGURANÇA
   ============================================================ */

static void generate_security(void)
{
    FILE *f = fopen(
        "docs/observatorio/seguranca.svg",
        "w"
    );

    if (!f)
        return;

    svg_begin(
        f,
        1200,
        800,
        "🛡️ Escudo do Projeto"
    );

    fprintf(
        f,
        "<path d=\"M600 130 "
        "L850 220 "
        "L810 500 "
        "L600 670 "
        "L390 500 "
        "L350 220 Z\" "
        "fill=\"#171717\" "
        "stroke=\"#a855f7\" "
        "stroke-width=\"8\"/>"
    );

    fprintf(
        f,
        "<text x=\"600\" y=\"390\" "
        "text-anchor=\"middle\" "
        "font-family=\"Arial\" "
        "font-size=\"90\" "
        "fill=\"white\">"
        "✓"
        "</text>"
    );

    fprintf(
        f,
        "<text x=\"600\" y=\"450\" "
        "text-anchor=\"middle\" "
        "font-family=\"Arial\" "
        "font-size=\"24\" "
        "fill=\"#aaa\">"
        "ANÁLISE DO REPOSITÓRIO"
        "</text>"
    );

    fprintf(
        f,
        "<text x=\"60\" y=\"730\" "
        "font-family=\"Arial\" font-size=\"18\" "
        "fill=\"#aaa\">"
        "Permissões encontradas: %d"
        "</text>",
        permission_count
    );

    svg_end(f);
    fclose(f);
}


/* ============================================================
   🧠 MAPA FUNCIONAL
   ============================================================ */

static void generate_functional(void)
{
    FILE *f = fopen(
        "docs/observatorio/mapa-funcional.svg",
        "w"
    );

    if (!f)
        return;

    svg_begin(
        f,
        1400,
        850,
        "🧠 Mapa Funcional"
    );

    const char *nodes[] = {
        "APLICATIVO",
        "ANDROID",
        "KOTLIN",
        "SOS",
        "LOCALIZAÇÃO",
        "SENSORES",
        "NOTIFICAÇÕES",
        "RECURSOS",
        "GRADLE",
        "GITHUB"
    };

    int n = sizeof(nodes) / sizeof(nodes[0]);

    int cx[10] = {
        700, 700, 700,
        250, 500, 900,
        1150, 250, 500, 900
    };

    int cy[10] = {
        120, 270, 420,
        600, 600, 600,
        600, 760, 760, 760
    };

    for (int i = 1; i < n; i++) {

        fprintf(
            f,
            "<line x1=\"%d\" y1=\"%d\" "
            "x2=\"%d\" y2=\"%d\" "
            "stroke=\"#492060\" stroke-width=\"3\"/>",
            cx[0],
            cy[0],
            cx[i],
            cy[i]
        );
    }

    for (int i = 0; i < n; i++) {

        fprintf(
            f,
            "<circle cx=\"%d\" cy=\"%d\" r=\"55\" "
            "fill=\"#151515\" "
            "stroke=\"#a855f7\" "
            "stroke-width=\"3\"/>",
            cx[i],
            cy[i]
        );

        fprintf(
            f,
            "<text x=\"%d\" y=\"%d\" "
            "text-anchor=\"middle\" "
            "font-family=\"Arial\" "
            "font-size=\"13\" "
            "fill=\"white\">",
            cx[i],
            cy[i] + 5
        );

        xml(f, nodes[i]);

        fprintf(f, "</text>");
    }

    svg_end(f);
    fclose(f);
}


/* ============================================================
   MAIN
   ============================================================ */

int main(void)
{
    printf("\n");
    printf("============================================\n");
    printf("   MULHER AMPARADA - OBSERVATORIO\n");
    printf("============================================\n\n");

    prepare_dirs();

    printf("[1/5] Escaneando repositorio...\n");
    scan(".");

    printf("[2/5] Analisando AndroidManifest...\n");
    scan_manifest();

    printf("[3/5] Analisando Git...\n");
    scan_git();

    printf("[4/5] Gerando observatorio...\n");

    generate_universe();
    generate_dna();
    generate_pulse();
    generate_evolution();
    generate_security();
    generate_functional();

    printf("[5/5] Finalizado.\n\n");

    printf("Arquivos encontrados : %d\n", file_count);
    printf("Linhas               : %ld\n", total_lines);
    printf("Bytes                : %ld\n", total_bytes);
    printf("Tipos de arquivo     : %d\n", type_count);
    printf("Commits              : %d\n", commit_count);
    printf("Permissoes           : %d\n", permission_count);

    printf("\nResultados:\n");
    printf("  docs/observatorio/universo.svg\n");
    printf("  docs/observatorio/dna.svg\n");
    printf("  docs/observatorio/pulsacao.svg\n");
    printf("  docs/observatorio/evolucao.svg\n");
    printf("  docs/observatorio/seguranca.svg\n");
    printf("  docs/observatorio/mapa-funcional.svg\n");

    return 0;
}