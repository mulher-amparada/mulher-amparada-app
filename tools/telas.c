#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <sys/stat.h>

#define MAX_SCREENS 500
#define MAX_ELEMENTS 1000
#define MAX_TEXT 512

typedef struct {
    char type[64];
    char text[MAX_TEXT];
    int depth;
} Element;

typedef struct {
    char name[256];
    char file[512];

    Element elements[MAX_ELEMENTS];
    int element_count;
} Screen;

Screen screens[MAX_SCREENS];
int screen_count = 0;

/* =========================================================
   UTILIDADES
   ========================================================= */

static void trim(char *s) {
    char *p = s;

    while (*p == ' ' || *p == '\t')
        p++;

    if (p != s)
        memmove(s, p, strlen(p) + 1);

    size_t len = strlen(s);

    while (len > 0 &&
           (s[len - 1] == '\n' ||
            s[len - 1] == '\r' ||
            s[len - 1] == ' ' ||
            s[len - 1] == '\t')) {
        s[--len] = '\0';
    }
}

static int is_kotlin(const char *name) {
    size_t len = strlen(name);

    return
        (len > 3 && strcmp(name + len - 3, ".kt") == 0) ||
        (len > 4 && strcmp(name + len - 4, ".kts") == 0);
}

static void ensure_docs(void) {
#ifdef _WIN32
    _mkdir("docs");
    _mkdir("docs/telas");
#else
    mkdir("docs", 0755);
    mkdir("docs/telas", 0755);
#endif
}

/* =========================================================
   ELEMENTOS
   ========================================================= */

static void add_element(
    Screen *screen,
    const char *type,
    const char *text,
    int depth
) {
    if (screen->element_count >= MAX_ELEMENTS)
        return;

    Element *e =
        &screen->elements[screen->element_count++];

    snprintf(e->type, sizeof(e->type), "%s", type);
    snprintf(e->text, sizeof(e->text), "%s", text);
    e->depth = depth;
}

/* =========================================================
   EXTRAIR TEXTO
   ========================================================= */

static void extract_string(
    const char *line,
    char *output,
    size_t size
) {
    output[0] = '\0';

    const char *start = strchr(line, '"');

    if (!start)
        return;

    start++;

    const char *end = strchr(start, '"');

    if (!end)
        return;

    size_t len = end - start;

    if (len >= size)
        len = size - 1;

    memcpy(output, start, len);
    output[len] = '\0';
}

/* =========================================================
   ANALISAR COMPOSABLE
   ========================================================= */

