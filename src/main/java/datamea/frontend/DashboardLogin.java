package datamea.frontend;

import datamea.backend.Main;
import datamea.backend.User;
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
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.mail.Session;
import javax.mail.Store;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.io.*;

public class DashboardLogin implements Initializable {
    @FXML
    JFXTextField emailField;

    @FXML
    JFXPasswordField passwordField;

    @FXML
    StackPane stackPane;

    @FXML
    JFXCheckBox rememberEmail;

    @FXML
    VBox vBox;

    @FXML
    Hyperlink cannotLogin;

    //------------------Declaring Variables------------------//
    private static String email, password;
    private static Stage myStage;
    private        BooleanProperty loginSuccessful = new SimpleBooleanProperty(false);
    private        boolean opened = false;
    private        static String lastEmail;
    boolean        firstLogin = false;
    private        Parent homePageParent;

    @FXML
    public void getEmailField(KeyEvent event) {
        email = emailField.getText();
    }

    @FXML
    public void getPasswordField(KeyEvent event) {
        password = passwordField.getText();
    }


    @FXML
    public void setEmail() {
        emailField.setText(lastEmail);
    }

    @FXML
    public void erasePreviousLogin(MouseEvent event){
        if (!rememberEmail.isSelected()) {
            String fileName = "TextFiles/lastLogin.txt";
            boolean exists = new File(fileName).isFile();
            if (exists) {
                (new File(fileName)).delete();
            }

        }
    }



    public void checkRememberedEmail(){
        rememberEmail.setSelected(true);
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

    public static void rememeberUserName(String userName, boolean rem) {

            String fileName = "TextFiles/lastLogin.txt";

            try {
                BufferedWriter br = new BufferedWriter(new FileWriter(fileName));
                if (rem) {
                    br.write(User.encrypt(userName));
                    br.close();
                } else {
                    br.write(">");
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("File does not exist");
            }

        }

    public boolean previousLoginRemembered() {
        String fileName = "TextFiles/lastLogin.txt";
        try{
            boolean exists = new File(fileName).isFile();
            if (!exists) {
                (new File(fileName)).createNewFile();
            }
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String last = br.readLine();

            if (last == null) {

                return false;
            } else {
                lastEmail = User.decrypt(last);
                email=User.decrypt(last);
                checkRememberedEmail();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
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
                        rememeberUserName(DashboardLogin.getEmail(),rememberEmail.isSelected());
                        loginSuccessful.setValue(true);
                        Main.setStartLoadingToTrue();
                    }
                    catch(javax.mail.AuthenticationFailedException  e) {
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
                    }catch (com.sun.mail.util.MailConnectException f){
                        BoxBlur blur = new BoxBlur(3,3,3);
                        JFXDialogLayout content = new JFXDialogLayout();
                        content.setHeading(new Text("No internet connection!"));
                        content.setBody(new Text("Please make sure you are connected to the internet and try again."));
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
                    }
                    catch(javax.mail.NoSuchProviderException g){
                        g.printStackTrace();
                    }catch(javax.mail.MessagingException h){
                        h.printStackTrace();
                    }
                });
            }
        }, 200, TimeUnit.MILLISECONDS);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        rememberEmail.setCursor(Cursor.HAND);
        if (previousLoginRemembered()) {
            setEmail();
            checkRememberedEmail();
        }
            DashboardLoading.setStage(myStage);

            cannotLogin.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
                BoxBlur blur = new BoxBlur(3,3,3);
                JFXDialogLayout content = new JFXDialogLayout();
                content.setHeading(new Text("Cannot Login?"));
                Hyperlink link = new Hyperlink("\n\n\n\nhttps://support.google.com/accounts/answer/185833");
                link.setBorder(Border.EMPTY);
                link.setPadding(new Insets(4, 0, 4, 0));
                link.addEventHandler(MouseEvent.MOUSE_PRESSED, (f) -> {
                    try {
                        Desktop.getDesktop().browse(new URI("https://support.google.com/accounts/answer/185833"));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                });
                content.setBody(new Text("Data Mea currently only works with a Gmail account, aka a Google Mail account.\nIf you have two step authentication enabled, please set a app password to login with DataMea at: "), link);
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
                    cannotLogin.setVisited(false);
                });
            });

            loginSuccessful.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        try {
                            if (User.existingUser(getEmail())) {
                                homePageParent = FXMLLoader.load(getClass().getClassLoader().getResource("Second_Loading_Screen.fxml"));
                            } else {
                                firstLogin = true;
                                homePageParent = FXMLLoader.load(getClass().getClassLoader().getResource("Loading_Screen.fxml"));
                            }
                        }catch(IOException io){
                            io.printStackTrace();
                        }

                        if (firstLogin){
                            DashboardLoading.setReadyLoadingScreenToTrue();
                            DashboardLoading.setLoadingOnCloseRequest(true);
                        }
                        //Scene homePage = new Scene(homePageParent,myStage.getWidth(), myStage.getHeight());
                        setScene(homePageParent);
                        myStage.show();
                        myStage.requestFocus();
                    }
                }
            });
    }

    public void setScene(Parent root) {
        Scene oldScene = myStage.getScene();
        myStage.setScene(oldScene == null
                ? new Scene(root, myStage.getMinWidth(), myStage.getMinHeight())
                : new Scene(root, oldScene.getWidth(), oldScene.getHeight()));
    }
}
