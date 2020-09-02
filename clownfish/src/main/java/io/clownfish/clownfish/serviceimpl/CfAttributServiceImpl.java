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

import io.clownfish.clownfish.daointerface.CfAttributDAO;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
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
public class CfAttributServiceImpl implements CfAttributService {
    private final CfAttributDAO cfattributDAO;
    
    @Autowired
    public CfAttributServiceImpl(CfAttributDAO cfattributDAO) {
        this.cfattributDAO = cfattributDAO;
    }
    
    @Override
    public CfAttribut findById(Long id) {
        return this.cfattributDAO.findById(id);
    }
    
    @Override
    public boolean create(CfAttribut entity) {
        return this.cfattributDAO.create(entity);
    }

    @Override
    public boolean delete(CfAttribut entity) {
        return this.cfattributDAO.delete(entity);
    }

    @Override
    public boolean edit(CfAttribut entity) {
        return this.cfattributDAO.edit(entity);
    }

    @Override
    public List<CfAttribut> findByClassref(CfClass classref) {
        return this.cfattributDAO.findByClassref(classref);
    }

    @Override
    public CfAttribut findByNameAndClassref(String name, CfClass classref) {
        return this.cfattributDAO.findByNameAndClassref(name, classref);
    }

}
