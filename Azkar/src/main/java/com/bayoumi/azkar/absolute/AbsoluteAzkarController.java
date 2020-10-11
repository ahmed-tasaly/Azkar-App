package com.bayoumi.azkar.absolute;

import com.bayoumi.datamodel.AbsoluteZekr;
import com.bayoumi.util.gui.button.TableViewButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class AbsoluteAzkarController implements Initializable {

    @FXML
    private JFXTextField newZekr;
    @FXML
    private JFXTreeTableView<AbsoluteZekr> azkarTable;
    @FXML
    private TreeTableColumn<AbsoluteZekr, String> zekrCol;
    @FXML
    private TreeTableColumn<AbsoluteZekr, TableViewButton> editCol;
    @FXML
    private TreeTableColumn<AbsoluteZekr, TableViewButton> deleteCol;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableConfiguration();
        AbsoluteZekr.fetchData();
    }

    private void tableConfiguration() {
        zekrCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("text"));
        zekrCol.setStyle("-fx-alignment: center;");
        editCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("edit"));
        editCol.setStyle("-fx-alignment: center;");
        deleteCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("delete"));
        deleteCol.setStyle("-fx-alignment: center;");
        TreeItem<AbsoluteZekr> root = new RecursiveTreeItem<>(AbsoluteZekr.absoluteZekrObservableList, RecursiveTreeObject::getChildren);
        azkarTable.setRoot(root);
        azkarTable.setShowRoot(false);
    }

    @FXML
    private void newZekrAddAction() {
        if (!newZekr.getText().trim().equals("")) {
            AbsoluteZekr zekr = new AbsoluteZekr(0, newZekr.getText().trim());
            zekr.insert();
            AbsoluteZekr.fetchData();
            newZekr.setText("");
        }
    }
}
