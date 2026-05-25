package io.github.ascasso.garmin.livetrack.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LiveTrackExceptionTest {
    @Test
    void httpExceptionDoesNotIncludeTokens() {
        LiveTrackHttpException exception = new LiveTrackHttpException(404);

        assertThat(exception).asString().doesNotContain("token");
    }
}
