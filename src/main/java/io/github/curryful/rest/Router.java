package io.github.curryful.rest;

import static io.github.curryful.rest.Uri.getPathParameters;
import static io.github.curryful.rest.Uri.getQueryParameters;
import static io.github.curryful.rest.http.Http.getBody;
import static io.github.curryful.rest.http.Http.getHeaders;
import static io.github.curryful.rest.http.Http.getMethod;
import static io.github.curryful.rest.http.Http.getPath;

import java.net.InetAddress;
import java.util.function.Function;
import java.util.function.Predicate;

import io.github.curryful.commons.collections.ImmutableArrayList;
import io.github.curryful.commons.monads.Maybe;
import io.github.curryful.commons.monads.Try;
import io.github.curryful.rest.http.HttpContext;
import io.github.curryful.rest.http.HttpMethod;
import io.github.curryful.rest.http.HttpResponse;
import io.github.curryful.rest.http.HttpResponseCode;
import io.github.curryful.rest.middleware.PostMiddleware;
import io.github.curryful.rest.middleware.PreMiddleware;

/**
 * Class to hold functions for routing.
 */
public final class Router {

	/**
	 * Processes an HTTP request.
	 * Creates an {@link HttpContext} from the raw HTTP, applies pre-middleware,
	 * finds the matching endpoint, applies the endpoint's rest function and applies post-middleware.
	 * Returns an {@link HttpResponse}.
	 */
	public static final Function<
		ImmutableArrayList<PreMiddleware>,
		Function<
			ImmutableArrayList<Endpoint>,
			Function<
				ImmutableArrayList<PostMiddleware>,
				Function<
					InetAddress,
					Function<
						ImmutableArrayList<String>,
						Try<HttpResponse>
					>
				>
			>
		>	
    > route = preMiddleware -> endpoints -> postMiddleware -> address -> rawHttp -> {
        var method = getMethod.apply(rawHttp.stream());
        var path = getPath.apply(rawHttp.stream());
        var httpMethod = method.flatMap(HttpMethod::fromString);

        if (!httpMethod.hasValue() || !path.hasValue()) {
			// http protocol invalid
            return Try.success(HttpResponse.of(HttpResponseCode.BAD_REQUEST));
        }

        var headers = getHeaders.apply(rawHttp.stream());
        var body = getBody.apply(rawHttp.stream());

        var actualUri = path.getValue();
		var actualDestination = Destination.of(httpMethod.getValue(), actualUri);

		Predicate<Endpoint> formalPredicate = e -> Destination.isFormal(e.getDestination(), actualDestination);
        var endpoint = Maybe.from(endpoints.stream().filter(formalPredicate).findFirst());

        if (!endpoint.hasValue()) {
			// endpoint not found
            return Try.success(HttpResponse.of(HttpResponseCode.NOT_FOUND));
        }

        var unpackedEndpoint = endpoint.getValue();
		var formalUri = unpackedEndpoint.getDestination().getUri();
		var httpContext = HttpContext.of(httpMethod.getValue(), actualUri, formalUri,
				getPathParameters.apply(formalUri).apply(actualUri), getQueryParameters.apply(actualUri), headers, address, body);

		var reducedPreMiddleware = preMiddleware.stream().reduce(PreMiddleware::andThen);
		var reducedPostMiddleware = postMiddleware.stream().reduce(PostMiddleware::andThen);

		try {
			httpContext = Maybe.from(reducedPreMiddleware).orElse(PreMiddleware.none).apply(httpContext);
			var restFunctionResponse = unpackedEndpoint.getRestFunction().apply(httpContext);
			restFunctionResponse = Maybe.from(reducedPostMiddleware)
					.orElse(PostMiddleware.none)
					.apply(httpContext)
					.apply(restFunctionResponse);
			return Try.success(restFunctionResponse);
		} catch (Throwable t) {
			return Try.failure(t);
		}
	};
}

