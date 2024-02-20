package io.github.curryful.rest;

public final class Destination {
    
    private final HttpMethod method;
    private final String uri;

    public Destination(HttpMethod method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    @Override
    public int hashCode() {
        return (method.toString() + uri).hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Destination)) {
            return false;
        }

        return hashCode() == other.hashCode();
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }
}
