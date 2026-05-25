package io.github.ascasso.garmin.livetrack.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class LiveTrackClientOptionsTest {
    @Test
    void providesDefaultRequestTimeout() {
        assertEquals(Duration.ofSeconds(30), LiveTrackClientOptions.defaults().requestTimeout());
    }

    @Test
    void rejectsNonPositiveTimeout() {
        assertThrows(IllegalArgumentException.class, () -> new LiveTrackClientOptions(Duration.ZERO));
    }
}
