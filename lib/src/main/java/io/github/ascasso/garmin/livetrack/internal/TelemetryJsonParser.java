package io.github.ascasso.garmin.livetrack.internal;

import io.github.ascasso.garmin.livetrack.model.Position;
import io.github.ascasso.garmin.livetrack.model.SessionReference;
import io.github.ascasso.garmin.livetrack.model.TelemetrySnapshot;
import io.github.ascasso.garmin.livetrack.model.TrackPoint;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public final class TelemetryJsonParser {
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    public TelemetryJsonParser() {
    }

    public TelemetrySnapshot parse(SessionReference sessionReference, String json) {
        if (sessionReference == null) {
            throw new IllegalArgumentException("sessionReference is required");
        }
        if (json == null) {
            throw new IllegalArgumentException("json is required");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("json must be valid telemetry JSON", e);
        }
        JsonNode trackPoints = root.path("trackPoints");
        if (trackPoints.isMissingNode() || trackPoints.isNull()) {
            return new TelemetrySnapshot(sessionReference, List.of());
        }
        if (!trackPoints.isArray()) {
            throw new IllegalArgumentException("trackPoints must be an array when present");
        }

        List<TrackPoint> parsedTrackPoints = new ArrayList<>();
        for (JsonNode trackPoint : trackPoints) {
            parsedTrackPoints.add(parseTrackPoint(trackPoint));
        }
        return new TelemetrySnapshot(sessionReference, parsedTrackPoints);
    }

    private static TrackPoint parseTrackPoint(JsonNode trackPoint) {
        Position position = new Position(requiredDouble(trackPoint, "latitude"), requiredDouble(trackPoint, "longitude"));
        Instant timestamp = parseTimestamp(trackPoint);
        Double altitude = optionalDouble(trackPoint, "altitude");
        if (altitude == null) {
            altitude = optionalDouble(trackPoint, "altitudeMeters");
        }
        return new TrackPoint(position, timestamp, altitude);
    }

    private static double requiredDouble(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull() || !value.isNumber()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.doubleValue();
    }

    private static Double optionalDouble(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        if (!value.isNumber()) {
            throw new IllegalArgumentException(fieldName + " must be numeric when present");
        }
        return value.doubleValue();
    }

    private static Instant parseTimestamp(JsonNode trackPoint) {
        for (String fieldName : List.of("timestamp", "time", "dateTime", "gpsTime")) {
            JsonNode value = trackPoint.path(fieldName);
            if (value.isMissingNode() || value.isNull()) {
                continue;
            }
            String timestamp = value.asString();
            try {
                return Instant.parse(timestamp);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(fieldName + " must be an ISO-8601 instant", e);
            }
        }
        throw new IllegalArgumentException("timestamp is required");
    }
}
