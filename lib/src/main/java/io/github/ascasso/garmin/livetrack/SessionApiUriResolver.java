package io.github.ascasso.garmin.livetrack;

import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SessionApiUriResolver {
    private static final Pattern SESSION_PATH = Pattern.compile("^/session/([A-Za-z0-9-]+)/token/([A-Za-z0-9]+)");
    private static final Pattern API_PATH = Pattern.compile("^/api/sessions/[^/]+$");

    private SessionApiUriResolver() {
    }

    static URI resolve(URI sessionUri) {
        return parseSessionPath(sessionUri)
                .map(session -> URI.create(sessionUri.getScheme() + "://" + sessionUri.getRawAuthority()
                        + "/api/sessions/" + session.sessionId() + "?token=" + session.token()))
                .orElse(sessionUri);
    }

    static Optional<SessionToken> parseSessionPath(URI sessionUri) {
        String rawPath = sessionUri.getRawPath();
        Matcher matcher = SESSION_PATH.matcher(rawPath);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(new SessionToken(matcher.group(1), matcher.group(2)));
    }

    static boolean isSessionApiUri(URI uri) {
        return API_PATH.matcher(uri.getRawPath()).matches();
    }

    record SessionToken(String sessionId, String token) {
    }
}
