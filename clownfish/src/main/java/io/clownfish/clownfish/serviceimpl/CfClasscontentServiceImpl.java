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

import io.clownfish.clownfish.daointerface.CfClasscontentDAO;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author sulzbachr
 */
@Service
@Transactional
public class CfClasscontentServiceImpl implements CfClasscontentService {
    private final CfClasscontentDAO cfclasscontentDAO;

    public CfClasscontentServiceImpl(CfClasscontentDAO cfclasscontentDAO) {
        this.cfclasscontentDAO = cfclasscontentDAO;
    }

    @Override
    public List<CfClasscontent> findAll() {
        return this.cfclasscontentDAO.findAll();
    }

    @Override
    public CfClasscontent findById(Long id) {
        return this.cfclasscontentDAO.findById(id);
    }

    @Override
    public CfClasscontent findByName(String name) {
        return this.cfclasscontentDAO.findByName(name);
    }

    @Override
    public CfClasscontent create(CfClasscontent entity) {
        return this.cfclasscontentDAO.create(entity);
    }

    @Override
    public boolean delete(CfClasscontent entity) {
        return this.cfclasscontentDAO.delete(entity);
    }

    @Override
    public CfClasscontent edit(CfClasscontent entity) {
        return this.cfclasscontentDAO.edit(entity);
    }

    @Override
    public List<CfClasscontent> findByClassref(CfClass classref) {
        return this.cfclasscontentDAO.findByClassref(classref);
    }
    
}
