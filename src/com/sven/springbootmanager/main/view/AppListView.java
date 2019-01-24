package com.sven.springbootmanager.main.view;

import com.sven.springbootmanager.main.bean.BootAppCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.List;

public class AppListView extends ListView {
    private OnItemClickListener onItemClickListener;
    private List<BootAppCell> lastCellDataList;
    public void loadData(List<BootAppCell> list){
        lastCellDataList = list;
        refreshList();
    }

    public void refreshList() {
        if(lastCellDataList == null){
            return;
        }
        List<BootAppHBoxCell> cellList = new ArrayList<>();
        for(BootAppCell c: lastCellDataList){
            BootAppHBoxCell cell = new BootAppHBoxCell(c);
            cell.getButton().setOnAction((ActionEvent e) -> {
                if(onItemClickListener != null){
                    onItemClickListener.onStatusButtonClick(cell, e);
                }
            });
            cell.getOpenUrlButton().setOnAction((ActionEvent e) -> {
                if(onItemClickListener != null){
                    onItemClickListener.onOpenUrlButtonClick(cell, e);
                }
            });
            cell.getPortButton().setOnAction((ActionEvent e) -> {
                if(onItemClickListener != null){
                    onItemClickListener.onPortButtonClick(cell, e);
                }
            });
            cellList.add(cell);
        }
        ObservableList<BootAppHBoxCell> myObservableList = FXCollections.observableList(cellList);
        setItems(myObservableList);
    }

    public interface OnItemClickListener{
        void onStatusButtonClick(BootAppHBoxCell cell, ActionEvent e);
        void onOpenUrlButtonClick(BootAppHBoxCell cell, ActionEvent e);
        void onPortButtonClick(BootAppHBoxCell cell, ActionEvent e);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
