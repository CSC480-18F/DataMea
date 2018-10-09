

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, IOException {

        // get user Email address, password

        Scanner kb = new Scanner(System.in);
        System.out.println("Enter email address");
        String address = kb.nextLine();
        System.out.println("Enter password for " + address);
        String password = kb.nextLine();

        String encryptedString = User.encrypt("chansen@oswego.edu");
        System.out.println("email address encrypted: " + encryptedString);
        String oldDecrypted = User.decrypt(encryptedString);
        System.out.println("decrypted version: "  + oldDecrypted + "\n");

        long startTime = System.nanoTime();
        User currentUser = new User(address, password, false);
        endTimer(startTime);

        System.out.println("Done!");
    }




    public static void endTimer(long startTime) {
        long endTime = System.nanoTime();
        long totalTime = (endTime - startTime) / 1000000000;
        System.out.println("Total runtime: " + totalTime + " seconds");
    }

}