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
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Session;
import java.net.URL;
import java.util.Properties;
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
    //---------Declaring Variables for Email Sending---------//
    private static String appEmail_Username = "datameaapp@gmail.com";
    private static String appEmail_Password = "CSC480HCI521";
    private static  String appEmail_Recipient;

    public static void setStopVideoToTrue(){
        stopVideo.setValue(true);
    }

    public static void setStage(Stage stage) {
        myStage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        appEmail_Recipient = DashboardLogin.getEmail();
        String from = appEmail_Username;
        String pass = appEmail_Password;
        String to = appEmail_Recipient;
        String subject = "Java send mail example";
        String body = "Welcome to JavaMail!";
        sendFromGMail(from, pass, to, subject, body);
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

        emailNotify.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                sendFromGMail(appEmail_Username, appEmail_Password, appEmail_Recipient, "DataMea Ready", "Body Stuff");
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
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
    }
}