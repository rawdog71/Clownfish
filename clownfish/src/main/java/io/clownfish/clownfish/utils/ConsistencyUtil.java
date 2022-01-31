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
package io.clownfish.clownfish.utils;

import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Component
public class ConsistencyUtil {
    @Autowired CfListcontentService cflistcontentservice;
    @Autowired CfListService cflistservice;
    @Autowired CfClasscontentService cfclasscontentservice;
    @Autowired CfAttributcontentService cfattributcontentservice;
    @Autowired CfAssetlistcontentService cfassetlistcontentservice;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(ConsistencyUtil.class);
    
    public void checkConsistency() {
        LOGGER.info("CONSISTENCY CHECK ListContent - START");
        List<CfListcontent> listcontentList = cflistcontentservice.findAll();
        for (CfListcontent listcontent : listcontentList) {
            // Check Classcontent
            try {
                CfClasscontent cc = cfclasscontentservice.findById(listcontent.getCfListcontentPK().getClasscontentref());
            } catch (Exception ex) {
                LOGGER.warn("Classcontent does not exist: " + listcontent.getCfListcontentPK().getClasscontentref());
                cflistcontentservice.delete(listcontent);
            }
            // Check List
            try {
                CfList l = cflistservice.findById(listcontent.getCfListcontentPK().getListref());
            } catch (Exception ex) {
                LOGGER.warn("List does not exist: " + listcontent.getCfListcontentPK().getListref());
                cflistcontentservice.delete(listcontent);
            }
        }
        LOGGER.info("CONSISTENCY CHECK ListContent - END");
        
        LOGGER.info("CONSISTENCY CHECK AttributContent - START");     
        List<CfClasscontent> classcontentlist = cfclasscontentservice.findAll();
        if (!classcontentlist.isEmpty()) {
            long lastId = classcontentlist.get(classcontentlist.size()-1).getId();
            for (long i = 0; i <= lastId; i++) {
                try {
                    CfClasscontent cc =  cfclasscontentservice.findById(i);
                } catch (Exception ex) {
                    CfClasscontent cc = new CfClasscontent();
                    cc.setId(i);
                    //System.out.println(i);
                    try {
                        List<CfAttributcontent> aclist = cfattributcontentservice.findByClasscontentref(cc);
                    } catch (Exception ex2) {
                        cfattributcontentservice.delete(i);
                    }    

                }
            }
        }
        List<CfList> list = cflistservice.findAll();
        if (!list.isEmpty()) {
            long lastlistId = list.get(list.size()-1).getId();
            for (long i = 0; i <= lastlistId; i++) {
                try {
                    CfList l =  cflistservice.findById(i);
                } catch (Exception ex) {
                    CfList cl = new CfList();
                    cl.setId(i);
                    //System.out.println(i);
                    try {
                        List<CfAttributcontent> aclist = cfattributcontentservice.findByContentclassRef(cl);
                    } catch (Exception ex2) {
                        //System.out.println(i);
                        cfattributcontentservice.updateContentref(i);
                    }    

                }
            }
        }
        LOGGER.info("CONSISTENCY CHECK AttributContent - END");
    }
}
