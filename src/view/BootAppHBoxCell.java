package view;


import bean.BootAppCell;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;


public class BootAppHBoxCell extends HBox {
    private Label path = new Label();
    private Label statusLabel = new Label();
    private Button openUrlButton = new Button();
    private Button portButton = new Button();
    private Button button = new Button();
    private BootAppCell bootAppCell;

    public BootAppHBoxCell(BootAppCell bootAppCell) {
        super();
        this.bootAppCell = bootAppCell;
        statusLabel.setStyle("-fx-text-fill: red;");
        path.setPrefWidth(400);
        path.setMaxWidth(400);
        path.setWrapText(true);
        HBox.setHgrow(path, Priority.ALWAYS);
        button.setPrefWidth(88);
        HBox.setMargin(statusLabel, new Insets(0, 10, 0, 10));
        HBox.setMargin(portButton, new Insets(0, 10, 0, 10));
        HBox.setMargin(openUrlButton, new Insets(0, 10, 0, 10));
        getChildren().addAll(statusLabel, path, openUrlButton, portButton, button);

        refresh();
    }
    public void refresh(){
        path.setText(bootAppCell.getPath());
        statusLabel.setText(bootAppCell.getStatus());
        button.setText(bootAppCell.getButtonText());
        portButton.setText("Server port: " + bootAppCell.getPort());
        openUrlButton.setText(bootAppCell.getUrl());
    }

    public Label getPath() {
        return path;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }

    public Button getOpenUrlButton() {
        return openUrlButton;
    }

    public Button getPortButton() {
        return portButton;
    }

    public Button getButton() {
        return button;
    }

    public BootAppCell getBootAppCell() {
        return bootAppCell;
    }
}