static void analyze_file(const char *path) {

    if (screen_count >= MAX_SCREENS)
        return;

    FILE *fp = fopen(path, "r");

    if (!fp)
        return;

    char line[4096];

    int composable = 0;
    int inside_screen = 0;
    int brace_level = 0;
    int depth = 0;

    Screen current;

    memset(&current, 0, sizeof(current));

    snprintf(current.file,
             sizeof(current.file),
             "%s",
             path);

    while (fgets(line, sizeof(line), fp)) {

        char clean[4096];

        snprintf(clean,
                 sizeof(clean),
                 "%s",
                 line);

        trim(clean);

        /* ---------------------------------------------
           Encontrou @Composable
           --------------------------------------------- */

        if (strstr(clean, "@Composable")) {
            composable = 1;
            continue;
        }

        /* ---------------------------------------------
           Próxima função depois do @Composable
           --------------------------------------------- */

        if (composable &&
            strstr(clean, "fun ")) {

            char *fun = strstr(clean, "fun ");

            fun += 4;

            char name[256];

            int i = 0;

            while (
                fun[i] &&
                fun[i] != '(' &&
                fun[i] != ' ' &&
                i < 255
            ) {
                name[i] = fun[i];
                i++;
            }

            name[i] = '\0';

            snprintf(current.name,
                     sizeof(current.name),
                     "%s",
                     name);

            inside_screen = 1;
            composable = 0;

            /* contar chaves */
            for (char *p = clean; *p; p++) {
                if (*p == '{')
                    brace_level++;

                if (*p == '}')
                    brace_level--;
            }

            continue;
        }

        if (!inside_screen)
            continue;

        /* ---------------------------------------------
           Containers
           --------------------------------------------- */

        if (strstr(clean, "Column(")) {
            add_element(
                &current,
                "COLUMN",
                "",
                depth
            );
            depth++;
        }

        else if (strstr(clean, "Row(")) {
            add_element(
                &current,
                "ROW",
                "",
                depth
            );
            depth++;
        }

        else if (strstr(clean, "Box(")) {
            add_element(
                &current,
                "BOX",
                "",
                depth
            );
            depth++;
        }

        else if (strstr(clean, "Card(")) {
            add_element(
                &current,
                "CARD",
                "",
                depth
            );
            depth++;
        }

        else if (strstr(clean, "Surface(")) {
            add_element(
                &current,
                "SURFACE",
                "",
                depth
            );
            depth++;
        }

        /* ---------------------------------------------
           Text
           --------------------------------------------- */

        else if (strstr(clean, "Text(")) {

            char text[MAX_TEXT];

            extract_string(clean, text, sizeof(text));

            add_element(
                &current,
                "TEXT",
                text,
                depth
            );
        }

        /* ---------------------------------------------
           Button
           --------------------------------------------- */

        else if (strstr(clean, "Button(")) {

            add_element(
                &current,
                "BUTTON",
                "",
                depth
            );

            depth++;
        }

        /* ---------------------------------------------
           OutlinedButton
           --------------------------------------------- */

        else if (strstr(clean, "OutlinedButton(")) {

            add_element(
                &current,
                "OUTLINED_BUTTON",
                "",
                depth
            );

            depth++;
        }

        /* ---------------------------------------------
           Image
           --------------------------------------------- */

        else if (strstr(clean, "Image(")) {

            add_element(
                &current,
                "IMAGE",
                "",
                depth
            );
        }

        /* ---------------------------------------------
           Icon
           --------------------------------------------- */

        else if (strstr(clean, "Icon(")) {

            add_element(
                &current,
                "ICON",
                "",
                depth
            );
        }

        /* ---------------------------------------------
           Spacer
           --------------------------------------------- */

        else if (strstr(clean, "Spacer(")) {

            add_element(
                &current,
                "SPACER",
                "",
                depth
            );
        }

        /* ---------------------------------------------
           TextField
           --------------------------------------------- */

        else if (
            strstr(clean, "TextField(") ||
            strstr(clean, "OutlinedTextField(")
        ) {

            add_element(
                &current,
                "TEXT_FIELD",
                "",
                depth
            );
        }

        /* ---------------------------------------------
           fechamento de bloco
           --------------------------------------------- */

        int opens = 0;
        int closes = 0;

        for (char *p = clean; *p; p++) {

            if (*p == '{')
                opens++;

            if (*p == '}')
                closes++;
        }

        if (closes > 0) {

            depth -= closes;

            if (depth < 0)
                depth = 0;
        }

        brace_level += opens;
        brace_level -= closes;

        /* ---------------------------------------------
           fim da função
           --------------------------------------------- */

        if (brace_level <= 0 &&
            current.name[0] != '\0') {

            screens[screen_count++] = current;

            memset(&current,
                   0,
                   sizeof(current));

            inside_screen = 0;
            depth = 0;
            brace_level = 0;
        }
    }

    fclose(fp);
}

/* =========================================================
   SVG
   ========================================================= */

static void escape_svg(
    FILE *out,
    const char *text
) {

    for (const char *p = text; *p; p++) {

        switch (*p) {

            case '&':
                fprintf(out, "&amp;");
                break;

            case '<':
                fprintf(out, "&lt;");
                break;

            case '>':
                fprintf(out, "&gt;");
                break;

            case '"':
                fprintf(out, "&quot;");
                break;

            default:
                fputc(*p, out);
        }
    }
}

/* =========================================================
   GERAR UMA TELA
   ========================================================= */

