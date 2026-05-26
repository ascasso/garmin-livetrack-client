package io.github.ascasso.garmin.livetrack.config;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public record LiveTrackClientOptions(Duration requestTimeout, Optional<String> userAgent) {
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);

    public LiveTrackClientOptions {
        requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
        userAgent = Objects.requireNonNull(userAgent, "userAgent");
        if (requestTimeout.isZero() || requestTimeout.isNegative()) {
            throw new IllegalArgumentException("requestTimeout must be positive");
        }
        userAgent.ifPresent(value -> {
            if (value.isBlank()) {
                throw new IllegalArgumentException("userAgent must not be blank when present");
            }
        });
    }

    public LiveTrackClientOptions(Duration requestTimeout) {
        this(requestTimeout, Optional.empty());
    }

    public static LiveTrackClientOptions defaults() {
        return new LiveTrackClientOptions(DEFAULT_REQUEST_TIMEOUT);
    }

    public LiveTrackClientOptions withUserAgent(String userAgent) {
        return new LiveTrackClientOptions(requestTimeout, Optional.of(userAgent));
    }
}
