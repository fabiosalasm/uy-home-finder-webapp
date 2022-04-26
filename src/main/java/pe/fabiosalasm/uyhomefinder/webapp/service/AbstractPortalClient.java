package pe.fabiosalasm.uyhomefinder.webapp.service;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public abstract class AbstractPortalClient implements PortalClient {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected abstract Set<String> getPageEntryUrls(Page webWindow, String cityPageUrl);

    protected abstract Set<String> getPropertyEntryUrls(Page webWindow, String pageUrl);

    @SuppressWarnings("Duplicates")
    protected final Set<String> getPageEntryUrls(BrowserContext browserContext, String cityPageUrl) {
        log.info("Navigating url '{}' to calculate number of pages to navigate afterwards", cityPageUrl);

        try (var webWindow = browserContext.newPage()) {
            var response = webWindow.navigate(cityPageUrl);
            if (!response.ok()) {
                log.warn("Cannot continue navigating url '{}': Returned status code: {}",
                        cityPageUrl, response.status());
                return Set.of();
            }

            return this.getPageEntryUrls(webWindow, cityPageUrl);

        } catch (Exception ex) {
            log.error("Cannot continue navigating url '{}': There was a communication problem while reaching it",
                    cityPageUrl, ex);
            return Set.of();
        }
    }

    @SuppressWarnings("Duplicates")
    protected final Set<String> getPropertyEntryUrls(BrowserContext browserContext, String pageUrl) {
        log.info("Navigating url '{}' to calculate number of properties to evaluate", pageUrl);

        try (var webWindow = browserContext.newPage()) {
            var response = webWindow.navigate(pageUrl);
            if (!response.ok()) {
                log.warn("Cannot continue navigating url '{}': Returned status code: {}", pageUrl, response.status());
                return Set.of();
            }

            return this.getPropertyEntryUrls(webWindow, pageUrl);

        } catch (Exception ex) {
            log.error("Cannot continue navigating url '{}': There was a communication problem while reaching it",
                    pageUrl, ex);
            return Set.of();
        }
    }
}