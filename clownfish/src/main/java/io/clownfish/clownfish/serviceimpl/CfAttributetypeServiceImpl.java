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

import io.clownfish.clownfish.daointerface.CfAttributetypeDAO;
import io.clownfish.clownfish.dbentities.CfAttributetype;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
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
public class CfAttributetypeServiceImpl implements CfAttributetypeService {
    private final CfAttributetypeDAO cfattributetypeDAO;
    
    @Autowired
    public CfAttributetypeServiceImpl(CfAttributetypeDAO cfattributetypeDAO) {
        this.cfattributetypeDAO = cfattributetypeDAO;
    }
    
    //@Cacheable(value = "attributtype", key = "#id")
    @Override
    public CfAttributetype findById(Long id) {
        return this.cfattributetypeDAO.findById(id);
    }
    
    //@CachePut(value = "attributtype", key = "#entity.id")
    @Override
    public CfAttributetype create(CfAttributetype entity) {
        return this.cfattributetypeDAO.create(entity);
    }

    //@CacheEvict(value = "attributtype", key = "#entity.id")
    @Override
    public boolean delete(CfAttributetype entity) {
        return this.cfattributetypeDAO.delete(entity);
    }

    //@CachePut(value = "attributtype", key = "#entity.id")
    @Override
    public CfAttributetype edit(CfAttributetype entity) {
        return this.cfattributetypeDAO.edit(entity);
    }

    //@Cacheable(value = "attributtype")
    @Override
    public List<CfAttributetype> findAll() {
        return this.cfattributetypeDAO.findAll();
    }

    //@Cacheable(value = "attributtype", key = "#name")
    @Override
    public CfAttributetype findByName(String name) {
        return this.cfattributetypeDAO.findByName(name);
    }
    
}
