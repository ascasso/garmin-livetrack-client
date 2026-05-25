package io.github.ascasso.garmin.livetrack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.ascasso.garmin.livetrack.exception.LiveTrackHttpException;
import io.github.ascasso.garmin.livetrack.model.SessionReference;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LiveTrackClientTest {
    @Test
    void resolvesEmptyWhenProfilePageHasNoActiveSessionLink() throws IOException {
        try (TestServer server = TestServer.start()) {
            server.get("/ascasso", 200, "<html><body>No active session</body></html>");
            LiveTrackClient client = new LiveTrackClient(HttpClient.newHttpClient());

            Optional<SessionReference> session = client.resolveActiveSession(server.uri("/ascasso"));

            assertThat(session).isEmpty();
        }
    }

    @Test
    void resolvesSessionFromProfileRedirect() throws IOException {
        try (TestServer server = TestServer.start()) {
            server.redirect("/ascasso", "/session/current/token/secret-token");
            LiveTrackClient client = new LiveTrackClient(HttpClient.newHttpClient());

            Optional<SessionReference> session = client.resolveActiveSession(server.uri("/ascasso"));

            assertThat(session).hasValueSatisfying(reference -> {
                assertThat(reference.userUri()).isEqualTo(server.uri("/ascasso"));
                assertThat(reference.sessionUri()).isEqualTo(server.uri("/session/current/token/secret-token"));
                assertThat(reference.toString()).doesNotContain("secret-token");
            });
        }
    }

    @Test
    void resolvesSessionFromEmbeddedProfileLink() throws IOException {
        try (TestServer server = TestServer.start()) {
            server.get(
                    "/ascasso",
                    200,
                    """
                    <html>
                      <body>
                        <a href="/session/current/token/secret-token?unit=metric&amp;track=true">Current</a>
                      </body>
                    </html>
                    """);
            LiveTrackClient client = new LiveTrackClient(HttpClient.newHttpClient());

            Optional<SessionReference> session = client.resolveActiveSession(server.uri("/ascasso"));

            assertThat(session).hasValueSatisfying(reference ->
                    assertThat(reference.sessionUri())
                            .isEqualTo(server.uri("/session/current/token/secret-token?unit=metric&track=true")));
        }
    }

    @Test
    void resolvesSessionFromEscapedJavascriptString() throws IOException {
        try (TestServer server = TestServer.start()) {
            server.get(
                    "/ascasso",
                    200,
                    """
                    <script>
                      window.__session = "https://livetrack.garmin.com/session/current/token/secret-token\\"";
                    </script>
                    """);
            LiveTrackClient client = new LiveTrackClient(HttpClient.newHttpClient());

            Optional<SessionReference> session = client.resolveActiveSession(server.uri("/ascasso"));

            assertThat(session).hasValueSatisfying(reference -> {
                assertThat(reference.sessionUri())
                        .isEqualTo(URI.create("https://livetrack.garmin.com/session/current/token/secret-token"));
                assertThat(reference.toString()).doesNotContain("secret-token");
            });
        }
    }


    @Test
    void rejectsInvalidProfileName() {
        LiveTrackClient client = new LiveTrackClient();

        assertThatThrownBy(() -> client.resolveActiveSession("../ascasso"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("profileName");
    }

    @Test
    void throwsForNonSuccessProfileStatus() throws IOException {
        try (TestServer server = TestServer.start()) {
            server.get("/ascasso", 404, "not found");
            LiveTrackClient client = new LiveTrackClient(HttpClient.newHttpClient());

            assertThatThrownBy(() -> client.resolveActiveSession(server.uri("/ascasso")))
                    .isInstanceOf(LiveTrackHttpException.class)
                    .hasMessageContaining("404");
        }
    }

    private static final class TestServer implements AutoCloseable {
        private final HttpServer server;

        private TestServer(HttpServer server) {
            this.server = server;
        }

        static TestServer start() throws IOException {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.start();
            return new TestServer(server);
        }

        URI uri(String path) {
            return URI.create("http://localhost:" + server.getAddress().getPort() + path);
        }

        void get(String path, int statusCode, String body) {
            server.createContext(path, exchange -> respond(exchange, statusCode, body));
        }

        void redirect(String path, String location) {
            server.createContext(path, exchange -> {
                exchange.getResponseHeaders().set("Location", location);
                respond(exchange, 302, "");
            });
        }

        private static void respond(HttpExchange exchange, int statusCode, String body) throws IOException {
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, bodyBytes.length);
            exchange.getResponseBody().write(bodyBytes);
            exchange.close();
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}
