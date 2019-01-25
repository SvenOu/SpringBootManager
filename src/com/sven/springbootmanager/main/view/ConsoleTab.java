package com.sven.springbootmanager.main.view;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;

import java.io.IOException;

public class ConsoleTab extends Tab {
    @FXML private TextArea consoleText;
    @FXML private Button openCmdBtn;
    @FXML private Button clearConsoleBtn;

    public ConsoleTab() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "console_tab.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public ConsoleTab(String text) {
        super(text);
    }

    public ConsoleTab(String text, Node content) {
        super(text, content);
    }

    public TextArea getConsoleText() {
        return consoleText;
    }

    public Button getOpenCmdBtn() {
        return openCmdBtn;
    }

    public Button getClearConsoleBtn() {
        return clearConsoleBtn;
    }
}
