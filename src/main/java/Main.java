

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.*;
import org.apache.xpath.operations.Bool;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {

        // get user Email address, password

        Scanner kb = new Scanner(System.in);
        System.out.println("Enter email address");
        String address = kb.nextLine();
        System.out.println("Enter password for " + address);
        String password = kb.nextLine();

        User currentUser = new User(address, password);


        System.out.println("Select which folder to get emails from (type 0-" + (currentUser.getFolders().length - 1) + ")");

        //grab selected folder from user
        //Folder selectedFolder = folders[Integer.parseInt(kb.nextLine())];
        Folder selectedFolder = currentUser.getFolders()[Integer.parseInt(kb.nextLine())];

        long startTime = System.nanoTime();
        //read and print all emails from the selected folder
        ArrayList<Sender> senderList = currentUser.readFolder(selectedFolder, false);
        endTimer(startTime);
    }










    public static void endTimer(long startTime) {
        long endTime = System.nanoTime();
        long totalTime = (endTime - startTime) / 1000000000;
        System.out.println("Total runtime: " + totalTime + " seconds");
    }

}