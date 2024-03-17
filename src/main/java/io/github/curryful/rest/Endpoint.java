package io.github.curryful.rest;

/**
 * An endpoint is a combination of a {@link Destination} and a {@link RestFunction}.
 */
public final class Endpoint {

    private final Destination destination;
    private final RestFunction restFunction;

    private Endpoint(Destination destination, RestFunction restFunction) {
        this.destination = destination;
        this.restFunction = restFunction;
    }

    public static Endpoint of(Destination destination, RestFunction restFunction) {
        return new Endpoint(destination, restFunction);
    }

    public Destination getDestination() {
        return destination;
    }

    public RestFunction getRestFunction() {
        return restFunction;
    }
}
