package datamea.backend;

import java.io.*;
import java.util.ArrayList;
import datamea.frontend.*;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import static datamea.frontend.DashboardController.setLoadedFromLoginScreenToTrue;

public class Main extends Application {

    //------------------Declaring Variables------------------//
    private static User                     currentUser;
    private        Main.ResourceLoadingTask task                = new Main.ResourceLoadingTask();
    private static BooleanProperty          startLoading        = new SimpleBooleanProperty(false);
    private static ArrayList<UserFolder>    folders;
    private        ArrayList<Color>         colors              = new ArrayList<>();
    private        BackgroundSentiment      backgroundSentiment;


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
        File textFilesDir = new File("TextFiles/");
        textFilesDir.mkdirs();
        Platform.setImplicitExit(false);
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(600);
        primaryStage.setTitle("Data Mea");
        Pane root = FXMLLoader.load(this.getClass().getClassLoader().getResource("Login_Screen.fxml"));
        primaryStage.setScene(new Scene(root));
        root.requestFocus();
        primaryStage.show();
        DashboardLogin.setStage(primaryStage);
        DashboardLoading.setStage(primaryStage);
        DashboardController.setStage(primaryStage);

        colors.add(javafx.scene.paint.Color.valueOf("#fc5c65"));
        colors.add(javafx.scene.paint.Color.valueOf("#fd9644"));
        colors.add(javafx.scene.paint.Color.valueOf("#fed330"));
        colors.add(javafx.scene.paint.Color.valueOf("#26de81"));
        colors.add(javafx.scene.paint.Color.valueOf("#2bcbba"));
        colors.add(javafx.scene.paint.Color.valueOf("#eb3b5a"));
        colors.add(javafx.scene.paint.Color.valueOf("#fa8231"));
        colors.add(javafx.scene.paint.Color.valueOf("#f7b731"));
        colors.add(javafx.scene.paint.Color.valueOf("#20bf6b"));
        colors.add(javafx.scene.paint.Color.valueOf("#0fb9b1"));
        colors.add(javafx.scene.paint.Color.valueOf("#45aaf2"));
        colors.add(javafx.scene.paint.Color.valueOf("#4b7bec"));
        colors.add(javafx.scene.paint.Color.valueOf("#a55eea"));
        colors.add(javafx.scene.paint.Color.valueOf("#d1d8e0"));
        colors.add(javafx.scene.paint.Color.valueOf("#778ca3"));
        colors.add(javafx.scene.paint.Color.valueOf("#2d98da"));
        colors.add(javafx.scene.paint.Color.valueOf("#3867d6"));
        colors.add(javafx.scene.paint.Color.valueOf("#8854d0"));
        colors.add(javafx.scene.paint.Color.valueOf("#a5b1c2"));
        colors.add(javafx.scene.paint.Color.valueOf("#4b6584"));

        startLoading.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    try {
                        Thread t = new Thread(task);
                        Parent homeScreenParent = FXMLLoader.load(getClass().getClassLoader().getResource("Dashboard_Home.fxml"));

                        task.setOnSucceeded(e -> {
                            //Add top senders data to Radial Chart
                            //if (currentUser.getFolders().get(0).getSenders().size() < 5) {
                            String folderName = currentUser.recoverFolders().get(0).folderName;
                            int numSendersInFolder = currentUser.getTopSendersForFolder("AllFolders", "").size();
                            //only display top 7 senders for the selected folder
                                for (int i = 0; i < 7; i++) {
                                    //Created ChartData for top senders radial chart
                                    ChartData temp = new ChartData();
                                    if (i < numSendersInFolder) {
                                        temp.setValue((double) currentUser.getTopSendersForFolder(folderName, "").get(i).numEmailsSent);
                                        temp.setName(currentUser.getTopSendersForFolder(folderName, "").get(i).filterName());
                                        temp.setFillColor(colors.get(i));
                                    } else {
                                        temp.setValue(0);
                                        temp.setName("");
                                    }
                                    DashboardController.addTopSendersData(temp);
                                }

                            backgroundSentiment = new BackgroundSentiment();
                            Thread t2 = new Thread(backgroundSentiment);
                            backgroundSentiment.setOnSucceeded(f -> {

                            });
                            t2.start();

                            setLoadedFromLoginScreenToTrue();
                            DashboardLoading.setStopVideoToTrue();
                            DashboardLoading.setLoadingOnCloseRequest(false);
                            DashboardDrawer.setLoadFolderList(true);
                            DashboardController.setHomeOnCloseRequest(true);
                            Scene homeScreen = new Scene(homeScreenParent, DashboardLoading.getMyStage().getWidth(), DashboardLoading.getMyStage().getHeight());
                            DashboardLoading.getMyStage().setScene(homeScreen);
                            DashboardLoading.getMyStage().requestFocus();
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
                System.exit(0);
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