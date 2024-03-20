package io.github.curryful.rest;

import java.util.function.Function;

import io.github.curryful.rest.http.HttpContext;
import io.github.curryful.rest.http.HttpResponse;

/**
 * Represents a function that takes an HTTP context and returns an HTTP response.
 */
public interface RestFunction extends Function<HttpContext, HttpResponse> {
    // noop
}

