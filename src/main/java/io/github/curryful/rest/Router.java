package io.github.curryful.rest;

import static io.github.curryful.rest.Http.getBody;
import static io.github.curryful.rest.Http.getHeaders;
import static io.github.curryful.rest.Http.getMethod;
import static io.github.curryful.rest.Http.getPath;
import static io.github.curryful.rest.Uri.getPathParameters;
import static io.github.curryful.rest.Uri.getQueryParameters;

import java.net.InetAddress;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import io.github.curryful.commons.monads.Maybe;
import io.github.curryful.rest.middleware.PostMiddleware;
import io.github.curryful.rest.middleware.PreMiddleware;

/**
 * Class to hold functions for routing.
 */
public final class Router {

	/**
	 * {@link RestFunction} that returns a 404 response.
	 */
    private static final RestFunction notFound = _context -> HttpResponse.of(HttpResponseCode.NOT_FOUND);

	/**
	 * Processes an HTTP request.
	 * Creates an {@link HttpContext} from the raw HTTP, applies pre-middleware,
	 * finds the matching endpoint, applies the endpoint's rest function and applies post-middleware.
	 * Returns an {@link HttpResponse}.
	 */
	public static final Function<
		List<PreMiddleware>,
		Function<
			List<Endpoint>,
			Function<
				List<PostMiddleware>,
				Function<
					InetAddress,
					Function<
						List<String>,
						HttpResponse<?>
					>
				>
			>
		>	
    > process = preMiddleware -> endpoints -> postMiddleware -> address -> rawHttp -> {
        var method = getMethod.apply(rawHttp.stream());
        var path = getPath.apply(rawHttp.stream());
        var httpMethod = method.flatMap(HttpMethod::fromString);

        if (!httpMethod.hasValue() || !path.hasValue()) {
            return HttpResponse.of(HttpResponseCode.BAD_REQUEST);
        }

        var headers = getHeaders.apply(rawHttp.stream());
        var body = getBody.apply(rawHttp.stream());

        var actualUri = path.getValue();
		var actualDestination = Destination.of(httpMethod.getValue(), actualUri);

		Predicate<Endpoint> formalPredicate = e -> Destination.isFormal(e.getDestination(), actualDestination);
        var endpoint = Maybe.from(endpoints.stream().filter(formalPredicate).findFirst());

        if (!endpoint.hasValue()) {
            return notFound.apply(HttpContext.empty());
        }

        var unpackedEndpoint = endpoint.getValue();
		var formalUri = unpackedEndpoint.getDestination().getUri();
		var httpContext = HttpContext.of(httpMethod.getValue(), actualUri, formalUri,
				getPathParameters(formalUri, actualUri), getQueryParameters(actualUri), headers, address, body);

		var reducedPreMiddleware = preMiddleware.stream().reduce(PreMiddleware::andThen);
		httpContext = Maybe.from(reducedPreMiddleware).orElse(PreMiddleware.empty).apply(httpContext);
        
		var restFunctionResponse = unpackedEndpoint.getRestFunction().apply(httpContext);

		var reducedPostMiddleware = postMiddleware.stream().reduce(PostMiddleware::andThen);
		return Maybe.from(reducedPostMiddleware).orElse(PostMiddleware.empty).apply(httpContext, restFunctionResponse);
	};
}

