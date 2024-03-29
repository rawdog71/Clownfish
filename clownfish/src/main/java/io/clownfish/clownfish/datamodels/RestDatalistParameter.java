/*
 * Copyright 2020 sulzbachr.
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

import io.clownfish.clownfish.dbentities.CfList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author sulzbachr
 */
public class RestDatalistParameter {
    private @Getter @Setter String returncode;
    private @Getter @Setter String apikey;
    private @Getter @Setter String token;
    private @Getter @Setter String classname;
    private @Getter @Setter String listname;
    private @Getter @Setter Long listid;
    private @Getter @Setter List<CfList> list;

    public RestDatalistParameter() {
    }
}
