package io.github.ascasso.garmin.livetrack.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.ascasso.garmin.livetrack.internal.ProfileSessionJsonParser.ProfileSessionsPage;
import java.net.URI;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ProfileSessionJsonParserTest {
    private final ProfileSessionJsonParser parser = new ProfileSessionJsonParser();
    private final URI profileUri = URI.create("https://live.garmin.com/ascasso");

    @Test
    void parsesCompletedSessions() {
        ProfileSessionsPage page = parser.parse(
                profileUri,
                """
                {
                  "completedSessions": [
                    {
                      "sessionId": "4213eb17-aeb1-8384-bd1b-06c4d0810201",
                      "sessionToken": "secrettoken",
                      "sessionName": "Maupin May 23, 2026",
                      "startDate": "2026-05-23T19:38:15Z",
                      "sessionUrl": "https://livetrack.garmin.com/session/4213eb17-aeb1-8384-bd1b-06c4d0810201/token/secrettoken"
                    },
                    {
                      "sessionId": "dcb80f13-e01f-86c2-804a-c84209c29501",
                      "sessionToken": "othertoken",
                      "sessionName": "Williams Apr 22, 2025",
                      "startDate": "2025-04-22T16:54:30Z"
                    }
                  ]
                }
                """);

        assertThat(page.savedSessions()).hasSize(2);
        assertThat(page.savedSessions().getFirst().sessionName()).contains("Maupin May 23, 2026");
        assertThat(page.savedSessions().getFirst().sessionReference().sessionUri())
                .isEqualTo(URI.create(
                        "https://livetrack.garmin.com/session/4213eb17-aeb1-8384-bd1b-06c4d0810201/token/secrettoken"));
        assertThat(page.savedSessions().get(1).sessionReference().sessionUri())
                .isEqualTo(URI.create(
                        "https://live.garmin.com/session/dcb80f13-e01f-86c2-804a-c84209c29501/token/othertoken"));
        assertThat(page.nextStartBefore()).contains(Instant.parse("2025-04-22T16:54:30Z"));
        assertThat(page.savedSessions().getFirst().toString()).doesNotContain("secrettoken");
    }

    @Test
    void rejectsSessionWithoutUrlOrTokenFields() {
        assertThatThrownBy(() -> parser.parse(
                        profileUri,
                        """
                        {"completedSessions": [{"sessionName": "Missing URL"}]}
                        """))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sessionId");
    }
}
