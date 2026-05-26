package io.github.ascasso.garmin.livetrack.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        assertThat(snapshot.isEmpty()).isTrue();
    }

    @Test
    void parsesAbsentTrackPointsAsEmptySnapshot() {
        TelemetrySnapshot snapshot = parser.parse(sessionReference, "{\"unexpected\":true}");

        assertThat(snapshot.isEmpty()).isTrue();
    }

    @Test
    void parsesGarminSessionPointsAsTelemetry() {
        TelemetrySnapshot snapshot = parser.parse(
                sessionReference,
                """
                {
                  "points": [
                    {
                      "position": {"lat": 45.0, "lon": -122.0},
                      "dateTime": "2026-05-25T20:00:00Z"
                    }
                  ]
                }
                """);

        assertThat(snapshot.trackPoints()).hasSize(1);
        assertThat(snapshot.trackPoints().getFirst().position().latitude()).isEqualTo(45.0);
        assertThat(snapshot.trackPoints().getFirst().position().longitude()).isEqualTo(-122.0);
    }

    @Test
    void rejectsTrackPointWithoutRequiredCoordinates() {
        assertThatThrownBy(() -> parser.parse(
                        sessionReference,
                        "{\"trackPoints\":[{\"timestamp\":\"2026-05-24T12:00:00Z\",\"latitude\":45.0}]}"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
