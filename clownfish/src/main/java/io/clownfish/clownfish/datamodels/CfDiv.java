/*
 * Copyright 2022 raine.
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
package io.clownfish.clownfish.datamodels;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author raine
 */
public class CfDiv {
    private @Getter @Setter String id;
    private @Getter @Setter String name;
    private @Getter @Setter ArrayList<String> contentArray;
    private @Getter @Setter ArrayList<String> contentlistArray;
    private @Getter @Setter ArrayList<String> assetArray;
    private @Getter @Setter ArrayList<String> assetlistArray;
    private @Getter @Setter ArrayList<String> keywordlistArray;
    private @Getter @Setter boolean visible;

    public CfDiv() {
        contentArray = new ArrayList<>();
        contentlistArray = new ArrayList<>();
        assetArray = new ArrayList<>();
        assetlistArray = new ArrayList<>();
        keywordlistArray = new ArrayList<>();
        visible = true;
    }

    public CfDiv(String id, String name) {
        this.id = id;
        this.name = name;
        contentArray = new ArrayList<>();
        contentlistArray = new ArrayList<>();
        assetArray = new ArrayList<>();
        assetlistArray = new ArrayList<>();
        keywordlistArray = new ArrayList<>();
        visible = true;
    }
    
    public String showIcon() {
        if (visible) {
            return "pi-eye";
        } else {
            return "pi-eye-slash";
        }
    }
}
