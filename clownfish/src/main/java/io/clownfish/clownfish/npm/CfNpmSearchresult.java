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
package io.clownfish.clownfish.npm;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author SulzbachR
 */
public class CfNpmSearchresult {
    private @Getter @Setter String name;
    private @Getter @Setter String scope;
    private @Getter @Setter String version;
    private @Getter @Setter String description;
    private @Getter @Setter ArrayList<String> keywords;
    private @Getter @Setter String date;
    private @Getter @Setter CfNpmLinks links;
    private @Getter @Setter CfNpmAuthor author;
    private @Getter @Setter CfNpmPublisher publisher;
    private @Getter @Setter ArrayList<CfNpmPublisher> maintainers;
}
