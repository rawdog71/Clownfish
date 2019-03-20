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

import io.clownfish.clownfish.daointerface.CfStylesheetversionDAO;
import io.clownfish.clownfish.dbentities.CfStylesheetversion;
import io.clownfish.clownfish.serviceinterface.CfStylesheetversionService;
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
public class CfStylesheetversionServiceImpl implements CfStylesheetversionService {
    private final CfStylesheetversionDAO cfstylesheetversionDAO;
    
    @Autowired
    public CfStylesheetversionServiceImpl(CfStylesheetversionDAO cfstylesheetversionDAO) {
        this.cfstylesheetversionDAO = cfstylesheetversionDAO;
    }

    @Override
    public boolean create(CfStylesheetversion entity) {
        return this.cfstylesheetversionDAO.create(entity);
    }

    @Override
    public boolean delete(CfStylesheetversion entity) {
        return this.cfstylesheetversionDAO.delete(entity);
    }

    @Override
    public boolean edit(CfStylesheetversion entity) {
        return this.cfstylesheetversionDAO.edit(entity);
    }

    @Override
    public List<CfStylesheetversion> findAll() {
        return this.cfstylesheetversionDAO.findAll();
    }

    @Override
    public List<CfStylesheetversion> findByStylesheetref(long ref) {
        return this.cfstylesheetversionDAO.findByStylesheetref(ref);
    }

    @Override
    public long findMaxVersion(long ref) {
        return this.cfstylesheetversionDAO.findMaxVersion(ref);
    }

    @Override
    public CfStylesheetversion findByPK(long ref, long version) {
        return this.cfstylesheetversionDAO.findByPK(ref, version);
    }
}
