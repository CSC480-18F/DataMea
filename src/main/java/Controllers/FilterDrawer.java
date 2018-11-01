package Controllers;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class FilterDrawer implements Initializable{

    @FXML
    public HBox filterHbox;

    @FXML
    public AnchorPane filtersAnchorPane;

    @FXML
    public JFXButton applyFilters;

    private static BooleanProperty filtersDrawerLoaded = new SimpleBooleanProperty(false);

    public static void setFiltersDrawerLoadedToTrue(){
        filtersDrawerLoaded.setValue(true);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb){

        //String chipViewStyleSheet = this.getClass().getClassLoader().getResource("chipview.css").toExternalForm();
        //filtersChipView.getStylesheets().add(chipViewStyleSheet);

        filtersDrawerLoaded.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    //Disable text area in pre-made chipview
                    /*Platform.runLater(()->{
                        Pane chipsPane = (Pane) filtersChipView.lookup(".jfx-chip-view .chips-pane");
                        TextArea textarea = (TextArea) filtersChipView.lookup(".jfx-chip-view .chips-pane > .text-area");
                        HBox chipHBox = (HBox) filtersChipView.lookup(".jfx-chip-view .jfx-chip > HBox");
                        //Label label = (Label) filtersChipView.lookup(".jfx-chip-view .jfx-chip > HBox > .label");
                        //label.setWrapText(false);
                        textarea.setDisable(true);
                        //textarea.setEditable(false);
                        //textarea.setCursor(Cursor.DEFAULT);
                        chipsPane.setCursor(Cursor.DEFAULT);
                    });*/
                }
            }
        });
    }
}
