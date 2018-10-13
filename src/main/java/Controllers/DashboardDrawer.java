package Controllers;

import Engine.Main;
import com.jfoenix.controls.JFXListView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardDrawer implements Initializable{

    @FXML
    private JFXListView listView;

    @FXML
    public VBox dashboardDrawerVBox;

    //------------------Declaring Variables------------------//
    ObservableList<String> list = FXCollections.observableArrayList();
    private static BooleanProperty loadFolderList = new SimpleBooleanProperty(false);

    public static void setLoadFolderList(Boolean b){
        loadFolderList.setValue(b);
    }


    @Override
    public void initialize(URL url, ResourceBundle rb){
        loadFolderList.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    for (String f : Main.getFolders()){
                        list.add(f);
                    }
                    listView.setItems(list);
                }
            }
        });

        //listView.setExpanded(true);
        //listView.setVerticalGap(20.0);
    }

    public void expandListView(){
        listView.setExpanded(true);
        listView.setVerticalGap(20.0);
    }

    public void shrinkListView(){
        listView.setExpanded(false);
        listView.setVerticalGap(0.0);
    }
}
