package io.github.ascasso.garmin.livetrack.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public record SavedSession(
        SessionReference sessionReference,
        Optional<String> sessionName,
        Optional<Instant> startedAt) {
    public SavedSession {
        sessionReference = Objects.requireNonNull(sessionReference, "sessionReference");
        sessionName = Objects.requireNonNull(sessionName, "sessionName");
        startedAt = Objects.requireNonNull(startedAt, "startedAt");
    }
}
