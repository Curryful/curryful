package io.github.curryful.rest;

import static io.github.curryful.rest.Http.getContent;
import static io.github.curryful.rest.Http.getHeaders;
import static io.github.curryful.rest.Http.getMethod;
import static io.github.curryful.rest.Http.getPath;
import static io.github.curryful.rest.Uri.getPathParameters;
import static io.github.curryful.rest.Uri.getQueryParameters;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import io.github.curryful.commons.Maybe;
import io.github.curryful.rest.middleware.PostMiddleware;
import io.github.curryful.rest.middleware.PreMiddleware;

public final class Router {

    private static final RestFunction notFound = _context ->
			new HttpResponse<>(HttpResponseCode.NOT_FOUND);

	public static final Function<
		List<PreMiddleware>,
		Function<
			List<Endpoint>,
			Function<
				List<PostMiddleware>,
				Function<
					List<String>,
					HttpResponse<?>
				>
			>
		>	
    > process = preMiddleware -> endpoints -> postMiddleware -> rawHttp -> {
        var method = getMethod.apply(rawHttp.stream());
        var path = getPath.apply(rawHttp.stream());
        var httpMethod = method.flatMap(HttpMethod::fromString);

        if (!httpMethod.hasValue() || !path.hasValue()) {
            return new HttpResponse<>(HttpResponseCode.BAD_REQUEST);
        }

        var headers = getHeaders.apply(rawHttp.stream());
        var content = getContent.apply(rawHttp.stream());

        var actualUri = path.getValue();
		var actualDestination = new Destination(httpMethod.getValue(), actualUri);

		Predicate<Endpoint> formalPredicate = e -> Destination.isFormal(e.getDestination(), actualDestination);
        var endpoint = Maybe.from(endpoints.stream().filter(formalPredicate).findFirst());

        if (!endpoint.hasValue()) {
            return notFound.apply(HttpContext.empty());
        }

        var unpackedEndpoint = endpoint.getValue();
		var formalUri = unpackedEndpoint.getDestination().getUri();
		var httpContext = HttpContext.of(httpMethod.getValue(), actualUri, formalUri,
				getPathParameters(formalUri, actualUri), getQueryParameters(actualUri), headers, content);

		var reducedPreMiddleware = preMiddleware.stream().reduce(PreMiddleware::andThen);
		httpContext = Maybe.from(reducedPreMiddleware).orElse(PreMiddleware.empty).apply(httpContext);
        
		var restFunctionResponse = unpackedEndpoint.getRestFunction().apply(httpContext);

		var reducedPostMiddleware = postMiddleware.stream().reduce(PostMiddleware::andThen);
		return Maybe.from(reducedPostMiddleware).orElse(PostMiddleware.empty).apply(httpContext, restFunctionResponse);
	};
}

