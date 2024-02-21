/*
 * Copyright 2024 SulzbachR.
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
package io.clownfish.clownfish.odata;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author SulzbachR
 */
public class SourceStructure {
    private @Getter @Setter int source;
    private @Getter @Setter String classname;
    private @Getter @Setter String url;
    private @Getter @Setter String user;
    private @Getter @Setter String password;
    private @Getter @Setter String tablename;

    public SourceStructure(int source, String classname, String url, String user, String password, String tablename) {
        this.source = source;
        this.classname = classname;
        this.url = url;
        this.user = user;
        this.password = password;
        this.tablename = tablename;
    }

    
}
