package io.clownfish.clownfish.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Slf4j
public class InternalEndpointsFilter implements Filter {
    private final int internalPort;
    private final ArrayList<String> internalPaths;

    private final String BAD_REQUEST = String.format("{\"code\":%d,\"error\":true,\"errorMessage\":\"%s\"}",
            HttpStatus.BAD_REQUEST.value(), "This page is not accessible from this port.");

    public InternalEndpointsFilter(int internalPort, ArrayList<String> internalPaths) {
        this.internalPort = internalPort;
        this.internalPaths = internalPaths;
    }

    private boolean shouldBlock(ServletRequest request) {
        return !(request.getLocalPort() == internalPort);
    }

    private boolean isPathBlocked(String path) {
        for (String str : internalPaths) {
            if (str.equalsIgnoreCase(path))
                return true;
        }
        return false;
    }

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static String tryGetClientIP(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String uri = ((HttpServletRequest) servletRequest).getRequestURI();
        if (shouldBlock(servletRequest) && isPathBlocked(uri)) {
            log.info("Denying request to visit page " + uri + " from IP " + tryGetClientIP((HttpServletRequest) servletRequest));
            ((HttpServletResponse) servletResponse).setStatus(HttpStatus.BAD_REQUEST.value());
            servletResponse.getOutputStream().write(BAD_REQUEST.getBytes(StandardCharsets.UTF_8));
            servletResponse.getOutputStream().close();
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
