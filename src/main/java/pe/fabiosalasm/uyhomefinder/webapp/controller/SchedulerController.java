package pe.fabiosalasm.uyhomefinder.webapp.controller;

import lombok.AllArgsConstructor;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.fabiosalasm.uyhomefinder.webapp.dto.PortalFilterOptionsDTO;
import pe.fabiosalasm.uyhomefinder.webapp.dto.PropertyOperation;
import pe.fabiosalasm.uyhomefinder.webapp.dto.PropertyType;
import pe.fabiosalasm.uyhomefinder.webapp.dto.SyncResultDTO;
import pe.fabiosalasm.uyhomefinder.webapp.service.PortalClient;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/scheduler")
public class SchedulerController {
    private final PluginRegistry<PortalClient, String> portalClients;

    // Sync operations consists of fetching home info from portals (based on filters at app level or passed
    // as parameters during the invocation) and same them into the database

    // During fetch, right now the algorithm doesn't know how to distinguish between portals added before
    // and new ones, so a sync clear old registries and adds new ones as part of the operation

    @PostMapping("sync")
    public SyncResultDTO syncByPortal(@RequestParam(name = "portal", required = true) String portal,
                                      @Validated @RequestBody PortalFilterOptionsDTO portalFilterOptions) {
        var portalClient = portalClients.getPluginFor(portal)
                .orElseThrow(() -> new IllegalArgumentException("Portal with name: " + portal + " not found"));

        return portalClient.fetchProperties(portalFilterOptions);
    }

    @PostMapping("sync/all")
    public Map<String, SyncResultDTO> syncAll() {
        var hardcodedFilterOptions = new PortalFilterOptionsDTO(
                PropertyOperation.RENT,
                PropertyType.CONDO,
                Set.of("montevideo"),
                "USD", 1_000, 10_000
        );

        return portalClients.getPlugins().stream()
                .map(portalClient -> Map.entry(portalClient.getName(), portalClient.fetchProperties(hardcodedFilterOptions)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}