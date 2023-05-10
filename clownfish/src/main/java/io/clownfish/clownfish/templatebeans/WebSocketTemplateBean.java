/*
 * Copyright 2021 raine.
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
package io.clownfish.clownfish.templatebeans;

import com.google.gson.Gson;
import io.clownfish.clownfish.utils.ClownfishUtil;
import io.clownfish.clownfish.websocket.WebSocketClient;
import io.clownfish.clownfish.websocket.WebSocketMessage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author raine
 */
@Scope("request")
@Component
public class WebSocketTemplateBean implements Serializable {
    final transient Logger LOGGER = LoggerFactory.getLogger(WebSocketTemplateBean.class);
    private @Getter @Setter int websocketPort;

    public WebSocketTemplateBean() {
    }
    
    public void broadcast(String webservice, boolean broadcast, Map input) throws Exception {
        WebSocketMessage wsm = new WebSocketMessage();
        HashMap<String, String> inputmap = ClownfishUtil.getHashmap(input);
        wsm.setWebservice(webservice);
        wsm.setBroadcast(broadcast);
        wsm.setInput(inputmap);
        final String url = "ws://localhost:" + websocketPort + "/websocket";
        final WebSocketClient client = new WebSocketClient(url);
        client.open();
        client.send(new Gson().toJson(wsm).toString());
        client.close();
        
    }
}
