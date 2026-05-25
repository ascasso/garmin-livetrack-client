package io.github.ascasso.garmin.livetrack.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class LiveTrackClientOptionsTest {
    @Test
    void providesDefaultRequestTimeout() {
        assertThat(LiveTrackClientOptions.defaults().requestTimeout()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void rejectsNonPositiveTimeout() {
        assertThatThrownBy(() -> new LiveTrackClientOptions(Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
