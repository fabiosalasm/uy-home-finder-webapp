package pe.fabiosalasm.uyhomefinder.webapp.validation;


import org.junit.jupiter.api.Test;
import pe.fabiosalasm.uyhomefinder.webapp.dto.PortalFilterOptionsDTO;
import pe.fabiosalasm.uyhomefinder.webapp.dto.PropertyOperation;
import pe.fabiosalasm.uyhomefinder.webapp.dto.PropertyType;

import javax.validation.Validation;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PortalFilterOptionsDTOValidationTest {

    @Test
    public void testDTOIsValid() {
        PortalFilterOptionsDTO dto = validDTO();

        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var validator = validatorFactory.getValidator();
            var constraintViolations = validator.validate(dto);

            assertThat(constraintViolations.size()).isEqualTo(0);
        }
    }

    @Test
    public void testDTOIsInvalid() {
        PortalFilterOptionsDTO dto = invalidDTO();

        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var validator = validatorFactory.getValidator();
            var constraintViolations = validator.validate(dto);

            assertThat(constraintViolations.size()).isGreaterThan(0);
        }
    }

    private PortalFilterOptionsDTO validDTO() {
        return new PortalFilterOptionsDTO(
                PropertyOperation.BUY,
                PropertyType.CONDO,
                Set.of("montevideo", "canelones"),
                "USD",
                1_000,
                2_000
        );
    }

    private PortalFilterOptionsDTO invalidDTO() {
        return new PortalFilterOptionsDTO(
                null,
                PropertyType.CONDO,
                Set.of("montevideo", "canelones"),
                "USD",
                1_000,
                2_000
        );
    }
}
