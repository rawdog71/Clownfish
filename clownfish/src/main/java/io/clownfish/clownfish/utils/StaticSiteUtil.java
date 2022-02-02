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
package io.clownfish.clownfish.utils;

import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author raine
 */
public class StaticSiteUtil {
    final static transient Logger LOGGER = LoggerFactory.getLogger(StaticSiteUtil.class);

    /**
     * generateStaticSite
     * 
     */
    public static void generateStaticSite(String sitename, String content, CfAssetService cfassetService, FolderUtil folderUtil) {
        FileOutputStream fileStream = null;
        try {
            Document doc = Jsoup.parse(content);
            Elements elem_images = doc.body().select("img");
            for (Element image : elem_images) {
                String src = image.attr("src");
                src = makeStaticImage(src, cfassetService);
                image.attr("src", src);
            }
            Elements elem_styles = doc.body().getElementsByAttribute("style");
            for (Element style : elem_styles) {
                String src = style.attr("style");
                if (src.contains("url")) {
                    src = makeStaticImageInStyle(src, cfassetService);
                    style.attr("style", src);
                }
            }
            /* UIKIT data-src */
            elem_images = doc.getElementsByAttribute("data-src");
            for (Element image : elem_images) {
                String src = image.attr("data-src");
                src = makeStaticImage(src, cfassetService);
                image.attr("data-src", src);
            }
            /* UIKIT data-srcset */
            elem_images = doc.getElementsByAttribute("data-srcset");
            for (Element image : elem_images) {
                String src = image.attr("data-srcset");
                src = makeStaticImage(src, ",", cfassetService);
                image.attr("data-srcset", src);
            }
            // Remove preview utils in preview
            Elements divs = doc.getElementsByClass("cf_div");
            for (Element div : divs) {
                div.removeClass("cf_div");
            }
            Elements links = doc.head().getElementsByAttribute("href");
            for (Element link : links) {
                if (0 == link.attr("href").compareToIgnoreCase("resources/css/cf_preview.css")) {
                    link.remove();
                }
            }
            Elements scripts = doc.head().getElementsByAttribute("src");
            for (Element script : scripts) {
                if (0 == script.attr("src").compareToIgnoreCase("resources/js/cf_preview.js")) {
                    script.remove();
                }
            }
            fileStream = new FileOutputStream(new File(folderUtil.getStatic_folder()+ File.separator + sitename));
            OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF-8");
            try {
                writer.write(doc.html());
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create the destination file", e);
            }
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            LOGGER.error(ex.getMessage());
        } finally {
            try {
                if (null != fileStream) {
                    fileStream.close();
                }
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }
    
    public static String makeStaticImage(String src, CfAssetService cfassetService) {
        if (src.contains("GetAsset?apikey=")) {
            String[] src_params = src.split("&");
            CfAsset asset = null;
            String width = "W0";
            String height = "H0";
            for (String param : src_params) {
                if (param.startsWith("file")) {
                    String[] file_params = param.split("=");
                    asset = cfassetService.findByName(file_params[1]);
                }
                if (param.startsWith("mediaid")) {
                    String[] media_params = param.split("=");
                    asset = cfassetService.findById(Long.parseLong(media_params[1]));
                }
                if (param.startsWith("width")) {
                    String[] width_params = param.split("=");
                    width = "W" + width_params[1];
                }
                if (param.startsWith("height")) {
                    String[] height_params = param.split("=");
                    height = "H" + height_params[1];
                }
            }
            if (null != asset) {
                if (asset.getMimetype().contains("svg")) {
                    src = "cache/cache" + asset.getName();
                } else {
                    src = "cache/cache" + asset.getName() + width + height;
                }
            }
        }
        return src;
    }
    
    private static String makeStaticImage(String src, String separator, CfAssetService cfassetService) {
        if (src.contains("GetAsset?apikey=")) {
            String[] datasources = src.split(separator);
            StringBuilder src_out = new StringBuilder();
            for (String datasource : datasources) {
                String[] datasource_components = datasource.trim().split(" ");
                String[] src_params = datasource_components[0].split("&");
                CfAsset asset = null;
                String width = "W0";
                String height = "H0";
                for (String param : src_params) {
                    if (param.startsWith("file")) {
                        String[] file_params = param.split("=");
                        asset = cfassetService.findByName(file_params[1]);
                    }
                    if (param.startsWith("mediaid")) {
                        String[] media_params = param.split("=");
                        asset = cfassetService.findById(Long.parseLong(media_params[1]));
                    }
                    if (param.startsWith("width")) {
                        String[] width_params = param.split("=");
                        width = "W" + width_params[1];
                    }
                    if (param.startsWith("height")) {
                        String[] height_params = param.split("=");
                        height = "H" + height_params[1];
                    }
                }
                if (null != asset) {
                    src_out = src_out.append("cache/cache").append(asset.getName());
                    if (!asset.getMimetype().contains("svg")) {
                        src_out.append(width).
                        append(height);
                    }
                    src_out.append(" ").
                    append(datasource_components[1]).
                    append(", ");
                }
            }
            src_out = src_out.delete(src_out.length() - 2, src_out.length());
            src = src_out.toString();
        }
        return src;
    }

    private static String makeStaticImageInStyle(String src, CfAssetService cfassetService) {
        String output = "";
        String[] semikolon_split = src.split(";");
        for (String semikolon_part : semikolon_split) {
            String[] colon_split = semikolon_part.split(":");
            String[] comma_split = colon_split[1].trim().split(",");
            String url = "";
            for (String comma_part : comma_split) {
                if (comma_part.contains("'")) {
                    url = comma_part.substring(comma_part.indexOf("'")+1, comma_part.lastIndexOf("'"));
                    url = makeStaticImage(url, cfassetService);
                    url = "url('" + url + "'),";
                }
                if (comma_part.contains("\"")) {
                    url = comma_part.substring(comma_part.indexOf("\"")+1, comma_part.lastIndexOf("\""));
                    url = makeStaticImage(url, cfassetService);
                    url = "url(\"" + url + "\"),";
                }
            }
            output += colon_split[0] + ": " + url.substring(0, url.length()-1) +"; ";
        }
        if (!output.isBlank()) {
            return output;
        } else {
            return src;
        }
    }
}
