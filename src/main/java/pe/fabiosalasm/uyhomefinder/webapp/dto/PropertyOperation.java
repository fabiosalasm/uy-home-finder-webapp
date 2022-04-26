package pe.fabiosalasm.uyhomefinder.webapp.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PropertyOperation {
    RENT("alquiler"),
    BUY("compra"),

    BUY_PROJECT ("compra-proyecto");

    private final String name;

    PropertyOperation(String name) {
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
