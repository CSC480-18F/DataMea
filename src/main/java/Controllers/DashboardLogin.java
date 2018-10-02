package Controllers;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardLogin implements Initializable {
    @FXML
    JFXTextField emailField;

    @FXML
    JFXPasswordField passwordField;

    private static String email, password;
    private BooleanProperty correctLogin = new SimpleBooleanProperty();

    @FXML
    public void getEmail(KeyEvent event) {
        email = emailField.getText();
    }

    @FXML
    public void getPassword(KeyEvent event) {
        password = passwordField.getText();
    }

    public static String getEmail() {
        return email;
    }

    public static String getPassword() {
        return password;
    }

    public void login(ActionEvent event) throws IOException {
        Parent homePageParent = FXMLLoader.load(getClass().getClassLoader().getResource("Dashboard_Home.fxml"));
        Scene homePage = new Scene(homePageParent);
        Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        appStage.setScene(homePage);
        Platform.runLater(() -> {
            appStage.show();
            appStage.requestFocus();
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /*correctLogin.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {

                }
            }
        });*/
    }
}
