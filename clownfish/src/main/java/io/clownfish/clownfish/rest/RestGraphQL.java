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

import com.github.openjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetcherFactory;
import graphql.schema.DataFetcherFactoryEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring.Builder;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.graphql.GraphQLDataFetchers;
import io.clownfish.clownfish.graphql.GraphQLUtil;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
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

    @RequestMapping(value = "/graphql", method = RequestMethod.POST)
    public Map<String, Object> myGraphql(@RequestBody String request) throws Exception {
        JSONObject jsonRequest = new JSONObject(request);

        String sdl = graphQLUtil.generateSchema();
        
        //URL url = Resources.getResource("schema.graphqls");
        //sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query(jsonRequest.getString("query")).build();
        ExecutionResult executionResult = build.execute(executionInput);

        return executionResult.toSpecification();
    }
    
/*
    @PostConstruct
    public void init() throws IOException {
        try {
            String sdl = graphQLUtil.generateSchema();
            GraphQLSchema graphQLSchema = buildSchema(sdl);
            this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }
    }
*/

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }
    
    private RuntimeWiring buildWiring() {
        RuntimeWiring.Builder rtw = RuntimeWiring.newRuntimeWiring();
        List<CfClass> classlist = cfclassservice.findAll();
        Map<String, DataFetcher> dataFetchersMap = new HashMap();
        for (CfClass clazz : classlist) {
            List<CfAttribut> attributlist = cfattributservice.findByClassref(clazz);
            for (CfAttribut attribut : attributlist) {
                if (0 != attribut.getAttributetype().getName().compareToIgnoreCase("classref")) {
                    String fetchername = clazz.getName().toLowerCase()+"By"+attribut.getName().toUpperCase().charAt(0)+attribut.getName().substring(1);
                    dataFetchersMap.put(fetchername, graphQLDataFetchers.getDataByField(clazz.getName(), attribut.getName()));
                }
            }
        }
        return rtw.type(newTypeWiring("Query").dataFetchers(dataFetchersMap)).build();
    }
}
