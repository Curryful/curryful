package io.github.curryful.rest.http;

import java.net.InetAddress;

import io.github.curryful.commons.collections.ImmutableMaybeHashMap;
import io.github.curryful.commons.monads.Maybe;

/**
 * Represents the context of an HTTP request.
 */
public final class HttpContext {

	private final HttpMethod method;
	private final String actualUri;
	private final String formalUri;
	private final ImmutableMaybeHashMap<String, String> pathParameters;
	private final ImmutableMaybeHashMap<String, String> queryParameters;
    private final ImmutableMaybeHashMap<String, String> headers;
	private final InetAddress address;
    private final Maybe<String> body;

    private HttpContext(
		HttpMethod method,
		String actualUri,
		String formalUri,
		ImmutableMaybeHashMap<String, String> pathParameters,
		ImmutableMaybeHashMap<String, String> queryParameters,
		ImmutableMaybeHashMap<String, String> headers,
		InetAddress address,
		Maybe<String> body
	) {
		this.method = method;
		this.actualUri = actualUri;
		this.formalUri = formalUri;
		this.pathParameters = pathParameters;
		this.queryParameters = queryParameters;
        this.headers = headers;
		this.address = address;
        this.body = body;
    }

	public static final HttpContext of(
		HttpMethod method,
		String actualUri,
		String formalUri,
		ImmutableMaybeHashMap<String, String> pathParameters,
		ImmutableMaybeHashMap<String, String> queryParameters, 
		ImmutableMaybeHashMap<String, String> headers,
		InetAddress address,
		Maybe<String> body
	) {
		return new HttpContext(method, actualUri, formalUri, pathParameters, queryParameters, headers, address, body);
	}

    public static final HttpContext empty() {
        return new HttpContext(HttpMethod.NONE, "", "", ImmutableMaybeHashMap.empty(), ImmutableMaybeHashMap.empty(),
				ImmutableMaybeHashMap.empty(), InetAddress.getLoopbackAddress(), Maybe.none());
    }

	public HttpMethod getMethod() {
	    return method;
	}

	public String getActualUri() {
	    return actualUri;
	}

	public String getFormalUri() {
	    return formalUri;
	}

	public ImmutableMaybeHashMap<String, String> getPathParameters() {
		return pathParameters;
	}

	public ImmutableMaybeHashMap<String, String> getQueryParameters() {
		return queryParameters;
	}

    public ImmutableMaybeHashMap<String, String> getHeaders() {
    	return headers;
    } 

	public InetAddress getAddress() {
	    return address;
	}

    public Maybe<String> getBody() {
        return body;
    }
}

