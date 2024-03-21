package io.github.curryful.rest.middleware;

import java.util.function.UnaryOperator;

import io.github.curryful.rest.http.HttpContext;
import io.github.curryful.rest.http.HttpResponse;

/**
 * {@link FunctionalInterface} to register middleware to be executed after the
 * request is processed.
 */
@FunctionalInterface
public interface PostMiddleware {

	public static final PostMiddleware none = context -> response -> response;

	public UnaryOperator<HttpResponse> apply(HttpContext context);

	default PostMiddleware andThen(PostMiddleware after) {
        return (HttpContext context) -> (HttpResponse reponse) -> after.apply(context).apply(apply(context).apply(reponse));
    }
}

