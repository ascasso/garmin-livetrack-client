package io.github.ascasso.garmin.livetrack.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TelemetrySnapshotTest {
    @Test
    void copiesTrackPointsDefensively() {
        List<TrackPoint> trackPoints = new ArrayList<>(List.of(
                new TrackPoint(new Position(45.0, -122.0), Instant.parse("2026-05-24T12:00:00Z"), null)));
        TelemetrySnapshot snapshot = new TelemetrySnapshot(SessionReference.of(URI.create("https://example.test/session")), trackPoints);

        trackPoints.clear();

        assertTrue(trackPoints.isEmpty());
        assertFalse(snapshot.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.trackPoints().add(null));
    }
}
