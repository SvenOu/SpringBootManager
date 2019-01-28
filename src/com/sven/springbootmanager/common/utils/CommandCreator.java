package com.sven.springbootmanager.common.utils;

import java.util.ArrayList;
import java.util.List;

public class CommandCreator {

    public static List<String> cdCommand(String path){
        List<String> cmds = new ArrayList<>();
        OsCheck.OSType ostype= OsCheck.getOperatingSystemType();
        if(ostype == OsCheck.OSType.Windows){
            cmds.add(path.substring(0,2));
        }
        cmds.add("cd "+ path);
        return cmds;
    }

    public static String getDiliver(){
        OsCheck.OSType ostype= OsCheck.getOperatingSystemType();
        switch (ostype) {
            case Windows: return "\\";
            case MacOS: return "/";
        }
        throw new RuntimeException("不支持此系统");
//        OsCheck.OSType ostype= OsCheck.getOperatingSystemType();
//        switch (ostype) {
//            case Windows: break;
//            case MacOS: break;
//            case Linux: break;
//            case Other: break;
//        }
    }

    public static List<String> addRunCommand(List<String> cmds, String name) {
        cmds.add("java -Dfile.encoding=utf-8 -jar " +name);
        return cmds;
    }

//    public static List<String> runAndroidCommand(List<String> cmds) {
//        cmds.add(getIonicPreFix()+"cordova run android");
//        return cmds;
//    }
}
