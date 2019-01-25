package com.sven.springbootmanager.common.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UIutils {
    public static void showDialog(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
    public static Optional<String> showTextInputDialog(String defaultValue, String headerText, String contentText) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle("Message");
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        return dialog.showAndWait();
    }

    public static boolean isTextEmpty(String projectName) {
        if(null == projectName ||
                projectName.trim().length() <=0){
            return  true;
        }
        return false;
    }
    @SuppressWarnings("AlibabaThreadPoolCreation")
    public static void delayRun(Runnable runnable, int delay, TimeUnit timeUnit) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(runnable, delay, timeUnit);
        executor.shutdown();
    }

    public static void runCMDBatch(List<String> commands, File logFile, RunCmdCallBack callback)
    {
        StringBuilder commandStr = new StringBuilder(commands.get(0));
        for (int i =1; i<commands.size(); i++) {
            commandStr.append(" && ").append(commands.get(i));
        }
        if(callback != null){
            callback.onReadline(commandStr.toString() +"\n");
        }
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("cmd /c "+ commandStr);
            FileWriter fw = new FileWriter(logFile);
            fw.write(commandStr + "\n");
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(),"UTF-8"));
            String line = null;
            while((line=input.readLine()) != null) {
                String str = line + "\n";
                fw.write(str);
                if(callback != null){
                    callback.onReadline(str);
                }
            }
            fw.close();
            int exitVal = pr.waitFor();
            System.out.println("Exited with error code "+exitVal);
            pr.destroy();
        } catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
    public interface RunCmdCallBack{
        void onReadline(String str);
    }
}

