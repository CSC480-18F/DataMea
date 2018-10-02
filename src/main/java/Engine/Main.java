package Engine;

import java.io.*;
import java.util.*;

import Controllers.DashboardLogin;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Data Mea");
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("Login_Screen.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        DashboardLogin.setStage(primaryStage);
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