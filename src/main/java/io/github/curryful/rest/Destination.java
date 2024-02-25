package io.github.curryful.rest;

import java.util.regex.Pattern;

public final class Destination {

    private final HttpMethod method;
    private final String uri;

    public Destination(HttpMethod method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    /**
     * TODO needs a better name
     * @param other
     * @return
     */
	public boolean matches(Destination other) {
		return method.equals(other.method) &&
                Pattern.compile(UriUtils.replaceFormalParametersWithRegex(uri))
                        .matcher(other.uri)
                        .matches();
	}

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }
}
