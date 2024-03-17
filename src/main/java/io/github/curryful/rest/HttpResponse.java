package io.github.curryful.rest;

import io.github.curryful.commons.Maybe;

/**
 * An HTTP response.
 */
public final class HttpResponse<T> {

    private final HttpResponseCode code;
    private final Maybe<T> body;

    private HttpResponse(HttpResponseCode code) {
        this.code = code;
        this.body = Maybe.none();
    }

    private HttpResponse(HttpResponseCode code, T body) {
        this.code = code;
        this.body = Maybe.just(body);
    }

	public static <T> HttpResponse<T> of(HttpResponseCode code) {
		return new HttpResponse<>(code);
	}

	public static <T> HttpResponse<T> of(HttpResponseCode code, T body) {
		return new HttpResponse<>(code, body);
	}

    public HttpResponseCode getCode() {
        return code;
    }

    public Maybe<T> getBody() {
        return body;
    }
}

