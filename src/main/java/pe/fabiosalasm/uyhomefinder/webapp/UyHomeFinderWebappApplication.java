package pe.fabiosalasm.uyhomefinder.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import pe.fabiosalasm.uyhomefinder.webapp.service.PortalClient;

@SpringBootApplication
@EnablePluginRegistries(PortalClient.class)
public class UyHomeFinderWebappApplication {

    public static void main(String[] args) {
        SpringApplication.run(UyHomeFinderWebappApplication.class, args);
    }

}
