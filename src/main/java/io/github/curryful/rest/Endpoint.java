package io.github.curryful.rest;

import java.util.function.Function;

import io.github.curryful.model.HttpContext;
import io.github.curryful.model.HttpResponse;

public interface Endpoint extends Function<HttpContext, HttpResponse<?>> {
    // noop
}
