package io.github.ascasso.garmin.livetrack;

import io.github.ascasso.garmin.livetrack.config.LiveTrackClientOptions;
import io.github.ascasso.garmin.livetrack.exception.LiveTrackHttpException;
import io.github.ascasso.garmin.livetrack.exception.LiveTrackParseException;
import io.github.ascasso.garmin.livetrack.exception.LiveTrackTransportException;
import io.github.ascasso.garmin.livetrack.internal.TelemetryJsonParser;
import io.github.ascasso.garmin.livetrack.model.SessionReference;
import io.github.ascasso.garmin.livetrack.model.TelemetrySnapshot;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

/**
 * Synchronous client for fetching Garmin LiveTrack telemetry snapshots.
 */
public final class LiveTrackClient {
    private final HttpClient httpClient;
    private final LiveTrackClientOptions options;
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
        this.telemetryJsonParser = new TelemetryJsonParser();
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