static void generate_screen(
    const Screen *screen
) {

    char svg[1024];

    snprintf(
        svg,
        sizeof(svg),
        "docs/telas/%s.svg",
        screen->name
    );

    FILE *out = fopen(svg, "w");

    if (!out)
        return;

    const int width = 430;
    const int height = 900;

    fprintf(
        out,
        "<svg xmlns=\"http://www.w3.org/2000/svg\" "
        "width=\"%d\" height=\"%d\">\n",
        width,
        height
    );

    /* celular */

    fprintf(
        out,
        "<rect width=\"430\" height=\"900\" "
        "rx=\"30\" fill=\"#080808\"/>\n"
    );

    /* barra superior */

    fprintf(
        out,
        "<rect x=\"15\" y=\"15\" "
        "width=\"400\" height=\"45\" "
        "rx=\"15\" fill=\"#151515\"/>\n"
    );

    fprintf(
        out,
        "<text x=\"215\" y=\"44\" "
        "text-anchor=\"middle\" "
        "font-family=\"Arial\" "
        "font-size=\"17\" "
        "font-weight=\"bold\" "
        "fill=\"white\">"
    );

    escape_svg(out, screen->name);

    fprintf(out, "</text>\n");

    int y = 90;

    for (int i = 0;
         i < screen->element_count;
         i++) {

        const Element *e =
            &screen->elements[i];

        int x =
            25 + e->depth * 15;

        if (y > 830)
            break;

        /* TEXT */

        if (strcmp(e->type, "TEXT") == 0) {

            fprintf(
                out,
                "<text x=\"%d\" y=\"%d\" "
                "font-family=\"Arial\" "
                "font-size=\"18\" "
                "fill=\"white\">",
                x,
                y
            );

            escape_svg(out, e->text);

            fprintf(out, "</text>\n");

            y += 38;
        }

        /* BUTTON */

        else if (
            strcmp(e->type, "BUTTON") == 0 ||
            strcmp(e->type, "OUTLINED_BUTTON") == 0
        ) {

            fprintf(
                out,
                "<rect x=\"%d\" y=\"%d\" "
                "width=\"340\" height=\"55\" "
                "rx=\"14\" "
                "fill=\"#7B3FA1\"/>\n",
                x,
                y
            );

            fprintf(
                out,
                "<text x=\"%d\" y=\"%d\" "
                "text-anchor=\"middle\" "
                "font-family=\"Arial\" "
                "font-size=\"16\" "
                "fill=\"white\">"
                "BOTÃO"
                "</text>\n",
                x + 170,
                y + 34
            );

            y += 75;
        }

        /* CARD */

        else if (strcmp(e->type, "CARD") == 0) {

            fprintf(
                out,
                "<rect x=\"%d\" y=\"%d\" "
                "width=\"350\" height=\"100\" "
                "rx=\"18\" "
                "fill=\"#181818\" "
                "stroke=\"#555\"/>\n",
                x,
                y
            );

            y += 120;
        }

        /* IMAGE */

        else if (strcmp(e->type, "IMAGE") == 0) {

            fprintf(
                out,
                "<rect x=\"%d\" y=\"%d\" "
                "width=\"150\" height=\"110\" "
                "rx=\"12\" "
                "fill=\"#292929\"/>\n",
                x,
                y
            );

            fprintf(
                out,
                "<text x=\"%d\" y=\"%d\" "
                "font-family=\"Arial\" "
                "font-size=\"14\" "
                "fill=\"#aaa\">"
                "IMAGEM"
                "</text>\n",
                x + 45,
                y + 60
            );

            y += 130;
        }

        /* TEXT FIELD */

        else if (
            strcmp(e->type, "TEXT_FIELD") == 0
        ) {

            fprintf(
                out,
                "<rect x=\"%d\" y=\"%d\" "
                "width=\"350\" height=\"55\" "
                "rx=\"12\" "
                "fill=\"#151515\" "
                "stroke=\"#777\"/>\n",
                x,
                y
            );

            fprintf(
                out,
                "<text x=\"%d\" y=\"%d\" "
                "font-family=\"Arial\" "
                "font-size=\"14\" "
                "fill=\"#999\">"
                "Campo de texto"
                "</text>\n",
                x + 15,
                y + 34
            );

            y += 75;
        }

        /* SPACER */

        else if (
            strcmp(e->type, "SPACER") == 0
        ) {
            y += 30;
        }

        /* CONTAINERS */

        else if (
            strcmp(e->type, "COLUMN") == 0 ||
            strcmp(e->type, "ROW") == 0 ||
            strcmp(e->type, "BOX") == 0 ||
            strcmp(e->type, "SURFACE") == 0
        ) {
            y += 5;
        }

        /* ICON */

        else if (
            strcmp(e->type, "ICON") == 0
        ) {

            fprintf(
                out,
                "<circle cx=\"%d\" cy=\"%d\" "
                "r=\"22\" fill=\"#333\"/>\n",
                x + 22,
                y + 22
            );

            y += 55;
        }
    }

    fprintf(out, "</svg>\n");

    fclose(out);
}

/* =========================================================
   SCAN
   ========================================================= */

static void scan_directory(
    const char *directory
) {

    DIR *dir = opendir(directory);

    if (!dir)
        return;

    struct dirent *entry;

    while ((entry = readdir(dir))) {

        if (
            strcmp(entry->d_name, ".") == 0 ||
            strcmp(entry->d_name, "..") == 0
        )
            continue;

        if (
            strcmp(entry->d_name, ".git") == 0 ||
            strcmp(entry->d_name, "build") == 0 ||
            strcmp(entry->d_name, ".gradle") == 0
        )
            continue;

        char path[1024];

        snprintf(
            path,
            sizeof(path),
            "%s/%s",
            directory,
            entry->d_name
        );

        struct stat st;

        if (stat(path, &st) != 0)
            continue;

        if (S_ISDIR(st.st_mode)) {

            scan_directory(path);

        } else if (is_kotlin(entry->d_name)) {

            analyze_file(path);
        }
    }

    closedir(dir);
}

/* =========================================================
   MAIN
   ========================================================= */

int main(void) {

    printf(
        "========================================\n"
        "   GERADOR DE TELAS KOTLIN / COMPOSE\n"
        "========================================\n\n"
    );

    ensure_docs();

    scan_directory(".");

    printf(
        "Telas encontradas: %d\n\n",
        screen_count
    );

    for (int i = 0;
         i < screen_count;
         i++) {

        printf(
            "Gerando: %s\n",
            screens[i].name
        );

        generate_screen(&screens[i]);
    }

    printf(
        "\nSVGs criados em: docs/telas/\n"
    );

    return 0;
}