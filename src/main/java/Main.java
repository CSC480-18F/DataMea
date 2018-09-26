

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, IOException {

        // get user Email address, password

        Scanner kb = new Scanner(System.in);
        System.out.println("Enter email address");
        String address = kb.nextLine();
        System.out.println("Enter password for " + address);
        String password = kb.nextLine();

        long startTime = System.nanoTime();
        User currentUser = new User(address, password, true);
        endTimer(startTime);

        System.out.println("\n\nSelect which folder to get emails from (type 0-" + (currentUser.getFolders().size() - 1) + ")");
        currentUser.printFolders();

        int folderNum = Integer.parseInt(kb.nextLine());
        UserFolder selectedFolder = currentUser.getFolders().get(folderNum);


        //read and print all emails from the selected folder
        ArrayList<Sender> senderList = currentUser.getFolders().get(folderNum).getSenders();
        System.out.println("Done!");

    }




    public static void endTimer(long startTime) {
        long endTime = System.nanoTime();
        long totalTime = (endTime - startTime) / 1000000000;
        System.out.println("Total runtime: " + totalTime + " seconds");
    }

}