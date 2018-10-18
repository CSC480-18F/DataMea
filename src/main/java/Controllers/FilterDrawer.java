package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.util.ResourceBundle;

public class FilterDrawer implements Initializable{

    @FXML
    public HBox filterHbox;

    //ObservableList<String> list = FXCollections.observableArrayList("All","Folder 1","Folder 2", "Folder 3");

    @Override
    public void initialize(URL url, ResourceBundle rb){
        //listView.setItems(list);
        //listView.setExpanded(true);
        //listView.setVerticalGap(20.0);
    }
}
