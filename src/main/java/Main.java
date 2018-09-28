import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.xpath.operations.Bool;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Data Mea");
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("Dashboard_Home.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        //launch(args);

        // get user Email address, password

        Scanner kb = new Scanner(System.in);
        System.out.println("Enter email address");
        String address = kb.nextLine();
        System.out.println("Enter password for " + address);
        String password = kb.nextLine();

        long startTime = System.nanoTime();
        User currentUser = new User(address, password, true);
        endTimer(startTime);

        //System.out.println("\n\nSelect which folder to get emails from (type 0-" + (currentUser.getFolders().size() - 1) + ")");
        //currentUser.printFolders();

        //int folderNum = Integer.parseInt(kb.nextLine());
        //UserFolder selectedFolder = currentUser.getFolders().get(folderNum);


        //read and print all emails from the selected folder
        //ArrayList<Sender> senderList = currentUser.getFolders().get(folderNum).getSenders();
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