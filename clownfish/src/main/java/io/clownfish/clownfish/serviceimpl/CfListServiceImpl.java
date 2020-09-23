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

import io.clownfish.clownfish.daointerface.CfListDAO;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.serviceinterface.CfListService;
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
public class CfListServiceImpl implements CfListService {
    private final CfListDAO cflistDAO;
    
    @Autowired
    public CfListServiceImpl(CfListDAO cflistDAO) {
        this.cflistDAO = cflistDAO;
    }

    @Cacheable(value = "list")
    @Override
    public List<CfList> findAll() {
        return this.cflistDAO.findAll();
    }

    @Cacheable(value = "list", key = "#id")
    @Override
    public CfList findById(Long id) {
        return this.cflistDAO.findById(id);
    }

    @CachePut(value = "list", key = "#entity.id")
    @Override
    public CfList create(CfList entity) {
        return this.cflistDAO.create(entity);
    }

    @CacheEvict(value = "list", key = "#entity.id")
    @Override
    public boolean delete(CfList entity) {
        return this.cflistDAO.delete(entity);
    }

    @CachePut(value = "list", key = "#entity.id")
    @Override
    public CfList edit(CfList entity) {
        return this.cflistDAO.edit(entity);
    }

    @Cacheable(value = "list", key = "#name")
    @Override
    public CfList findByName(String name) {
        return this.cflistDAO.findByName(name);
    }

    @Cacheable(value = "list", key = "#ref.id")
    @Override
    public List<CfList> findByClassref(CfClass ref) {
        return this.cflistDAO.findByClassref(ref);
    }

    @CacheEvict(value = "class", allEntries = true)
    @Override
    public void evictAll() {
    }
    
}
