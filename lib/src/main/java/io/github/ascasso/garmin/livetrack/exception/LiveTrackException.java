package io.github.ascasso.garmin.livetrack.exception;

public class LiveTrackException extends RuntimeException {
    public LiveTrackException(String message) {
        super(message);
    }

    public LiveTrackException(String message, Throwable cause) {
        super(message, cause);
    }
}
