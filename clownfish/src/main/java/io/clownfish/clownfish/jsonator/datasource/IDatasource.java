/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package io.clownfish.clownfish.jsonator.datasource;

/**
 *
 * @author SulzbachR
 */
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DatabaseDatasource.class, name = "database"),
    @JsonSubTypes.Type(value = WebserviceDatasource.class, name = "webservice"),
    @JsonSubTypes.Type(value = FileDatasource.class, name = "file")
})
public interface IDatasource {
    public String getName();
    public String getType();
    public String getConnection();
    public String getUser();
    public String getPassword();
    public String getFormat();
    public String getMethod();
    public Auth getAuth();
}
