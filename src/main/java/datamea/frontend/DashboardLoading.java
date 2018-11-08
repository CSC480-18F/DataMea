package datamea.frontend;

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
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Session;
import java.io.File;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class DashboardLoading implements Initializable {
    @FXML
    private WebView tutorialVideo;

    @FXML
    private JFXCheckBox emailNotify;

    @FXML
    public JFXProgressBar progressBar;

    @FXML
    private StackPane loadingStackPane;

    @FXML
    private VBox loadingVbox;

    //------------------Declaring Variables------------------//
    private static Stage myStage;
    private static BooleanProperty stopVideo = new SimpleBooleanProperty(false);
    //---------Declaring Variables for Email Sending---------//
    private static String appEmail_Username = "datameaapp@gmail.com";
    private static String appEmail_Password = "CSC480HCI521";
    private static String appEmail_Recipient;
    private static BooleanProperty readyLoadingScreen = new SimpleBooleanProperty(false);
    private static BooleanProperty loadingOnCloseRequest = new SimpleBooleanProperty(false);


    public static void setStopVideoToTrue() {
        stopVideo.setValue(true);
    }

    public static void setReadyLoadingScreenToTrue() {
        readyLoadingScreen.setValue(true);
    }

    public static void setLoadingOnCloseRequest(Boolean b) {
        loadingOnCloseRequest.setValue(b);
    }

    public static void setStage(Stage stage) {
        myStage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadingOnCloseRequest.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    myStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {
                            //you have to run event.consume() so the window doesn't close automatically before showing the warning dialog
                            event.consume();
                            BoxBlur blur = new BoxBlur(3,3,3);
                            JFXDialogLayout content = new JFXDialogLayout();
                            content.setHeading(new Text("All progress will be lost!"));
                            content.setBody(new Text("Are you sure you want to exit Data Mea, all progress will be lost. Click \"Okay\" to exit or \"Cancel\" to continue."));
                            JFXDialog wrongInfo = new JFXDialog(loadingStackPane, content, JFXDialog.DialogTransition.CENTER);
                            JFXButton okay = new JFXButton("Okay");
                            JFXButton cancel = new JFXButton("Cancel");
                            okay.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    wrongInfo.close();
                                    deleteFolder(new File("TextFiles/"));
                                    Platform.exit();
                                    System.exit(0);
                                    //Delete TextFiles here
                                }
                            });
                            cancel.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    wrongInfo.close();
                                }
                            });
                            content.setActions(okay,cancel);
                            loadingVbox.setEffect(blur);
                            wrongInfo.show();
                            wrongInfo.setOnDialogClosed((JFXDialogEvent closedEvent) -> {
                                loadingVbox.setEffect(null);
                            });
                            }
                    });
                }
            }
        });


        tutorialVideo.getEngine().load(
                "https://www.youtube.com/embed/J---aiyznGQ?autoplay=1"
        );

        emailNotify.setCursor(Cursor.HAND);

        readyLoadingScreen.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {

                    appEmail_Recipient = DashboardLogin.getEmail();

                    emailNotify.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            //sendFromGMail(appEmail_Username, appEmail_Password, appEmail_Recipient, "DataMea Ready", "Body Stuff");
                        }
                    });
                }
            }
        });

        stopVideo.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    tutorialVideo.getEngine().load(null);
                }
            }
        });
    }

    private static void sendFromGMail(String from, String pass, String to, String subject, String body) {
        Properties properties = System.getProperties();
        String host = "smtp.gmail.com";
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.user", from);
        properties.put("mail.smtp.password", pass);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(properties);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(from));
            InternetAddress toAddress = new InternetAddress(to);

            message.setSubject(subject);
            message.setText(body);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (AddressException ae) {
            ae.printStackTrace();
        } catch (MessagingException me) {
            me.printStackTrace();
        }
    }

    public void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}