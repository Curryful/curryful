package io.github.curryful.rest;

import static io.github.curryful.commons.combinators.YCombinator.Y;
import static io.github.curryful.rest.Pair.putPairIntoMMHashMap;
import static java.util.regex.Pattern.compile;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import io.github.curryful.commons.collections.ImmutableMaybeHashMap;
import io.github.curryful.commons.collections.MutableMaybeHashMap;

public final class Uri {

    private static final Pattern PATH_PARAMETER_PATTERN = Pattern.compile("(:([^/?]+))");
    private static final String PATH_PARAMETER_GROUP_PLACEHOLDER_REGEX = "(?<%s>[^/?]+)";
    private static final Pattern QUERY_PARAMETER_PATTERN = Pattern.compile("([^&=]+)=([^&=]+)");

    private Uri() {
        // noop
    }

	private static final Function<
		Function<String, String>,
		Function<String, String>
	> replaceFormalParameterWithRegex = function -> uri -> {
		var matcher = PATH_PARAMETER_PATTERN.matcher(uri);

		if (!matcher.find()) {
			return uri;
		}

		var formalParameter = matcher.group(1);
		var formalParameterName = matcher.group(2);

		var replaced = uri.replace(formalParameter,
				String.format(PATH_PARAMETER_GROUP_PLACEHOLDER_REGEX, formalParameterName));
		return function.apply(replaced);
	};

	public static final Function<String, String> replaceFormalParametersWithRegex = Y(replaceFormalParameterWithRegex);

    public static final Function<
		String,
		Function<
			String,
			ImmutableMaybeHashMap<String, String>
		>
	>  getPathParameters = formalUri -> actualUri -> {
        var matcher = compile(replaceFormalParametersWithRegex.apply(formalUri)).matcher(actualUri);

		if (!matcher.find()) {
			return ImmutableMaybeHashMap.empty();
		}

        var namedGroups = matcher.namedGroups();
        return namedGroups
                .keySet()
                .stream()
                .map(key -> Pair.of(key, matcher.group(key)))
                .collect(MutableMaybeHashMap::empty, putPairIntoMMHashMap, MutableMaybeHashMap::putAll);
    };

    public static final Function<String, ImmutableMaybeHashMap<String, String>> getQueryParameters = uri -> {
        var uriParts = uri.split("\\?");

        if (uriParts.length < 2) {
            return ImmutableMaybeHashMap.empty();
        }

        BiConsumer<MutableMaybeHashMap<String, String>, MatchResult> putMatchResult =
                (map, matchResult) -> map.put(matchResult.group(1), matchResult.group(2));

        return QUERY_PARAMETER_PATTERN
                .matcher(uriParts[1])
                .results()
                .collect(MutableMaybeHashMap::empty, putMatchResult, MutableMaybeHashMap::putAll);
    };
}

