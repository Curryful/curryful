package io.github.curryful.rest;

import static io.github.curryful.rest.Http.buildResponse;
import static io.github.curryful.rest.Http.getContent;
import static io.github.curryful.rest.Http.getHeaders;
import static io.github.curryful.rest.Http.getMethod;
import static io.github.curryful.rest.Http.getPath;
import static io.github.curryful.rest.Uri.getPathParameters;
import static io.github.curryful.rest.Uri.getQueryParameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.curryful.commons.Maybe;
import io.github.curryful.commons.Try;

public final class Router {

    private static final RestFunction notFound = _context -> new HttpResponse<>(HttpResponseCode.NOT_FOUND);

    private static final Function<
        List<Endpoint>,
        Function<
            List<String>,
            HttpResponse<?>
        >
    > process = endpoints -> rawHttp -> {
        var method = getMethod.apply(rawHttp.stream());
        var path = getPath.apply(rawHttp.stream());
        var headers = getHeaders.apply(rawHttp.stream());
        var content = getContent.apply(rawHttp.stream());

        var httpMethod = method.flatMap(HttpMethod::fromString);

        if (!httpMethod.hasValue() || !path.hasValue()) {
            return new HttpResponse<>(HttpResponseCode.BAD_REQUEST);
        }

        var actualUri = path.getValue();
		var actualDestination = new Destination(httpMethod.getValue(), actualUri);

        var endpoint = Maybe.from(endpoints
				.stream()
				.filter(e -> Destination.isFormal(e.getDestination(), actualDestination))
				.findFirst());

        if (!endpoint.hasValue()) {
            return notFound.apply(HttpContext.empty());
        }

        var unpackedEndpoint = endpoint.getValue();
        var pathParameters = getPathParameters(unpackedEndpoint.getDestination().getUri(), actualUri);
        var httpContext = new HttpContext(pathParameters, getQueryParameters(actualUri), headers, content);
        return unpackedEndpoint.getRestFunction().apply(httpContext);
    };

    public static final Function<
        List<Endpoint>,
        Function<
            Integer,
            Try<Void>
        >
    > listen = endpoints -> port -> {
        var registeredProcess = process.apply(endpoints);
        System.out.println("Registered routes");

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println(String.format("Listening on port %d...", port));

            while (true) {
                var socket = server.accept();

                System.out.println("Accepted connection");

                // Would be a lot nicer but this line does not finish
                // var rawHttp = new BufferedReader(new InputStreamReader(socket.getInputStream())).lines().toList();
                var rawHttp = new ArrayList<String>();
                var bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while (!(line = bufferedReader.readLine()).isBlank()) {
                    rawHttp.add(line);
                }

                var response = registeredProcess.andThen(buildResponse).apply(rawHttp);

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
