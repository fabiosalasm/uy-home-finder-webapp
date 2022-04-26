package pe.fabiosalasm.uyhomefinder.webapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Set;

@Data
public class PortalFilterOptionsDTO {
    @NotNull
    @JsonProperty(required = true)
    private PropertyOperation propertyOperation; //mandatory

    @NotNull
    @JsonProperty(required = true)
    private PropertyType propertyType; // mandatory

    @NotEmpty
    @JsonProperty(required = true)
    private Set<@Pattern(regexp = "montevideo|canelones|maldonado") String> cities;

    @JsonProperty
    @Pattern(regexp = "USD|UYU")
    private String currency;

    // minPriceRange < maxPriceRange
    @JsonProperty
    private Integer minPrice;

    @JsonProperty
    private Integer maxPrice;

    public PortalFilterOptionsDTO(PropertyOperation propertyOperation, PropertyType propertyType, Set<String> cities,
                                  String currency, Integer minPrice, Integer maxPrice) {
        this.propertyOperation = propertyOperation;
        this.propertyType = propertyType;
        this.cities = cities;
        this.currency = (currency == null)? "USD" : currency;
        this.minPrice = (minPrice == null)? 0 : minPrice;
        this.maxPrice = (maxPrice == null)? 100_000 : maxPrice;
    }
}
