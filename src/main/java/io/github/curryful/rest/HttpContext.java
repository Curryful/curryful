package io.github.curryful.rest;

import java.util.Map;

import io.github.curryful.commons.Maybe;

public final class HttpContext {

    private final Maybe<Map<String, String>> headers;
    private final Maybe<String> content;

    public HttpContext(Maybe<Map<String, String>> headers, Maybe<String> content) {
        this.headers = headers;
        this.content = content;
    }

    public Maybe<Map<String, String>> getHeaders() {
        return headers;
    }

    public Maybe<String> getContent() {
        return content;
    }
}
