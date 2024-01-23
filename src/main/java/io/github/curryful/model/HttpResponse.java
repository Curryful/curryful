package io.github.curryful.model;

import io.github.curryful.commons.Maybe;

public final class HttpResponse<T> {

    private final HttpResponseCode code;
    private final Maybe<T> body;

    public HttpResponse(HttpResponseCode code) {
        this.code = code;
        this.body = Maybe.none();
    }

    public HttpResponse(HttpResponseCode code, T body) {
        this.code = code;
        this.body = Maybe.just(body);
    }

    public HttpResponseCode getCode() {
        return code;
    }

    public Maybe<T> getBody() {
        return body;
    }
}
