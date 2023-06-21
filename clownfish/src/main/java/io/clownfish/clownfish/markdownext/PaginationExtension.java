/*
 * Copyright 2023 raine.
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
package io.clownfish.clownfish.markdownext;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;

/**
 *
 * @author raine
 */
public class PaginationExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension, Formatter.FormatterExtension {
    
    private PaginationExtension() {
    }

    public static PaginationExtension create() {
        return new PaginationExtension();
    }

    @Override
    public void parserOptions(MutableDataHolder mdh) {   
    }

    @Override
    public void extend(Parser.Builder bldr) {
        bldr.customBlockParserFactory(new PaginationBlockParser.Factory());
    }

    @Override
    public void rendererOptions(MutableDataHolder mdh) {
    }

    @Override
    public void extend(HtmlRenderer.Builder bldr, String string) {
        if (bldr.isRendererType("HTML")) {
            bldr.nodeRendererFactory(new PaginationNodeRenderer.Factory());
        }
    }

    @Override
    public void extend(Formatter.Builder bldr) {
        bldr.nodeFormatterFactory(new PaginationNodeFormatter.Factory());
    }

}
