package pe.fabiosalasm.uyhomefinder.webapp.service;

import org.springframework.stereotype.Component;
import pe.fabiosalasm.uyhomefinder.webapp.dto.SyncResultDTO;

@Component
public class MercadoLibreClient implements PortalClient {
    @Override
    public String getName() {
        return "mercadolibre";
    }

    @Override
    public SyncResultDTO fetchHouses() {
        return new SyncResultDTO(0, 0, 0);
    }
}
