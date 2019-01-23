package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import main.PersistenceSaver;
import bean.BootAppCell;

import model.SystemModel;
import utils.JobTask;
import view.AppListView;

import java.util.logging.Logger;

public class Controller {
    /* view start*/
    public AppListView springBootAppList;
    public HBox loadingPanel;
    public TextArea consoleText;
    public Button clearConsoleBtn;
    public Button openCmdBtn;
    public Label bootAppDir;
    public Button refreshAppListBtn;

    /* view end*/
    private Logger logger =  Logger.getLogger(Controller.class.getName());

    private BootAppBinder cordovaBinder;
    public BootAppCell cordovaModel;
    public SystemModel systemModel;

    public PersistenceSaver persistenceSaver;

    @FXML
    private void initialize() {
        cordovaBinder = new BootAppBinder();
        persistenceSaver = new PersistenceSaver();
        cordovaModel = persistenceSaver.loadModel(BootAppCell.class, PersistenceSaver.BOOT_APP_SAVE_PATH);
        systemModel = persistenceSaver.loadModel(SystemModel.class, PersistenceSaver.SYSTEM_SAVE_PATH);
        init();

    }
    private void init() {
        cordovaBinder.bind(this);
    }

    public void saveModels() {
        new JobTask<Void>(){
            @Override
            public Void onCall() {
                persistenceSaver.saveModel(cordovaModel, PersistenceSaver.BOOT_APP_SAVE_PATH);
                persistenceSaver.saveModel(systemModel, PersistenceSaver.SYSTEM_SAVE_PATH);
                return null;
            }
        }.excuteJob();
    }

}
