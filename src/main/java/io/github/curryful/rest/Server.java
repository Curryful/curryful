package io.github.curryful.rest;

import static io.github.curryful.commons.combinators.YCombinator.Y;
import static io.github.curryful.rest.Router.route;
import static io.github.curryful.rest.http.Http.serializeResponse;
import static io.github.curryful.rest.http.HttpResponseCode.INTERNAL_SERVER_ERROR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.ServerSocket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import io.github.curryful.commons.collections.ImmutableArrayList;
import io.github.curryful.commons.collections.MutableArrayList;
import io.github.curryful.commons.collections.MutableMaybeHashMap;
import io.github.curryful.commons.monads.Maybe;
import io.github.curryful.commons.monads.Try;
import io.github.curryful.rest.http.HttpContext;
import io.github.curryful.rest.http.HttpResponse;
import io.github.curryful.rest.middleware.PostMiddleware;
import io.github.curryful.rest.middleware.PreMiddleware;

/**
 * Class to hold functions for serving the {@link Endpoint}s etc.
 */
public final class Server {

	/**
	 * Get runtime of current application, i.e. how long it has been running.
	 */
	private static Supplier<Long> runtime = () -> {
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		return runtimeBean.getUptime();
	};

	/**
	 * Pre-middleware that logs the request.
	 */
	private static final Function<Consumer<String>, PreMiddleware> logRequest = logF -> context -> {
		var headers = MutableMaybeHashMap.of(context.getHeaders());
		headers.put("Curryful-Received-Request", Long.toString(Instant.now().toEpochMilli()));
		var userAgent = context.getHeaders().get("User-Agent").orElse("Unknown");

		var log = String.format("%s %s %s - %s %s", LocalDateTime.now(), context.getMethod().name(),
				context.getFormalUri(), context.getAddress(), userAgent);
		logF.accept(log);
		return HttpContext.of(context.getMethod(), context.getActualUri(), context.getFormalUri(),
				context.getPathParameters(), context.getQueryParameters(), headers, context.getAddress(),
				context.getBody());
	};

	/**
	 * Post-middleware that logs the response and the time it took to process the request.
	 */
	private static final Function<Consumer<String>, PostMiddleware> logResponse = logF -> context -> response -> {
		var timeDelta = Instant.now().toEpochMilli() - context.getHeaders().get("Curryful-Received-Request")
				.map(Long::parseLong).orElse(0L);
		var userAgent = context.getHeaders().get("User-Agent").orElse("Unknown");

		var log = String.format("%s %d %s %s (took %dms) - %s %s", LocalDateTime.now(),
				response.getCode().getCode(), response.getCode().getText(),
				context.getActualUri(), timeDelta, context.getAddress(), userAgent);
		logF.accept(log);
		return response;
	};

	/**
	 * Copies a list and adds a line to it.
	 */
	private static final Function<String, UnaryOperator<ImmutableArrayList<String>>> copyAndAdd = line -> lines -> {
		var newLines = MutableArrayList.of(lines);
		newLines.add(line);
		return newLines;
	};

	/**
	 * Reads HTTP from a buffer and applies a function to it.
	 */
	private static final Function<
		BufferedReader,
		Function<
			Function<ImmutableArrayList<String>, Try<ImmutableArrayList<String>>>,
			Function<ImmutableArrayList<String>, Try<ImmutableArrayList<String>>>
		>
	> readHttpFromBuffer = bufferedReader -> function -> http -> {
		try {
			var line = Maybe.ofNullable(bufferedReader.readLine());

			if (!line.hasValue()) {
				return Try.success(http);
			}

			var curriedCopyAndAdd = copyAndAdd.apply(line.getValue());
			Function<ImmutableArrayList<String>, Try<ImmutableArrayList<String>>> identityTry = list -> Try.success(list);
			return Try.success(http).map(curriedCopyAndAdd).flatMap(bufferedReader.ready() ? function : identityTry);
		} catch (IOException e) {
			return Try.failure(e);
		}
	};

	/**
	 * Listens for HTTP requests on the given port.
	 * Passes pre-middleware, endpoints and post-middleware to {@link Router#route}
	 * to register and then listens for requests.
	 * Will never finish unless an exception is thrown.
	 */
    public static final Function<
		Consumer<String>,
		Function<
			ImmutableArrayList<PreMiddleware>,
			Function<
				ImmutableArrayList<Endpoint>,
				Function<
					ImmutableArrayList<PostMiddleware>,
					Function<
						Integer,
						Try<?>
					>
				>
			>
		>
    > listen = logF -> preMiddleware -> endpoints -> postMiddleware -> port -> {
		var preMiddlewareWithLogging = MutableArrayList.of(preMiddleware);
		preMiddlewareWithLogging.addFirst(logRequest.apply(logF));

		var postMiddlewareWithLogging = MutableArrayList.of(postMiddleware);
		postMiddlewareWithLogging.add(logResponse.apply(logF));

        var registeredProcess = route
				.apply(preMiddlewareWithLogging)
				.apply(endpoints)
				.apply(postMiddlewareWithLogging);

        try (var server = new ServerSocket(port)) {
			logF.accept(String.format("%s Curryful server started in %dms", LocalDateTime.now(), runtime.get()));

            while (true) {
				try (var client = server.accept()) {
					var attachedProcess = registeredProcess.apply(client.getInetAddress());
					var readHttp = readHttpFromBuffer.apply(new BufferedReader(new InputStreamReader(client.getInputStream())));
					var httpTry = Y(readHttp).apply(ImmutableArrayList.empty());
					var httpResponseTry = httpTry.flatMap(attachedProcess);
					Supplier<HttpResponse> internalServerError = () -> {
						logF.accept(httpResponseTry.getError().getMessage());
						return HttpResponse.of(INTERNAL_SERVER_ERROR);
					};
					var httpResponse = httpResponseTry.orElseGet(internalServerError);
					var out = client.getOutputStream();
					out.write(serializeResponse.andThen(String::getBytes).apply(httpResponse));
					out.flush();
				} catch (IOException clientException) {
					logF.accept(clientException.getMessage());
				}
            }
        } catch (IOException serverException) {
            return Try.failure(serverException);
        }
    };
}

