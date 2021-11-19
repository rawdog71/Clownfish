package io.clownfish.clownfish.serviceimpl;

import io.clownfish.clownfish.daointerface.CfJavaversionDAO;
import io.clownfish.clownfish.dbentities.CfJavaversion;
import io.clownfish.clownfish.serviceinterface.CfJavaversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Service
@Transactional
public class CfJavaversionServiceImpl implements CfJavaversionService, Serializable
{
    private transient final CfJavaversionDAO CfJavaversionDAO;

    @Autowired
    public CfJavaversionServiceImpl(CfJavaversionDAO CfJavaversionDAO)
    {
        this.CfJavaversionDAO = CfJavaversionDAO;
    }

    @Override
    public CfJavaversion create(CfJavaversion entity)
    {
        return this.CfJavaversionDAO.create(entity);
    }

    @Override
    public boolean delete(CfJavaversion entity)
    {
        return this.CfJavaversionDAO.delete(entity);
    }

    @Override
    public CfJavaversion edit(CfJavaversion entity)
    {
        return this.CfJavaversionDAO.edit(entity);
    }

    @Override
    public List<CfJavaversion> findAll()
    {
        return this.CfJavaversionDAO.findAll();
    }

    @Override
    public List<CfJavaversion> findByJavaref(long ref)
    {
        return this.CfJavaversionDAO.findByJavaref(ref);
    }

    @Override
    public long findMaxVersion(long ref)
    {
        return this.CfJavaversionDAO.findMaxVersion(ref);
    }

    @Override
    public CfJavaversion findByPK(long ref, long version)
    {
        return this.CfJavaversionDAO.findByPK(ref, version);
    }
}
