package io.github.ascasso.garmin.livetrack;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascasso.garmin.livetrack.config.LiveTrackClientOptions;
import io.github.ascasso.garmin.livetrack.model.SavedSession;
import io.github.ascasso.garmin.livetrack.model.SessionReference;
import io.github.ascasso.garmin.livetrack.model.TelemetrySnapshot;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class GarminLiveTrackManualIT {
    @Test
    @EnabledIfSystemProperty(named = "garmin.livetrack.sessionUrl", matches = "https://.+")
    void fetchesTelemetryFromGarminSessionUrl() {
        URI sessionUri = URI.create(System.getProperty("garmin.livetrack.sessionUrl"));
        String host = sessionUri.getHost();
        assertThat(host).isNotNull();
        assertThat(host.equals("garmin.com") || host.endsWith(".garmin.com")).isTrue();

        LiveTrackClient client = new LiveTrackClient(java.net.http.HttpClient.newHttpClient(), manualOptions());

        TelemetrySnapshot snapshot = client.fetchTelemetry(SessionReference.of(sessionUri));

        assertThat(snapshot).isNotNull();
        assertThat(snapshot.trackPoints()).isNotNull();
    }

    @Test
    @EnabledIfSystemProperty(named = "garmin.livetrack.profileName", matches = "[A-Za-z0-9_-]{3,64}")
    void resolvesActiveSessionFromGarminProfileName() {
        LiveTrackClient client = new LiveTrackClient(java.net.http.HttpClient.newHttpClient(), manualOptions());

        Optional<SessionReference> session = client.resolveActiveSession(System.getProperty("garmin.livetrack.profileName"));

        if (Boolean.getBoolean("garmin.livetrack.expectActive")) {
            assertThat(session)
                    .as("Garmin profile should expose an active LiveTrack session")
                    .isPresent();
        }

        session.ifPresent(reference -> {
            assertThat(reference.userUri()).isEqualTo(URI.create("https://live.garmin.com/")
                    .resolve(System.getProperty("garmin.livetrack.profileName")));
            assertThat(reference.sessionUri().getHost()).isNotNull();
        });
    }

    @Test
    @EnabledIfSystemProperty(named = "garmin.livetrack.profileName", matches = "[A-Za-z0-9_-]{3,64}")
    void listsSavedSessionsFromGarminProfileName() {
        LiveTrackClient client = new LiveTrackClient(java.net.http.HttpClient.newHttpClient(), manualOptions());

        List<SavedSession> sessions = client.listSavedSessions(System.getProperty("garmin.livetrack.profileName"));

        assertThat(sessions).isNotNull();
        sessions.forEach(session -> {
            assertThat(session.sessionReference().sessionUri().getHost()).isNotNull();
            assertThat(session.toString()).contains("<redacted>");
        });
    }

    private static LiveTrackClientOptions manualOptions() {
        String userAgent = System.getProperty("garmin.livetrack.userAgent");
        LiveTrackClientOptions options = LiveTrackClientOptions.defaults();
        return userAgent == null || userAgent.isBlank() ? options : options.withUserAgent(userAgent);
    }
}
