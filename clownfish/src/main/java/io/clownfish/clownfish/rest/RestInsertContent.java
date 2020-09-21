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
package io.clownfish.clownfish.rest;

import io.clownfish.clownfish.datamodels.InsertContentParameter;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.ContentUtil;
import java.math.BigInteger;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author SulzbachR
 */
@RestController
public class RestInsertContent {
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAttributService cfattributService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired transient CfAttributetypeService cfattributetypeService;
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient CfListService cflistService;
    @Autowired ContentUtil contentUtil;
    @Autowired ApiKeyUtil apikeyutil;
    private static final Logger logger = LoggerFactory.getLogger(RestInsertContent.class);

    @PostMapping("/insertcontent")
    public InsertContentParameter restInsertContent(@RequestBody InsertContentParameter icp) {
        return insertContent(icp);
    }
    
    private InsertContentParameter insertContent(InsertContentParameter icp) {
        try {
            String apikey = icp.getApikey();
            if (apikeyutil.checkApiKey(apikey, "InsertContent")) {
                CfClass clazz = cfclassService.findByName(icp.getClassname());
                System.out.println(clazz.isSearchrelevant());

                cfclasscontentService.evictAll();
                try {
                    CfClasscontent classcontent = cfclasscontentService.findByName(icp.getContentname());
                    icp.setReturncode("Duplicate Classcontent");
                } catch (javax.persistence.NoResultException ex) {
                    CfClasscontent newclasscontent = new CfClasscontent();
                    newclasscontent.setName(icp.getContentname());
                    newclasscontent.setClassref(clazz);
                    CfClasscontent newclasscontent2 = cfclasscontentService.create(newclasscontent);
                    List<CfAttribut> attributlist = cfattributService.findByClassref(newclasscontent2.getClassref());
                    attributlist.stream().forEach((attribut) -> {
                        if (attribut.getAutoincrementor() == true) {
                            List<CfClasscontent> classcontentlist2 = cfclasscontentService.findByClassref(newclasscontent2.getClassref());
                            long max = 0;
                            int last = classcontentlist2.size();
                            if (1 == last) {
                                max = 0;
                            } else {
                                CfClasscontent classcontent = classcontentlist2.get(last - 2);
                                CfAttributcontent attributcontent = cfattributcontentService.findByAttributrefAndClasscontentref(attribut, classcontent);        
                                if (attributcontent.getContentInteger().longValue() > max) {
                                    max = attributcontent.getContentInteger().longValue();
                                }
                            }
                            /*
                            for (CfClasscontent classcontent : classcontentlist2) {
                                try {
                                    CfAttributcontent attributcontent = cfattributcontentService.findByAttributrefAndClasscontentref(attribut, classcontent);
                                    if (attributcontent.getContentInteger().longValue() > max) {
                                        max = attributcontent.getContentInteger().longValue();
                                    }
                                } catch (javax.persistence.NoResultException ex2) {
                                    logger.error(ex2.getMessage());
                                }
                            }
                            */
                            CfAttributcontent newcontent = new CfAttributcontent();
                            newcontent.setAttributref(attribut);
                            newcontent.setClasscontentref(newclasscontent);
                            newcontent.setContentInteger(BigInteger.valueOf(max+1));
                            CfAttributcontent newcontent2 = cfattributcontentService.create(newcontent);
                            icp.getAttributmap().put(attribut.getName(), newcontent2.getContentInteger().toString());
                            icp.setReturncode("OK");
                        } else {
                            CfAttributcontent newcontent = new CfAttributcontent();
                            newcontent.setAttributref(attribut);
                            newcontent.setClasscontentref(newclasscontent);
                            newcontent = contentUtil.setAttributValue(newcontent, icp.getAttributmap().get(attribut.getName()));
                            
                            cfattributcontentService.create(newcontent);
                            if (icp.isIndexing()) {
                                contentUtil.indexContent();
                            }
                            icp.setReturncode("OK");
                        }
                    });
                }
            } else {
                icp.setReturncode("Wrong API KEY");
            }
        } catch (javax.persistence.NoResultException ex) {
            icp.setReturncode("NoResultException");
        }
        return icp;
    }
}
