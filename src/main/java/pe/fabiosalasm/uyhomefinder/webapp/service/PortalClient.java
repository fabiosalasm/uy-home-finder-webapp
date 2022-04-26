package pe.fabiosalasm.uyhomefinder.webapp.service;

import org.springframework.plugin.core.Plugin;
import pe.fabiosalasm.uyhomefinder.webapp.dto.PortalFilterOptionsDTO;
import pe.fabiosalasm.uyhomefinder.webapp.dto.SyncResultDTO;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.util.Set;

public interface PortalClient extends Plugin<String> {

    static Set<CurrencyUnit> availableCurrencies() {
        return Set.of(
                Monetary.getCurrency("UYU"),
                Monetary.getCurrency("USD"));
    }

    String getName();

    SyncResultDTO fetchProperties(PortalFilterOptionsDTO filterOptions);

    @Override
    default boolean supports(String delimiter) {
        return delimiter.equals(getName());
    }
}
