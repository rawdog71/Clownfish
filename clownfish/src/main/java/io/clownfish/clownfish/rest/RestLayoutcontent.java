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

import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.datamodels.RestLayoutcontentParameter;
import io.clownfish.clownfish.dbentities.CfLayoutcontent;
import io.clownfish.clownfish.dbentities.CfLayoutcontentPK;
import io.clownfish.clownfish.serviceinterface.CfLayoutcontentService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.FolderUtil;
import java.math.BigInteger;
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
public class RestLayoutcontent {
    @Autowired transient CfLayoutcontentService cflayoutcontentService;
    @Autowired FolderUtil folderUtil;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestLayoutcontent.class);

    @PostMapping("/insertlayoutcontent")
    public RestLayoutcontentParameter restInsertLayoutcontent(@RequestBody RestLayoutcontentParameter ilcp) {
        return insertLayoutcontent(ilcp);
    }
    
    private RestLayoutcontentParameter insertLayoutcontent(RestLayoutcontentParameter ilcp) {
        try {
            String token = ilcp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ilcp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        CfLayoutcontent layoutcontent = cflayoutcontentService.findBySiterefAndTemplaterefAndContenttypeAndLfdnr(ilcp.getSiteref(), ilcp.getDivref(), ilcp.getContenttype(), ilcp.getLfdnr());
                        ilcp.setReturncode("EntityExistsException");
                    } catch (Exception ex) {
                        CfLayoutcontent layoutcontent = new CfLayoutcontent();
                        CfLayoutcontentPK cflayoutcontentPK = new CfLayoutcontentPK();
                        
                        cflayoutcontentPK.setSiteref(ilcp.getSiteref());
                        cflayoutcontentPK.setDivref(ilcp.getDivref());
                        cflayoutcontentPK.setContenttype(ilcp.getContenttype());
                        cflayoutcontentPK.setLfdnr(ilcp.getLfdnr());
                        layoutcontent.setCfLayoutcontentPK(cflayoutcontentPK);
                        layoutcontent.setPreview_contentref(BigInteger.valueOf(ilcp.getPreview_contentref()));
                        layoutcontent.setContentref(BigInteger.valueOf(ilcp.getContentref()));
                        cflayoutcontentService.create(layoutcontent);
                        ilcp.setReturncode("OK");
                    }
                } else {
                    ilcp.setReturncode("Wrong API KEY");
                }
            } else {
                ilcp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            ilcp.setReturncode("NoResultException");
        }
        return ilcp;
    }
    
    @PostMapping("/updatelayoutcontent")
    public RestLayoutcontentParameter restDeleteDatalist(@RequestBody RestLayoutcontentParameter ilcp) {
        return updateLayoutcontent(ilcp);
    }
    
    private RestLayoutcontentParameter updateLayoutcontent(RestLayoutcontentParameter ilcp) {
        try {
            String token = ilcp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ilcp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    try {
                        ilcp.setReturncode("");
                        CfLayoutcontent layoutcontent = cflayoutcontentService.findBySiterefAndTemplaterefAndContenttypeAndLfdnr(ilcp.getSiteref(), ilcp.getDivref(), ilcp.getContenttype(), ilcp.getLfdnr());
                        layoutcontent.setPreview_contentref(BigInteger.valueOf(ilcp.getPreview_contentref()));
                        layoutcontent.setContentref(BigInteger.valueOf(ilcp.getContentref()));
                        cflayoutcontentService.edit(layoutcontent);
                        ilcp.setReturncode("OK");
                    } catch (javax.persistence.EntityNotFoundException ex) {
                        ilcp.setReturncode("EntityNotFoundException");
                    }
                } else {
                    ilcp.setReturncode("Wrong API KEY");
                }
            } else {
                ilcp.setReturncode("Invalid token");
            }
        } catch (javax.persistence.NoResultException ex) {
            ilcp.setReturncode("NoResultException");
        }
        return ilcp;
    }
}
