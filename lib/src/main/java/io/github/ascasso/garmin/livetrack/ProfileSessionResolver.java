package io.github.ascasso.garmin.livetrack;

import io.github.ascasso.garmin.livetrack.config.LiveTrackClientOptions;
import io.github.ascasso.garmin.livetrack.exception.LiveTrackHttpException;
import io.github.ascasso.garmin.livetrack.exception.LiveTrackTransportException;
import io.github.ascasso.garmin.livetrack.model.SessionReference;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ProfileSessionResolver {
    private static final Pattern SESSION_LINK = Pattern.compile(
            "(https?://[^\"'\\\\\\s<>]+/session/[A-Za-z0-9-]+/token/[A-Za-z0-9]+(?:\\?[^\"'\\\\\\s<>]+)?"
                    + "|/session/[A-Za-z0-9-]+/token/[A-Za-z0-9]+(?:\\?[^\"'\\\\\\s<>]+)?)");

    private final HttpClient httpClient;
    private final LiveTrackClientOptions options;

    ProfileSessionResolver(HttpClient httpClient, LiveTrackClientOptions options) {
        this.httpClient = httpClient;
        this.options = options;
    }

    Optional<SessionReference> resolve(URI profileUri) {
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
