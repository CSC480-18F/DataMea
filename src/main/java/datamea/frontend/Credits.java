package datamea.frontend;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class Credits implements Initializable {
    @FXML
    ScrollPane creditsScrollpane;

    @FXML
    Hyperlink github;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String scrollPaneCss = this.getClass().getClassLoader().getResource("darker_scrollpane.css").toExternalForm();
        creditsScrollpane.getStylesheets().add(scrollPaneCss);

        github.setBorder(Border.EMPTY);
        github.setPadding(new Insets(4, 0, 4, 0));
        github.addEventHandler(MouseEvent.MOUSE_PRESSED, (f) -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/CSC480-18F/DataMea/"));
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
            }
        });


        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        KeyValue kv = new KeyValue(creditsScrollpane.vvalueProperty(), creditsScrollpane.getVmax());
        KeyFrame kf = new KeyFrame(Duration.seconds(30), kv);
        timeline.getKeyFrames().addAll(kf);
        timeline.play();
    }
}
