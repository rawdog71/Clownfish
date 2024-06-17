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
package io.clownfish.clownfish.constants;

/**
 *
 * @author sulzbachr
 */
public class ClownfishConst {
    public enum ViewModus {
        DEVELOPMENT,
        STAGING
    }
    
    public enum AccessTypes {
        TYPE_CLASS(0),
        TYPE_CONTENTLIST(1),
        TYPE_CONTENT(2),
        TYPE_ASSETLIST(3),
        TYPE_ASSET(4),
        TYPE_SITE(5);
        
        private final Integer value;

        AccessTypes(final Integer value) {
           this.value = value;
        }
        public Integer getValue(){
           return value;
        }
    }
    
    public enum JavascriptTypes {
        TYPE_JS(0),
        TYPE_TS(1);    
        
        private final int value;
        
        JavascriptTypes(final int value) {
           this.value = value;
        }
        public int getValue(){
           return value;
        }
    }
}
