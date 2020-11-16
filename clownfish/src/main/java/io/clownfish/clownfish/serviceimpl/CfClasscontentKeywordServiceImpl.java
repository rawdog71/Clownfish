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


import io.clownfish.clownfish.daointerface.CfClasscontentkeywordDAO;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
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
public class CfClasscontentKeywordServiceImpl implements CfClasscontentKeywordService {
    private final CfClasscontentkeywordDAO cfclasscontentkeywordDAO;
    
    @Autowired
    public CfClasscontentKeywordServiceImpl(CfClasscontentkeywordDAO cfclasscontentkeywordDAO) {
        this.cfclasscontentkeywordDAO = cfclasscontentkeywordDAO;
    }
    
    //@CachePut(value = "classcontentkeyword", key = "#entity.id")
    @Override
    public CfClasscontentkeyword create(CfClasscontentkeyword entity) {
        return this.cfclasscontentkeywordDAO.create(entity);
    }

    //@CacheEvict(value = "classcontentkeyword", key = "#entity.id")
    @Override
    public boolean delete(CfClasscontentkeyword entity) {
        return this.cfclasscontentkeywordDAO.delete(entity);
    }

    //@CachePut(value = "classcontentkeyword", key = "#entity.id")
    @Override
    public CfClasscontentkeyword edit(CfClasscontentkeyword entity) {
        return this.cfclasscontentkeywordDAO.edit(entity);
    }    

    //@Cacheable(value = "classcontentkeyword", key = "#id")
    @Override
    public List<CfClasscontentkeyword> findByClassContentRef(Long id) {
        return this.cfclasscontentkeywordDAO.findByClassContentRef(id);
    }

    //@Cacheable(value = "classcontentkeyword", key = "#id")
    @Override
    public List<CfClasscontentkeyword> findByKeywordRef(Long id) {
        return this.cfclasscontentkeywordDAO.findByKeywordRef(id);
    }

    //@Cacheable(value = "classcontentkeyword")
    @Override
    public List<CfClasscontentkeyword> findAll() {
        return this.cfclasscontentkeywordDAO.findAll();
    }
}
