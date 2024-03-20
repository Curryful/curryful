package io.github.curryful.rest.http;

import io.github.curryful.commons.monads.Maybe;

/**
 * An HTTP response.
 */
public final class HttpResponse {

    private final HttpResponseCode code;
    private final Maybe<String> body;
	private final HttpContentType contentType;

    private HttpResponse(HttpResponseCode code) {
        this.code = code;
        this.body = Maybe.none();
		this.contentType = HttpContentType.NONE;
    }

    private HttpResponse(HttpResponseCode code, String body, HttpContentType contentType) {
        this.code = code;
        this.body = Maybe.just(body);
		this.contentType = contentType;
    }

	public static HttpResponse of(HttpResponseCode code) {
		return new HttpResponse(code);
	}

	public static HttpResponse of(HttpResponseCode code, String body, HttpContentType contentType) {
		return new HttpResponse(code, body, contentType);
	}

    public HttpResponseCode getCode() {
        return code;
    }

    public Maybe<String> getBody() {
        return body;
    }

	public HttpContentType getContentType() {
	    return contentType;
	}
}

