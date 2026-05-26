package io.github.ascasso.garmin.livetrack.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record LiveTrackSession(
        SessionReference sessionReference,
        Optional<String> sessionName,
        Optional<String> sessionType,
        boolean viewable,
        List<TrackPoint> trackPoints) {
    public LiveTrackSession {
        sessionReference = Objects.requireNonNull(sessionReference, "sessionReference");
        sessionName = Objects.requireNonNull(sessionName, "sessionName");
        sessionType = Objects.requireNonNull(sessionType, "sessionType");
        trackPoints = List.copyOf(Objects.requireNonNull(trackPoints, "trackPoints"));
    }

    public TelemetrySnapshot telemetrySnapshot() {
        return new TelemetrySnapshot(sessionReference, trackPoints);
    }

    public boolean isEmpty() {
        return trackPoints.isEmpty();
    }
}
