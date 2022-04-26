package pe.fabiosalasm.uyhomefinder.webapp.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UrlUtilsTest {

    @Test
    @DisplayName("Return same URL if it doesnt have fragments")
    public void testUrlWithoutFragmentUnmodified() throws URISyntaxException {
        var actualUrl = UrlUtils.removeFragment("https://www.example.com/foo.html?param1=123");

        var expectedUrl = "https://www.example.com/foo.html?param1=123";

        assertThat(actualUrl).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("Return URL without fragment")
    public void testUrlWithFragment() throws URISyntaxException {
        var actualUrl = UrlUtils.removeFragment("https://www.example.com/foo.html?param1=123#bar");

        var expectedUrl = "https://www.example.com/foo.html?param1=123";

        assertThat(actualUrl).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("Append new path segment to actual URL")
    public void testUrlAppendPathSegment() {
       var actualUrl = UrlUtils.appendPathSegment("https://www.example.com/foo.html?param1=123", "page1");

       var expectedUrl = "https://www.example.com/foo.html/page1?param1=123";

       assertThat(actualUrl).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("URL not modified if path segment to add is empty")
    public void testUrlUnmodifiedWhenPathSegmentEmpty() {
        var actualUrl = UrlUtils.appendPathSegment("https://www.example.com/foo.html?param1=123", "");

        var expectedUrl = "https://www.example.com/foo.html?param1=123";

        assertThat(actualUrl).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("URL not modified if path segment to add is null")
    public void testUrlUnmodifiedWhenPathSegmentNull() {
        var actualUrl = UrlUtils.appendPathSegment("https://www.example.com/foo.html?param1=123", null);

        var expectedUrl = "https://www.example.com/foo.html?param1=123";

        assertThat(actualUrl).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("Get path segment using a valid index")
    public void testGetPathSegment() {
        var url = "https://www.example.com/joy/boy/test123/test/123";
        var actualPathSegment = UrlUtils.getPathSegmentByIndex(url, 2);

        var expectedPathSegment = "test123";

        assertThat(actualPathSegment).isEqualTo(expectedPathSegment);
    }

    @Test
    @DisplayName("Throw exception if it's an invalid index")
    public void testThrowExceptionIfIndexOutOfBounds() {
        var url = "https://www.example.com/joy/boy/test123/test/123";

        assertThrows(IllegalStateException.class, () -> UrlUtils.getPathSegmentByIndex(url, 7),
                "Cannot get path segment by index 7: Index exceed number of path segments.");
    }

    @Test
    @DisplayName("Throw exception if any index but there is no path segment")
    public void testThrowExceptionIfNoPathSegments() {
        var url = "https://www.example.com";

        assertThrows(IllegalStateException.class, () -> UrlUtils.getPathSegmentByIndex(url, 2),
                "Cannot get path segment by index 2: Index exceed number of path segments.");
    }

    @Test
    @DisplayName("Get path segment using a valid reverse index")
    public void testGetPathSegmentByValidReverseIndex() {
        var url = "https://www.example.com/joy/boy/test123/test/123";
        var actualPathSegment = UrlUtils.getPathSegmentByReverseIndex(url, -2);

        var expectedPathSegment = "test";

        assertThat(actualPathSegment).isEqualTo(expectedPathSegment);
    }

    @Test
    @DisplayName("Throw exception if it's an invalid index")
    public void testThrowExceptionIfReverseIndexOutOfBounds() {
        var url = "https://www.example.com/joy/boy/test123/test/123";

        assertThrows(IllegalStateException.class, () -> UrlUtils.getPathSegmentByReverseIndex(url, -6),
                "Cannot get path segment by reverse index -6: Reverse index exceed number of path segments");
    }

    //TODO: Remove
    @Test
    @DisplayName("Get path segment using a valid reverse index 2")
    public void testGetPathSegmentByValidReverseIndex2() {
        var url = "https://www.infocasas.com.uy/edificio-oficinas-estudio-corporativo/188180779";
        var actualPathSegment = UrlUtils.getPathSegmentByReverseIndex(url, -2);

        var expectedPathSegment = "edificio-oficinas-estudio-corporativo";

        assertThat(actualPathSegment).isEqualTo(expectedPathSegment);
    }
}