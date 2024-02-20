package io.github.curryful.rest;

import java.util.function.Function;

public interface Endpoint extends Function<HttpContext, HttpResponse<?>> {
    // noop
}
