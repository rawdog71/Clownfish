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

import io.clownfish.clownfish.daointerface.CfAttributcontentDAO;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import java.util.List;
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
public class CfAttributcontentServiceImpl implements CfAttributcontentService {
    private final CfAttributcontentDAO cfattributcontentDAO;

    public CfAttributcontentServiceImpl(CfAttributcontentDAO cfattributcontentDAO) {
        this.cfattributcontentDAO = cfattributcontentDAO;
    }

    @Cacheable(value = "attributcontent")
    @Override
    public List<CfAttributcontent> findAll() {
        return this.cfattributcontentDAO.findAll();
    }

    //@CachePut(value = "attributcontent", key = "#entity.id")
    @Override
    public CfAttributcontent create(CfAttributcontent entity) {
        return this.cfattributcontentDAO.create(entity);
    }

    //@CacheEvict(value = "attributcontent", key = "#entity.id")
    @Override
    public boolean delete(CfAttributcontent entity) {
        return this.cfattributcontentDAO.delete(entity);
    }

    //@CachePut(value = "attributcontent", key = "#entity.id")
    @Override
    public CfAttributcontent edit(CfAttributcontent entity) {
        return this.cfattributcontentDAO.edit(entity);
    }

    //@Cacheable(value = "attributcontent", key = "#classcontentref.id")
    @Override
    public List<CfAttributcontent> findByClasscontentref(CfClasscontent classcontentref) {
        return this.cfattributcontentDAO.findByClasscontentref(classcontentref);
    }

    //@Cacheable(value = "attributcontent", key = "#attributref.id #classcontentref.id")
    @Override
    public CfAttributcontent findByAttributrefAndClasscontentref(CfAttribut attributref, CfClasscontent classcontentref) {
        return this.cfattributcontentDAO.findByAttributrefAndClasscontentref(attributref, classcontentref);
    }

    //@Cacheable(value = "attributcontent", key = "#indexed")
    @Override
    public List<CfAttributcontent> findByIndexed(boolean indexed) {
        return this.cfattributcontentDAO.findByIndexed(indexed);
    }

    //@Cacheable(value = "attributcontent", key = "#classcontentref.id")
    @Override
    public List<CfAttributcontent> findByContentclassRef(CfList classcontentref) {
        return this.cfattributcontentDAO.findByContentclassRef(classcontentref);
    }

    //@Cacheable(value = "attributcontent", key = "#assetcontentref.id")
    @Override
    public List<CfAttributcontent> findByContentAssetRef(CfAssetlist assetcontentref) {
        return this.cfattributcontentDAO.findByContentAssetRef(assetcontentref);
    }
}
