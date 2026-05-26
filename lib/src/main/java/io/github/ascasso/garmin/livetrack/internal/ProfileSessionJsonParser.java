package io.github.ascasso.garmin.livetrack.internal;

import io.github.ascasso.garmin.livetrack.model.SavedSession;
import io.github.ascasso.garmin.livetrack.model.SessionReference;
import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public final class ProfileSessionJsonParser {
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    public ProfileSessionsPage parse(URI profileUri, String json) {
        if (profileUri == null) {
            throw new IllegalArgumentException("profileUri is required");
        }
        if (json == null) {
            throw new IllegalArgumentException("json is required");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("json must be valid profile session JSON", e);
        }

        JsonNode completedSessions = root.path("completedSessions");
        if (completedSessions.isMissingNode() || completedSessions.isNull()) {
            return new ProfileSessionsPage(List.of(), Optional.empty());
        }
        if (!completedSessions.isArray()) {
            throw new IllegalArgumentException("completedSessions must be an array when present");
        }

        List<SavedSession> savedSessions = new ArrayList<>();
        Optional<Instant> nextStartBefore = Optional.empty();
        for (JsonNode completedSession : completedSessions) {
            SavedSession savedSession = parseCompletedSession(profileUri, completedSession);
            savedSessions.add(savedSession);
            nextStartBefore = savedSession.startedAt();
        }
        return new ProfileSessionsPage(List.copyOf(savedSessions), nextStartBefore);
    }

    private static SavedSession parseCompletedSession(URI profileUri, JsonNode completedSession) {
        String sessionUrl = optionalText(completedSession, "sessionUrl")
                .orElseGet(() -> sessionPath(
                        requiredText(completedSession, "sessionId"),
                        requiredText(completedSession, "sessionToken")));
        Optional<Instant> startedAt = optionalText(completedSession, "startDate")
                .map(ProfileSessionJsonParser::parseInstant);

        return new SavedSession(
                new SessionReference(profileUri, profileUri.resolve(sessionUrl)),
                optionalText(completedSession, "sessionName"),
                startedAt);
    }

    private static String sessionPath(String sessionId, String sessionToken) {
        return "/session/" + sessionId + "/token/" + sessionToken;
    }

    private static String requiredText(JsonNode node, String fieldName) {
        return optionalText(node, fieldName)
                .orElseThrow(() -> new IllegalArgumentException(fieldName + " is required"));
    }

    private static Optional<String> optionalText(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return Optional.empty();
        }
        if (!value.isString()) {
            throw new IllegalArgumentException(fieldName + " must be a string when present");
        }
        String text = value.asString().strip();
        return text.isEmpty() ? Optional.empty() : Optional.of(text);
    }

    private static Instant parseInstant(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("startDate must be an ISO-8601 instant", e);
        }
    }

    public record ProfileSessionsPage(
            List<SavedSession> savedSessions,
            Optional<Instant> nextStartBefore) {
        public ProfileSessionsPage {
            savedSessions = List.copyOf(savedSessions);
            nextStartBefore = nextStartBefore == null ? Optional.empty() : nextStartBefore;
        }
    }
}
