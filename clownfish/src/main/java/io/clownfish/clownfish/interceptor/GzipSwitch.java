/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.interceptor;

import java.io.Serializable;
import javax.faces.bean.RequestScoped;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author sulzbachr
 */
@RequestScoped
@Named("gzipswitch")
public class GzipSwitch implements Serializable {
    private @Getter @Setter boolean gzipon;

    public GzipSwitch() {
        this.gzipon = false;
    }
}
