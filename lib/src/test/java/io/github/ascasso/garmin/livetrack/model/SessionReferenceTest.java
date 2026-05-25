package io.github.ascasso.garmin.livetrack.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import org.junit.jupiter.api.Test;

class SessionReferenceTest {
    @Test
    void toStringRedactsKnownTokenQueryParameters() {
        SessionReference reference = new SessionReference(
                URI.create("https://share.garmin.com/example?token=user-secret"),
                URI.create("https://livetrack.garmin.com/session?sessionToken=session-secret&keep=true"));

        String rendered = reference.toString();

        assertFalse(rendered.contains("user-secret"));
        assertFalse(rendered.contains("session-secret"));
        assertTrue(rendered.contains("token=<redacted>"));
        assertTrue(rendered.contains("sessionToken=<redacted>"));
    }

    @Test
    void toStringRedactsTokenFragments() {
        SessionReference reference = SessionReference.of(URI.create("https://example.test/live#token=fragment-secret&mode=public"));

        String rendered = reference.toString();

        assertFalse(rendered.contains("fragment-secret"));
        assertTrue(rendered.contains("token=<redacted>"));
    }
}
