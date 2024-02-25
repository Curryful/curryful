package io.github.curryful.rest;

import java.util.regex.Pattern;

public final class UriUtils {

    private static final Pattern pathParameterPattern = Pattern.compile("(:([^/?]+))");

    private UriUtils() {
        // noop
    }

    public static String replaceFormalParametersWithRegex(String uri) {
        var matcher = pathParameterPattern.matcher(uri);

        if (!matcher.find()) {
            return uri;
        }

        var formalParameter = matcher.group(1);
        var formalParameterName = matcher.group(2);

        var replaced = uri.replace(formalParameter, String.format("(?<%s>[^/?]+)", formalParameterName));
        return replaceFormalParametersWithRegex(replaced);
    }
}
