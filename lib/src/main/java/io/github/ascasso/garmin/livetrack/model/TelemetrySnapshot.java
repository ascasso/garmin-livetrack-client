package io.github.ascasso.garmin.livetrack.model;

import java.util.List;
import java.util.Objects;

public record TelemetrySnapshot(SessionReference sessionReference, List<TrackPoint> trackPoints) {
    public TelemetrySnapshot {
        sessionReference = Objects.requireNonNull(sessionReference, "sessionReference");
        trackPoints = List.copyOf(Objects.requireNonNull(trackPoints, "trackPoints"));
    }

    public boolean isEmpty() {
        return trackPoints.isEmpty();
    }
}
