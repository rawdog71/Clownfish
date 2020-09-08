/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.clownfish.clownfish.datamodels;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author sulzbachr
 */
public class GetContentParameter {
    private @Getter @Setter String returncode;
    private @Getter @Setter String apikey;
    private @Getter @Setter String classname;
    private @Getter @Setter String listname;
    private @Getter @Setter String identifier;
    private @Getter @Setter String searches;
    private @Getter @Setter String range;
    private @Getter @Setter String keywords;
    private @Getter @Setter String json;

    public GetContentParameter() {
    }
}
