package io.github.ascasso.garmin.livetrack.internal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.ascasso.garmin.livetrack.model.SessionReference;
import io.github.ascasso.garmin.livetrack.model.TelemetrySnapshot;
import java.net.URI;
import org.junit.jupiter.api.Test;

class TelemetryJsonParserTest {
    private final TelemetryJsonParser parser = new TelemetryJsonParser();
    private final SessionReference sessionReference = SessionReference.of(URI.create("https://example.test/session?token=secret"));

    @Test
    void parsesEmptyTrackPointsAsEmptySnapshot() {
        TelemetrySnapshot snapshot = parser.parse(sessionReference, "{\"trackPoints\":[]}");

        assertTrue(snapshot.isEmpty());
    }

    @Test
    void parsesAbsentTrackPointsAsEmptySnapshot() {
        TelemetrySnapshot snapshot = parser.parse(sessionReference, "{\"unexpected\":true}");

        assertTrue(snapshot.isEmpty());
    }

    @Test
    void rejectsTrackPointWithoutRequiredCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(
                sessionReference,
                "{\"trackPoints\":[{\"timestamp\":\"2026-05-24T12:00:00Z\",\"latitude\":45.0}]}"));
    }
}
