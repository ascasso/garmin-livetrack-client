package io.github.ascasso.garmin.livetrack.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LiveTrackExceptionTest {
    @Test
    void storesMessage() {
        LiveTrackException exception = new LiveTrackException("Garmin LiveTrack request failed");

        assertThat(exception)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Garmin LiveTrack request failed")
                .hasNoCause();
    }

    @Test
    void storesMessageAndCause() {
        IllegalStateException cause = new IllegalStateException("network unavailable");

        LiveTrackException exception = new LiveTrackException("Garmin LiveTrack request failed", cause);

        assertThat(exception)
                .hasMessage("Garmin LiveTrack request failed")
                .hasCause(cause);
    }

    @Test
    void httpExceptionStoresStatusCode() {
        LiveTrackHttpException exception = new LiveTrackHttpException(404);

        assertThat(exception)
                .isInstanceOf(LiveTrackException.class)
                .hasMessage("Garmin LiveTrack request failed with HTTP status 404")
                .hasNoCause();
        assertThat(exception.statusCode()).isEqualTo(404);
    }

    @Test
    void parseExceptionStoresMessageAndCause() {
        IllegalArgumentException cause = new IllegalArgumentException("malformed telemetry");

        LiveTrackParseException exception = new LiveTrackParseException(
                "Garmin LiveTrack telemetry payload could not be parsed",
                cause);

        assertThat(exception)
                .isInstanceOf(LiveTrackException.class)
                .hasMessage("Garmin LiveTrack telemetry payload could not be parsed")
                .hasCause(cause);
    }

    @Test
    void transportExceptionStoresMessageAndCause() {
        IllegalStateException cause = new IllegalStateException("request interrupted");

        LiveTrackTransportException exception = new LiveTrackTransportException(
                "Garmin LiveTrack request was interrupted",
                cause);

        assertThat(exception)
                .isInstanceOf(LiveTrackException.class)
                .hasMessage("Garmin LiveTrack request was interrupted")
                .hasCause(cause);
    }

    @Test
    void fixedMessagesDoNotExposeTokenTerminology() {
        assertThat(new LiveTrackHttpException(404)).asString().doesNotContainIgnoringCase("token");
        assertThat(new LiveTrackParseException(
                        "Garmin LiveTrack telemetry payload could not be parsed",
                        new IllegalArgumentException("malformed telemetry")))
                .asString()
                .doesNotContainIgnoringCase("token");
        assertThat(new LiveTrackTransportException(
                        "Garmin LiveTrack request failed",
                        new IllegalStateException("network unavailable")))
                .asString()
                .doesNotContainIgnoringCase("token");
    }
}
