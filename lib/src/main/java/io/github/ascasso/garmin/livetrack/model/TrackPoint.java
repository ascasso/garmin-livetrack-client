package io.github.ascasso.garmin.livetrack.model;

import java.time.Instant;
import java.util.Objects;

public record TrackPoint(Position position, Instant timestamp, Double altitudeMeters) {
    public TrackPoint {
        position = Objects.requireNonNull(position, "position");
        timestamp = Objects.requireNonNull(timestamp, "timestamp");
        if (altitudeMeters != null && !Double.isFinite(altitudeMeters)) {
            throw new IllegalArgumentException("altitudeMeters must be finite when present");
        }
    }
}
