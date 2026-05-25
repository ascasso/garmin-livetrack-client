package io.github.ascasso.garmin.livetrack;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.ascasso.garmin.livetrack.model.SessionReference;
import io.github.ascasso.garmin.livetrack.model.TelemetrySnapshot;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class GarminLiveTrackManualIT {
    @Test
    @EnabledIfSystemProperty(named = "garmin.livetrack.sessionUrl", matches = "https://.+")
    void fetchesTelemetryFromGarminSessionUrl() {
        URI sessionUri = URI.create(System.getProperty("garmin.livetrack.sessionUrl"));
        String host = sessionUri.getHost();
        assertNotNull(host);
        assertTrue(host.equals("garmin.com") || host.endsWith(".garmin.com"));

        LiveTrackClient client = new LiveTrackClient();

        TelemetrySnapshot snapshot = client.fetchTelemetry(SessionReference.of(sessionUri));

        assertNotNull(snapshot);
        assertNotNull(snapshot.trackPoints());
    }
}
