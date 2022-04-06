/*
 * Copyright 2020 sulzbachr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.clownfish.clownfish.graphql;

import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.utils.MarkdownUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author SulzbachR
 */
@Component
public class GraphQLUtil {
    @Autowired private CfClassService cfclassservice;
    @Autowired private CfAttributService cfattributservice;
    @Autowired MarkdownUtil markdownUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLUtil.class);
    
    public String generateSchema() {
        List<CfClass> classlist = cfclassservice.findAll();
        StringBuilder sb = new StringBuilder();
        
        sb.append("type Query {\n");
        for (CfClass clazz : classlist) {
            List<CfAttribut> attributlist = cfattributservice.findByClassref(clazz);
            for (CfAttribut attribut : attributlist) {
                if (0 != attribut.getAttributetype().getName().compareToIgnoreCase("classref")) {
                    if (attribut.getIdentity()) {
                        sb.append("  ").append(clazz.getName().toLowerCase()).append("By")
                            .append(attribut.getName().toUpperCase().charAt(0)).append(attribut.getName().substring(1))
                            .append("(").append(attribut.getName()).append(": ").append(getSchemaType(attribut.getAttributetype().getName())).append("): ").append(clazz.getName()).append("\n");
                    } else {
                        sb.append("  ").append(clazz.getName().toLowerCase()).append("By")
                            .append(attribut.getName().toUpperCase().charAt(0)).append(attribut.getName().substring(1))
                            .append("(").append(attribut.getName()).append(": ").append(getSchemaType(attribut.getAttributetype().getName())).append("): [").append(clazz.getName()).append("]\n");
                    }
                }
            }
        }
        sb.append("}\n\n");

        for (CfClass clazz : classlist) {
            sb.append("type ").append(clazz.getName()).append(" {\n");
  
            List<CfAttribut> attributlist = cfattributservice.findByClassref(clazz);
            for (CfAttribut attribut : attributlist) {
                if (0 == attribut.getAttributetype().getName().compareToIgnoreCase("classref")) {
                    sb.append("  ").append(attribut.getName()).append(": [").append(attribut.getRelationref().getName()).append("]\n");
                } else {
                    sb.append("  ").append(attribut.getName()).append(": ").append(getSchemaType(attribut.getAttributetype().getName())).append("\n");
                }
            }
            sb.append("}\n\n");
        }
        return sb.toString();
    }

    private String getSchemaType(String clownfishtype) {
        switch (clownfishtype) {
            case "boolean":
                return "Boolean";
            case "string":
                return "String";
            case "hashstring":
                return "String";
            case "integer":
                return "Int";
            case "real":
                return "Float";
            case "htmltext":
                return "String";
            case "markdown":
                return "String";
            case "datetime":
                return "String";
            case "media":
                return "Int";
            case "text":
                return "String";
            case "classref":
                return "Int";
            case "assetref":
                return "Int";
            default:
                return null;
        }
    }
}
