package io.github.ascasso.garmin.livetrack.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.Test;

class SessionReferenceTest {
    @Test
    void toStringRedactsKnownTokenQueryParameters() {
        SessionReference reference = new SessionReference(
                URI.create("https://share.garmin.com/example?token=user-secret"),
                URI.create("https://livetrack.garmin.com/session?sessionToken=session-secret&keep=true"));

        String rendered = reference.toString();

        assertThat(rendered)
                .doesNotContain("user-secret", "session-secret")
                .contains("token=<redacted>", "sessionToken=<redacted>");
    }

    @Test
    void toStringRedactsTokenFragments() {
        SessionReference reference = SessionReference.of(URI.create("https://example.test/live#token=fragment-secret&mode=public"));

        String rendered = reference.toString();

        assertThat(rendered)
                .doesNotContain("fragment-secret")
                .contains("token=<redacted>");
    }
}
