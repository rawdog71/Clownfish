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
package io.clownfish.clownfish.utils;

import io.clownfish.clownfish.dbentities.CfSearchhistory;
import io.clownfish.clownfish.serviceinterface.CfSearchhistoryService;
import javax.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author raine
 */
@Scope("singleton")
@Component
public class SearchUtil {
    @Autowired CfSearchhistoryService cfsearchhistoryService;
    
    /**
     * updateSearchhistory
     * 
     */
    public void updateSearchhistory(String[] searchexpressions) {
        for (String expression : searchexpressions) {
            if ((expression.length() >= 3) && (!expression.endsWith("*"))) {
                try {
                    CfSearchhistory searchhistory = cfsearchhistoryService.findByExpression(expression.toLowerCase());
                    searchhistory.setCounter(searchhistory.getCounter()+1);
                    cfsearchhistoryService.edit(searchhistory);
                } catch (NoResultException ex) {
                    CfSearchhistory newsearchhistory = new CfSearchhistory();
                    newsearchhistory.setExpression(expression.toLowerCase());
                    newsearchhistory.setCounter(1);
                    cfsearchhistoryService.create(newsearchhistory);
                }
            }
        }
    }
}
