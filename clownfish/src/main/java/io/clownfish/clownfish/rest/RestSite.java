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
import io.clownfish.clownfish.datamodels.RestSiteParameter;
import io.clownfish.clownfish.dbentities.CfLayoutcontent;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.serviceinterface.CfLayoutcontentService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.FolderUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author SulzbachR
 */
@RestController
public class RestSite {
    @Autowired transient CfSiteService cfsiteService;
    @Autowired transient CfLayoutcontentService cflayoutcontentService;
    @Autowired FolderUtil folderUtil;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestSite.class);

    @PostMapping(value = "/publishsite", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestSiteParameter restPublishSite(@RequestBody RestSiteParameter ilcp) {
        return publishsite(ilcp);
    }
    
    private RestSiteParameter publishsite(RestSiteParameter ilcp) {
        try {
            String token = ilcp.getToken();
            if (authtokenlist.checkValidToken(token)) {
                String apikey = ilcp.getApikey();
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfSite site = cfsiteService.findByName(ilcp.getSite());
                    if (null != folderUtil.getStatic_folder()) {
                        File file = new File(folderUtil.getStatic_folder() + File.separator + site.getName());
                        try {
                            Files.deleteIfExists(file.toPath());
                        } catch (IOException ex) {
                            LOGGER.error(ex.getMessage());
                        }
                    }

                    List<CfLayoutcontent> layoutcontentlist = cflayoutcontentService.findBySiteref(site.getId());
                    for (CfLayoutcontent layoutcontent : layoutcontentlist) {
                        layoutcontent.setContentref(layoutcontent.getPreview_contentref());
                        cflayoutcontentService.edit(layoutcontent);
                    }
                    ilcp.setReturncode("OK");
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
