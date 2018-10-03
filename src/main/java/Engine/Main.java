package Engine;

import java.io.*;
import java.util.*;

import Controllers.DashboardLoading;
import Controllers.DashboardLogin;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    private static User currentUser;
    private Main.ResourceLoadingTask task = new Main.ResourceLoadingTask();
    private static BooleanProperty startLoading = new SimpleBooleanProperty(false);

    public static void setStartLoadingToTrue(){
        startLoading.setValue(true);
    }

    public class ResourceLoadingTask extends Task<Void> {
        @Override
        protected Void call() throws Exception {
            currentUser = new User(DashboardLogin.getEmail(), DashboardLogin.getPassword(), false);
            System.out.println("Data Loaded");
            return null;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Data Mea");
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("Login_Screen.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        DashboardLogin.setStage(primaryStage);

        startLoading.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    try {
                        Thread t = new Thread(task);
                        Pane homeScreen = FXMLLoader.load(getClass().getClassLoader().getResource("Dashboard_Home.fxml"));

                        task.setOnSucceeded(e -> {
                            DashboardLoading.setStopVideoToTrue();
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
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        //launch(args);

        // get user Engine.Email address, password

        //Scanner kb = new Scanner(System.in);
        //System.out.println("Enter email address");
        //String address = kb.nextLine();
        //System.out.println("Enter password for " + address);
        //String password = kb.nextLine();

        //long startTime = System.nanoTime();
        //User currentUser = new User(DashboardLogin.getEmail(), DashboardLogin.getEmail(), true);
        //endTimer(startTime);

        //System.out.println("\n\nSelect which folder to get emails from (type 0-" + (currentUser.getFolders().size() - 1) + ")");
        //currentUser.printFolders();

        //int folderNum = Integer.parseInt(kb.nextLine());
        //Engine.UserFolder selectedFolder = currentUser.getFolders().get(folderNum);


        //read and print all emails from the selected folder
        //ArrayList<Engine.Sender> senderList = currentUser.getFolders().get(folderNum).getSenders();
        //System.out.println("Done!");

        //ArrayList<Sender> topSenders = currentUser.getFolders().get(0).readFolder(false);

        launch(args);


    }

    public static void endTimer(long startTime) {
        long endTime = System.nanoTime();
        long totalTime = (endTime - startTime) / 1000000000;
        System.out.println("Total runtime: " + totalTime + " seconds");
    }
}