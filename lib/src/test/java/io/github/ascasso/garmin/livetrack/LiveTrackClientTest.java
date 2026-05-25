package io.github.ascasso.garmin.livetrack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.ascasso.garmin.livetrack.config.LiveTrackClientOptions;
import io.github.ascasso.garmin.livetrack.exception.LiveTrackHttpException;
import io.github.ascasso.garmin.livetrack.model.LiveTrackSession;
import io.github.ascasso.garmin.livetrack.model.SessionReference;
import io.github.ascasso.garmin.livetrack.model.TelemetrySnapshot;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
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
            server.redirect("/ascasso", "/session/current/token/secrettoken");
            LiveTrackClient client = new LiveTrackClient(HttpClient.newHttpClient());

            Optional<SessionReference> session = client.resolveActiveSession(server.uri("/ascasso"));

            assertThat(session).hasValueSatisfying(reference -> {
                assertThat(reference.userUri()).isEqualTo(server.uri("/ascasso"));
                assertThat(reference.sessionUri()).isEqualTo(server.uri("/session/current/token/secrettoken"));
                assertThat(reference.toString()).doesNotContain("secrettoken");
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
                        <a href="/session/current/token/secrettoken?unit=metric&amp;track=true">Current</a>
                      </body>
                    </html>
                    """);
            LiveTrackClient client = new LiveTrackClient(HttpClient.newHttpClient());

            Optional<SessionReference> session = client.resolveActiveSession(server.uri("/ascasso"));

            assertThat(session).hasValueSatisfying(reference ->
                    assertThat(reference.sessionUri())
                            .isEqualTo(server.uri("/session/current/token/secrettoken?unit=metric&track=true")));
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
                      window.__session = "https://livetrack.garmin.com/session/current/token/secrettoken\\"";
                    </script>
                    """);
            LiveTrackClient client = new LiveTrackClient(HttpClient.newHttpClient());

            Optional<SessionReference> session = client.resolveActiveSession(server.uri("/ascasso"));

            assertThat(session).hasValueSatisfying(reference -> {
                assertThat(reference.sessionUri())
                        .isEqualTo(URI.create("https://livetrack.garmin.com/session/current/token/secrettoken"));
                assertThat(reference.toString()).doesNotContain("secrettoken");
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

    @Test
    void fetchesSessionPayloadFromSessionLinkApi() throws IOException {
        try (TestServer server = TestServer.start()) {
            AtomicReference<HttpExchange> request = new AtomicReference<>();
            server.get(
                    "/api/sessions/session-id",
                    200,
                    """
                    {
                      "sessionName": "Morning ride",
                      "sessionType": "INREACH_TRACKING",
                      "viewable": true,
                      "points": [
                        {
                          "position": {"lat": 45.5, "lon": -122.6},
                          "dateTime": "2026-05-25T20:00:00Z",
                          "altitude": 123.4
                        }
                      ]
                    }
                    """,
                    request);
            LiveTrackClient client = new LiveTrackClient(
                    HttpClient.newHttpClient(),
                    new LiveTrackClientOptions(Duration.ofSeconds(5))
                            .withUserAgent("Mozilla/5.0 test"));
            SessionReference reference = SessionReference.of(server.uri("/session/session-id/token/secrettoken"));

            LiveTrackSession session = client.fetchSession(reference);

            assertThat(session.sessionName()).contains("Morning ride");
            assertThat(session.sessionType()).contains("INREACH_TRACKING");
            assertThat(session.viewable()).isTrue();
            assertThat(session.trackPoints()).hasSize(1);
            assertThat(session.trackPoints().getFirst().position().latitude()).isEqualTo(45.5);
            assertThat(session.trackPoints().getFirst().position().longitude()).isEqualTo(-122.6);
            assertThat(session.trackPoints().getFirst().timestamp()).isEqualTo(Instant.parse("2026-05-25T20:00:00Z"));
            assertThat(request.get().getRequestURI().toString())
                    .isEqualTo("/api/sessions/session-id?token=secrettoken");
            assertThat(request.get().getRequestHeaders().getFirst("Referer"))
                    .isEqualTo(server.uri("/session/session-id/token/secrettoken").toString());
            assertThat(request.get().getRequestHeaders().getFirst("User-Agent"))
                    .isEqualTo("Mozilla/5.0 test");
        }
    }

    @Test
    void fetchTelemetryUsesSessionPayloadApiForSessionLinks() throws IOException {
        try (TestServer server = TestServer.start()) {
            server.get(
                    "/api/sessions/session-id",
                    200,
                    """
                    {
                      "viewable": true,
                      "points": [
                        {"position": {"lat": 45.5, "lon": -122.6}, "dateTime": "2026-05-25T20:00:00Z"}
                      ]
                    }
                    """);
            LiveTrackClient client = new LiveTrackClient(HttpClient.newHttpClient());
            SessionReference reference = SessionReference.of(server.uri("/session/session-id/token/secrettoken"));

            TelemetrySnapshot snapshot = client.fetchTelemetry(reference);

            assertThat(snapshot.trackPoints()).hasSize(1);
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

        void get(String path, int statusCode, String body, AtomicReference<HttpExchange> request) {
            server.createContext(path, exchange -> {
                request.set(exchange);
                respond(exchange, statusCode, body);
            });
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
