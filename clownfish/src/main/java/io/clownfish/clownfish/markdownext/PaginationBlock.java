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

import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.ParagraphContainer;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.sequence.BasedSequence;

/**
 *
 * @author raine
 */
public class PaginationBlock extends Block implements ParagraphContainer {
    private BasedSequence openingMarker = BasedSequence.NULL;
    
    @Override
    public BasedSequence[] getSegments() {
        return new BasedSequence[] {
                openingMarker
        };
    }

    @Override
    public boolean isParagraphEndWrappingDisabled(Paragraph node) {
        return false;
    }

    @Override
    public boolean isParagraphStartWrappingDisabled(Paragraph node) {
        if (node == getFirstChild()) {
            // need to see if there is a blank line between it and our start
            int ourEOL = getChars().getBaseSequence().endOfLine(getChars().getStartOffset());
            int childStartEOL = node.getStartOfLine();
            return ourEOL + 1 == childStartEOL;
        }
        return false;
    }

    void setOpeningMarker(BasedSequence openingMarker) {
        this.openingMarker = openingMarker;
    }

    public BasedSequence getOpeningMarker() {
        return this.openingMarker;
    }
}
