package io.github.ascasso.garmin.livetrack.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class LiveTrackClientOptionsTest {
    @Test
    void providesDefaultRequestTimeout() {
        assertThat(LiveTrackClientOptions.defaults().requestTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(LiveTrackClientOptions.defaults().userAgent()).isEmpty();
    }

    @Test
    void rejectsNonPositiveTimeout() {
        assertThatThrownBy(() -> new LiveTrackClientOptions(Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void configuresUserAgent() {
        LiveTrackClientOptions options = LiveTrackClientOptions.defaults().withUserAgent("Mozilla/5.0 test");

        assertThat(options.userAgent()).contains("Mozilla/5.0 test");
    }

    @Test
    void rejectsBlankUserAgent() {
        assertThatThrownBy(() -> LiveTrackClientOptions.defaults().withUserAgent(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
