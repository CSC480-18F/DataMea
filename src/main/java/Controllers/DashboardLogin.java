package Controllers;

import Engine.Main;
import com.jfoenix.controls.*;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.mail.Session;
import javax.mail.Store;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DashboardLogin implements Initializable {
    @FXML
    JFXTextField emailField;

    @FXML
    JFXPasswordField passwordField;

    @FXML
    StackPane stackPane;

    @FXML
    VBox vBox;

    //------------------Declaring Variables------------------//
    private static String email, password;
    private static Stage myStage;
    private        BooleanProperty loginSuccessful = new SimpleBooleanProperty(false);
    private        boolean opened = false;

    @FXML
    public void getEmailField(KeyEvent event) {
        email = emailField.getText();
    }

    @FXML
    public void getPasswordField(KeyEvent event) {
        password = passwordField.getText();
    }

    public static String getEmail() {
        return email;
    }

    public static String getPassword() {
        return password;
    }

    public static void setStage(Stage stage) {
        myStage = stage;
    }

    public void login(ActionEvent event) throws IOException {
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    try{
                        Properties props = System.getProperties();
                        props.setProperty("mail.store.protocol", "imaps");
                        props.setProperty("mail.store.protocol", "imaps");
                        Session session = Session.getDefaultInstance(props, null);
                        Store store = session.getStore("imaps");
                        store.connect("imap.gmail.com", DashboardLogin.getEmail(), DashboardLogin.getPassword());
                        //User currentUser = new User(DashboardLogin.getEmail(), DashboardLogin.getPassword(), true);
                        loginSuccessful.setValue(true);
                        Main.setStartLoadingToTrue();
                    }
                    catch(javax.mail.AuthenticationFailedException  e)
                    {
                        BoxBlur blur = new BoxBlur(3,3,3);
                        JFXDialogLayout content = new JFXDialogLayout();
                        content.setHeading(new Text("Incorrect Login!"));
                        content.setBody(new Text("Please check your email/password and try again."));
                        JFXDialog wrongInfo = new JFXDialog(stackPane,content, JFXDialog.DialogTransition.CENTER);
                        JFXButton button =  new JFXButton("Okay");
                        button.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                wrongInfo.close();
                            }
                        });
                        content.setActions(button);
                        if (!opened) {
                            wrongInfo.show();
                            vBox.setEffect(blur);
                            opened = true;
                        }
                        wrongInfo.setOnDialogClosed((JFXDialogEvent closedEvent) -> {
                            vBox.setEffect(null);
                            opened = false;
                        });
                    }catch(javax.mail.NoSuchProviderException f){
                        f.printStackTrace();
                    }catch(javax.mail.MessagingException g){
                        g.printStackTrace();
                    }
                });
            }
        }, 200, TimeUnit.MILLISECONDS);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            DashboardLoading.setStage(myStage);
            Parent homePageParent = FXMLLoader.load(getClass().getClassLoader().getResource("Loading_Screen.fxml"));
            Scene homePage = new Scene(homePageParent);

            loginSuccessful.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        DashboardLoading.setReadyLoadingScreenToTrue();
                        myStage.setScene(homePage);
                        myStage.show();
                        myStage.requestFocus();
                    }
                }
            });
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
