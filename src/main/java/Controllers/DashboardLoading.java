package Controllers;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXProgressBar;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardLoading implements Initializable {
    @FXML
    WebView tutorialVideo;

    @FXML
    JFXCheckBox emailNotify;

    @FXML
    JFXProgressBar progressBar;

    //------------------Declaring Variables------------------//
    private static Stage myStage;
    private static BooleanProperty stopVideo = new SimpleBooleanProperty(false);

    public static void setStopVideoToTrue(){
        stopVideo.setValue(true);
    }

    public static void setStage(Stage stage) {
        myStage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tutorialVideo.getEngine().load(
                "https://www.youtube.com/embed/J---aiyznGQ?autoplay=1"
        );

        stopVideo.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    tutorialVideo.getEngine().load(null);
                }
            }
        });
    }
}
