package io.github.ascasso.garmin.livetrack.config;

import java.time.Duration;
import java.util.Objects;

public record LiveTrackClientOptions(Duration requestTimeout) {
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);

    public LiveTrackClientOptions {
        requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
        if (requestTimeout.isZero() || requestTimeout.isNegative()) {
            throw new IllegalArgumentException("requestTimeout must be positive");
        }
    }

    public static LiveTrackClientOptions defaults() {
        return new LiveTrackClientOptions(DEFAULT_REQUEST_TIMEOUT);
    }
}
