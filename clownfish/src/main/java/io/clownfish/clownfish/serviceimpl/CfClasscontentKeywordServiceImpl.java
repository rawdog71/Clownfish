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
    
    @Override
    public boolean create(CfClasscontentkeyword entity) {
        return this.cfclasscontentkeywordDAO.create(entity);
    }

    @Override
    public boolean delete(CfClasscontentkeyword entity) {
        return this.cfclasscontentkeywordDAO.delete(entity);
    }

    @Override
    public boolean edit(CfClasscontentkeyword entity) {
        return this.cfclasscontentkeywordDAO.edit(entity);
    }    

    @Override
    public List<CfClasscontentkeyword> findByClassContentRef(Long id) {
        return this.cfclasscontentkeywordDAO.findByClassContentRef(id);
    }

    @Override
    public List<CfClasscontentkeyword> findByKeywordRef(Long id) {
        return this.cfclasscontentkeywordDAO.findByKeywordRef(id);
    }

    @Override
    public List<CfClasscontentkeyword> findAll() {
        return this.cfclasscontentkeywordDAO.findAll();
    }
}
