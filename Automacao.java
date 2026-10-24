
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class Automacao {
    private static final String OWNER = "mulher-amparada";
    private static final String REPO = "mulher-amparada-app";
    private static final String API = "https://api.github.com/repos/"
            + OWNER + "/" + REPO + "/actions/workflows/";

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static void executar(String workflow, String inputs)
            throws Exception {

        String token = System.getenv("GH_TOKEN");

        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                "Configure o segredo GH_TOKEN no GitHub."
            );
        }

        String body = "{\"ref\":\"main\",\"inputs\":" + inputs + "}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API + workflow + "/dispatches"))
            .header("Accept", "application/vnd.github+json")
            .header("Authorization", "Bearer " + token)
            .header("X-GitHub-Api-Version", "2026-03-10")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(
                body, StandardCharsets.UTF_8
            ))
            .build();

        HttpResponse<String> response = CLIENT.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 204
                && response.statusCode() != 200) {
            throw new RuntimeException(
                workflow + " falhou: HTTP "
                + response.statusCode() + " — "
                + response.body()
            );
        }

        System.out.println("Solicitado: " + workflow);
    }

    public static void main(String[] args) throws Exception {
        // Inicia o agente na opção Dependabot.
        executar(
            "github-agent.yml",
            "{\"action\":\"dependabot\",\"branch\":\"main\"}"
        );

        // Solicita a automação principal.
        executar("automations.yml", "{}");

        System.out.println("Solicitações enviadas.");
    }
}
