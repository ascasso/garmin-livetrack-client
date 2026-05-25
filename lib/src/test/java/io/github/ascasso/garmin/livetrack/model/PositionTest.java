package io.github.ascasso.garmin.livetrack.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PositionTest {
    @Test
    void acceptsValidCoordinates() {
        Position position = new Position(45.123, -122.456);

        assertEquals(45.123, position.latitude());
        assertEquals(-122.456, position.longitude());
    }

    @Test
    void rejectsInvalidLatitude() {
        assertThrows(IllegalArgumentException.class, () -> new Position(91.0, 10.0));
    }

    @Test
    void rejectsInvalidLongitude() {
        assertThrows(IllegalArgumentException.class, () -> new Position(10.0, 181.0));
    }
}
