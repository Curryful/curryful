package io.github.curryful.rest;

import static io.github.curryful.rest.Pair.putPairIntoMMHashMap;
import static java.util.stream.Collectors.joining;

import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import io.github.curryful.commons.collections.ImmutableMaybeHashMap;
import io.github.curryful.commons.collections.MutableMaybeHashMap;
import io.github.curryful.commons.monads.Maybe;

/**
 * A class for HTTP utilities.
 */
public final class Http {

    /**
     * Get a single group match from a regex.
     */
    private static final Function<String, Function<String, Maybe<String>>> getSingleGroupMatch = regex -> text -> {
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(text);

        if (matcher.find()) {
            return Maybe.just(matcher.group(1));
        } else {
            return Maybe.none();
        }
    };

	/**
	 * Get the method of an HTTP request.
	 * Takes the request as a string and returns the method as a string,
	 * wrapped in a {@link Maybe}.
	 */
    public static final Function<Stream<String>, Maybe<String>> getMethod = stream -> {
        return Maybe.from(stream.findFirst()).flatMap(getSingleGroupMatch.apply("(\\S+)"));
    };

	/**
	 * Get the path of an HTTP request.
	 * Takes the request as a string and returns the path as a string,
	 * wrapped in a {@link Maybe}.
	 */
    public static final Function<Stream<String>, Maybe<String>> getPath = stream -> {
        return Maybe.from(stream.findFirst()).flatMap(getSingleGroupMatch.apply("\\S+ (\\S+)"));
    };

    /**
     * Get the headers of an HTTP request.
     * Takes the request as a string and returns the headers as a map.
     */
    public static final Function<Stream<String>, ImmutableMaybeHashMap<String, String>> getHeaders = stream -> {
        var regex = "(\\S+): (.*)";
        var pattern = Pattern.compile(regex);

        Function<String, Maybe<Pair<String, String>>> headerAsPair = line -> {
            var matcher = pattern.matcher(line);

            if (matcher.find()) {
                return Maybe.just(Pair.of(matcher.group(1), matcher.group(2)));
            } else {
                return Maybe.none();
            }
        };

		return stream
				.skip(1)
				.takeWhile(l -> !l.isEmpty())
				.map(headerAsPair)
				.filter(Maybe::hasValue)
				.map(Maybe::getValue)
				.collect(MutableMaybeHashMap::empty, putPairIntoMMHashMap, MutableMaybeHashMap::putAll);
    };

    /**
     * Get the body of an HTTP request.
     * Takes the request as a string and returns the body as a string,
	 * wrapped in a {@link Maybe}.
     */
    public static final Function<Stream<String>, Maybe<String>> getBody = stream -> {
        var body = stream.dropWhile(l -> !l.isEmpty()).skip(1).collect(joining("\n"));

        if (body.isEmpty()) {
            return Maybe.none();
        } else {
            return Maybe.just(body);
        }
    };

	/**
	 * Serialize an {@link HttpResponse}.
	 * Takes an {@link HttpResponse} and returns a string.
	 */
    public static final Function<HttpResponse<?>, String> serializeResponse = httpResponse -> {
        var sb = new StringBuilder(String.format("HTTP/1.1 %d %s\r\n", httpResponse.getCode().getCode(), httpResponse.getCode().getText()));

        if (httpResponse.getBody().hasValue()) {
            // TODO: Change to fit actual content type
            sb.append("Content-Type: text/html\r\n");
            sb.append("\r\n");
            sb.append(String.format("%s\r\n", httpResponse.getBody().getValue().toString()));
        }

        sb.append("\r\n");
        return sb.toString();
    };
}

