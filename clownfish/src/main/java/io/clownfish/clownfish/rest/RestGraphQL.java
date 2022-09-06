/*
 * Copyright 2022 raine.
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
package io.clownfish.clownfish.rest;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.google.gson.Gson;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.graphql.GraphQLDataFetchers;
import io.clownfish.clownfish.graphql.GraphQLUtil;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author raine
 */
@RestController
public class RestGraphQL {
    private GraphQL graphQL;
    @Autowired GraphQLDataFetchers graphQLDataFetchers;
    @Autowired GraphQLUtil graphQLUtil;
    @Autowired private CfClassService cfclassservice;
    @Autowired private CfAttributService cfattributservice;
    @Autowired transient AuthTokenList authtokenlist;

    @RequestMapping(value = "/graphql", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String myGraphql(@RequestHeader("token") String token, @RequestBody String request) throws Exception {
        if (authtokenlist.checkValidToken(token)) {
        
            JSONObject jsonRequest = new JSONObject(request);
            JSONObject jsonVariables = jsonRequest.getJSONObject("variables");
            Map<String, Object> variables = new LinkedHashMap<>();
            for (String key : jsonVariables.keySet()) {
                variables.put(key, jsonVariables.opt(key));
            }
            ExecutionInput executionInput = ExecutionInput.newExecutionInput().query(jsonRequest.getString("query")).variables(variables).build();

            CfClass clazz = cfclassservice.findByName(graphQLUtil.getClassnameFromQuery(jsonRequest.getString("query")));
            String sdl = graphQLUtil.generateSchema(clazz.getName());

            GraphQLSchema graphQLSchema = buildSchema(clazz, sdl);
            GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
            ExecutionResult executionResult = build.execute(executionInput);

            Map<String, Object> toSpecificationResult = executionResult.toSpecification();
            Gson gson = new Gson();
            return gson.toJson(toSpecificationResult);
        } else {
            return null;
        }
    }
    
    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }
    
    private GraphQLSchema buildSchema(CfClass clazz, String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring(clazz);
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }
    
    private RuntimeWiring buildWiring() {
        RuntimeWiring.Builder rtw = RuntimeWiring.newRuntimeWiring();
        List<CfClass> classlist = cfclassservice.findAll();
        Map<String, DataFetcher> dataFetchersMap = new HashMap();
        for (CfClass clazz : classlist) {
            String fetchername = clazz.getName()+"All";
            dataFetchersMap.put(fetchername, graphQLDataFetchers.getDataByField(clazz.getName(), ""));
            List<CfAttribut> attributlist = cfattributservice.findByClassref(clazz);
            for (CfAttribut attribut : attributlist) {
                if (0 != attribut.getAttributetype().getName().compareToIgnoreCase("classref")) {
                    fetchername = clazz.getName()+"By"+attribut.getName().toUpperCase().charAt(0)+attribut.getName().substring(1);
                    dataFetchersMap.put(fetchername, graphQLDataFetchers.getDataByField(clazz.getName(), attribut.getName()));
                }
            }
        }
        return rtw.type(newTypeWiring("Query").dataFetchers(dataFetchersMap)).build();
    }
    
    private RuntimeWiring buildWiring(CfClass clazz) {
        RuntimeWiring.Builder rtw = RuntimeWiring.newRuntimeWiring();
        Map<String, DataFetcher> dataFetchersMap = new HashMap();
        String fetchername = clazz.getName()+"All";
        dataFetchersMap.put(fetchername, graphQLDataFetchers.getDataByField(clazz.getName(), ""));
        List<CfAttribut> attributlist = cfattributservice.findByClassref(clazz);
        for (CfAttribut attribut : attributlist) {
            if (0 != attribut.getAttributetype().getName().compareToIgnoreCase("classref")) {
                fetchername = clazz.getName()+"By"+attribut.getName().toUpperCase().charAt(0)+attribut.getName().substring(1);
                dataFetchersMap.put(fetchername, graphQLDataFetchers.getDataByField(clazz.getName(), attribut.getName()));
            }
        }
        return rtw.type(newTypeWiring("Query").dataFetchers(dataFetchersMap)).build();
    }
}
