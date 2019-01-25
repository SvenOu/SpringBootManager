package com.sven.springbootmanager.main.dao;

import com.sven.springbootmanager.common.utils.Jackson2Util;

import java.io.*;

import static java.lang.System.getProperty;

public class TextFileDbManager {

    public static final String DEFAULT_SPRING_BOOT_APP_DIR = "SpringBootApps/";
    public static final String MANAGER_FILES_DIR = "manager-files/";
    public static final String BOOT_APP_SAVE_PATH =  MANAGER_FILES_DIR + "boot-app.txt";
    private static final String TAB_CONSOLE_SAVE_PATH_FORMATOR = MANAGER_FILES_DIR + "%s-console.txt";

    public static String getBootAppRootPath(){
        String path = System.getProperty("user.dir") + "\\" + DEFAULT_SPRING_BOOT_APP_DIR;
        return path.replaceAll("\\\\","/");
    }

    public void saveModelToFile(Object model, String fileName) {
        Jackson2Util.objWriteToFile(model, getFile(fileName));
    }

    public File getTabLogFile(String tabName){
        String path = String.format(TAB_CONSOLE_SAVE_PATH_FORMATOR, tabName);
        return getFile(path);
    }
    public File getFile(String filePath){
        File baseDir = new File(MANAGER_FILES_DIR);
        if(!baseDir.exists()){
            baseDir.mkdirs();
        }
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

    public String getConsoleLog(File file){
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

    public <T> T loadModelFromFile(Class<T> cls, String fileName) {
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
