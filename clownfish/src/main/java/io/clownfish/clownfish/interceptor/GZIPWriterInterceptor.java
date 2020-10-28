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
package io.clownfish.clownfish.interceptor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sulzbachr
 */
@Provider
@Compress
public class GZIPWriterInterceptor implements WriterInterceptor {
    private GzipSwitch gzipswitch;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(GZIPWriterInterceptor.class);
    
    @Override
    public void aroundWriteTo(WriterInterceptorContext responseContext) {
        try {
            MultivaluedMap<String,Object> headers = responseContext.getHeaders();
            headers.add("Content-Encoding", "gzip");

            final OutputStream outputStream = responseContext.getOutputStream();
            responseContext.setOutputStream(new GZIPOutputStream(outputStream));
            responseContext.proceed();
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
}
