package io.github.curryful.rest;

import static java.util.regex.Pattern.compile;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public final class Uri {

    private static final Pattern PATH_PARAMETER_PATTERN = Pattern.compile("(:([^/?]+))");
    private static final String PATH_PARAMETER_GROUP_PLACEHOLDER_REGEX = "(?<%s>[^/?]+)";
    private static final Pattern QUERY_PARAMETER_PATTERN = Pattern.compile("([^&=]+)=([^&=]+)");

    private Uri() {
        // noop
    }

    public static String replaceFormalParametersWithRegex(String uri) {
        var matcher = PATH_PARAMETER_PATTERN.matcher(uri);

        if (!matcher.find()) {
            return uri;
        }

        var formalParameter = matcher.group(1);
        var formalParameterName = matcher.group(2);

        var replaced = uri.replace(formalParameter,
                String.format(PATH_PARAMETER_GROUP_PLACEHOLDER_REGEX, formalParameterName));
        return replaceFormalParametersWithRegex(replaced);
    }

    public static Map<String, String> getPathParameters(String formalUri, String actualUri) {
        var matcher = compile(replaceFormalParametersWithRegex(formalUri)).matcher(actualUri);

		if (!matcher.find()) {
			return Map.of();
		}

        BiConsumer<HashMap<String, String>, Pair<String, String>> putPair =
                (map, pair) -> map.put(pair.getFirst(), pair.getSecond());

        var namedGroups = matcher.namedGroups();
        return namedGroups
                .keySet()
                .stream()
                .map(key -> Pair.of(key, matcher.group(key)))
                .collect(HashMap::new, putPair, Map::putAll);
    }

    public static Map<String, String> getQueryParameters(String uri) {
        var uriParts = uri.split("\\?");

        if (uriParts.length < 2) {
            return Map.of();
        }

        BiConsumer<HashMap<String, String>, MatchResult> putMatchResult =
                (map, matchResult) -> map.put(matchResult.group(1), matchResult.group(2));

        return QUERY_PARAMETER_PATTERN
                .matcher(uriParts[1])
                .results()
                .collect(HashMap::new, putMatchResult, HashMap::putAll);
    }
}
