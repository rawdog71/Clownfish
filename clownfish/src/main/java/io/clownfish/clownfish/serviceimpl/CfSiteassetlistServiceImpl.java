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

import io.clownfish.clownfish.daointerface.CfSiteassetlistDAO;
import io.clownfish.clownfish.dbentities.CfSiteassetlist;
import io.clownfish.clownfish.serviceinterface.CfSiteassetlistService;
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
public class CfSiteassetlistServiceImpl implements CfSiteassetlistService {
    private final CfSiteassetlistDAO cfsiteassetlistDAO;
    
    @Autowired
    public CfSiteassetlistServiceImpl(CfSiteassetlistDAO cfsiteassetlistDAO) {
        this.cfsiteassetlistDAO = cfsiteassetlistDAO;
    }

    @Override
    public List<CfSiteassetlist> findAll() {
        return this.cfsiteassetlistDAO.findAll();
    }
    
    @Override
    public List<CfSiteassetlist> findBySiteref(Long ref) {
        return this.cfsiteassetlistDAO.findBySiteref(ref);
    }

    @Override
    public CfSiteassetlist create(CfSiteassetlist entity) {
        return this.cfsiteassetlistDAO.create(entity);
    }

    @Override
    public boolean delete(CfSiteassetlist entity) {
        return this.cfsiteassetlistDAO.delete(entity);
    }

    @Override
    public CfSiteassetlist edit(CfSiteassetlist entity) {
        return this.cfsiteassetlistDAO.edit(entity);
    }

    @Override
    public List<CfSiteassetlist> findByAssetlistref(Long ref) {
        return this.cfsiteassetlistDAO.findByAssetlistref(ref);
    }
}
