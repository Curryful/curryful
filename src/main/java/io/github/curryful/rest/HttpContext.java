package io.github.curryful.rest;

import java.net.InetAddress;

import io.github.curryful.commons.Maybe;
import io.github.curryful.commons.MaybeHashMap;

public final class HttpContext {

	private final HttpMethod method;
	private final String actualUri;
	private final String formalUri;
	private final MaybeHashMap<String, String> pathParameters;
	private final MaybeHashMap<String, String> queryParameters;
    private final MaybeHashMap<String, String> headers;
	private final InetAddress address;
    private final Maybe<String> content;

    private HttpContext(
		HttpMethod method,
		String actualUri,
		String formalUri,
		MaybeHashMap<String, String> pathParameters,
		MaybeHashMap<String, String> queryParameters,
		MaybeHashMap<String, String> headers,
		InetAddress address,
		Maybe<String> content
	) {
		this.method = method;
		this.actualUri = actualUri;
		this.formalUri = formalUri;
		this.pathParameters = pathParameters;
		this.queryParameters = queryParameters;
        this.headers = headers;
		this.address = address;
        this.content = content;
    }

	public static final HttpContext of(
		HttpMethod method,
		String actualUri,
		String formalUri,
		MaybeHashMap<String, String> pathParameters,
		MaybeHashMap<String, String> queryParameters, 
		MaybeHashMap<String, String> headers,
		InetAddress address,
		Maybe<String> content
	) {
		return new HttpContext(method, actualUri, formalUri, pathParameters, queryParameters, headers, address, content);
	}

    public static final HttpContext empty() {
        return new HttpContext(HttpMethod.NONE, "", "", new MaybeHashMap<>(), new MaybeHashMap<>(),
				new MaybeHashMap<>(), InetAddress.getLoopbackAddress(), Maybe.none());
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

	public MaybeHashMap<String, String> getPathParameters() {
		return pathParameters;
	}

	public MaybeHashMap<String, String> getQueryParameters() {
		return queryParameters;
	}

    public MaybeHashMap<String, String> getHeaders() {
    	return headers;
    } 

	public InetAddress getAddress() {
	    return address;
	}

    public Maybe<String> getContent() {
        return content;
    }
}

