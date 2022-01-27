package pe.fabiosalasm.uyhomefinder.webapp.service;

import org.springframework.plugin.core.Plugin;
import pe.fabiosalasm.uyhomefinder.webapp.dto.SyncResultDTO;

public interface PortalClient extends Plugin<String> {
    String getName();

    SyncResultDTO fetchHouses();

    @Override
    default boolean supports(String delimiter) {
        return delimiter.equals(getName());
    }
}
