package io.github.curryful.rest;

public final class Destination {
    
    private final HttpMethod method;
    private final String uri;

    public Destination(HttpMethod method, String uri) {
        this.method = method;
        this.uri = uri;
    }

	public boolean matches(Destination other) {
		return method.equals(other.method) && true;
	}

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }
}
