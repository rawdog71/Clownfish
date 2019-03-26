/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.interceptor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

/**
 *
 * @author sulzbachr
 */
@Provider
@Compress
public class GZIPWriterInterceptor implements WriterInterceptor {
    private GzipSwitch gzipswitch;
    
    @Override
    public void aroundWriteTo(WriterInterceptorContext responseContext) {
        try {
            MultivaluedMap<String,Object> headers = responseContext.getHeaders();
            headers.add("Content-Encoding", "gzip");

            final OutputStream outputStream = responseContext.getOutputStream();
            responseContext.setOutputStream(new GZIPOutputStream(outputStream));
            responseContext.proceed();
        } catch (IOException ex) {
            Logger.getLogger(GZIPWriterInterceptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
