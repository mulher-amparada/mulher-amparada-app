package main

import (
	"bytes"
	"encoding/base64"
	"fmt"
	"image"
	"image/color"
	"image/draw"
	"image/png"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"sort"
	"strconv"
	"strings"
	"time"

	"golang.org/x/image/font"
	"golang.org/x/image/font/basicfont"
	"golang.org/x/image/math/fixed"
	"gopkg.in/yaml.v3"
)

const (
	linguistURL = "https://raw.githubusercontent.com/github-linguist/linguist/main/lib/linguist/languages.yml"

	outputPNG = "github-languages.png"
	readmeFile = "README.md"

	readmeStart = "<!-- GITHUB_LANGUAGES_INICIO -->"
	readmeEnd   = "<!-- GITHUB_LANGUAGES_FIM -->"
)

type Language struct {
	Type       string   `yaml:"type"`
	Color      string   `yaml:"color"`
	Extensions []string `yaml:"extensions"`
	Aliases    []string `yaml:"aliases"`
}

type LanguageInfo struct {
	Name      string
	Color     color.RGBA
	Hex       string
	Files     int
	Bytes     int64
	Percent   float64
}

func main() {
	fmt.Println("🎨 Mulher Amparada — Gerador de linguagens")
	fmt.Println()

	fmt.Println("📥 Baixando dados oficiais do GitHub Linguist...")

	data, err := downloadLinguist()
	if err != nil {
		fatal(err)
	}

	languages, err := parseLinguist(data)
	if err != nil {
		fatal(err)
	}

	fmt.Printf("✅ %d linguagens conhecidas pelo Linguist\n", len(languages))

	fmt.Println("🔎 Detectando linguagens do repositório...")

	detected := detectLanguages(languages)

	if len(detected) == 0 {
		fatal(fmt.Errorf("nenhuma linguagem reconhecida foi encontrada"))
	}

	sort.Slice(detected, func(i, j int) bool {
		if detected[i].Bytes == detected[j].Bytes {
			return detected[i].Name < detected[j].Name
		}

		return detected[i].Bytes > detected[j].Bytes
	})

	var totalBytes int64

	for _, language := range detected {
		totalBytes += language.Bytes
	}

	for i := range detected {
		if totalBytes > 0 {
			detected[i].Percent =
				float64(detected[i].Bytes) /
					float64(totalBytes) *
					100
		}
	}

	fmt.Printf(
		"✅ %d linguagens encontradas no projeto\n",
		len(detected),
	)

	fmt.Println("🖼️ Gerando github-languages.png...")

	if err := generatePNG(detected, outputPNG); err != nil {
		fatal(err)
	}

	fmt.Printf("✅ PNG criado: %s\n", outputPNG)

	fmt.Println("📝 Atualizando README.md...")

	if err := updateREADME(detected); err != nil {
		fatal(err)
	}

	fmt.Println("✅ README.md atualizado.")
	fmt.Println()
	fmt.Println("🌈 Processo concluído.")
}

func fatal(err error) {
	fmt.Fprintf(os.Stderr, "❌ %v\n", err)
	os.Exit(1)
}

func downloadLinguist() ([]byte, error) {
	client := &http.Client{
		Timeout: 30 * time.Second,
	}

	request, err := http.NewRequest(
		http.MethodGet,
		linguistURL,
		nil,
	)

	if err != nil {
		return nil, err
	}

	request.Header.Set(
		"User-Agent",
		"mulher-amparada-languages",
	)

	response, err := client.Do(request)

	if err != nil {
		return nil, err
	}

	defer response.Body.Close()

	if response.StatusCode != http.StatusOK {
		return nil, fmt.Errorf(
			"GitHub Linguist retornou HTTP %d",
			response.StatusCode,
		)
	}

	return io.ReadAll(response.Body)
}

func parseLinguist(
	data []byte,
) (map[string]Language, error) {

	var languages map[string]Language

	if err := yaml.Unmarshal(data, &languages); err != nil {
		return nil, err
	}

	return languages, nil
}

func detectLanguages(
	languages map[string]Language,
) []LanguageInfo {

	type stats struct {
		Language Language
		Name     string
		Files    int
		Bytes    int64
	}

	byExtension := make(map[string]stats)

	for name, language := range languages {
		for _, extension := range language.Extensions {
			extension = strings.ToLower(
				strings.TrimSpace(extension),
			)

			if extension == "" {
				continue
			}

			if !strings.HasPrefix(extension, ".") {
				extension = "." + extension
			}

			byExtension[extension] = stats{
				Language: language,
				Name:     name,
			}
		}
	}

	found := make(map[string]*LanguageInfo)

	err := filepath.Walk(
		".",
		func(
			path string,
			info os.FileInfo,
			err error,
		) error {

			if err != nil {
				return err
			}

			if info.IsDir() {
				if shouldIgnoreDirectory(info.Name()) {
					return filepath.SkipDir
				}

				return nil
			}

			if shouldIgnoreFile(path) {
				return nil
			}

			extension := strings.ToLower(
				filepath.Ext(path),
			)

			if extension == "" {
				return nil
			}

			language, ok := byExtension[extension]

			if !ok {
				return nil
			}

			existing, exists := found[language.Name]

			if !exists {
				parsedColor := parseColor(language.Color)

				existing = &LanguageInfo{
					Name:  language.Name,
					Color: parsedColor,
					Hex:   language.Color,
				}

				if existing.Hex == "" {
					existing.Hex = "#30363D"
				}

				found[language.Name] = existing
			}

			existing.Files++
			existing.Bytes += info.Size()

			return nil
		},
	)

	if err != nil {
		fatal(err)
	}

	result := make([]LanguageInfo, 0, len(found))

	for _, language := range found {
		result = append(result, *language)
	}

	return result
}

