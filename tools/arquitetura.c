#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <sys/stat.h>

#define MAX_FILES 1000
#define MAX_NAME 256
#define MAX_ITEMS 200

typedef struct {
    char path[MAX_NAME];
    char package_name[MAX_NAME];
    char elements[MAX_ITEMS][MAX_NAME];
    int element_count;
} KotlinFile;

KotlinFile files[MAX_FILES];
int file_count = 0;

int is_directory(const char *path) {
    struct stat st;
    return stat(path, &st) == 0 && S_ISDIR(st.st_mode);
}

int is_kotlin(const char *name) {
    size_t len = strlen(name);
    return len > 3 &&
           (strcmp(name + len - 3, ".kt") == 0 ||
            (len > 4 && strcmp(name + len - 4, ".kts") == 0));
}

void add_element(KotlinFile *file, const char *element) {
    if (file->element_count >= MAX_ITEMS)
        return;

    for (int i = 0; i < file->element_count; i++) {
        if (strcmp(file->elements[i], element) == 0)
            return;
    }

    snprintf(
        file->elements[file->element_count],
        MAX_NAME,
        "%s",
        element
    );

    file->element_count++;
}

void analyze_file(const char *path) {
    if (file_count >= MAX_FILES)
        return;

    FILE *fp = fopen(path, "r");
    if (!fp)
        return;

    KotlinFile *file = &files[file_count];

    snprintf(file->path, MAX_NAME, "%s", path);
    file->package_name[0] = '\0';
    file->element_count = 0;

    char line[2048];

    while (fgets(line, sizeof(line), fp)) {

        char name[MAX_NAME];

        if (sscanf(line, " package %255s", name) == 1) {
            snprintf(file->package_name,
                     MAX_NAME,
                     "%s",
                     name);
        }

        if (strstr(line, "data class ")) {
            if (sscanf(strstr(line, "data class ") + 11,
                       "%255s",
                       name) == 1) {
                add_element(file, name);
            }
        }

        else if (strstr(line, "class ")) {
            if (sscanf(strstr(line, "class ") + 6,
                       "%255s",
                       name) == 1) {
                add_element(file, name);
            }
        }

        if (strstr(line, "interface ")) {
            if (sscanf(strstr(line, "interface ") + 10,
                       "%255s",
                       name) == 1) {
                add_element(file, name);
            }
        }

        if (strstr(line, "object ")) {
            if (sscanf(strstr(line, "object ") + 7,
                       "%255s",
                       name) == 1) {
                add_element(file, name);
            }
        }

        if (strstr(line, "fun ")) {
            if (sscanf(strstr(line, "fun ") + 4,
                       "%255[^ (]",
                       name) == 1) {

                char formatted[MAX_NAME];
                snprintf(formatted,
                         MAX_NAME,
                         "fun %s()",
                         name);

                add_element(file, formatted);
            }
        }

        if (strstr(line, "@Composable")) {
            add_element(file, "@Composable");
        }
    }

    fclose(fp);
    file_count++;
}

void scan_directory(const char *directory) {
    DIR *dir = opendir(directory);

    if (!dir)
        return;

    struct dirent *entry;

    while ((entry = readdir(dir)) != NULL) {

        if (strcmp(entry->d_name, ".") == 0 ||
            strcmp(entry->d_name, "..") == 0)
            continue;

        if (strcmp(entry->d_name, ".git") == 0 ||
            strcmp(entry->d_name, "build") == 0 ||
            strcmp(entry->d_name, ".gradle") == 0)
            continue;

        char path[1024];

        snprintf(path,
                 sizeof(path),
                 "%s/%s",
                 directory,
                 entry->d_name);

        if (is_directory(path)) {
            scan_directory(path);
        }
        else if (is_kotlin(entry->d_name)) {
            analyze_file(path);
        }
    }

    closedir(dir);
}

void xml_escape(FILE *out, const char *text) {
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

void generate_svg(const char *output) {

    const int card_width = 360;
    const int card_height = 170;
    const int gap_x = 40;
    const int gap_y = 40;
    const int columns = 3;

    int rows = (file_count + columns - 1) / columns;

    int width = columns * card_width +
                (columns + 1) * gap_x;

    int height = rows * card_height +
                 (rows + 1) * gap_y +
                 100;

    FILE *out = fopen(output, "w");

    if (!out)
        return;

    fprintf(out,
        "<svg xmlns=\"http://www.w3.org/2000/svg\" "
        "width=\"%d\" height=\"%d\" viewBox=\"0 0 %d %d\">\n",
        width, height, width, height);

    fprintf(out,
        "<rect width=\"100%%\" height=\"100%%\" fill=\"#080808\"/>\n");

    fprintf(out,
        "<text x=\"%d\" y=\"55\" "
        "font-family=\"Arial\" font-size=\"30\" "
        "font-weight=\"bold\" fill=\"white\" "
        "text-anchor=\"middle\">"
        "Arquitetura — Mulher Amparada"
        "</text>\n",
        width / 2);

    for (int i = 0; i < file_count; i++) {

        int col = i % columns;
        int row = i / columns;

        int x = gap_x + col * (card_width + gap_x);
        int y = 85 + gap_y + row * (card_height + gap_y);

        fprintf(out,
            "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" "
            "rx=\"18\" fill=\"#151515\" "
            "stroke=\"#8e44ad\" stroke-width=\"2\"/>\n",
            x, y, card_width, card_height);

        fprintf(out,
            "<text x=\"%d\" y=\"%d\" "
            "font-family=\"Arial\" font-size=\"17\" "
            "font-weight=\"bold\" fill=\"white\">",
            x + 18, y + 30);

        xml_escape(out, files[i].path);

        fprintf(out, "</text>\n");

        int element_y = y + 58;

        for (int j = 0;
             j < files[i].element_count && j < 6;
             j++) {

            fprintf(out,
                "<text x=\"%d\" y=\"%d\" "
                "font-family=\"Arial\" font-size=\"14\" "
                "fill=\"#dddddd\">• ",
                x + 20,
                element_y);

            xml_escape(out, files[i].elements[j]);

            fprintf(out, "</text>\n");

            element_y += 18;
        }

        if (files[i].element_count > 6) {
            fprintf(out,
                "<text x=\"%d\" y=\"%d\" "
                "font-family=\"Arial\" font-size=\"13\" "
                "fill=\"#999999\">+ %d elementos</text>\n",
                x + 20,
                element_y,
                files[i].element_count - 6);
        }
    }

    fprintf(out, "</svg>\n");

    fclose(out);
}

int main(void) {

    printf("Analisando arquivos Kotlin...\n");

    scan_directory(".");

    printf("Arquivos Kotlin encontrados: %d\n", file_count);

    mkdir("docs", 0755);

    generate_svg("docs/arquitetura.svg");

    printf("Arquitetura gerada em docs/arquitetura.svg\n");

    return 0;
}