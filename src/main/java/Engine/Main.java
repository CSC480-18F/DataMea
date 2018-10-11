package Engine;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import Controllers.*;
import eu.hansolo.fx.charts.SunburstChart;
import eu.hansolo.fx.charts.SunburstChartBuilder;
import eu.hansolo.fx.charts.data.ChartItem;
import eu.hansolo.tilesfx.chart.ChartData;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import static Controllers.DashboardController.setLoadedFromLoginScreenToTrue;

public class Main extends Application {

    private static User currentUser;
    private Main.ResourceLoadingTask task = new Main.ResourceLoadingTask();
    private static BooleanProperty startLoading = new SimpleBooleanProperty(false);
    private static ArrayList<String> folders;
    private ArrayList<Color> colors = new ArrayList<>();

    public static ArrayList<String> getFolders() {
        return folders;
    }

    public static void setStartLoadingToTrue(){
        startLoading.setValue(true);
    }

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
                            //Add top senders data to Doughnut Chart
                            //if (currentUser.getFolders().get(0).getSenders().size() < 5) {
                            String folderName = currentUser.recoverFolders().get(0);
                            int numSendersInFolder = currentUser.getTopSendersForFolder(folderName).size();

                            //only display top 10 senders for the selected folder
                            if (numSendersInFolder > 5) {
                                numSendersInFolder = 5;
                            }
                                for (int i = 0; i < numSendersInFolder; i++) {
                                    ChartData temp = new ChartData();
                                    temp.setValue((double) currentUser.getTopSendersForFolder(folderName).get(i).numEmailsSent);
                                    temp.setName(currentUser.getTopSendersForFolder(folderName).get(i).getAddress());
                                    temp.setFillColor(colors.get(i));
                                    DashboardController.addTopSendersData(temp);
                                }
                            setLoadedFromLoginScreenToTrue();
                            //}
//
//
//                            else{
//                                for (int i = 0; i < currentUser.getFolders().get(0).getSenders().size(); i++) {
//                                    DashboardController.addTopSendersData(new PieChart.Data(i + 1 + ". " +
//                                            currentUser.getFolders().get(0).getSenders().get(i).getAddress(),
//                                            currentUser.getFolders().get(0).getSenders().get(i).getEmails().size()));
//                                }
//                            }

                            DashboardLoading.setStopVideoToTrue();
                            DashboardDrawer.setLoadFolderListToTrue();
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




//        Scanner kb = new Scanner(System.in);
//        System.out.println("Enter email address");
//        String address = kb.nextLine();
//        System.out.println("Enter password for " + address);
//        String password = kb.nextLine();
//
//        long startTime = System.nanoTime();
//        User currentUser = new User(address, password, false);
//        endTimer(startTime);



        launch(args);


    }

    public static void endTimer(long startTime) {
        long endTime = System.nanoTime();
        long totalTime = (endTime - startTime) / 1000000000;
        System.out.println("Total runtime: " + totalTime + " seconds");
    }
}