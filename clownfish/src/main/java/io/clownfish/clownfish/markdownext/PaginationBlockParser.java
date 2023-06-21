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

import com.vladsch.flexmark.ast.ListItem;
import com.vladsch.flexmark.ast.util.Parsing;
import com.vladsch.flexmark.parser.block.AbstractBlockParser;
import com.vladsch.flexmark.parser.block.AbstractBlockParserFactory;
import com.vladsch.flexmark.parser.block.BlockContinue;
import com.vladsch.flexmark.parser.block.BlockParser;
import com.vladsch.flexmark.parser.block.BlockParserFactory;
import com.vladsch.flexmark.parser.block.BlockStart;
import com.vladsch.flexmark.parser.block.CustomBlockParserFactory;
import com.vladsch.flexmark.parser.block.MatchedBlockParser;
import com.vladsch.flexmark.parser.block.ParserState;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.sequence.mappers.SpecialLeadInHandler;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @author raine
 */
public class PaginationBlockParser extends AbstractBlockParser {
    final private static String PAGINATION_START_FORMAT = "^(\\[\\[page\\]\\])$";
    
    final PaginationBlock block;
    private static @Getter @Setter int blockcounter = 0;
    private boolean hadBlankLine;

    public PaginationBlockParser() {
        this.block = new PaginationBlock();
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        final int nonSpaceIndex = state.getNextNonSpaceIndex();
        if (state.isBlank()) {
            hadBlankLine = true;
            return BlockContinue.atIndex(nonSpaceIndex);
        } else if (!hadBlankLine) {
            return BlockContinue.atIndex(nonSpaceIndex);
        } else {
            return BlockContinue.none();
        }
    }

    @Override
    public void closeBlock(ParserState ps) {
        block.setCharsFromContent();
    }
    
    public static class Factory implements CustomBlockParserFactory {
        @Nullable
        @Override
        public Set<Class<?>> getAfterDependents() {
            return null;
        }

        @Nullable
        @Override
        public Set<Class<?>> getBeforeDependents() {
            return null;
        }

        @Override
        public @Nullable SpecialLeadInHandler getLeadInHandler(@NotNull DataHolder options) {
            return PaginationLeadInHandler.HANDLER;
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }

        @NotNull
        @Override
        public BlockParserFactory apply(@NotNull DataHolder options) {
            return new BlockFactory(options);
        }
    }
    
    static class PaginationLeadInHandler implements SpecialLeadInHandler {
        final static SpecialLeadInHandler HANDLER = new PaginationLeadInHandler();

        @Override
        public boolean escape(@NotNull BasedSequence sequence, @Nullable DataHolder options, @NotNull Consumer<CharSequence> consumer) {
            if ((sequence.length() == 3 || sequence.length() == 4 && sequence.charAt(3) == '+') && (sequence.startsWith("???") || sequence.startsWith("!!!"))) {
                consumer.accept("\\");
                consumer.accept(sequence);
                return true;
            }
            return false;
        }

        @Override
        public boolean unEscape(@NotNull BasedSequence sequence, @Nullable DataHolder options, @NotNull Consumer<CharSequence> consumer) {
            if ((sequence.length() == 4 || sequence.length() == 5 && sequence.charAt(4) == '+') && (sequence.startsWith("\\???") || sequence.startsWith("\\!!!"))) {
                consumer.accept(sequence.subSequence(1));
                return true;
            }
            return false;
        }
    }
    
    static boolean isMarker(
            final ParserState state,
            final int index,
            final boolean inParagraph,
            final boolean inParagraphListItem
    ) {
        CharSequence line = state.getLine();
        if (!inParagraph) {
            if ((state.getIndent() == 0) && (!inParagraphListItem)) {
                if (inParagraphListItem) {
                    return state.getIndent() == 0;
                } else {
                    return state.getIndent() < state.getParsing().CODE_BLOCK_INDENT;
                }
            }
        }
        return false;
    }
    
    private static class BlockFactory extends AbstractBlockParserFactory {
        BlockFactory(DataHolder options) {
            super(options);
        }

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (state.getIndent() >= 4) {
                return BlockStart.none();
            }

            int nextNonSpace = state.getNextNonSpaceIndex();
            BlockParser matched = matchedBlockParser.getBlockParser();
            boolean inParagraph = matched.isParagraphParser();
            boolean inParagraphListItem = inParagraph && matched.getBlock().getParent() instanceof ListItem && matched.getBlock() == matched.getBlock().getParent().getFirstChild();

            if (isMarker(state, nextNonSpace, inParagraph, inParagraphListItem)) {
                BasedSequence line = state.getLine();
                BasedSequence trySequence = line.subSequence(nextNonSpace, line.length());
                Parsing parsing = state.getParsing();
                Pattern startPattern = Pattern.compile(String.format(PAGINATION_START_FORMAT, parsing.ATTRIBUTENAME, parsing.LINK_TITLE_STRING));
                Matcher matcher = startPattern.matcher(trySequence);

                if (matcher.find()) {
                    // Pagination block
                    blockcounter++;
                    
                    BasedSequence openingMarker = line.subSequence(nextNonSpace + matcher.start(1), nextNonSpace + matcher.end(1));
                    //BasedSequence info = line.subSequence(nextNonSpace + matcher.start(2), nextNonSpace + matcher.end(2));
                    //BasedSequence titleChars = matcher.group(3) == null ? BasedSequence.NULL : line.subSequence(nextNonSpace + matcher.start(3), nextNonSpace + matcher.end(3));

                    //int contentOffset = options.contentIndent;

                    PaginationBlockParser paginationBlockParser = new PaginationBlockParser();
                    paginationBlockParser.block.setOpeningMarker(openingMarker);
                    //paginationBlockParser.block.setInfo(info);
                    //paginationBlockParser.block.setTitleChars(titleChars);

                    return BlockStart.of(paginationBlockParser)
                            .atIndex(line.length());
                } else {
                    return BlockStart.none();
                }
            } else {
                return BlockStart.none();
            }
        }
    }
}
