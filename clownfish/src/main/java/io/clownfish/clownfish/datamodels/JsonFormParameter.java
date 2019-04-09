/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.datamodels;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author sulzbachr
 */
public class JsonFormParameter {
    private @Getter @Setter String name;
    private @Getter @Setter String value;

    public JsonFormParameter() {
    }
}