func shouldIgnoreDirectory(name string) bool {
	switch name {
	case ".git",
		".github",
		".idea",
		".gradle",
		"build",
		"node_modules",
		"target",
		"dist",
		"out":
		return true
	}

	return false
}

func shouldIgnoreFile(path string) bool {
	if strings.Contains(
		filepath.ToSlash(path),
		"/.git/",
	) {
		return true
	}

	return false
}

func parseColor(value string) color.RGBA {
	value = strings.TrimSpace(value)
	value = strings.TrimPrefix(value, "#")

	if len(value) != 6 {
		return color.RGBA{
			R: 48,
			G: 54,
			B: 61,
			A: 255,
		}
	}

	r, _ := strconv.ParseUint(value[0:2], 16, 8)
	g, _ := strconv.ParseUint(value[2:4], 16, 8)
	b, _ := strconv.ParseUint(value[4:6], 16, 8)

	return color.RGBA{
		R: uint8(r),
		G: uint8(g),
		B: uint8(b),
		A: 255,
	}
}

func generatePNG(
	languages []LanguageInfo,
	filename string,
) error {

	const (
		width       = 1400
		header      = 150
		cardHeight  = 90
		cardGap     = 14
		sidePadding = 60
		topGap      = 30
	)

	columns := 2

	rows := (len(languages) + columns - 1) / columns

	height :=
		header +
			topGap +
			rows*(cardHeight+cardGap) +
			80

	canvas := image.NewRGBA(
		image.Rect(
			0,
			0,
			width,
			height,
		),
	)

	draw.Draw(
		canvas,
		canvas.Bounds(),
		&image.Uniform{
			color.RGBA{
				R: 13,
				G: 17,
				B: 23,
				A: 255,
			},
		},
		image.Point{},
		draw.Src,
	)

	faceTitle := basicfont.Face7x13
	face := basicfont.Face7x13

	drawText(
		canvas,
		"🎨 Linguagens de programação",
		60,
		55,
		faceTitle,
		color.RGBA{
			R: 240,
			G: 246,
			B: 252,
			A: 255,
		},
	)

	subtitle := fmt.Sprintf(
		"%d linguagens detectadas • cores oficiais do GitHub Linguist",
		len(languages),
	)

	drawText(
		canvas,
		subtitle,
		60,
		85,
		face,
		color.RGBA{
			R: 139,
			G: 148,
			B: 158,
			A: 255,
		},
	)

	total := totalBytes(languages)

	barX := sidePadding
	barY := 110
	barWidth := width - sidePadding*2
	barHeight := 14

	currentX := barX

	for _, language := range languages {
		if total == 0 {
			continue
		}

		segmentWidth :=
			int(
				float64(barWidth) *
					float64(language.Bytes) /
					float64(total),
			)

		if segmentWidth < 1 {
			segmentWidth = 1
		}

		rect := image.Rect(
			currentX,
			barY,
			currentX+segmentWidth,
			barY+barHeight,
		)

		draw.Draw(
			canvas,
			rect,
			&image.Uniform{language.Color},
			image.Point{},
			draw.Src,
		)

		currentX += segmentWidth
	}

	for index, language := range languages {
		column := index % columns
		row := index / columns

		cardWidth :=
			(width -
				sidePadding*2 -
				cardGap) /
				columns

		x :=
			sidePadding +
				column*(cardWidth+cardGap)

		y :=
			header +
				topGap +
				row*(cardHeight+cardGap)

		drawCard(
			canvas,
			x,
			y,
			cardWidth,
			cardHeight,
			language,
			face,
		)
	}

	drawText(
		canvas,
		"Mulher Amparada • GitHub Linguist",
		60,
		height-25,
		face,
		color.RGBA{
			R: 139,
			G: 148,
			B: 158,
			A: 255,
		},
	)

	file, err := os.Create(filename)

	if err != nil {
		return err
	}

	defer file.Close()

	return png.Encode(file, canvas)
}

