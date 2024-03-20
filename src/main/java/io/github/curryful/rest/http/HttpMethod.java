package io.github.curryful.rest.http;

import java.util.Map;

import io.github.curryful.commons.monads.Maybe;

public enum HttpMethod {

	GET,
	POST,
	PUT,
	DELETE,
	NONE;

	private static final Map<String, HttpMethod> parseMap = Map.of(
		"GET", GET,
		"POST", POST,
		"PUT", PUT,
		"DELETE", DELETE
	);

	public static Maybe<HttpMethod> fromString(String method) {
		return Maybe.ofNullable(parseMap.get(method));
	}
}

