package com.sven.springbootmanager.main.service;

import com.sven.springbootmanager.main.bean.BootAppCell;
import com.sven.springbootmanager.main.controller.Controller;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import com.sven.springbootmanager.main.dao.PersistenceSaver;
import com.sven.springbootmanager.common.utils.*;
import com.sven.springbootmanager.main.view.AppListView;
import com.sven.springbootmanager.main.view.BootAppHBoxCell;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BootAppBinder {
    private Controller ctr;
    private static final int MAX_CONSOLE_TEXT = Integer.MAX_VALUE;
    private String serverPort = "8888";

    public void bind(Controller controller) {
        this.ctr = controller;
        initUI();
        refreshFields();
    }

    private void runCMDBatch(List<String> commands, File logFile) {
        ctr.consoleText.setText("");
        UIutils.runCMDBatch(commands, logFile, new UIutils.RunCmdCallBack() {
            private long time = System.currentTimeMillis();
            private String tempStr = "";
            private boolean isFirst = true;
            @Override
            public void onReadline(String str) {
                //利用时间间隔解决刷新频繁问题
                long curTime = System.currentTimeMillis();
                int timeSpace = (int) (curTime- time);
                tempStr += str;
                if(timeSpace > 1500 || isFirst){
                    isFirst = false;
                    time = curTime;
                    ctr.consoleText.setText(ctr.consoleText.getText() + tempStr);
                    tempStr = "";
                }
            }
        });
        String info = ctr.systemModel.getLogInfo();
        int maxSize = MAX_CONSOLE_TEXT;
        if (info != null && info.length() > maxSize) {
            info = info.substring(0, maxSize);
            ctr.systemModel.setLogInfo(info);
        }
        ctr.systemModel.setLogInfo(ctr.persistenceSaver.getSystemConsoleLog() + ctr.systemModel.getLogInfo());
    }

    private void initUI() {
        try {
            modifyBootAppConfigs();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ctr.bootAppDir.setText(PersistenceSaver.getBootAppRootPath());

        ctr.refreshAppListBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                refreshSpringBootAppList();
            }
        });

        ctr.springBootAppList.setOnItemClickListener(new AppListView.OnItemClickListener() {
            @Override
            public void onStatusButtonClick(BootAppHBoxCell cell, ActionEvent e) {
                new loadingTask<Void>(true) {
                    @Override
                    public Void onCall() {
                        if(BootAppCell.STATUS_RUNNING.equalsIgnoreCase(cell.getBootAppCell().getStatus())){
                            JavaTaskHelp.killTask(cell.getBootAppCell().getName());
                            Platform.runLater(new Runnable(){
                                @Override
                                public void run() {
                                    refreshSpringBootAppList();
                                }
                            });
                        }else if(BootAppCell.STATUS_STOPPED.equalsIgnoreCase(cell.getBootAppCell().getStatus())){
                            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                            Runnable task = new Runnable() {
                                @SuppressWarnings("AlibabaThreadPoolCreation")
                                @Override
                                public void run() {
                                    Platform.runLater(new Runnable(){
                                        @Override
                                        public void run() {
                                            refreshSpringBootAppList();
                                            ctr.loadingPanel.setVisible(false);
                                        }
                                    });
                                }
                            };
                            executor.schedule(task, 3, TimeUnit.SECONDS);
                            executor.shutdown();

                            File f = new File(cell.getBootAppCell().getPath());
                            List<String> cmmands = CommandCreator.cdCommand(f.getParentFile().getAbsolutePath());
                            cmmands = CommandCreator.addRunCommand(cmmands, f.getName());
                            runCMDBatch(cmmands, ctr.persistenceSaver.getFile(PersistenceSaver.SYSTEM_CONSOLE_TEMP_SAVE_PATH));
                        }
                        return null;
                    }
                }.excuteJob();
            }

            @Override
            public void onOpenUrlButtonClick(BootAppHBoxCell cell, ActionEvent e) {
                if(!cell.getBootAppCell().getStatus().equalsIgnoreCase(BootAppCell.STATUS_RUNNING)){
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warnning");
                    alert.setHeaderText(null);
                    alert.setContentText("Must run the spring boot app before opening the URL !");
                    alert.showAndWait();
                    return;
                }
                try {
                    Desktop.getDesktop().browse(new URL(cell.getBootAppCell().getUrl()).toURI());
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onPortButtonClick(BootAppHBoxCell cell, ActionEvent e) {
                BootAppCell cellData = cell.getBootAppCell();
                TextInputDialog dialog = new TextInputDialog(cellData.getPort());
                dialog.setTitle("Message");
                dialog.setHeaderText("Change server port");
                dialog.setContentText("Server port:");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()){
                    String port = result.get();
                    cellData.setPort(result.get());
                    if(!port.matches("-?\\d+(\\.\\d+)?")){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Server port is invalid !");
                        alert.showAndWait();
                        return;
                    }
                    cellData.setPort(port);
                    String url = "http://localhost:" + port + "/webs/security/login.html";
                    cellData.setUrl(url);
                    cell.refresh();
                }
            }
        });

        refreshSpringBootAppList();
        bindFields();
    }

    @SuppressWarnings("AlibabaAvoidPatternCompileInMethod")
    private void modifyBootAppConfigs() throws IOException {
        String path = PersistenceSaver.getBootAppRootPath() + "_conf/app-config.properties";
        File appConfigFile = new File(path);
        if(appConfigFile.exists()){
            Charset charset = StandardCharsets.UTF_8;
            String content = new String(Files.readAllBytes(Paths.get(path)), charset);

            Matcher m = Pattern.compile("server.port = .*").matcher(content);
            if (m.find()) {
                serverPort = m.group().replaceAll("server.port =\\s*", "");
            }
            String result = content.replaceAll("jdbc.sqlite.url=jdbc:sqlite:.*",
                    "jdbc.sqlite.url=jdbc:sqlite:" + PersistenceSaver.getBootAppRootPath() + "_conf/db/SourceGenerator.sqlite3");
            result = result.replaceAll("sql-code-templates.baseRoot = .*",
                    "sql-code-templates.baseRoot = " + PersistenceSaver.getBootAppRootPath() + "_conf/_Root/");

            Files.write(Paths.get(path), result.getBytes(charset));
        }
    }

    private void refreshSpringBootAppList() {
        List<BootAppCell> list = findSpringBootProcess();
        ctr.springBootAppList.loadData(list);
    }

    private List<BootAppCell> findSpringBootProcess() {
        List<BootAppCell> list = new ArrayList<>();
        File bootAppDir = new File(PersistenceSaver.DEFAULT_SPRING_BOOT_APP_DIR);
        if(!bootAppDir.isDirectory()){
            ctr.systemModel.setLogInfo("Error!  file: " + bootAppDir.getAbsolutePath() + " is not a folder.");
        }
        File[] bootJars = bootAppDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.substring(name.length() - 4, name.length()).equalsIgnoreCase(".jar")) {
                    ctr.systemModel.setLogInfo(ctr.systemModel.getLogInfo() + "\n" + name);
                    return true;
                }
                return false;
            }
        });
        if(bootJars == null){
            return list;
        }
        for (File f: bootJars) {
            BootAppCell cell = null;
            String url = "http://localhost:" + serverPort + "/webs/security/login.html";
            if(JavaTaskHelp.findProcess(f.getName()) != null){
                cell = new BootAppCell(f.getName(), f.getAbsolutePath(), BootAppCell.STATUS_RUNNING, BootAppCell.TEXT_STOP,
                        serverPort, url);
            }else {
                cell = new BootAppCell(f.getName(), f.getAbsolutePath(), BootAppCell.STATUS_STOPPED, BootAppCell.TEXT_RUN,
                        serverPort, url);
            }
            list.add(cell);
        }
        return list;
    }

    private void bindFields() {

        ctr.clearConsoleBtn.setOnAction((ActionEvent e) -> {
            ctr.systemModel.setLogInfo("");
            ctr.saveModels();
            refreshFields();
        });

        ctr.openCmdBtn.setOnAction((ActionEvent e) -> {
            new JobTask<Void>(){
                @Override
                public Void onCall() {
                    UIutils.runCMDBatch(new ArrayList<String>(){{add("start");}},
                            ctr.persistenceSaver.getFile(PersistenceSaver.SYSTEM_CONSOLE_TEMP_SAVE_PATH),
                            null);
                    return null;
                }
            }.excuteJob();
        });

    }

    private void refreshFields() {
        ctr.consoleText.setText(ctr.systemModel.getLogInfo());
    }

    private abstract class loadingTask<V> extends JobTask<V>{
        private boolean showLoading = true;

        public loadingTask() { }

        public loadingTask(boolean showLoading) {
            this.showLoading = showLoading;
        }

        @Override
        protected void onPreCall() {
            super.onPreCall();
            if(showLoading){
                ctr.loadingPanel.setVisible(true);
            }
        }

        @Override
        protected void onDone(V v) {
            super.onDone(v);
            ctr.loadingPanel.setVisible(false);
        }
    }
}
