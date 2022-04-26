package pe.fabiosalasm.uyhomefinder.webapp.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import pe.fabiosalasm.uyhomefinder.webapp.dto.PortalFilterOptionsDTO;
import pe.fabiosalasm.uyhomefinder.webapp.dto.PropertyOperation;
import pe.fabiosalasm.uyhomefinder.webapp.dto.SyncResultDTO;
import pe.fabiosalasm.uyhomefinder.webapp.util.UrlUtils;

import javax.money.Monetary;
import javax.money.UnknownCurrencyException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Component
@Slf4j
public class InfoCasasClient extends AbstractPortalClient {
    private static final String BASE_URL = "https://www.infocasas.com.uy/"; //TODO: Make it environment property
    private static final String URL_PATH_TEMPLATE = "{propertyOperation}/{propertyType}/{city}/{priceRange}/{squareMeters}?&ordenListado=3";
    private static final String TOTAL_PAGES_CSS_LOCATOR = "li.ant-pagination-item >> nth=-1";
    private final Browser webKitBrowser;

    @Override
    public String getName() {
        return "info-casas";
    }

    @SuppressWarnings("Duplicates")
    @Override
    public SyncResultDTO fetchProperties(PortalFilterOptionsDTO filterOptions) {
        //https://playwright.dev/java/docs/browser-contexts
        var browserContext = webKitBrowser.newContext();

        var urls = filterOptions.getCities().stream()
                .map(cityName -> UriComponentsBuilder.fromHttpUrl(BASE_URL)
                        .path(URL_PATH_TEMPLATE)
                        .buildAndExpand(
                                this.mapPropertyOperation(filterOptions.getPropertyOperation()),
                                filterOptions.getPropertyType().value(),
                                cityName,
                                this.mapPriceRangeToSubpath(filterOptions.getCurrency(), filterOptions.getMinPrice(), filterOptions.getMaxPrice()),
                                "m2-desde-100/m2-hasta-1000/edificados"  //TODO: Use parameter (filter attribute) instead of hardcoded value
                        )
                        .toString())
                .map(url -> this.getPageEntryUrls(browserContext, url))
                .flatMap(Collection::stream)
                .map(url -> this.getPropertyEntryUrls(browserContext, url))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        var webWindow = browserContext.newPage();
        Set<String> featuresToConsider = new HashSet<>();

        urls.forEach(v -> {
            log.info("url: {}", v);
            var result = this.undefined(featuresToConsider, webWindow, filterOptions, v);
            log.info("resultados: {}", result.keySet().stream()
                    .map(key -> key + "=" + result.get(key))
                    .collect(Collectors.joining(", ", "{", "}")));
        });

        featuresToConsider.forEach(v -> log.info("feature to consider found: {}", v));

        browserContext.close();

        return new SyncResultDTO(urls.size(), 0, 0);
    }

    @Override
    public Set<String> getPageEntryUrls(Page webWindow, String cityPageUrl) {
        var pagesElement = webWindow.locator(TOTAL_PAGES_CSS_LOCATOR);
        if (pagesElement.count() == 0) {
            return Set.of(cityPageUrl);

        } else {
            var pagesText = pagesElement.textContent();

            try {
                var pages = Integer.parseInt(pagesText);

                return IntStream.rangeClosed(1, pages)
                        .mapToObj(v -> (v == 1) ? cityPageUrl : UrlUtils.appendPathSegment(cityPageUrl, "pagina" + v))
                        .collect(Collectors.toSet());
            } catch (NumberFormatException nfex) {
                log.error("Cannot continue evaluating url '{}': Cannot parse value found in css query selector {}",
                        cityPageUrl, TOTAL_PAGES_CSS_LOCATOR, nfex);
                return Set.of();

            }
        }
    }

    @Override
    public Set<String> getPropertyEntryUrls(Page webWindow, String pageUrl) {
        var propertiesEntriesLinks = webWindow.locator("div.ant-row.ant-row-top >> a.containerLink");

        return IntStream.range(0, propertiesEntriesLinks.count())
                .mapToObj(i -> propertiesEntriesLinks.nth(i).getAttribute("href"))
                .filter(Objects::nonNull)
                .map(v -> UriComponentsBuilder.fromHttpUrl(BASE_URL).path(v).build().toString())
                .collect(Collectors.toSet());
    }

    private Map<String, String> undefined(Set<String> featuresToConsider,
                                          Page webWindow, PortalFilterOptionsDTO filterOptions, String pageUrl) {
        webWindow.navigate(pageUrl, new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.NETWORKIDLE));

