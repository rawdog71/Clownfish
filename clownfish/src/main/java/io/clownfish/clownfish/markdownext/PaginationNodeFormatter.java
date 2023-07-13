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

import com.vladsch.flexmark.formatter.MarkdownWriter;
import com.vladsch.flexmark.formatter.NodeFormatter;
import com.vladsch.flexmark.formatter.NodeFormatterContext;
import com.vladsch.flexmark.formatter.NodeFormatterFactory;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.RepeatedSequence;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author raine
 */
public class PaginationNodeFormatter implements NodeFormatter {

    @Override
    public Set<NodeFormattingHandler<?>> getNodeFormattingHandlers() {
        return new HashSet<>(Collections.singletonList(
                new NodeFormattingHandler<>(PaginationBlock.class, PaginationNodeFormatter.this::render)
        ));
    }

    @Override
    public Set<Class<?>> getNodeClasses() {
        return null;
    }
    
    private void render(PaginationBlock node, NodeFormatterContext context, MarkdownWriter markdown) {
        markdown.blankLine();
        markdown.append(node.getOpeningMarker()).append(' ');
        markdown.line();
        markdown.pushPrefix().addPrefix(RepeatedSequence.repeatOf(" ", 0).toString());
        context.renderChildren(node);
        markdown.blankLine();
        markdown.popPrefix();
    }
    
    public static class Factory implements NodeFormatterFactory {
        @NotNull
        @Override
        public NodeFormatter create(@NotNull DataHolder options) {
            return new PaginationNodeFormatter();
        }
    }
}
