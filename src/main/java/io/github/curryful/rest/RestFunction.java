package io.github.curryful.rest;

import java.util.function.Function;

/**
 * Represents a function that takes an HTTP context and returns an HTTP response.
 */
public interface RestFunction extends Function<HttpContext, HttpResponse<?>> {
    // noop
}

