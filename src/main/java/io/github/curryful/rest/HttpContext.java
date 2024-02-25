package io.github.curryful.rest;

import java.util.Map;

import io.github.curryful.commons.Maybe;

public final class HttpContext {

	private final Map<String, String> pathParameters;
	private final Map<String, String> queryParameters;
    private final Map<String, String> headers;
    private final Maybe<String> content;

    public HttpContext(Map<String, String> pathParameters, Map<String, String> queryParameters, Map<String, String> headers, Maybe<String> content) {
		this.pathParameters = pathParameters;
		this.queryParameters = queryParameters;
        this.headers = headers;
        this.content = content;
    }

    public static final HttpContext empty() {
        return new HttpContext(Map.of(), Map.of(), Map.of(), Maybe.none());
    }

	public Map<String, String> getPathParameters() {
		return pathParameters;
	}

	public Map<String, String> getQueryParameters() {
		return queryParameters;
	}

    public Map<String, String> getHeaders() {
    	return headers;
    } 

    public Maybe<String> getContent() {
        return content;
    }
}
