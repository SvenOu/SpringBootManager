package com.sven.springbootmanager.main.service;

import com.sven.springbootmanager.common.utils.CommandCreator;
import com.sven.springbootmanager.common.utils.JavaProcessHelper;
import com.sven.springbootmanager.common.utils.JobTask;
import com.sven.springbootmanager.common.utils.UIutils;
import com.sven.springbootmanager.main.bean.BootAppCell;
import com.sven.springbootmanager.main.bean.SourceCodeGeneratorConfig;
import com.sven.springbootmanager.main.controller.MainController;
import com.sven.springbootmanager.main.dao.PropertyFileManager;
import com.sven.springbootmanager.main.dao.TextFileDbManager;
import com.sven.springbootmanager.main.model.LogInfo;
import com.sven.springbootmanager.main.view.AppListView;
import com.sven.springbootmanager.main.view.BootAppHBoxCell;
import com.sven.springbootmanager.main.view.ConsoleTab;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class BootAppBinder {
    private static final int MAX_CONSOLE_TEXT = Integer.MAX_VALUE;
    private PropertyFileManager propFileManager;
    private MainController ctr;
    private SourceCodeGeneratorConfig sCGeneratorConfig;

    public void bind(MainController controller) {
        this.ctr = controller;
        this.propFileManager = PropertyFileManager.getInstance();
        new LoadingTask<Void>(true){
            @Override
            public Void onCall() {
                doPreBackgroundWork();
                return null;
            }
            @Override
            protected void onDone(Void aVoid) {
                super.onDone(aVoid);
                initUI();
            }
        }.excuteJob();
    }

    private void doPreBackgroundWork() {
        modifyBootAppConfigs();
    }

    private void runCMDBatch(List<String> commands, File logFile, ConsoleTab consoleTab) {
        consoleTab.getConsoleText().setText("");
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
                    consoleTab.getConsoleText().setText(
                            consoleTab.getConsoleText().getText() + tempStr);
                    tempStr = "";
                }
            }
        });
        String owner = consoleTab.getId();
        String info = ctr.systemModel.findLogInfoByOwner(owner).getLogInfo();
        int maxSize = MAX_CONSOLE_TEXT;
        if (info != null && info.length() > maxSize) {
            info = info.substring(0, maxSize);
            ctr.systemModel.findLogInfoByOwner(owner).setLogInfo(info);
        }
        ctr.systemModel.findLogInfoByOwner(owner).setLogInfo(ctr.textFileDb.getConsoleLog(logFile)
                + ctr.systemModel.findLogInfoByOwner(owner).getLogInfo());
        ctr.saveModels();
    }

    private void initUI() {
        ctr.bootAppDir.setText(TextFileDbManager.getBootAppRootPath());
        syncRefreshSpringBootAppList(true);
        bindFields();
        loadConsoleFiles();
    }

    private void modifyBootAppConfigs() {
        String path = TextFileDbManager.getBootAppRootPath() + "_conf/app-config.properties";
        sCGeneratorConfig = propFileManager.loadProperty(SourceCodeGeneratorConfig.class, path);
        sCGeneratorConfig.setJdbcSqliteUrl("jdbc:sqlite:" + TextFileDbManager.getBootAppRootPath() + "_conf/db/SourceGenerator.sqlite3");
        sCGeneratorConfig.setSqlCodeTemplatesBaseRoot(TextFileDbManager.getBootAppRootPath() + "_conf/_Root/");
        propFileManager.saveProperty(sCGeneratorConfig, path);
    }
    private void saveBootAppConfigs(){
        String path = TextFileDbManager.getBootAppRootPath() + "_conf/app-config.properties";
        propFileManager.saveProperty(sCGeneratorConfig, path);
    }
    private void syncSaveBootAppConfigs(boolean showLoading){
        new LoadingTask<Void>(showLoading){
            @Override
            public Void onCall() {
                saveBootAppConfigs();
                return null;
            }
        }.excuteJob();
    }

    private void syncRefreshSpringBootAppList(boolean showLoading) {
        new LoadingTask<List<BootAppCell>>(showLoading){
            @Override
            public List<BootAppCell> onCall() {
                List<BootAppCell> list = findSpringBootProcess();
                return list;
            }

            @Override
            protected void onDone(List<BootAppCell> list) {
                super.onDone(list);
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        ctr.springBootAppList.loadData(list);
                        refreshTabs(list);
                    }
                });
            }
        }.excuteJob();
    }

    private void refreshTabs(List<BootAppCell> list) {
        if(list == null){
            return;
        }
        List<Tab> needAddRab = new ArrayList<>();
        List<Tab> needRemoveRab = new ArrayList<>();

        for(BootAppCell c: list) {
            boolean needAdd = true;
            for (Tab tab : ctr.consoleTabPanel.getTabs()) {
                if (c.getName().equals(tab.getId())) {
                    needAdd = false;
                    break;
                }
            }
            if(needAdd){
                ConsoleTab consoleTab = new ConsoleTab();
                consoleTab.setId(c.getName());
                consoleTab.setText(c.getName());
                consoleTab.getClearConsoleBtn().setOnAction((ActionEvent e) -> {
                    ctr.systemModel.findLogInfoByOwner(consoleTab.getText()).setLogInfo("");
                    ctr.saveModels();
                    loadConsoleFiles();
                });

                consoleTab.getOpenCmdBtn().setOnAction((ActionEvent e) -> {
                    new JobTask<Void>(){
                        @Override
                        public Void onCall() {
                            UIutils.runCMDBatch(new ArrayList<String>(){{add("start");}},
                                    ctr.textFileDb.getTabLogFile(consoleTab.getText()),
                                    null);
                            return null;
                        }
                    }.excuteJob();
                });
                needAddRab.add(consoleTab);
            }
        }
        for (Tab tab : ctr.consoleTabPanel.getTabs()) {
            boolean needRemove= true;
            for(BootAppCell c: list) {
                if (c.getName().equals(tab.getId())) {
                    needRemove = false;
                    break;
                }
            }
            if(needRemove){
                needRemoveRab.add(tab);
            }
        }
        ctr.consoleTabPanel.getTabs().removeAll(needRemoveRab.toArray(new Tab[needRemoveRab.size()]));
        ctr.consoleTabPanel.getTabs().addAll(needAddRab.toArray(new Tab[needAddRab.size()]));

        List<LogInfo> logInfos = ctr.systemModel.getLogInfos();
        if(null == logInfos){
            logInfos = new ArrayList<>();
            ctr.systemModel.setLogInfos(logInfos);
        }
        for (Tab tab : ctr.consoleTabPanel.getTabs()) {
            if(null == ctr.systemModel.findLogInfoByOwner(tab.getText())){
                LogInfo newLogInfo = new LogInfo();
                newLogInfo.setOwner(tab.getText());
                newLogInfo.setLogInfo("");
                logInfos.add(newLogInfo);
            }
        }
    }

    private List<BootAppCell> findSpringBootProcess() {
        List<BootAppCell> list = new ArrayList<>();
        File bootAppDir = new File(TextFileDbManager.DEFAULT_SPRING_BOOT_APP_DIR);
        if(!bootAppDir.isDirectory()){
            System.out.print("Error!  file: " + bootAppDir.getAbsolutePath() + " is not a folder.");
        }
        File[] bootJars = bootAppDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.substring(name.length() - 4, name.length()).equalsIgnoreCase(".jar")) {
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
            String url = String.format(SourceCodeGeneratorConfig.URL_FORMATER, sCGeneratorConfig.getServerPort());
            if(JavaProcessHelper.findProcess(f.getName()) != null){
                cell = new BootAppCell(f.getName(), f.getAbsolutePath(), BootAppCell.STATUS_RUNNING, BootAppCell.TEXT_STOP,
                        sCGeneratorConfig.getServerPort(), url);
            }else {
                cell = new BootAppCell(f.getName(), f.getAbsolutePath(), BootAppCell.STATUS_STOPPED, BootAppCell.TEXT_RUN,
                        sCGeneratorConfig.getServerPort(), url);
            }
            list.add(cell);
        }
        return list;
    }

    private void bindFields() {
        ctr.refreshAppListBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                syncRefreshSpringBootAppList(true);
            }
        });

        ctr.springBootAppList.setOnItemClickListener(new AppListView.OnItemClickListener() {
            @Override
            public void onStatusButtonClick(BootAppHBoxCell cell, ActionEvent e) {
                new LoadingTask<Void>(true) {
                    @Override
                    public Void onCall() {
                        if(BootAppCell.STATUS_RUNNING.equalsIgnoreCase(cell.getBootAppCell().getStatus())){
                            JavaProcessHelper.killProcess(cell.getBootAppCell().getName());
                            syncRefreshSpringBootAppList(true);
                        }else if(BootAppCell.STATUS_STOPPED.equalsIgnoreCase(cell.getBootAppCell().getStatus())){
                            ConsoleTab tab = findConsoleTabByName(cell.getBootAppCell().getName());
                            Runnable task = new Runnable() {
                                @Override
                                public void run() {
//                                    String owner = cell.getBootAppCell().getName();
//                                    LogInfo logInfo = ctr.systemModel.findLogInfoByOwner(owner);
//                                    logInfo.setLogInfo(logInfo + tab.getConsoleText().getText());
//                                    ctr.saveModels();
                                    syncRefreshSpringBootAppList(true);
                                }
                            };
                            UIutils.delayRun(task, 3, TimeUnit.SECONDS);
                            File f = new File(cell.getBootAppCell().getPath());
                            List<String> cmmands = CommandCreator.cdCommand(f.getParentFile().getAbsolutePath());
                            cmmands = CommandCreator.addRunCommand(cmmands, f.getName());
                            ctr.consoleTabPanel.getSelectionModel().select(tab);
                            runCMDBatch(cmmands, ctr.textFileDb.getTabLogFile(cell.getBootAppCell().getName()), tab);
                        }
                        return null;
                    }
                }.excuteJob();
            }

            @Override
            public void onOpenUrlButtonClick(BootAppHBoxCell cell, ActionEvent e) {
                if(!cell.getBootAppCell().getStatus().equalsIgnoreCase(BootAppCell.STATUS_RUNNING)){
                    UIutils.showDialog("Must run the spring boot app before opening the URL !");
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
                Optional<String> result = UIutils.showTextInputDialog(cellData.getPort(),
                        "Change server port", "Server port:");
                if (result.isPresent()){
                    String port = result.get();
                    cellData.setPort(result.get());
                    if(!port.matches("-?\\d+(\\.\\d+)?")){
                        UIutils.showDialog("Server port is invalid !");
                        return;
                    }
                    cellData.setPort(port);
                    String url = String.format(SourceCodeGeneratorConfig.URL_FORMATER, port);
                    cellData.setUrl(url);
                    cell.refresh();
                    sCGeneratorConfig.setServerPort(port);
                    syncSaveBootAppConfigs(true);
                }
            }
        });

//        ctr.consoleTabPanel.getSelectionModel().selectedItemProperty().addListener(
//                new ChangeListener<Tab>() {
//                    @Override
//                    public void changed(ObservableValue<? extends Tab> ov, Tab t, Tab t1) {
//                        refreshFields();
//                    }
//                }
//        );
    }

    private void loadConsoleFiles() {
//        ConsoleTab curTab = getCurrentConsoleTab();
//        if(curTab != null){
//            LogInfo logInfo = ctr.systemModel.findLogInfoByOwner(curTab.getText());
//            if(logInfo != null){
//                curTab.getConsoleText().setText(logInfo.getLogInfo());
//            }
//        }
    }
    private ConsoleTab getCurrentConsoleTab(){
        return (ConsoleTab) ctr.consoleTabPanel.getSelectionModel().getSelectedItem();
    }

    private ConsoleTab findConsoleTabByName(String tabId){
        ObservableList<Tab> tabs = ctr.consoleTabPanel.getTabs();
        if(null == tabs){
            return null;
        }
        for (Tab tab : ctr.consoleTabPanel.getTabs()) {
            if(tab.getId().equals(tabId)){
                return (ConsoleTab) tab;
            }
        }
        return null;
    }

    private abstract class LoadingTask<V> extends JobTask<V>{
        private boolean showLoading = true;

        public LoadingTask() { }

        public LoadingTask(boolean showLoading) {
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
