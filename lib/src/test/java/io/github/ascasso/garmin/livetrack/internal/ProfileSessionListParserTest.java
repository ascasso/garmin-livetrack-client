package io.github.ascasso.garmin.livetrack.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascasso.garmin.livetrack.model.SavedSession;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProfileSessionListParserTest {
    private final ProfileSessionListParser parser = new ProfileSessionListParser();
    private final URI profileUri = URI.create("https://live.garmin.com/ascasso");

    @Test
    void parsesSavedSessionsFromCompletedSessionCards() {
        List<SavedSession> sessions = parser.parse(
                profileUri,
                """
                <article data-sentry-component="CompletedSession">
                  <time dateTime="2026-05-23T16:12:30.000Z">May 23, 2026</time>
                  <h2>
                    <a title="Maupin May 23, 2026"
                       href="/session/4213eb17-aeb1-8384-bd1b-06c4d0810201/token/secrettoken?unit=metric&amp;track=true">
                      Maupin May 23, 2026
                    </a>
                  </h2>
                </article>
                <article data-sentry-component="CompletedSession">
                  <time dateTime="2026-05-22T21:45:00.000Z">May 22, 2026</time>
                  <h2>
                    <a title="Bend May 22, 2026"
                       href="/session/fc6293c5-88a9-8ab3-95a1-1ac900b89701/token/othertoken">
                      Bend May 22, 2026
                    </a>
                  </h2>
                </article>
                """);

        assertThat(sessions).hasSize(2);
        assertThat(sessions.getFirst().sessionName()).contains("Maupin May 23, 2026");
        assertThat(sessions.getFirst().startedAt()).contains(Instant.parse("2026-05-23T16:12:30Z"));
        assertThat(sessions.getFirst().sessionReference().sessionUri())
                .isEqualTo(URI.create(
                        "https://live.garmin.com/session/4213eb17-aeb1-8384-bd1b-06c4d0810201/token/secrettoken?unit=metric&track=true"));
        assertThat(sessions.getFirst().toString()).doesNotContain("secrettoken");
        assertThat(sessions.get(1).sessionName()).contains("Bend May 22, 2026");
    }

    @Test
    void fallsBackToSessionLinksWhenCompletedSessionCardsAreAbsent() {
        List<SavedSession> sessions = parser.parse(
                profileUri,
                """
                <a href="/session/current/token/secrettoken">Current Session</a>
                """);

        assertThat(sessions).singleElement().satisfies(session -> {
            assertThat(session.sessionName()).contains("Current Session");
            assertThat(session.startedAt()).isEmpty();
            assertThat(session.sessionReference().sessionUri())
                    .isEqualTo(URI.create("https://live.garmin.com/session/current/token/secrettoken"));
        });
    }

    @Test
    void findsProfileSessionsEndpointFromProfilePage() {
        assertThat(parser.findProfileSessionsEndpoint(
                        profileUri,
                        """
                        <meta name="csrf-token" content="csrf-value"/>
                        <script>{"garminGuid":"7c3258ba-a0e9-4311-a19c-1dcc1d263be7"}</script>
                        """))
                .hasValueSatisfying(endpoint -> {
                    assertThat(endpoint.uri())
                            .isEqualTo(URI.create(
                                    "https://live.garmin.com/api/user/7c3258ba-a0e9-4311-a19c-1dcc1d263be7/profile-sessions"));
                    assertThat(endpoint.csrfToken()).isEqualTo("csrf-value");
                });
    }

    @Test
    void findsProfileSessionsEndpointFromEscapedHydrationData() {
        assertThat(parser.findProfileSessionsEndpoint(
                        profileUri,
                        """
                        <meta name="csrf-token" content="csrf-value"/>
                        <script>{\\"garminGuid\\":\\"7c3258ba-a0e9-4311-a19c-1dcc1d263be7\\"}</script>
                        """))
                .isPresent();
    }
}
