package pe.fabiosalasm.uyhomefinder.webapp.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PropertyType {
    HOUSE("casas"),
    CONDO("apartamentos");

    private final String name;

    PropertyType(String name) {
        this.name = name;
    }

    @JsonValue
    public String value() {
        return name;
    }

    public String getName() {
        return name;
    }
}
