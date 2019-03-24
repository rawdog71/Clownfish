/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.datamodels;

/**
 *
 * @author sulzbachr
 */
public class JsonFormParameter {
    private String name;
    private String value;

    public JsonFormParameter() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
