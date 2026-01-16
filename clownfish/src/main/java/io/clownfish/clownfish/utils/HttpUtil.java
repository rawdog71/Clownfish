package io.clownfish.clownfish.utils;

import io.clownfish.clownfish.datamodels.ClientInformation;
import org.springframework.stereotype.Component;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.beans.factory.annotation.Value;

@Component
public class HttpUtil {
    
    @Value("${server.name:Clownfish Server Open Source}") 
    String servername;
    @Value("${server.x-powered:Clownfish Server Open Source by Rainer Sulzbach}") 
    String serverxpowered;

    public ClientInformation getClientInformation(String ip) {
        ClientInformation ci = new ClientInformation();
        ci.setIpadress(ip);
        try {
            InetAddress addr = InetAddress.getByName(ip);
            ci.setHostname(addr.getHostName());
        } catch (UnknownHostException ex) {
            // Logging optional
        }
        return ci;
    }

    public String getCookieVal(Cookie[] cookies, String key) {
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                if (0 == cookie.getName().compareToIgnoreCase(key)) {
                    return cookie.getValue();
                }
            }
        }
        return "";
    }
    
    public void addHeader(HttpServletResponse response, String version) {
        String serverString = servername.replaceAll("#version#", version);
        String serverxpowerdedString = serverxpowered.replaceAll("#version#", version);
        response.addHeader("Server", serverString);
        response.addHeader("X-Powered-By", serverxpowerdedString);
    }
}