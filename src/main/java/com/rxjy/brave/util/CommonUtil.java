package com.rxjy.brave.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by 11019 on 17.8.21.
 */
public class CommonUtil {
    public static String getPath(String file, String key) {
        InputStream inputStream = CommonUtil.class.getResourceAsStream("/" + file);
        if(inputStream==null){
            return null;
        }
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return properties.getProperty(key);
    }
}
