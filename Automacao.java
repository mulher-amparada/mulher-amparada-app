import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Automacao {

    private static final String OWNER = "mulher-amparada";
    private static final String REPO = "mulher-amparada-app";

    private static final String API =
        "https://api.github.com/repos/"
        + OWNER + "/" + REPO + "/actions/workflows/";

    private static final HttpClient CLIENT =
        HttpClient.newHttpClient();

    private static String obterToken() {
        String tokenBase64 = System.getenv("GH_TOKEN_B64");

        if (tokenBase64 == null || tokenBase64.isBlank()) {
            throw new IllegalStateException(
                "Configure o segredo GH_TOKEN_B64 no GitHub."
            );
        }

        try {
            return new String(
                Base64.getDecoder().decode(tokenBase64),
                StandardCharsets.UTF_8
            ).trim();
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "O segredo GH_TOKEN_B64 não contém Base64 válido."
            );
        }
    }

    private static void executar(
            String token,
            String workflow,
            String inputs) throws Exception {

        String body =
            "{\"ref\":\"main\",\"inputs\":" + inputs + "}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API + workflow + "/dispatches"))
            .header("Accept", "application/vnd.github+json")
            .header("Authorization", "Bearer " + token)
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(
                body, StandardCharsets.UTF_8
            ))
            .build();

        HttpResponse<String> response = CLIENT.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 204) {
            throw new RuntimeException(
                "Falha em " + workflow
                + ": HTTP " + response.statusCode()
                + " — " + response.body()
            );
        }

        System.out.println(
            "Workflow solicitado: " + workflow
        );
    }

    public static void main(String[] args) throws Exception {
        String token = obterToken();

        executar(
            token,
            "github-agent.yml",
            "{\"action\":\"dependabot\",\"branch\":\"main\"}"
        );

        executar(
            token,
            "automations.yml",
            "{}"
        );

        System.out.println(
            "Solicitações de automação enviadas."
        );
    }
}