func drawCard(
	img *image.RGBA,
	x int,
	y int,
	width int,
	height int,
	language LanguageInfo,
	face font.Face,
) {

	cardColor := color.RGBA{
		R: 22,
		G: 27,
		B: 34,
		A: 255,
	}

	borderColor := color.RGBA{
		R: 48,
		G: 54,
		B: 61,
		A: 255,
	}

	fillRoundedRect(
		img,
		x,
		y,
		width,
		height,
		12,
		cardColor,
	)

	strokeRoundedRect(
		img,
		x,
		y,
		width,
		height,
		12,
		borderColor,
	)

	drawCircle(
		img,
		x+28,
		y+28,
		10,
		language.Color,
	)

	drawText(
		img,
		language.Name,
		x+50,
		y+25,
		face,
		color.RGBA{
			R: 240,
			G: 246,
			B: 252,
			A: 255,
		},
	)

	drawText(
		img,
		language.Hex,
		x+50,
		y+47,
		face,
		color.RGBA{
			R: 139,
			G: 148,
			B: 158,
			A: 255,
		},
	)

	info := fmt.Sprintf(
		"%d arquivo(s) • %.2f%%",
		language.Files,
		language.Percent,
	)

	drawText(
		img,
		info,
		x+50,
		y+68,
		face,
		color.RGBA{
			R: 139,
			G: 148,
			B: 158,
			A: 255,
		},
	)
}

func drawText(
	img *image.RGBA,
	text string,
	x int,
	y int,
	face font.Face,
	c color.Color,
) {

	d := &font.Drawer{
		Dst:  img,
		Src:  image.NewUniform(c),
		Face: face,
		Dot: fixed.Point26_6{
			X: fixed.I(x),
			Y: fixed.I(y),
		},
	}

	d.DrawString(text)
}

func drawCircle(
	img *image.RGBA,
	cx int,
	cy int,
	radius int,
	c color.Color,
) {

	for y := -radius; y <= radius; y++ {
		for x := -radius; x <= radius; x++ {
			if x*x+y*y <= radius*radius {
				img.Set(
					cx+x,
					cy+y,
					c,
				)
			}
		}
	}
}

func fillRoundedRect(
	img *image.RGBA,
	x int,
	y int,
	width int,
	height int,
	radius int,
	c color.Color,
) {

	for py := y; py < y+height; py++ {
		for px := x; px < x+width; px++ {

			if insideRoundedRect(
				px,
				py,
				x,
				y,
				width,
				height,
				radius,
			) {
				img.Set(px, py, c)
			}
		}
	}
}

func strokeRoundedRect(
	img *image.RGBA,
	x int,
	y int,
	width int,
	height int,
	radius int,
	c color.Color,
) {

	for px := x + radius; px < x+width-radius; px++ {
		img.Set(px, y, c)
		img.Set(px, y+height-1, c)
	}

	for py := y + radius; py < y+height-radius; py++ {
		img.Set(x, py, c)
		img.Set(x+width-1, py, c)
	}
}

func insideRoundedRect(
	px int,
	py int,
	x int,
	y int,
	width int,
	height int,
	radius int,
) bool {

	left := x
	right := x + width - 1
	top := y
	bottom := y + height - 1

	if px >= left+radius &&
		px <= right-radius {
		return py >= top && py <= bottom
	}

	if py >= top+radius &&
		py <= bottom-radius {
		return px >= left && px <= right
	}

	corners := [][2]int{
		{left + radius, top + radius},
		{right - radius, top + radius},
		{left + radius, bottom - radius},
		{right - radius, bottom - radius},
	}

	for _, corner := range corners {
		dx := px - corner[0]
		dy := py - corner[1]

		if dx*dx+dy*dy <= radius*radius {
			return true
		}
	}

	return false
}

func totalBytes(
	languages []LanguageInfo,
) int64 {

	var total int64

	for _, language := range languages {
		total += language.Bytes
	}

	return total
}

func updateREADME(
	languages []LanguageInfo,
) error {

	data, err := os.ReadFile(readmeFile)

	if err != nil {
		return err
	}

	original := string(data)

	var section strings.Builder

	section.WriteString(readmeStart)
	section.WriteString("\n\n")

	section.WriteString(
		"![Linguagens do projeto](./github-languages.png)",
	)

	section.WriteString("\n\n")

	section.WriteString(
		fmt.Sprintf(
			"**%d linguagens detectadas** pelo GitHub Linguist.\n",
			len(languages),
		),
	)

	section.WriteString("\n")
	section.WriteString(readmeEnd)

	newSection := section.String()

	var result string

	if strings.Contains(
		original,
		readmeStart,
	) &&
		strings.Contains(
			original,
			readmeEnd,
		) {

		start := strings.Index(
			original,
			readmeStart,
		)

		end := strings.Index(
			original,
			readmeEnd,
		)

		end += len(readmeEnd)

		result =
			original[:start] +
				newSection +
				original[end:]

	} else {

		result =
			strings.TrimRight(
				original,
				"\n",
			) +
				"\n\n" +
				"## 🎨 Linguagens do projeto\n\n" +
				newSection +
				"\n"
	}

	return os.WriteFile(
		readmeFile,
		[]byte(result),
		0644,
	)
}

func _unusedBase64Compatibility() {
	_ = bytes.NewBuffer(nil)
	_ = base64.StdEncoding
}