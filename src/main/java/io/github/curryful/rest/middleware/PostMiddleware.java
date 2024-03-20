package io.github.curryful.rest.middleware;

import io.github.curryful.rest.http.HttpContext;
import io.github.curryful.rest.http.HttpResponse;

/**
 * {@link FunctionalInterface} to register middleware to be executed after the
 * request is processed.
 */
@FunctionalInterface
public interface PostMiddleware {

	public static final PostMiddleware empty = (context, response) -> response;

	public HttpResponse apply(HttpContext context, HttpResponse response);

	default PostMiddleware andThen(PostMiddleware after) {
        return (HttpContext context, HttpResponse reponse) -> after.apply(context, apply(context, reponse));
    }
}

