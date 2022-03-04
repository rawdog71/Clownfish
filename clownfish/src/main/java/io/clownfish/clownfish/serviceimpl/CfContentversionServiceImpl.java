package io.clownfish.clownfish.serviceimpl;

import io.clownfish.clownfish.daointerface.CfContentversionDAO;
import io.clownfish.clownfish.dbentities.CfContentversion;
import io.clownfish.clownfish.serviceinterface.CfContentversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Service
@Transactional
public class CfContentversionServiceImpl implements CfContentversionService, Serializable
{
    private transient final CfContentversionDAO cfContentversionDAO;

    @Autowired
    public CfContentversionServiceImpl(CfContentversionDAO cfContentversionDAO)
    {
        this.cfContentversionDAO = cfContentversionDAO;
    }

    @Override
    public CfContentversion create(CfContentversion entity)
    {
        return this.cfContentversionDAO.create(entity);
    }

    @Override
    public boolean delete(CfContentversion entity)
    {
        return this.cfContentversionDAO.delete(entity);
    }

    @Override
    public CfContentversion edit(CfContentversion entity)
    {
        return this.cfContentversionDAO.edit(entity);
    }

    @Override
    public List<CfContentversion> findAll()
    {
        return this.cfContentversionDAO.findAll();
    }

    @Override
    public List<CfContentversion> findByContentref(long ref)
    {
        return this.cfContentversionDAO.findByContentref(ref);
    }

    @Override
    public long findMaxVersion(long ref)
    {
        return this.cfContentversionDAO.findMaxVersion(ref);
    }

    @Override
    public CfContentversion findByPK(long ref, long version)
    {
        return this.cfContentversionDAO.findByPK(ref, version);
    }
}
