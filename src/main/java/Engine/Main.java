package Engine;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import Controllers.*;
import eu.hansolo.tilesfx.chart.ChartData;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import static Controllers.DashboardController.setLoadedFromLoginScreenToTrue;

public class Main extends Application {

    //------------------Declaring Variables------------------//
    private static User                     currentUser;
    private        Main.ResourceLoadingTask task         = new Main.ResourceLoadingTask();
    private static BooleanProperty          startLoading = new SimpleBooleanProperty(false);
    private static ArrayList<UserFolder>    folders;
    private        ArrayList<Color>         colors       = new ArrayList<>();

    public static ArrayList<UserFolder> getFolders() {
        return folders;
    }

    public static void setStartLoadingToTrue(){
        startLoading.setValue(true);
    }

    public static User getCurrentUser() { return currentUser; }

    public class ResourceLoadingTask extends Task<Void> {
        @Override
        protected Void call() throws Exception {
            currentUser = new User(DashboardLogin.getEmail(), DashboardLogin.getPassword(), false);
            System.out.println("Data Loaded");
            folders = currentUser.recoverFolders();
            return null;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(600);
        primaryStage.setTitle("Data Mea");
        Pane root = FXMLLoader.load(getClass().getClassLoader().getResource("Login_Screen.fxml"));
        primaryStage.setScene(new Scene(root));
        root.requestFocus();
        primaryStage.show();
        DashboardLogin.setStage(primaryStage);
        DashboardController.setStage(primaryStage);


        colors.add(Color.valueOf("#fc5c65"));
        colors.add(Color.valueOf("#fd9644"));
        colors.add(Color.valueOf("#fed330"));
        colors.add(Color.valueOf("#26de81"));
        colors.add(Color.valueOf("#2bcbba"));

        startLoading.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    try {
                        Thread t = new Thread(task);
                        Pane homeScreen = FXMLLoader.load(getClass().getClassLoader().getResource("Dashboard_Home.fxml"));

                        task.setOnSucceeded(e -> {
                            //Add top senders data to Radial Chart
                            //if (currentUser.getFolders().get(0).getSenders().size() < 5) {
                            String folderName = currentUser.recoverFolders().get(0).folderName;
                            int numSendersInFolder = currentUser.getTopSendersForFolder(folderName).size();
                            //only display top 10 senders for the selected folder
                            if (numSendersInFolder > 5) {
                                numSendersInFolder = 5;
                            }
                                for (int i = 0; i < numSendersInFolder; i++) {
                                    //Created ChartData for top senders radial chart
                                    ChartData temp = new ChartData();
                                    temp.setValue((double) currentUser.getTopSendersForFolder(folderName).get(i).numEmailsSent);
                                    temp.setName(currentUser.getTopSendersForFolder(folderName).get(i).filterName());
                                    temp.setFillColor(colors.get(i));
                                    DashboardController.addTopSendersData(temp);
                                }
                            setLoadedFromLoginScreenToTrue();
                            DashboardLoading.setStopVideoToTrue();
                            DashboardDrawer.setLoadFolderList(true);
                            Scene home = new Scene(homeScreen, 1000, 600);
                            primaryStage.setScene(home);
                            homeScreen.requestFocus();
                        });
                        t.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
            }
        });
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {




        launch(args);


    }

    public static void endTimer(long startTime) {
        long endTime = System.nanoTime();
        long totalTime = (endTime - startTime) / 1000000000;
        System.out.println("Total runtime: " + totalTime + " seconds");
    }
}