/*
 * Copyright 2019 sulzbachr.
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
package io.clownfish.clownfish.utils;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import io.clownfish.clownfish.Clownfish;
import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.dbentities.CfProperty;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */

@Component
public class MarkdownUtil {
    public @Getter @Setter MutableDataSet markdownOptions = null;
    private @Getter @Setter List<CfProperty> propertylist;
    private @Getter @Setter Map<String, String> propertymap;
    @Autowired private PropertyList proplist;

    public MarkdownUtil() {
    }
    
    public void initOptions() {
        markdownOptions = new MutableDataSet();
        propertymap = proplist.fillPropertyMap();
        
        propertymap.entrySet().stream().filter( 
            pm -> pm.getKey().startsWith("markdown_")
        ).filter( 
            pm -> pm.getValue().compareToIgnoreCase("on") == 0
        ).forEach( 
            pm -> putToMarkdownOptions(pm.getKey())
        );
    }
    
    private void putToMarkdownOptions(String option) {
        try {
            //System.out.println(option);
            ClassLoader classLoader = Clownfish.class.getClassLoader();
            switch (option) {
                case "markdown_StrikethroughExtension":
                    Class StrikethroughExtensionClass = classLoader.loadClass("com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension");
                    Object strikethroughExtensionObject = StrikethroughExtensionClass.newInstance();
                    markdownOptions.set(Parser.EXTENSIONS, Arrays.asList(strikethroughExtensionObject));
                    break;
                case "markdown_TablesExtension":
                    Class TablesExtensionClass = classLoader.loadClass("com.vladsch.flexmark.ext.tables.TablesExtension");
                    Object tablesExtensionClassObject = TablesExtensionClass.newInstance();
                    markdownOptions.set(Parser.EXTENSIONS, Arrays.asList(tablesExtensionClassObject));
                    break;
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SecurityException  ex) {
            java.util.logging.Logger.getLogger(Clownfish.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String parseMarkdown(String content, MutableDataSet markdownOptions) {
        // uncomment to convert soft-breaks to hard breaks
        //options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");

        Parser parser = Parser.builder(markdownOptions).build();
        HtmlRenderer renderer = HtmlRenderer.builder(markdownOptions).build();

        // You can re-use parser and renderer instances
        Node document = parser.parse(content);
        return renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"
    }
    
}
