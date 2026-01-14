/*
 * Copyright 2022 SulzbachR.
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
package io.clownfish.clownfish.websocket;

import com.google.gson.Gson;
import io.clownfish.clownfish.datamodels.ClownfishResponse;
import io.clownfish.clownfish.datamodels.JsonFormParameter;
import io.clownfish.clownfish.datamodels.RenderContext;
import io.clownfish.clownfish.service.PageRenderService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

/**
 *
 * @author SulzbachR
 */
public class CustomTextFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final PageRenderService renderservice;
    private static Set<ChannelHandlerContext> sessions = null;

    public CustomTextFrameHandler(PageRenderService renderservice, Set<ChannelHandlerContext> sessions) {
        this.sessions = sessions;
        this.renderservice = renderservice;
    }
    
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        sessions.add(ctx);
    }
    
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        sessions.remove(ctx);
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        String request = frame.text();
        
        WebSocketMessage wsbm = new Gson().fromJson(request, WebSocketMessage.class);
        List<JsonFormParameter> postmap = new ArrayList<>();
        wsbm.getInput().forEach((key, value) -> {
            JsonFormParameter jfp = new JsonFormParameter();
            jfp.setName(key);
            jfp.setValue(value);
            postmap.add(jfp);
        });
        
        RenderContext rc = new RenderContext();
        rc.setName(wsbm.getWebservice());
        rc.setPostmap(postmap);
        rc.setUrlParams(new ArrayList<>());
        rc.setMakestatic(false);
        rc.setFileitems(null);
        rc.setClientinfo(null);
        rc.setReferrer("");
        ClownfishResponse cfResponse = renderservice.renderPage(rc);
        
        //ClownfishResponse cfResponse = clownfish.makeResponse(wsbm.getWebservice(), postmap, new ArrayList<>(), false, null, null, "");
        
        if (wsbm.isBroadcast()) {
            for (ChannelHandlerContext session : sessions) {
                if (!session.isRemoved()) {
                    session.channel().writeAndFlush(new TextWebSocketFrame(cfResponse.getOutput()));
                }
            }
        } else {
            ctx.channel().writeAndFlush(new TextWebSocketFrame(cfResponse.getOutput()));
        }
    }
}
