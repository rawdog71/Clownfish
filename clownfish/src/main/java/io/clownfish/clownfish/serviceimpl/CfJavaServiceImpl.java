package io.clownfish.clownfish.serviceimpl;

import io.clownfish.clownfish.daointerface.CfJavaDAO;
import io.clownfish.clownfish.dbentities.CfJava;
import io.clownfish.clownfish.serviceinterface.CfJavaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Service
@Transactional
public class CfJavaServiceImpl implements CfJavaService, Serializable
{
    private transient final CfJavaDAO CfJavaDAO;

    @Autowired
    public CfJavaServiceImpl(CfJavaDAO cfpropertyDAO)
    {
        this.CfJavaDAO = cfpropertyDAO;
    }

    @Override
    public List<CfJava> findAll()
    {
        return this.CfJavaDAO.findAll();
    }

    @Override
    public CfJava create(CfJava entity)
    {
        return this.CfJavaDAO.create(entity);
    }

    @Override
    public boolean delete(CfJava entity)
    {
        return this.CfJavaDAO.delete(entity);
    }

    @Override
    public CfJava edit(CfJava entity)
    {
        return this.CfJavaDAO.edit(entity);
    }

    @Override
    public CfJava findById(Long id)
    {
        return this.CfJavaDAO.findById(id);
    }

    @Override
    public CfJava findByName(String name)
    {
        return this.CfJavaDAO.findByName(name);
    }
}
