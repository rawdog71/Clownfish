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
package io.clownfish.clownfish.jdbc;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author sulzbachr
 */
public class DatatableProperties {
    private @Getter @Setter String tablename;
    private @Getter @Setter String orderby;
    private @Getter @Setter String orderdir;
    private @Getter @Setter int pagination;
    private @Getter @Setter int page;
    private @Getter @Setter ArrayList<DatatableCondition> conditionlist;
    private @Getter @Setter ArrayList<String> groupbylist;
    private @Getter @Setter String groupbycount;

    public DatatableProperties() {
        conditionlist = new ArrayList<>();
        groupbylist = new ArrayList<>();
        groupbycount = "";
    }

    public DatatableProperties(String tablename, String orderby, String orderdir, int pagination, int page, String groupbycount) {
        this.tablename = tablename;
        this.orderby = orderby;
        this.orderdir = orderdir;
        this.pagination = pagination;
        this.page = page;
        conditionlist = new ArrayList<>();
        groupbylist = new ArrayList<>();
        this.groupbycount = groupbycount;
    }
}
