package io.github.curryful.rest.http;

import io.github.curryful.commons.collections.ImmutableMaybeHashMap;
import io.github.curryful.commons.monads.Maybe;

/**
 * An HTTP response.
 */
public final class HttpResponse {

    private final HttpResponseCode code;
	private final ImmutableMaybeHashMap<String, String> headers;
    private final Maybe<String> body;
	private final HttpContentType contentType;

    private HttpResponse(HttpResponseCode code) {
        this.code = code;
		this.headers = ImmutableMaybeHashMap.empty();
        this.body = Maybe.none();
		this.contentType = HttpContentType.NONE;
    }

	private HttpResponse(HttpResponseCode code, ImmutableMaybeHashMap<String, String> headers) {
		this.code = code;
		this.headers = headers;
		this.body = Maybe.none();
		this.contentType = HttpContentType.NONE;
	}

	private HttpResponse(HttpResponseCode code, String body, HttpContentType contentType) {
		this.code = code;
		this.headers = ImmutableMaybeHashMap.empty();
		this.body = Maybe.just(body);
		this.contentType = contentType;
	}

    private HttpResponse(HttpResponseCode code, ImmutableMaybeHashMap<String, String> headers, String body, HttpContentType contentType) {
        this.code = code;
		this.headers = headers;
        this.body = Maybe.just(body);
		this.contentType = contentType;
    }

	public static HttpResponse of(HttpResponseCode code) {
		return new HttpResponse(code);
	}

	public static HttpResponse of(HttpResponseCode code, ImmutableMaybeHashMap<String, String> headers) {
		return new HttpResponse(code, headers);
	}

	public static HttpResponse of(HttpResponseCode code, String body, HttpContentType contentType) {
		return new HttpResponse(code, body, contentType);
	}

	public static HttpResponse of(HttpResponseCode code, ImmutableMaybeHashMap<String, String> headers, String body, HttpContentType contentType) {
		return new HttpResponse(code, headers, body, contentType);
	}

    public HttpResponseCode getCode() {
        return code;
    }

	public ImmutableMaybeHashMap<String, String> getHeaders() {
	    return headers;
	}

    public Maybe<String> getBody() {
        return body;
    }

	public HttpContentType getContentType() {
	    return contentType;
	}
}

