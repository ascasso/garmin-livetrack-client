package io.github.ascasso.garmin.livetrack.model;

public record Position(double latitude, double longitude) {
    public Position {
        if (!Double.isFinite(latitude) || latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("latitude must be finite and between -90 and 90");
        }
        if (!Double.isFinite(longitude) || longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("longitude must be finite and between -180 and 180");
        }
    }
}
