package pe.fabiosalasm.uyhomefinder.webapp.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;

@UtilityClass
public class UrlUtils {
    public static String removeFragment(String url) throws URISyntaxException {
        var originalUri = UriComponentsBuilder.fromHttpUrl(url).build().toUri();
        return new URI(originalUri.getScheme(), originalUri.getSchemeSpecificPart(), null).toString();
    }

    public static String appendPathSegment(String url, String subpath) {
        return UriComponentsBuilder.fromHttpUrl(url)
                .pathSegment(subpath)
                .build()
                .toString();
    }

    public static String getPathSegmentByIndex(String url, int index) {
        Assert.isTrue(index >= 0,
                "Cannot get path segment: Invalid index number: %s".formatted(index));

        var pathSegments = UriComponentsBuilder.fromHttpUrl(url)
                .build()
                .getPathSegments();

        if (pathSegments.size() <= index) {
            throw new IllegalStateException("""
                    Cannot get path segment by index %s: Index exceed number of path segments.
                    """.formatted(index)
            );
        }

        return pathSegments.get(index);
    }


    public static String getLastPathSegment(String url) {
        var pathSegments = UriComponentsBuilder.fromHttpUrl(url)
                .build()
                .getPathSegments();

        if (pathSegments.isEmpty()) {
            throw new IllegalStateException("""
                    Cannot get last path segment: There are no path segments in url: %s.
                    """.formatted(url)
            );
        }

        return pathSegments.get(pathSegments.size() -1);
    }

    public static String getPathSegmentByReverseIndex(String url, int reverseIndex) {
        Assert.isTrue(reverseIndex < 0,
                "Cannot get path segment: Invalid index number: %s".formatted(reverseIndex));

        var pathSegments = UriComponentsBuilder.fromHttpUrl(url)
                .build()
                .getPathSegments();

        if (pathSegments.size() < (reverseIndex * -1)) {
            throw new IllegalStateException("""
                    Cannot get path segment by reverse index %s: Index exceed number of path segments.
                    """.formatted(reverseIndex)
            );
        }

        return pathSegments.get(pathSegments.size() + reverseIndex);
    }
}