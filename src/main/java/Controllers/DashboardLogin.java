package Controllers;

import com.jfoenix.controls.*;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.mail.Session;
import javax.mail.Store;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class DashboardLogin implements Initializable {
    @FXML
    JFXTextField emailField;

    @FXML
    JFXPasswordField passwordField;

    @FXML
    StackPane stackPane;


    private static String email, password;
    private static Stage myStage;
    private BooleanProperty loginSuccessful = new SimpleBooleanProperty(false);

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
        Platform.runLater(() -> {
            try{
                Properties props = System.getProperties();
                props.setProperty("mail.store.protocol", "imaps");
                props.setProperty("mail.store.protocol", "imaps");
                Session session = Session.getDefaultInstance(props, null);
                Store store = session.getStore("imaps");
                store.connect("imap.gmail.com", this.getEmail(), this.getPassword());
                //User currentUser = new User(DashboardLogin.getEmail(), DashboardLogin.getPassword(), true);
                loginSuccessful.setValue(true);
            }
            catch(javax.mail.AuthenticationFailedException  e)
            {
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
                wrongInfo.show();
                //System.out.println("Sorry your credentials are incorrect, please try again");
            }catch(javax.mail.NoSuchProviderException f){
                f.printStackTrace();
            }catch(javax.mail.MessagingException g){
                g.printStackTrace();
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Parent homePageParent = FXMLLoader.load(getClass().getClassLoader().getResource("Dashboard_Home.fxml"));
            Scene homePage = new Scene(homePageParent);

            loginSuccessful.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
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
