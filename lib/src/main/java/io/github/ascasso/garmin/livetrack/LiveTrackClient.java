package io.github.ascasso.garmin.livetrack;

import io.github.ascasso.garmin.livetrack.config.LiveTrackClientOptions;
import io.github.ascasso.garmin.livetrack.exception.LiveTrackHttpException;
import io.github.ascasso.garmin.livetrack.exception.LiveTrackParseException;
import io.github.ascasso.garmin.livetrack.exception.LiveTrackTransportException;
import io.github.ascasso.garmin.livetrack.internal.TelemetryJsonParser;
import io.github.ascasso.garmin.livetrack.model.SessionReference;
import io.github.ascasso.garmin.livetrack.model.TelemetrySnapshot;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Synchronous client for fetching Garmin LiveTrack telemetry snapshots.
 */
public final class LiveTrackClient {
    private static final URI DEFAULT_PROFILE_BASE_URI = URI.create("https://live.garmin.com/");
    private static final Pattern PROFILE_NAME = Pattern.compile("[A-Za-z0-9_-]{3,64}");

    private final HttpClient httpClient;
    private final LiveTrackClientOptions options;
    private final ProfileSessionResolver profileSessionResolver;
    private final TelemetryJsonParser telemetryJsonParser;

    public LiveTrackClient() {
        this(HttpClient.newHttpClient());
    }

    public LiveTrackClient(HttpClient httpClient) {
        this(httpClient, LiveTrackClientOptions.defaults());
    }

    public LiveTrackClient(HttpClient httpClient, LiveTrackClientOptions options) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.options = Objects.requireNonNull(options, "options");
        this.profileSessionResolver = new ProfileSessionResolver(httpClient, options);
        this.telemetryJsonParser = new TelemetryJsonParser();
    }

    public Optional<SessionReference> resolveActiveSession(String profileName) {
        Objects.requireNonNull(profileName, "profileName");
        if (!PROFILE_NAME.matcher(profileName).matches()) {
            throw new IllegalArgumentException("profileName must be 3 to 64 letters, numbers, underscores, or dashes");
        }
        return resolveActiveSession(DEFAULT_PROFILE_BASE_URI.resolve(profileName));
    }

    public Optional<SessionReference> resolveActiveSession(URI profileUri) {
        Objects.requireNonNull(profileUri, "profileUri");
        return profileSessionResolver.resolve(profileUri);
    }

    public TelemetrySnapshot fetchTelemetry(SessionReference sessionReference) {
        Objects.requireNonNull(sessionReference, "sessionReference");

        HttpRequest request = HttpRequest.newBuilder(sessionReference.sessionUri())
                .timeout(options.requestTimeout())
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new LiveTrackTransportException("Garmin LiveTrack request failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LiveTrackTransportException("Garmin LiveTrack request was interrupted", e);
        }

        int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new LiveTrackHttpException(statusCode);
        }

        try {
            return telemetryJsonParser.parse(sessionReference, response.body());
        } catch (IllegalArgumentException e) {
            throw new LiveTrackParseException("Garmin LiveTrack telemetry payload could not be parsed", e);
        }
    }
}
