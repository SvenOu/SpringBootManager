package com.sven.springbootmanager.main.dao;

import com.sven.springbootmanager.common.utils.Jackson2Util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyFileManager {
    private PropertyFileManager() { }
    private static class SingletonPatternHolder {
        private static final PropertyFileManager SINGLETON_PATTERN = new PropertyFileManager();
    }
    public static PropertyFileManager getInstance() {
        return SingletonPatternHolder.SINGLETON_PATTERN;
    }

    public <T> T loadProperty(Class<T> cls, String fileName) {
        Map data = new HashMap();
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(fileName);
            // load a properties file
            prop.load(input);

            // get the property value and print it out
            for(String key : prop.stringPropertyNames()) {
                String value = prop.getProperty(key);
                data.put(key, value);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Jackson2Util.objectToPojo(data, cls);
    }

    public void saveProperty(Object obj, String fileName) {
        Map data = Jackson2Util.objectToPojo(obj, Map.class);
        Properties prop = new Properties();
        OutputStream output = null;
        try {
            output = new FileOutputStream(fileName);
            // set the properties value
            for (Object key : data.keySet()) {
                Object value = data.get(key);
                prop.setProperty(key.toString(),
                        value.toString());
            }
            // save properties to project root folder
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