        var result = new HashMap<String, String>();
        result.put("portal", this.getName());
        result.put("type", filterOptions.getPropertyType().value());
        result.put("operation", filterOptions.getPropertyOperation().value());
        result.put("id", UrlUtils.getLastPathSegment(pageUrl));
        result.put("title", UrlUtils.getPathSegmentByReverseIndex(pageUrl, -2));
        result.put("priority", "1");

        var priceAmountElement = webWindow.locator("span.ant-typography.price > strong").first();

        if (priceAmountElement.count() > 0) {
            Optional.ofNullable(priceAmountElement.textContent())
                    .ifPresent(v -> result.put("price", v));
        }

        var neighbourhoodElement = webWindow.locator("""
                span.ant-page-header-heading-sub-title >> div.ant-breadcrumb.custom-page-breadcrumb >> span.ant-breadcrumb-link >> nth=4
                """).first();

        if (neighbourhoodElement.count() > 0) {
            Optional.ofNullable(neighbourhoodElement.textContent())
                    .ifPresent(v -> result.put("neighbourhood", v));
        }

        var bedroomsElement = webWindow.locator("""
                span.ant-typography.ant-typography-ellipsis.ant-typography-ellipsis-single-line >> nth=0
                """).first();

        if (bedroomsElement.count() > 0) {
            Optional.ofNullable(bedroomsElement.textContent())
                    .map(v -> v.replace(" Dorm.", "").trim())
                    .ifPresent(v -> result.put("number_bedrooms", v));
        }

        var bathroomsElement = webWindow.locator("""
                span.ant-typography.ant-typography-ellipsis.ant-typography-ellipsis-single-line >> nth=1
                """).first();

        if (bathroomsElement.count() > 0) {
            Optional.ofNullable(bathroomsElement.textContent())
                    .map(v -> v.replace(" Baños", "").trim())
                    .ifPresent(v -> result.put("number_bathrooms", v));
        }

        var squareMetersElement = webWindow.locator("""
                span.ant-typography.ant-typography-ellipsis.ant-typography-ellipsis-single-line >> nth=2
                """).first();

        if (squareMetersElement.count() > 0) {
            Optional.ofNullable(squareMetersElement.textContent())
                    .map(v -> v.replace(" m²", "").trim())
                    .ifPresent(v -> result.put("square_meters", v));
        }

        var featureElements = webWindow.locator("""
                div.technical-sheet >> div.ant-row
                """);
        IntStream.range(0, featureElements.count())
                .mapToObj(featureElements::nth)
                .filter(Objects::nonNull)
                .forEach(ele -> {
                    var descElement = ele.locator("span.ant-typography").last();
                    var valueElement = ele.locator("div.ant-typography");

                    // verify both conditions because in infocasas there are descElements that dont have a respective
                    // valueElement (instead, have links to ask that feature to the owner)
                    if (descElement.count() > 0 && valueElement.count() > 0) {
                        Optional.ofNullable(descElement.textContent())
                                .ifPresent(featuresToConsider::add);
                    }
                });
        // feature elements contains title and description; both should be captured

        return result;
    }

    private String mapPropertyOperation(PropertyOperation propertyOperation) {
        return PropertyOperation.BUY.equals(propertyOperation) ? "venta" : propertyOperation.value();
    }

    private String mapPriceRangeToSubpath(String priceCurrencyAsString, Integer minPrice, Integer maxPrice) {
        try {
            var priceCurrency = Monetary.getCurrency(priceCurrencyAsString);

            if (!PortalClient.availableCurrencies().contains(priceCurrency)) {
                throw new IllegalArgumentException("Cannot determine absolute path required for syncing houses in portal '" +
                        this.getName() + "': the currency sent is not supported");
            }

            if (minPrice >= maxPrice) {
                throw new IllegalArgumentException("Cannot determine absolute path required for syncing houses in portal '" +
                        this.getName() + "': the minimum price range should be less than the maximum");
            }

            return "desde-" + minPrice + "/hasta-" + maxPrice +
                    (priceCurrency.getCurrencyCode().equals("USD") ? "/dolares" : "/pesos");

        } catch (UnknownCurrencyException uce) {
            throw new IllegalArgumentException("Cannot determine absolute path required for syncing houses in portal '" +
                    this.getName() + "': invalid currency", uce);
        }
    }
}
