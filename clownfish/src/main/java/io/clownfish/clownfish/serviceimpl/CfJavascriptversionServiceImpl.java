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

import io.clownfish.clownfish.daointerface.CfJavascriptversionDAO;
import io.clownfish.clownfish.dbentities.CfJavascriptversion;
import io.clownfish.clownfish.serviceinterface.CfJavascriptversionService;
import java.io.Serializable;
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
public class CfJavascriptversionServiceImpl implements CfJavascriptversionService, Serializable {
    private transient final CfJavascriptversionDAO cfjavascriptversionDAO;
    
    @Autowired
    public CfJavascriptversionServiceImpl(CfJavascriptversionDAO cfjavascriptversionDAO) {
        this.cfjavascriptversionDAO = cfjavascriptversionDAO;
    }

    @Override
    public boolean create(CfJavascriptversion entity) {
        return this.cfjavascriptversionDAO.create(entity);
    }

    @Override
    public boolean delete(CfJavascriptversion entity) {
        return this.cfjavascriptversionDAO.delete(entity);
    }

    @Override
    public boolean edit(CfJavascriptversion entity) {
        return this.cfjavascriptversionDAO.edit(entity);
    }

    @Override
    public List<CfJavascriptversion> findAll() {
        return this.cfjavascriptversionDAO.findAll();
    }

    @Override
    public List<CfJavascriptversion> findByJavascriptref(long ref) {
        return this.cfjavascriptversionDAO.findByJavascriptref(ref);
    }

    @Override
    public long findMaxVersion(long ref) {
        return this.cfjavascriptversionDAO.findMaxVersion(ref);
    }

    @Override
    public CfJavascriptversion findByPK(long ref, long version) {
        return this.cfjavascriptversionDAO.findByPK(ref, version);
    }
}
