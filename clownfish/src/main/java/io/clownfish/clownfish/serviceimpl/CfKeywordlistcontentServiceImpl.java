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
package io.clownfish.clownfish.serviceimpl;

import io.clownfish.clownfish.daointerface.CfKeywordlistcontentDAO;
import io.clownfish.clownfish.dbentities.CfKeywordlistcontent;
import io.clownfish.clownfish.serviceinterface.CfKeywordlistcontentService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author sulzbachr
 */
@Service
@Transactional
public class CfKeywordlistcontentServiceImpl implements CfKeywordlistcontentService {
    private final CfKeywordlistcontentDAO cfkeywordlistcontentDAO;
    
    @Autowired
    public CfKeywordlistcontentServiceImpl(CfKeywordlistcontentDAO cfkeywordlistcontentDAO) {
        this.cfkeywordlistcontentDAO = cfkeywordlistcontentDAO;
    }

    //@Cacheable(value = "keywordlistcontent")
    @Override
    public List<CfKeywordlistcontent> findAll() {
        return cfkeywordlistcontentDAO.findAll();
    }

    //@Cacheable(value = "keywordlistcontent", key = "#keywordlistref")
    @Override
    public List<CfKeywordlistcontent> findByKeywordlistref(long keywordlistref) {
        return cfkeywordlistcontentDAO.findByKeywordlistref(keywordlistref);
    }

    //@Cacheable(value = "keywordlistcontent", key = "#keywordref")
    @Override
    public List<CfKeywordlistcontent> findByKeywordref(long keywordref) {
        return cfkeywordlistcontentDAO.findByKeywordref(keywordref);
    }
    
    @Override
    public CfKeywordlistcontent findByKeywordrefAndKeywordlistref(long keywordref, long keywordlistref) {
        return cfkeywordlistcontentDAO.findByKeywordrefAndKeywordlistref(keywordref, keywordlistref);
    }

    //@CachePut(value = "keywordlistcontent", key = "#entity.cfKeywordlistcontentPK")
    @Override
    public CfKeywordlistcontent create(CfKeywordlistcontent entity) {
        return cfkeywordlistcontentDAO.create(entity);
    }

    //@CacheEvict(value = "keywordlistcontent", key = "#entity.cfKeywordlistcontentPK")
    @Override
    public boolean delete(CfKeywordlistcontent entity) {
        return cfkeywordlistcontentDAO.delete(entity);
    }

    //@CachePut(value = "keywordlistcontent", key = "#entity.cfKeywordlistcontentPK")
    @Override
    public CfKeywordlistcontent edit(CfKeywordlistcontent entity) {
        return cfkeywordlistcontentDAO.edit(entity);
    }
}
