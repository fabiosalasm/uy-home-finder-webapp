package pe.fabiosalasm.uyhomefinder.webapp.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class PortalFilterOptionsDTOJsonTest {
    @Autowired
    private JacksonTester<PortalFilterOptionsDTO> json;

    @Test
    public void testSerialise() throws Exception{
        PortalFilterOptionsDTO portalFilterOptions = new PortalFilterOptionsDTO(
                PropertyOperation.RENT,
                PropertyType.CONDO,
                Set.of("montevideo"),
                "USD", 1_000, 10_000
        );

        JsonContent<PortalFilterOptionsDTO> result = json.write(portalFilterOptions);

        assertThat(result).hasJsonPathStringValue("$.property_operation");
        assertThat(result).hasJsonPathStringValue("$.property_type");

        assertThat(result).extractingJsonPathStringValue("$.property_operation").isEqualTo("alquiler");
        assertThat(result).extractingJsonPathStringValue("$.property_type").isEqualTo("apartamentos");
    }

    @Test
    public void testDeserialise() throws Exception {
        String jsonContent = """
                {
                    "property_operation": "compra",
                    "property_type": "casas",
                    "cities": ["montevideo"]
                }
                """;

        PortalFilterOptionsDTO result = json.parse(jsonContent).getObject();

        assertThat(result.getPropertyOperation()).isEqualTo(PropertyOperation.BUY);
        assertThat(result.getPropertyType()).isEqualTo(PropertyType.HOUSE);
        assertThat(result.getCities()).contains("montevideo");
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getMinPrice()).isEqualTo(0);
        assertThat(result.getMaxPrice()).isEqualTo(100_000);
    }
}
