package io.github.ascasso.garmin.livetrack.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PositionTest {
    @Test
    void acceptsValidCoordinates() {
        Position position = new Position(45.123, -122.456);

        assertThat(position.latitude()).isEqualTo(45.123);
        assertThat(position.longitude()).isEqualTo(-122.456);
    }

    @Test
    void rejectsInvalidLatitude() {
        assertThatThrownBy(() -> new Position(91.0, 10.0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidLongitude() {
        assertThatThrownBy(() -> new Position(10.0, 181.0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
