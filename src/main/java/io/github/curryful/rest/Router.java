package io.github.curryful.rest;

import static io.github.curryful.rest.Http.buildResponse;
import static io.github.curryful.rest.Http.getContent;
import static io.github.curryful.rest.Http.getHeaders;
import static io.github.curryful.rest.Http.getMethod;
import static io.github.curryful.rest.Http.getPath;
import static java.util.regex.Pattern.compile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    > process = routes -> rawHttp -> {
        var method = getMethod.apply(rawHttp.stream());
        var path = getPath.apply(rawHttp.stream());
        var headers = getHeaders.apply(rawHttp.stream());
        var content = getContent.apply(rawHttp.stream());

        var httpMethod = method.flatMap(HttpMethod::fromString);

        if (!httpMethod.hasValue() || !path.hasValue()) {
            return new HttpResponse<>(HttpResponseCode.BAD_REQUEST);
        }

		var destination = new Destination(httpMethod.getValue(), path.getValue());
        var route = Maybe.from(routes
				.stream()
				.filter(pair -> pair.getDestination().matches(destination))
				.findFirst());

        if (!route.hasValue()) {
            return notFound.apply(HttpContext.empty());
        }

        var unpackedRoute = route.getValue();

        var regex = UriUtils.replaceFormalParametersWithRegex(unpackedRoute.getDestination().getUri());
        var matcher = compile(regex).matcher(destination.getUri());

		var pathParameters = new HashMap<String, String>();

		if (matcher.find()) {
			var namedGroups = matcher.namedGroups();
			pathParameters = namedGroups.keySet()
                    .stream()
                    .map(key -> Pair.of(key, matcher.group(key)))
                    .collect(HashMap::new, (map, pair) -> map.put(pair.getFirst(), pair.getSecond()), Map::putAll);
		}

        return unpackedRoute.getRestFunction().apply(new HttpContext(pathParameters, Map.of(), headers, content));
    };

    public static final Function<
        List<Endpoint>,
        Function<
            Integer,
            Try<Void>
        >
    > listen = routes -> port -> {
        var registeredProcess = process.apply(routes);
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
