package pe.fabiosalasm.uyhomefinder.webapp.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScrappingConfig {

    @Bean
    public Playwright playwright() {
        return Playwright.create();
    }

    // Creating a browser as a bean implies it can be used in concurrent web requests (bean's default scope)
    // however, neither Playwright instance or Browser are thread-safe, so it's better
    //  1. http request that invoke playwright functionality should be synchronous and sequential
    @Bean
    public Browser webKitBrowser(Playwright playwright) {
        return playwright.webkit().launch();
    }
}