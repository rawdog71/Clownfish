package io.clownfish.clownfish.daointerface;

import io.clownfish.clownfish.dbentities.CfJava;

import java.util.List;

public interface CfJavaDAO
{
    CfJava findById(Long id);
    CfJava findByName(String name);
    List<CfJava> findAll();
    CfJava create(CfJava entity);
    boolean delete(CfJava entity);
    CfJava edit(CfJava entity);
}
