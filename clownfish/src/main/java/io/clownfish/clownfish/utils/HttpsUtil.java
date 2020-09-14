/*
 * Copyright 2020 SulzbachR.
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
package io.clownfish.clownfish.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 *
 * @author SulzbachR
 */
@Component
public class HttpsUtil implements HandlerInterceptor {
    private @Getter @Setter int serverPortHttps;
    private @Getter @Setter int serverPortHttp;

    public HttpsUtil() {
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        int requestedPort = request.getServerPort(); // if you're not behind a proxy
        //String requestedPort = request.getHeader("X-Forwarded-Port"); // I'm behind a proxy on Heroku

        if (requestedPort == serverPortHttp) { // This will still allow requests on :8080
            response.sendRedirect("https://" + request.getServerName() + ":"  + serverPortHttps + request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
            return false;
        }
        return true;
    }
}
