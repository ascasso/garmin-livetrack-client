package io.github.ascasso.garmin.livetrack.model;

import io.github.ascasso.garmin.livetrack.internal.TokenRedactor;
import java.net.URI;
import java.util.Objects;

public record SessionReference(URI userUri, URI sessionUri) {
    public SessionReference {
        userUri = Objects.requireNonNull(userUri, "userUri");
        sessionUri = Objects.requireNonNull(sessionUri, "sessionUri");
    }

    public static SessionReference of(URI sessionUri) {
        return new SessionReference(sessionUri, sessionUri);
    }

    public String redactedUserUri() {
        return TokenRedactor.redact(userUri);
    }

    public String redactedSessionUri() {
        return TokenRedactor.redact(sessionUri);
    }

    @Override
    public String toString() {
        return "SessionReference[userUri=" + redactedUserUri()
                + ", sessionUri=" + redactedSessionUri()
                + "]";
    }
}
