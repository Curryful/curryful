package io.github.curryful.rest;

import java.util.function.Function;

public interface RestFunction extends Function<HttpContext, HttpResponse<?>> {
    // noop
}
