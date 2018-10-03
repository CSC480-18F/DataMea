package Controllers;

import Engine.User;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardLoading implements Initializable {
    @FXML
    WebView tutorialVideo;

    @FXML
    JFXCheckBox emailNotify;

    @FXML
    JFXProgressBar progressBar;

    private static Stage myStage;
    //private ResourceLoadingTask task = new ResourceLoadingTask();
    //private User currentUser;

    public static void setStage(Stage stage) {
        myStage = stage;
    }

    /*public class ResourceLoadingTask extends Task<Void> {
        @Override
        protected Void call() throws Exception {
            currentUser = new User(DashboardLogin.getEmail(), DashboardLogin.getPassword(), false);
            System.out.println("Data Loaded");
            return null;
        }
    }*/

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tutorialVideo.getEngine().load(
                "https://www.youtube.com/embed/J---aiyznGQ?autoplay=1"
        );
        /*try {
            Thread t = new Thread(task);
            Pane homeScreen = FXMLLoader.load(getClass().getClassLoader().getResource("Dashboard_Home.fxml"));

            task.setOnSucceeded(e -> {
                Scene home = new Scene(homeScreen, 1000, 600);
                myStage.setScene(home);
                homeScreen.requestFocus();
            });
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
