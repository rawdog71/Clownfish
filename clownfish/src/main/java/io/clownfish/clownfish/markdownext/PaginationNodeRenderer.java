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
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.html.renderer.PhasedNodeRenderer;
import com.vladsch.flexmark.html.renderer.RenderingPhase;
import static com.vladsch.flexmark.html.renderer.RenderingPhase.BODY_TOP;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKeyBase;
import com.vladsch.flexmark.util.data.DataValueFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author raine
 */
public class PaginationNodeRenderer implements PhasedNodeRenderer {

    @Override
    public Set<RenderingPhase> getRenderingPhases() {
        LinkedHashSet<RenderingPhase> phaseSet = new LinkedHashSet<>();
        phaseSet.add(BODY_TOP);
        return phaseSet;
    }

    @Override
    public void renderDocument(NodeRendererContext context, HtmlWriter html, Document dcmnt, RenderingPhase phase) {
        if (phase == BODY_TOP) {
            HashMap<Integer, List<Node>> blocklist = new HashMap<>();
            int pages = 0;
            Node lastblock = null;
            ArrayList<Node> nodelist = null;
            for (Node childblock : dcmnt.getChildren()) {
                if (0 == childblock.getChars().compareTo("[[page]]")) {
                    if (pages > 0) {
                        blocklist.put(pages, nodelist);
                    }
                    nodelist = new ArrayList<>();
                    pages++;
                } else {
                    if (null != nodelist) {
                        nodelist.add(childblock);
                    }
                }
            }
            if (pages > 0) {
                blocklist.put(pages, nodelist);
                int currentpage = getPage(context);
                String site = getSite(context);
                String url = getUrl(context);
                if (currentpage > pages) currentpage = 1;
                dcmnt.removeChildren();
                List currentnodelist = blocklist.get(currentpage);
                for (Object node : currentnodelist) {
                    dcmnt.appendChild((Node) node);
                }
                
                // create a paragraph for the text
                /*
                Text text = new Text("TEST");
                Paragraph paragraph = new Paragraph(text.getChars());
                paragraph.setCharsFromContent();
                
                dcmnt.appendChild(paragraph);
                dcmnt.setCharsFromSegments();
                */
                for (int i = 1; i<= pages; i++) {
                    html.attr("href", "/" + site + url + "/page/" + i);
                    html.withAttr().tag("a").line();
                    html.raw(""+i);
                    html.closeTag("a").line();
                }
            }
        }
    }

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        Set<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(PaginationBlock.class, this::render));
        return set;
    }
    
    private void render(PaginationBlock node, NodeRendererContext context, HtmlWriter html) {
        /*
        System.out.println(html.toString());
        HashMap<Integer, String> blocklist = new HashMap<>();
        int pages = 0;
        String block = "";
        for (Node childblock : node.getParent().getChildren()) {
            if (0 == childblock.getChars().compareTo(node.getOpeningMarker())) {
                if (pages > 0) {
                    blocklist.put(pages, block);
                }
                pages++;
                block = "";
            } else {
                //System.out.println(childblock.getChars());
                block += childblock.getChars();
            }
        }
        blocklist.put(pages, block);

        int currentpage = getPage(context);
        if (currentpage > pages) currentpage = 1;
        html.text("TEST");
        */
        
        /*
        if (title.isEmpty()) {
            html.srcPos(node.getChars()).withAttr()
                    .attr(Attribute.CLASS_ATTR, "adm-block")
                    .attr(Attribute.CLASS_ATTR, "adm-" + type)
                    .tag("div", false).line();

            html.attr(Attribute.CLASS_ATTR, "adm-body").withAttr(ADMONITION_BODY_PART).tag("div").indent().line();

            context.renderChildren(node);

            html.unIndent().closeTag("div").line();
            html.closeTag("div").line();
        } else {
            html.srcPos(node.getChars())
                    .attr(Attribute.CLASS_ATTR, "adm-block").attr(Attribute.CLASS_ATTR, "adm-" + type);

            if (openClose != null) {
                html.attr(Attribute.CLASS_ATTR, openClose).attr(Attribute.CLASS_ATTR, "adm-" + type);
            }

            html.withAttr().tag("div", false).line();
            html.attr(Attribute.CLASS_ATTR, "adm-heading").withAttr(ADMONITION_HEADING_PART).tag("div").line();
            html.attr(Attribute.CLASS_ATTR, "adm-icon").withAttr(ADMONITION_ICON_PART).tag("svg").raw("<use xlink:href=\"#adm-").raw(type).raw("\" />").closeTag("svg");
            html.withAttr(ADMONITION_TITLE_PART).tag("span").text(title).closeTag("span").line();
            html.closeTag("div").line();

            html.attr(Attribute.CLASS_ATTR, "adm-body").withAttr(ADMONITION_BODY_PART).withCondIndent().tagLine("div", () -> context.renderChildren(node));

            html.closeTag("div").line();
        }
        */
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(DataHolder dh) {
            return new PaginationNodeRenderer();
        }
    }
    
    private String getSite(NodeRendererContext context) {
        String site = "";
        DataHolder dh = context.getOptions().toImmutable();
        for (DataKeyBase dkb : dh.getKeys()) {
            if (0 == dkb.getName().compareToIgnoreCase("urlparam/site")) {
                DataValueFactory dvf = dkb.getFactory();
                site = (String)dh.getOrCompute(dkb, dvf);
                break;
            }
        }
        return site;
    }
    
    private int getPage(NodeRendererContext context) {
        int page = 1;
        DataHolder dh = context.getOptions().toImmutable();
        for (DataKeyBase dkb : dh.getKeys()) {
            if (0 == dkb.getName().compareToIgnoreCase("urlparam/page")) {
                DataValueFactory dvf = dkb.getFactory();
                page = Integer.parseInt((String)dh.getOrCompute(dkb, dvf));
                break;
            }
        }
        return page;
    }
    
    private String getUrl(NodeRendererContext context) {
        String url = "";
        DataHolder dh = context.getOptions().toImmutable();
        for (DataKeyBase dkb : dh.getKeys()) {
            if (dkb.getName().startsWith("urlparam/")) {
                String[] paramparts = dkb.getName().split("/");
                if ((0 != paramparts[1].compareToIgnoreCase("page")) && (0 != paramparts[1].compareToIgnoreCase("site"))) {
                    DataValueFactory dvf = dkb.getFactory();
                    url += "/" + paramparts[1] + "/" + (String)dh.getOrCompute(dkb, dvf);
                }
            }
        }
        return url;
    }
}
