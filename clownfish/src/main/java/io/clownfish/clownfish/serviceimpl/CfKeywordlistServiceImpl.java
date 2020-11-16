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

import io.clownfish.clownfish.daointerface.CfKeywordlistDAO;
import io.clownfish.clownfish.dbentities.CfKeywordlist;
import io.clownfish.clownfish.serviceinterface.CfKeywordlistService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author sulzbachr
 */
@Service
@Transactional
public class CfKeywordlistServiceImpl implements CfKeywordlistService {
    private final CfKeywordlistDAO cfkeywordlistDAO;
    
    @Autowired
    public CfKeywordlistServiceImpl(CfKeywordlistDAO cfkeywordlistDAO) {
        this.cfkeywordlistDAO = cfkeywordlistDAO;
    }

    //@Cacheable(value = "keywordlist")
    @Override
    public List<CfKeywordlist> findAll() {
        return cfkeywordlistDAO.findAll();
    }

    //@Cacheable(value = "keywordlist", key = "#id")
    @Override
    public CfKeywordlist findById(Long id) {
        return cfkeywordlistDAO.findById(id);
    }

    //@Cacheable(value = "keywordlist", key = "#name")
    @Override
    public CfKeywordlist findByName(String name) {
        return cfkeywordlistDAO.findByName(name);
    }

    //@CachePut(value = "keywordlist", key = "#entity.id")
    @Override
    public CfKeywordlist create(CfKeywordlist entity) {
        return cfkeywordlistDAO.create(entity);
    }

    //@CacheEvict(value = "keywordlist", key = "#entity.id")
    @Override
    public boolean delete(CfKeywordlist entity) {
        return cfkeywordlistDAO.delete(entity);
    }

    //@CachePut(value = "keywordlist", key = "#entity.id")
    @Override
    public CfKeywordlist edit(CfKeywordlist entity) {
        return cfkeywordlistDAO.edit(entity);
    }

}
