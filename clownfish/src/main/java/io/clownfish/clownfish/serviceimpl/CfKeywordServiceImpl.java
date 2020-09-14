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

import io.clownfish.clownfish.daointerface.CfKeywordDAO;
import io.clownfish.clownfish.dbentities.CfKeyword;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author sulzbachr
 */
@Service
@Transactional
public class CfKeywordServiceImpl implements CfKeywordService {
    private final CfKeywordDAO cfkeywordDAO;
    
    @Autowired
    public CfKeywordServiceImpl(CfKeywordDAO cfkeywordDAO) {
        this.cfkeywordDAO = cfkeywordDAO;
    }
    
    @Override
    public boolean create(CfKeyword entity) {
        return this.cfkeywordDAO.create(entity);
    }

    @Override
    public boolean delete(CfKeyword entity) {
        return this.cfkeywordDAO.delete(entity);
    }

    @Override
    public boolean edit(CfKeyword entity) {
        return this.cfkeywordDAO.edit(entity);
    }    

    @Cacheable("keyword")
    @Override
    public List<CfKeyword> findAll() {
        return this.cfkeywordDAO.findAll();
    }

    @Cacheable("keyword")
    @Override
    public CfKeyword findById(Long id) {
        return this.cfkeywordDAO.findById(id);
    }

    @Cacheable("keyword")
    @Override
    public CfKeyword findByName(String name) {
        return this.cfkeywordDAO.findByName(name);
    }

    @Cacheable("keyword")
    @Override
    public List<CfKeyword> findByNameBeginning(String name) {
        return this.cfkeywordDAO.findByNameBeginning(name);
    }
}
