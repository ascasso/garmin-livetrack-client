package io.github.ascasso.garmin.livetrack.internal;

import java.net.URI;
import java.util.Locale;

public final class TokenRedactor {
    private static final String REDACTED = "<redacted>";

    private TokenRedactor() {
    }

    public static String redact(URI uri) {
        if (uri == null) {
            return null;
        }
        String value = uri.toString();
        value = redactQueryValue(value, "token");
        value = redactQueryValue(value, "sessionToken");
        value = redactQueryValue(value, "shareToken");
        value = redactQueryValue(value, "auth");
        value = redactFragmentValue(value, "token");
        return value;
    }

    private static String redactQueryValue(String value, String parameterName) {
        return value.replaceAll("(?i)([?&]" + parameterName + "=)[^&#]*", "$1" + REDACTED);
    }

    private static String redactFragmentValue(String value, String parameterName) {
        String lowerValue = value.toLowerCase(Locale.ROOT);
        String lowerParameter = parameterName.toLowerCase(Locale.ROOT) + "=";
        int fragmentStart = lowerValue.indexOf('#');
        if (fragmentStart < 0 || !lowerValue.substring(fragmentStart + 1).contains(lowerParameter)) {
            return value;
        }
        return value.substring(0, fragmentStart + 1)
                + value.substring(fragmentStart + 1)
                        .replaceAll("(?i)(" + parameterName + "=)[^&]*", "$1" + REDACTED);
    }
}
