package io.github.curryful.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UriUtilsTest {

    @Test
    public void testReplaceFormalParametersWithRegexOne() {
        // Arrange
        var uri = "/hello/:name";

        // Act
        var result = UriUtils.replaceFormalParametersWithRegex(uri);

        // Assert
        assertEquals("/hello/(?<name>[^/?]+)", result);
    }

    @Test
    public void testReplaceFormalParametersWithRegexMultiple() {
        // Arrange
        var uri = "/hello/:name/:age/:height";

        // Act
        var result = UriUtils.replaceFormalParametersWithRegex(uri);

        // Assert
        assertEquals("/hello/(?<name>[^/?]+)/(?<age>[^/?]+)/(?<height>[^/?]+)", result);
    }

    @Test
    public void testReplaceFormalParametersWithRegexNone() {
        // Arrange
        var uri = "/hello";

        // Act
        var result = UriUtils.replaceFormalParametersWithRegex(uri);

        // Assert
        assertEquals("/hello", result);
    }
}
