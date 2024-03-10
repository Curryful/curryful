package io.github.curryful.rest;

import static io.github.curryful.rest.Http.buildResponse;
import static io.github.curryful.rest.Router.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.curryful.commons.Try;
import io.github.curryful.rest.middleware.PostMiddleware;
import io.github.curryful.rest.middleware.PreMiddleware;

public class Server {

	private static final PreMiddleware logRequest = context -> {
		context.getHeaders().put("Curryful-Received-Request", Long.toString(Instant.now().toEpochMilli()));
		var userAgent = context.getHeaders().get("User-Agent").orElse("Unknown");

		String log = String.format("%s %s %s - %s %s", LocalDateTime.now(), context.getMethod().name(),
				context.getFormalUri(), context.getAddress(), userAgent);
		System.out.println(log);
		return context;
	};

	private static final PostMiddleware logResponse = (context, response) -> {
		var timeDelta = Instant.now().toEpochMilli() - context.getHeaders().get("Curryful-Received-Request")
				.map(Long::parseLong).orElse(0L);
		var userAgent = context.getHeaders().get("User-Agent").orElse("Unknown");

		String log = String.format("%s %d %s %s (took %dms) - %s %s", LocalDateTime.now(),
				response.getCode().getCode(), response.getCode().getText(),
				context.getActualUri(), timeDelta, context.getAddress(), userAgent);
		System.out.println(log);
		return response;
	};

    public static final Function<
		List<PreMiddleware>,
		Function<
			List<Endpoint>,
			Function<
				List<PostMiddleware>,
				Function<
					Integer,
					Try<Void>
				>
			>
		>
    > listen = preMiddleware -> endpoints -> postMiddleware -> port -> {
		var preMiddlewareWithLogging = new ArrayList<PreMiddleware>(preMiddleware);

		preMiddlewareWithLogging.addFirst(logRequest);

		var postMiddlewareWithLogging = new ArrayList<PostMiddleware>(postMiddleware);
		postMiddlewareWithLogging.addLast(logResponse);

        var registeredProcess = process.apply(preMiddlewareWithLogging).apply(endpoints).apply(postMiddlewareWithLogging);

        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                var socket = server.accept();
				var attachedProcess = registeredProcess.apply(socket.getInetAddress());

                // Would be a lot nicer but this line does not finish
                // var rawHttp = new BufferedReader(new InputStreamReader(socket.getInputStream())).lines().toList();
                var rawHttp = new ArrayList<String>();
                var bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
                    rawHttp.add(line);
                }

                var response = attachedProcess.andThen(buildResponse).apply(rawHttp);
                var out = socket.getOutputStream();
                out.write(response.getBytes());
                out.flush();
                socket.close();
            }
        } catch (IOException e) {
            return Try.failure(e);
        }
    };
}

