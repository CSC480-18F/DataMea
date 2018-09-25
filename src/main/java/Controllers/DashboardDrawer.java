package Controllers;

import com.jfoenix.controls.JFXListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardDrawer implements Initializable{

    @FXML
    private JFXListView listView;

    ObservableList<String> list = FXCollections.observableArrayList("All","Folder 1","Folder 2", "Folder 3");

    @Override
    public void initialize(URL url, ResourceBundle rb){
        listView.setItems(list);
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
