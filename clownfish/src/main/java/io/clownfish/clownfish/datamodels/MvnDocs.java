/*
 * Copyright 2021 SulzbachR.
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

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author SulzbachR
 */
public class MvnDocs {
    private @Getter @Setter String id;
    private @Getter @Setter String g;
    private @Getter @Setter String a;
    private @Getter @Setter String latestVersion;
    private @Getter @Setter String repositoryId;
    private @Getter @Setter String p;
    private @Getter @Setter long timestamp;
    private @Getter @Setter long versionCount;
    private @Getter @Setter List<String> text;
    private @Getter @Setter List<String> ec;
}
