package datamea.frontend;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsDrawer implements Initializable {
    @FXML
    public JFXButton resetButton;

    @FXML
    public JFXButton creditsButton;

    @FXML
    public VBox settingsDrawerVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        creditsButton.setOnAction((e) -> {
            Stage creditsStage = new Stage();
            creditsStage.setMinWidth(500);
            creditsStage.setMinHeight(500);
            try {
                Parent credits = FXMLLoader.load(this.getClass().getClassLoader().getResource("Credits.fxml"));
                creditsStage.setScene(new Scene(credits));
                creditsStage.show();
            } catch (IOException ex){
                ex.printStackTrace();
            }
        });
    }
}
