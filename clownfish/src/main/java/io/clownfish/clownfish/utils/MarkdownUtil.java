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

import com.vladsch.flexmark.ext.abbreviation.*;
import com.vladsch.flexmark.ext.admonition.*;
import com.vladsch.flexmark.ext.anchorlink.*;
import com.vladsch.flexmark.ext.aside.*;
import com.vladsch.flexmark.ext.attributes.*;
import com.vladsch.flexmark.ext.autolink.*;
import com.vladsch.flexmark.ext.definition.*;
import com.vladsch.flexmark.ext.emoji.*;
import com.vladsch.flexmark.ext.footnotes.*;
import com.vladsch.flexmark.ext.gitlab.*;
import com.vladsch.flexmark.ext.ins.*;
import com.vladsch.flexmark.ext.jekyll.tag.*;
import com.vladsch.flexmark.ext.macros.*;
import com.vladsch.flexmark.ext.media.tags.*;
import com.vladsch.flexmark.ext.superscript.*;
import com.vladsch.flexmark.ext.tables.*;
import com.vladsch.flexmark.ext.toc.*;
import com.vladsch.flexmark.ext.typographic.*;
import com.vladsch.flexmark.ext.wikilink.*;
import com.vladsch.flexmark.ext.youtube.embedded.*;
import com.vladsch.flexmark.ext.gfm.strikethrough.*;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.misc.Extension;
import com.vladsch.flexmark.util.data.MutableDataSet;
import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.dbentities.CfProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */

@Component
@Accessors(chain = true)
public class MarkdownUtil {
    public @Getter @Setter MutableDataSet markdownOptions = null;
    private @Getter @Setter List<CfProperty> propertylist;
    private @Getter @Setter Map<String, String> propertymap;
    @Autowired private final PropertyList proplist;
    private List<Extension> extensionList;

    final transient Logger LOGGER = LoggerFactory.getLogger(MarkdownUtil.class);
    
    public MarkdownUtil(PropertyList proplist) {
        this.proplist = proplist;
    }
    
    public void initOptions() {
        extensionList = new ArrayList<>();
        markdownOptions = new MutableDataSet();
        propertymap = proplist.fillPropertyMap();
        
        propertymap.entrySet().stream().filter( 
            pm -> pm.getKey().startsWith("markdown_")
        ).filter( 
            pm -> pm.getValue().compareToIgnoreCase("on") == 0
        ).forEach( 
            pm -> putToMarkdownOptions(pm.getKey())
        );
        if (!extensionList.isEmpty()) {
            markdownOptions.set(Parser.EXTENSIONS, extensionList);
        }
        markdownOptions.set(HtmlRenderer.GENERATE_HEADER_ID, true);
    }
    
    private void putToMarkdownOptions(String option) {
        try {
            switch (option) {
                case "markdown_AbbreviationExtension":
                    extensionList.add(AbbreviationExtension.create());
                    break;
                case "markdown_AdmonitionExtension":
                    extensionList.add(AdmonitionExtension.create());
                    break;
                case "markdown_AnchorLinkExtension":
                    extensionList.add(AnchorLinkExtension.create());
                    break;
                case "markdown_AsideExtension":
                    extensionList.add(AsideExtension.create());
                    break;
                case "markdown_AttributesExtension":
                    extensionList.add(AttributesExtension.create());
                    break;    
                case "markdown_StrikethroughExtension":
                    extensionList.add(StrikethroughExtension.create());
                    break;
                case "markdown_TablesExtension":
                    extensionList.add(TablesExtension.create());
                    break;
                case "markdown_JekyllTagExtension":
                    extensionList.add(JekyllTagExtension.create());
                    break;
                case "markdown_EmojiExtension":
                    extensionList.add(EmojiExtension.create());
                    break;
                case "markdown_AutolinkExtension":
                    extensionList.add(AutolinkExtension.create());
                    break;
                case "markdown_DefinitionExtension":
                    extensionList.add(DefinitionExtension.create());
                    break;
                case "markdown_FootnoteExtension":
                    extensionList.add(FootnoteExtension.create());
                    break;
                case "markdown_GitLabExtension":
                    extensionList.add(GitLabExtension.create());
                    break;
                case "markdown_InsExtension":
                    extensionList.add(InsExtension.create());
                    break;
                case "markdown_MacrosExtension":
                    extensionList.add(MacrosExtension.create());
                    break;
                case "markdown_MediaTagsExtension":
                    extensionList.add(MediaTagsExtension.create());
                    break;
                case "markdown_SuperscriptExtension":
                    extensionList.add(SuperscriptExtension.create());
                    break;
                case "markdown_TocExtension":
                    extensionList.add(TocExtension.create());
                    break;
                case "markdown_TypographicExtension":
                    extensionList.add(TypographicExtension.create());
                    break;
                case "markdown_WikiLinkExtension":
                    extensionList.add(WikiLinkExtension.create());
                    break;
                case "markdown_YouTubeLinkExtension":
                    extensionList.add(YouTubeLinkExtension.create());
                    break;
            }
        } catch (SecurityException  ex) {
            LOGGER.error(ex.getMessage());
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
