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


import io.clownfish.clownfish.daointerface.CfSitekeywordlistDAO;
import io.clownfish.clownfish.dbentities.CfSitekeywordlist;
import io.clownfish.clownfish.serviceinterface.CfSitekeywordlistService;
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
public class CfSitekeywordlistServiceImpl implements CfSitekeywordlistService {
    private final CfSitekeywordlistDAO cfsitekeywordlistDAO;
    
    @Autowired
    public CfSitekeywordlistServiceImpl(CfSitekeywordlistDAO cfsitekeywordlistDAO) {
        this.cfsitekeywordlistDAO = cfsitekeywordlistDAO;
    }

    @Override
    public List<CfSitekeywordlist> findAll() {
        return this.cfsitekeywordlistDAO.findAll();
    }
    
    @Override
    public List<CfSitekeywordlist> findBySiteref(Long ref) {
        return this.cfsitekeywordlistDAO.findBySiteref(ref);
    }

    @Override
    public CfSitekeywordlist create(CfSitekeywordlist entity) {
        return this.cfsitekeywordlistDAO.create(entity);
    }

    @Override
    public boolean delete(CfSitekeywordlist entity) {
        return this.cfsitekeywordlistDAO.delete(entity);
    }

    @Override
    public CfSitekeywordlist edit(CfSitekeywordlist entity) {
        return this.cfsitekeywordlistDAO.edit(entity);
    }

    @Override
    public List<CfSitekeywordlist> findByKeywordlistref(Long ref) {
        return this.cfsitekeywordlistDAO.findByKeywordlistref(ref);
    }
}
