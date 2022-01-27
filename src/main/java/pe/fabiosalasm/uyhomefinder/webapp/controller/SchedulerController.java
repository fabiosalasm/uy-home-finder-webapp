package pe.fabiosalasm.uyhomefinder.webapp.controller;

import lombok.AllArgsConstructor;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.fabiosalasm.uyhomefinder.webapp.dto.SyncResultDTO;
import pe.fabiosalasm.uyhomefinder.webapp.service.PortalClient;

import java.util.Map;
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
    public SyncResultDTO syncByPortal(@RequestParam(name = "portal", required = true) String portal) {
        var portalClient = portalClients.getPluginFor(portal)
                .orElseThrow(() -> new IllegalArgumentException("Portal with name: " + portal + " not found"));
        // Using spring plugin integration, detect with Portal implementation has as name the variable 'portal'
        // and executes a sync operation
        // the result how indicate how many registries were added, updated and deleted (now 0)
        return portalClient.fetchHouses();
    }

    @PostMapping("sync/all")
    public Map<String, SyncResultDTO> syncAll() {
        return portalClients.getPlugins().stream()
                .map(portalClient -> Map.entry(portalClient.getName(), portalClient.fetchHouses()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
