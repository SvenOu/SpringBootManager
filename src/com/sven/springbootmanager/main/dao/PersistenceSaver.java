package com.sven.springbootmanager.main.dao;

import com.sven.springbootmanager.common.utils.Jackson2Util;

import java.io.*;

import static java.lang.System.getProperty;

public class PersistenceSaver {

    public static final String DEFAULT_SPRING_BOOT_APP_DIR = "SpringBootApps/";
    public static final String BOOT_APP_SAVE_PATH = "boot-app.txt";
    public static final String SYSTEM_SAVE_PATH = "system.txt";
    public static final String SYSTEM_CONSOLE_TEMP_SAVE_PATH = "system-console.txt";

    public static String getBootAppRootPath(){
        String path = System.getProperty("user.dir") + "\\" + DEFAULT_SPRING_BOOT_APP_DIR;
        return path.replaceAll("\\\\","/");
    }

    public void saveModel(Object model, String fileName) {
        Jackson2Util.objWriteToFile(model, getFile(fileName));
    }

    public File getFile(String filePath){
        File file = new File(filePath);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public String getSystemConsoleLog(){
        File file = getFile(SYSTEM_CONSOLE_TEMP_SAVE_PATH);
        String log ="";
        FileReader in = null;
        try {
            in = new FileReader(file.getAbsolutePath());
            BufferedReader br = new BufferedReader(in);
            String line = br.readLine();
            while (line!=null) {
                System.out.println(line);
                log += (line + "\n") ;
                line = br.readLine();
            }
            log +="\n----------------------------------\n";
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return log;
    }

    public <T> T  loadModel(Class<T> cls, String fileName) {
        T t = Jackson2Util.readObjFromFile(cls, getFile(fileName));
        if(null == t){
            try {
                t = cls.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return t;
    }
}
