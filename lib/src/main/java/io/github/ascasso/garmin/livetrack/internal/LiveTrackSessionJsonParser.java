package io.github.ascasso.garmin.livetrack.internal;

import io.github.ascasso.garmin.livetrack.model.LiveTrackSession;
import io.github.ascasso.garmin.livetrack.model.SessionReference;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public final class LiveTrackSessionJsonParser {
    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private final TelemetryJsonParser telemetryJsonParser = new TelemetryJsonParser();

    public LiveTrackSession parse(SessionReference sessionReference, String json) {
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
            throw new IllegalArgumentException("json must be valid session JSON", e);
        }

        return new LiveTrackSession(
                sessionReference,
                optionalText(root, "sessionName"),
                optionalText(root, "sessionType"),
                optionalBoolean(root, "viewable").orElse(false),
                telemetryJsonParser.parseTrackPoints(root));
    }

    private static Optional<String> optionalText(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return Optional.empty();
        }
        if (!value.isString()) {
            throw new IllegalArgumentException(fieldName + " must be a string when present");
        }
        return Optional.of(value.asString());
    }

    private static Optional<Boolean> optionalBoolean(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return Optional.empty();
        }
        if (!value.isBoolean()) {
            throw new IllegalArgumentException(fieldName + " must be a boolean when present");
        }
        return Optional.of(value.booleanValue());
    }
}
