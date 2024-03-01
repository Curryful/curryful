package io.github.curryful.rest;

import io.github.curryful.commons.Maybe;
import io.github.curryful.commons.MaybeHashMap;

public final class HttpContext {

	private final MaybeHashMap<String, String> pathParameters;
	private final MaybeHashMap<String, String> queryParameters;
    private final MaybeHashMap<String, String> headers;
    private final Maybe<String> content;

    public HttpContext(MaybeHashMap<String, String> pathParameters, MaybeHashMap<String, String> queryParameters, MaybeHashMap<String, String> headers, Maybe<String> content) {
		this.pathParameters = pathParameters;
		this.queryParameters = queryParameters;
        this.headers = headers;
        this.content = content;
    }

    public static final HttpContext empty() {
        return new HttpContext(new MaybeHashMap<>(), new MaybeHashMap<>(), new MaybeHashMap<>(), Maybe.none());
    }

	public MaybeHashMap<String, String> getPathParameters() {
		return pathParameters;
	}

	public MaybeHashMap<String, String> getQueryParameters() {
		return queryParameters;
	}

    public MaybeHashMap<String, String> getHeaders() {
    	return headers;
    } 

    public Maybe<String> getContent() {
        return content;
    }
}

