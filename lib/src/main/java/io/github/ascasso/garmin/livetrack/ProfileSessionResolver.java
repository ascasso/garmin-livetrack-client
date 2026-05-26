package io.github.ascasso.garmin.livetrack;

import io.github.ascasso.garmin.livetrack.config.LiveTrackClientOptions;
import io.github.ascasso.garmin.livetrack.exception.LiveTrackException;
import io.github.ascasso.garmin.livetrack.exception.LiveTrackHttpException;
import io.github.ascasso.garmin.livetrack.exception.LiveTrackParseException;
import io.github.ascasso.garmin.livetrack.exception.LiveTrackTransportException;
import io.github.ascasso.garmin.livetrack.internal.ProfileSessionJsonParser;
import io.github.ascasso.garmin.livetrack.internal.ProfileSessionJsonParser.ProfileSessionsPage;
import io.github.ascasso.garmin.livetrack.internal.ProfileSessionListParser;
import io.github.ascasso.garmin.livetrack.internal.ProfileSessionListParser.ProfileSessionsEndpoint;
import io.github.ascasso.garmin.livetrack.model.SavedSession;
import io.github.ascasso.garmin.livetrack.model.SessionReference;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ProfileSessionResolver {
    private static final int PROFILE_SESSIONS_PAGE_LIMIT = 100;
    private static final Pattern SESSION_LINK = Pattern.compile(
            "(https?://[^\"'\\\\\\s<>]+/session/[A-Za-z0-9-]+/token/[A-Za-z0-9]+(?:\\?[^\"'\\\\\\s<>]+)?"
                    + "|/session/[A-Za-z0-9-]+/token/[A-Za-z0-9]+(?:\\?[^\"'\\\\\\s<>]+)?)");

    private final HttpClient httpClient;
    private final LiveTrackClientOptions options;
    private final ProfileSessionListParser profileSessionListParser = new ProfileSessionListParser();
    private final ProfileSessionJsonParser profileSessionJsonParser = new ProfileSessionJsonParser();

    ProfileSessionResolver(HttpClient httpClient, LiveTrackClientOptions options) {
        this.httpClient = httpClient;
        this.options = options;
    }

    Optional<SessionReference> resolve(URI profileUri) {
        HttpResponse<String> response = fetchProfile(profileUri);

        int statusCode = response.statusCode();
        if (statusCode >= 300 && statusCode < 400) {
            return response.headers()
                    .firstValue("Location")
                    .map(profileUri::resolve)
                    .map(sessionUri -> new SessionReference(profileUri, sessionUri));
        }
        if (statusCode < 200 || statusCode >= 300) {
            throw new LiveTrackHttpException(statusCode);
        }

        return findEmbeddedSessionUri(profileUri, response.body())
                .map(sessionUri -> new SessionReference(profileUri, sessionUri));
    }

    List<SavedSession> listSavedSessions(URI profileUri) {
        HttpResponse<String> response = fetchProfile(profileUri);

        int statusCode = response.statusCode();
        if (statusCode >= 300 && statusCode < 400) {
            return response.headers()
                    .firstValue("Location")
                    .map(profileUri::resolve)
                    .map(sessionUri -> new SavedSession(
                            new SessionReference(profileUri, sessionUri),
                            Optional.<String>empty(),
                            Optional.empty()))
                    .map(List::of)
                    .orElseGet(List::of);
        }
        if (statusCode < 200 || statusCode >= 300) {
            throw new LiveTrackHttpException(statusCode);
        }

        Optional<ProfileSessionsEndpoint> endpoint =
                profileSessionListParser.findProfileSessionsEndpoint(profileUri, response.body());
        if (endpoint.isPresent()) {
            try {
                return fetchProfileSessionsEndpoint(profileUri, endpoint.get(), cookieHeader(response));
            } catch (LiveTrackException e) {
                if (!(e instanceof LiveTrackHttpException httpException) || httpException.statusCode() != 403) {
                    throw e;
                }
            }
        }

        return profileSessionListParser.parse(profileUri, response.body());
    }

    private HttpResponse<String> fetchProfile(URI profileUri) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(profileUri)
                .timeout(options.requestTimeout())
                .GET();
        options.userAgent().ifPresent(userAgent -> requestBuilder.header("User-Agent", userAgent));
        HttpRequest request = requestBuilder.build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new LiveTrackTransportException("Garmin LiveTrack profile request failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LiveTrackTransportException("Garmin LiveTrack profile request was interrupted", e);
        }
        return response;
    }

    private List<SavedSession> fetchProfileSessionsEndpoint(
            URI profileUri,
            ProfileSessionsEndpoint endpoint,
            Optional<String> cookieHeader) {
        Map<URI, SavedSession> savedSessionsByUri = new LinkedHashMap<>();
        Optional<Instant> startBefore = Optional.empty();

        while (true) {
            HttpResponse<String> response = fetchProfileSessionsPage(endpoint, startBefore, cookieHeader);
            int statusCode = response.statusCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new LiveTrackHttpException(statusCode);
            }

            ProfileSessionsPage page;
            try {
                page = profileSessionJsonParser.parse(profileUri, response.body());
            } catch (IllegalArgumentException e) {
                throw new LiveTrackParseException("Garmin LiveTrack profile session payload could not be parsed", e);
            }

            page.savedSessions().forEach(savedSession ->
                    savedSessionsByUri.putIfAbsent(savedSession.sessionReference().sessionUri(), savedSession));

            if (page.savedSessions().size() < PROFILE_SESSIONS_PAGE_LIMIT
                    || page.nextStartBefore().isEmpty()
                    || page.nextStartBefore().equals(startBefore)) {
                break;
            }
            startBefore = page.nextStartBefore();
        }

        return List.copyOf(savedSessionsByUri.values());
    }

    private HttpResponse<String> fetchProfileSessionsPage(
            ProfileSessionsEndpoint endpoint,
            Optional<Instant> startBefore,
            Optional<String> cookieHeader) {
        URI uri = profileSessionsPageUri(endpoint.uri(), startBefore);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri)
                .timeout(options.requestTimeout())
                .header("Accept", "application/json")
                .header("Livetrack-Csrf-Token", endpoint.csrfToken())
                .GET();
        options.userAgent().ifPresent(userAgent -> requestBuilder.header("User-Agent", userAgent));
        cookieHeader.ifPresent(value -> requestBuilder.header("Cookie", value));

        HttpRequest request = requestBuilder.build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new LiveTrackTransportException("Garmin LiveTrack profile sessions request failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LiveTrackTransportException("Garmin LiveTrack profile sessions request was interrupted", e);
        }
    }

    private static URI profileSessionsPageUri(URI endpointUri, Optional<Instant> startBefore) {
        StringBuilder value = new StringBuilder(endpointUri.toString())
                .append("?limit=")
                .append(PROFILE_SESSIONS_PAGE_LIMIT);
        startBefore.ifPresent(instant -> value.append("&startBefore=")
                .append(URLEncoder.encode(instant.toString(), StandardCharsets.UTF_8)));
        return URI.create(value.toString());
    }

    private static Optional<String> cookieHeader(HttpResponse<?> response) {
        List<String> cookies = response.headers().allValues("Set-Cookie").stream()
                .map(value -> value.split(";", 2)[0])
                .filter(value -> !value.isBlank())
                .toList();
        return cookies.isEmpty() ? Optional.empty() : Optional.of(String.join("; ", cookies));
    }

    private static Optional<URI> findEmbeddedSessionUri(URI profileUri, String body) {
        if (body == null || body.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = SESSION_LINK.matcher(body);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String value = matcher.group(1).replace("&amp;", "&");
        return Optional.of(profileUri.resolve(value));
    }
}
