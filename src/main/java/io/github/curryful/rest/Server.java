package io.github.curryful.rest;

import static io.github.curryful.commons.combinators.YCombinator.Y;
import static io.github.curryful.rest.Router.process;
import static io.github.curryful.rest.http.Http.serializeResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import io.github.curryful.commons.collections.ImmutableArrayList;
import io.github.curryful.commons.collections.MutableArrayList;
import io.github.curryful.commons.collections.MutableMaybeHashMap;
import io.github.curryful.commons.monads.Maybe;
import io.github.curryful.commons.monads.Try;
import io.github.curryful.rest.http.HttpContext;
import io.github.curryful.rest.middleware.PostMiddleware;
import io.github.curryful.rest.middleware.PreMiddleware;

/**
 * Class to hold functions for serving the {@link Endpoint}s etc.
 */
public final class Server {

	private static final Long APPLICATION_BEGIN_TIME = Instant.now().toEpochMilli();

	/**
	 * Pre-middleware that logs the request.
	 */
	private static final PreMiddleware logRequest = context -> {
		var headers = MutableMaybeHashMap.of(context.getHeaders());
		headers.put("Curryful-Received-Request", Long.toString(Instant.now().toEpochMilli()));
		var userAgent = context.getHeaders().get("User-Agent").orElse("Unknown");

		String log = String.format("%s %s %s - %s %s", LocalDateTime.now(), context.getMethod().name(),
				context.getFormalUri(), context.getAddress(), userAgent);
		System.out.println(log);
		return HttpContext.of(context.getMethod(), context.getActualUri(), context.getFormalUri(),
				context.getPathParameters(), context.getQueryParameters(), headers, context.getAddress(),
				context.getBody());
	};

	/**
	 * Post-middleware that logs the response and the time it took to process the request.
	 */
	private static final PostMiddleware logResponse = context -> response -> {
		var timeDelta = Instant.now().toEpochMilli() - context.getHeaders().get("Curryful-Received-Request")
				.map(Long::parseLong).orElse(0L);
		var userAgent = context.getHeaders().get("User-Agent").orElse("Unknown");

		String log = String.format("%s %d %s %s (took %dms) - %s %s", LocalDateTime.now(),
				response.getCode().getCode(), response.getCode().getText(),
				context.getActualUri(), timeDelta, context.getAddress(), userAgent);
		System.out.println(log);
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
	 * Passes pre-middleware, endpoints and post-middleware to {@link Router#process}
	 * to register and then listens for requests.
	 * Will never finish unless an exception is thrown.
	 */
    public static final Function<
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
    > listen = preMiddleware -> endpoints -> postMiddleware -> port -> {
		var preMiddlewareWithLogging = MutableArrayList.of(preMiddleware);
		preMiddlewareWithLogging.addFirst(logRequest);

		var postMiddlewareWithLogging = MutableArrayList.of(postMiddleware);
		postMiddlewareWithLogging.add(logResponse);

        var registeredProcess = process.apply(preMiddlewareWithLogging).apply(endpoints).apply(postMiddlewareWithLogging);

        try (ServerSocket server = new ServerSocket(port)) {
			var startupLog = String.format("%s Curryful server started in &dms",
					LocalDateTime.now(), Instant.now().toEpochMilli() - APPLICATION_BEGIN_TIME);
			System.out.println(startupLog);

            while (true) {
                var socket = server.accept();
				var attachedProcess = registeredProcess.apply(socket.getInetAddress());
				var readHttp = readHttpFromBuffer.apply(new BufferedReader(new InputStreamReader(socket.getInputStream())));
				var httpTry = Y(readHttp).apply(ImmutableArrayList.empty());
				var responseTry = httpTry.map(attachedProcess).map(serializeResponse);

				if (responseTry.isFailure()) {
					return responseTry;
				}

                var out = socket.getOutputStream();
                out.write(responseTry.getValue().getBytes());
                out.flush();
                socket.close();
            }
        } catch (IOException e) {
            return Try.failure(e);
        }
    };
}

