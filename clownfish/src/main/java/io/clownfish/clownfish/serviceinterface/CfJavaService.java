package io.clownfish.clownfish.serviceinterface;

import io.clownfish.clownfish.dbentities.CfJava;

import java.util.List;

public interface CfJavaService
{
    List<CfJava> findAll();
    CfJava findById(Long id);
    CfJava findByName(String name);
    CfJava create(CfJava entity);
    boolean delete(CfJava entity);
    CfJava edit(CfJava entity);
}
