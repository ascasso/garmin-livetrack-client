package io.github.ascasso.garmin.livetrack.exception;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class LiveTrackExceptionTest {
    @Test
    void httpExceptionDoesNotIncludeTokens() {
        LiveTrackHttpException exception = new LiveTrackHttpException(404);

        assertFalse(exception.toString().contains("token"));
    }
}
