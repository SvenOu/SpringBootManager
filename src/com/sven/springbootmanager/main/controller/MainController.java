package com.sven.springbootmanager.main.controller;

import com.sven.springbootmanager.main.service.BootAppBinder;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import com.sven.springbootmanager.main.dao.TextFileDbManager;
import com.sven.springbootmanager.main.bean.BootAppCell;

import com.sven.springbootmanager.main.model.SystemModel;
import com.sven.springbootmanager.common.utils.JobTask;
import com.sven.springbootmanager.main.view.AppListView;

import java.util.logging.Logger;

public class MainController {
    /* view start*/
    public AppListView springBootAppList;
    public HBox loadingPanel;
    public Label bootAppDir;
    public Button refreshAppListBtn;
    public TabPane consoleTabPanel;

    /* view end*/
    private Logger logger =  Logger.getLogger(MainController.class.getName());

    private BootAppBinder cordovaBinder;
    public SystemModel systemModel;
    public TextFileDbManager textFileDb;

    @FXML
    private void initialize() {
        cordovaBinder = new BootAppBinder();
        textFileDb = new TextFileDbManager();

        systemModel = textFileDb.loadModelFromFile(SystemModel.class, TextFileDbManager.BOOT_APP_SAVE_PATH);
        if(null == systemModel){
            systemModel = new SystemModel();
        }
        init();

    }
    private void init() {
        cordovaBinder.bind(this);
    }

    public void saveModels() {
        new JobTask<Void>(){
            @Override
            public Void onCall() {
                textFileDb.saveModelToFile(systemModel, TextFileDbManager.BOOT_APP_SAVE_PATH);
                return null;
            }
        }.excuteJob();
    }
}
