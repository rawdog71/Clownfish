/*
 * Copyright 2020 SulzbachR.
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

import io.clownfish.clownfish.dbentities.CfClasscontent;
import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author SulzbachR
 */
public class ContentDataOutput {
    private @Getter @Setter CfClasscontent content;
    private @Getter @Setter ArrayList<HashMap> keyvals;
    private @Getter @Setter HashMap keyval;
    private @Getter @Setter ArrayList<String> keywords;
    private @Getter @Setter boolean difference;
    private @Getter @Setter long maxversion;
}
