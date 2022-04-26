package pe.fabiosalasm.uyhomefinder.webapp.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import pe.fabiosalasm.uyhomefinder.webapp.dto.PortalFilterOptionsDTO;
import pe.fabiosalasm.uyhomefinder.webapp.dto.SyncResultDTO;
import pe.fabiosalasm.uyhomefinder.webapp.util.UrlUtils;

import javax.money.Monetary;
import javax.money.UnknownCurrencyException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Component
public class GallitoUyClient extends AbstractPortalClient {
    private final Browser webKitBrowser;
    private static final String BASE_URL = "https://www.gallito.com.uy/"; //TODO: Make it environment property
    private static final String URL_PATH_TEMPLATE = "/inmuebles/{propertyType}/{propertyOperation}/{city}/{priceRange}/{squareMeters}/ord_rec!cant=80";
    private static final String TOTAL_PROPERTIES_CSS_LOCATOR = "#resultados > strong";
    private static final String PROPERTY_DESCRIPTION_CSS_LOCATOR = ".contactar > .contenedor-info > div.mas-info > a";

    @Override
    public String getName() {
        return "gallito-uy";
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
                                filterOptions.getPropertyType().getName(),
                                filterOptions.getPropertyOperation().getName(),
                                cityName,
                                this.mapPriceRangeToSubpath(filterOptions.getCurrency(), filterOptions.getMinPrice(), filterOptions.getMaxPrice()),
                                "sup-50-500-metros" //TODO: Use parameter (filter attribute) instead of hardcoded value
                        )
                        .toString())
                .map(url -> this.getPageEntryUrls(browserContext, url))
                .flatMap(Collection::stream)
                .limit(1)
                .map(url -> this.getPropertyEntryUrls(browserContext, url))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        var webWindow = browserContext.newPage();
        var evaluator = new HashSet<String>(); //TODO: Remove???

        urls.forEach(v -> this.undefined(webWindow, filterOptions, v, evaluator));

        log.info("Caracteristicas encontradas: {}", evaluator.stream().map(v -> "\n" + v).collect(Collectors.joining()));

        webWindow.close();
        browserContext.close();

        return new SyncResultDTO(urls.size(), 0, 0);
    }

    @Override
    public Set<String> getPageEntryUrls(Page webWindow, String cityPageUrl) {
        var totalProperties = webWindow.locator(TOTAL_PROPERTIES_CSS_LOCATOR).first().textContent();

        try {
            var totalPropertiesNumber = Float.parseFloat(totalProperties.replace("de ", ""));
            var pages = Math.round(totalPropertiesNumber / 80);

            return IntStream.rangeClosed(1, pages)
                    .mapToObj(v -> (v == 1) ? cityPageUrl : (cityPageUrl + "?pag=" + v))
                    .map(urlString -> {
                        try {
                            //We need the cleanse the url by verifying it's correct and removing any fragment present
                            return UrlUtils.removeFragment(urlString);

                        } catch (IllegalArgumentException | URISyntaxException iae) {
                            log.warn("Discarding entry url '{}': bad url format. ", urlString);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

        } catch (NumberFormatException nfex) {
            log.error("Cannot continue evaluating url '{}': Cannot parse value found in css query selector {}",
                    cityPageUrl, TOTAL_PROPERTIES_CSS_LOCATOR, nfex);
            return Set.of();
        }
    }

    @Override
    public Set<String> getPropertyEntryUrls(Page webWindow, String pageUrl) {
        var houseEntryElements = webWindow.locator(PROPERTY_DESCRIPTION_CSS_LOCATOR);
        if (houseEntryElements.count() == 0) {
            return Set.of(pageUrl);
        } else {
            return IntStream.range(0, houseEntryElements.count())
                    .mapToObj(i -> houseEntryElements.nth(i).getAttribute("href"))
                    .map(urlString -> {
                        try {
                            // just for standarization, we need the cleanse the url by verifying it's correct and removing any fragment present
                            return UrlUtils.removeFragment(urlString);

                        } catch (IllegalArgumentException | URISyntaxException iae) {
                            log.warn("Discarding entry url '{}': bad url format. ", urlString);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
    }

    private Map<String, String> undefined(Page webWindow, PortalFilterOptionsDTO filterOptions, String pageUrl,
                                          Set<String> evaluator) {
        webWindow.navigate(pageUrl);

        var result = new HashMap<String, String>();
        result.put("portal", this.getName());
        result.put("type", filterOptions.getPropertyType().value());
        result.put("operation", filterOptions.getPropertyOperation().value());

        var houseArticleIdElement = webWindow
                .locator("head#Head1 > meta[name='cXenseParse:recs:articleid']")
                .first();

        if (houseArticleIdElement.count() > 0) {
            Optional.ofNullable(houseArticleIdElement.getAttribute("content"))
                    .ifPresent(v -> result.put("id", v));
        }

        var houseTitleElement = webWindow.locator("head#Head1 > title").first();
        if (houseTitleElement.count() > 0) {
            Optional.ofNullable(houseTitleElement.textContent())
                    .map(v -> v.replace("\n", " ").trim())
                    .ifPresent(v -> result.put("title", v));
        }

        var priorityFlag = webWindow.locator("div.superdestaque").count() > 0 ? "1" : "2";
        result.put("priority", priorityFlag);

        var priceAmountElement = webWindow.locator("span.precio").first();
        if (priceAmountElement.count() > 0) {
            Optional.of(priceAmountElement.textContent())
                    .map(v -> v.replace("\n", " ").trim())
                    .ifPresent(v -> result.put("price", v));
        }

        var neighbourhoodElement = webWindow.locator(".wrapperDatos:has(.fas.fa-map-marked) p").first();
        if (neighbourhoodElement.count() > 0) {
            Optional.of(neighbourhoodElement.textContent()).ifPresent(v -> result.put("neighbourhood", v));
        }

        var bedroomsElement = webWindow.locator(".wrapperDatos:has(.fas.fa-bed) p").first();
        if (bedroomsElement.count() > 0) {
            Optional.of(bedroomsElement.textContent())
                    .map(v -> v.replace("\n", "").trim())
                    .map(v -> v.replaceFirst("(m|M?)ás de ", "+"))
                    .map(v -> v.replaceFirst(" (d|D?)ormitorio(s?)", ""))
                    .ifPresent(v -> result.put("number_bedrooms", v));
        }

        var bathroomsElement = webWindow.locator(".wrapperDatos:has(.fas.fa-bath) p").first();
        if (bathroomsElement.count() > 0) {
            Optional.of(bathroomsElement.textContent())
                    .map(v -> v.replace("\n", "").trim())
                    .map(v -> v.replaceFirst("(m|M?)ás de ", "+"))
                    .map(v -> v.replaceFirst(" (b|B?)año(s?)", ""))
                    .ifPresent(v -> result.put("number_bathrooms", v));
        }

        var squareMetersElement = webWindow.locator(".wrapperDatos:has(.far.fa-square) p").first();
        if (squareMetersElement.count() > 0) {
            Optional.of(squareMetersElement.textContent())
                    .map(v -> v.replaceFirst(" (m|M?)t(s?)", "").trim())
                    .ifPresent(v -> result.put("square_meters", v));
        }

        var featureElements = webWindow.locator("section#caracteristicas .list-group-item");
        IntStream.range(0, featureElements.count())
                .mapToObj(i -> featureElements.nth(i).textContent())
                .filter(Objects::nonNull)
                .forEach(t -> {
                    if (t.matches("Cantidad de plantas: \\w+")) {
                        // number_floors -> value

                    }
                    if (t.contains(":")) {
                        evaluator.add(t.split(":")[0] + "****");
                    } else {
                        evaluator.add(t);
                    }
                });

        return result;
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

            return "pre-" + minPrice + "-" + maxPrice + "-" +
                    (priceCurrency.getCurrencyCode().equals("USD") ? "dolares" : "pesos");

        } catch (UnknownCurrencyException uce) {
            throw new IllegalArgumentException("Cannot determine absolute path required for syncing houses in portal '" +
                    this.getName() + "': invalid currency", uce);
        }
    }
}
