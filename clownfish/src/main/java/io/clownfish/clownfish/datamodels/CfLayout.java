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
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author raine
 */
public class CfLayout {
    private @Getter @Setter String id;
    private @Getter @Setter HashMap<String, CfDiv> divArray;

    public CfLayout() {
        divArray = new HashMap<>();
    }

    public CfLayout(String id) {
        this.id = id;
        divArray = new HashMap<>();
    }
    
    public List<CfDiv> getDivs() {
        return new ArrayList<>(divArray.values());
     }
}
