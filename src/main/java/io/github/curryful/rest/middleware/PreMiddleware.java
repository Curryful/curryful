package io.github.curryful.rest.middleware;

import io.github.curryful.rest.HttpContext;

/**
 * {@link FunctionalInterface} to register middleware to be executed before the
 * request is processed.
 */
@FunctionalInterface
public interface PreMiddleware {

	public static final PreMiddleware empty = context -> context;

	public HttpContext apply(HttpContext context);

	default PreMiddleware andThen(PreMiddleware after) {
        return (HttpContext context) -> after.apply(apply(context));
    }
}

