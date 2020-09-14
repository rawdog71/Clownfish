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

import io.clownfish.clownfish.daointerface.CfAssetkeywordDAO;
import io.clownfish.clownfish.dbentities.CfAssetkeyword;
import io.clownfish.clownfish.serviceinterface.CfAssetKeywordService;
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
public class CfAssetKeywordServiceImpl implements CfAssetKeywordService {
    private final CfAssetkeywordDAO cfassetkeywordDAO;
    
    @Autowired
    public CfAssetKeywordServiceImpl(CfAssetkeywordDAO cfassetkeywordDAO) {
        this.cfassetkeywordDAO = cfassetkeywordDAO;
    }
    
    @Override
    public boolean create(CfAssetkeyword entity) {
        return this.cfassetkeywordDAO.create(entity);
    }

    @Override
    public boolean delete(CfAssetkeyword entity) {
        return this.cfassetkeywordDAO.delete(entity);
    }

    @Override
    public boolean edit(CfAssetkeyword entity) {
        return this.cfassetkeywordDAO.edit(entity);
    }    

    @Cacheable("assetkeyword")
    @Override
    public List<CfAssetkeyword> findByAssetRef(Long id) {
        return this.cfassetkeywordDAO.findByAssetRef(id);
    }

    @Cacheable("assetkeyword")
    @Override
    public List<CfAssetkeyword> findByKeywordRef(Long id) {
        return this.cfassetkeywordDAO.findByKeywordRef(id);
    }

    @Cacheable("assetkeyword")
    @Override
    public List<CfAssetkeyword> findAll() {
        return this.cfassetkeywordDAO.findAll();
    }
}
