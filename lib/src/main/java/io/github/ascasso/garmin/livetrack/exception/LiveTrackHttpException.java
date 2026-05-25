package io.github.ascasso.garmin.livetrack.exception;

public final class LiveTrackHttpException extends LiveTrackException {
    private final int statusCode;

    public LiveTrackHttpException(int statusCode) {
        super("Garmin LiveTrack request failed with HTTP status " + statusCode);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
