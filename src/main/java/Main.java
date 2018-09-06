

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Scanner;

/**
 * Class reads emails
 *
 * @author itcuties
 *
 */
public class Main {




    public static void main(String[] args) {
        // get user Email address, password
        String password, address, selectedFolder;
        Scanner kb = new Scanner(System.in);
        System.out.println("Enter email address");
        address = kb.nextLine();
        System.out.println("Enter password for " + address);
        password = kb.nextLine();

        //recover the list of folders in the users email account
        Folder[] folders = printFolders(address, password);

        System.out.println("Select which folder to get emails from (type 0-" + (folders.length-1) + ")" );

        //grab selected folder from user
        selectedFolder = folders[Integer.parseInt(kb.nextLine())].toString();


        long startTime = System.nanoTime();
        //read and print all emails from the selected folder
        readEmails(true, password, address, selectedFolder);
        long endTime   = System.nanoTime();
        long totalTime = (endTime - startTime)/ 1000000000;
        System.out.println("Total runtime: " + totalTime+  " seconds");
    }




/*

ALL CREDIT FOR THIS FUNCTION (readEmails) GOES TO itscuties from the site below

http://www.itcuties.com/java/javamail-read-email/

Some additional notes:
-whenever something like javax.mail.internet.MimeMultipart@396f6598 appears as the message content,
it appears to be whenever there is a thread of replies

-anything (from what i've checked) that is html, is a mass email

 */
    /**
     * Method reads emails from the IMAP or POP3 server.
     * @param isImap - if true then we are reading messages from the IMAP server, if no then read from the POP3 server.
     */
    private static void readEmails(boolean isImap, String password, String address, String selectedFolder) {
        // Create all the needed properties - empty!
        Properties connectionProperties = new Properties();
        // Create the session
        Session session = Session.getDefaultInstance(connectionProperties,null);

        try {
            System.out.print("Connecting to the IMAP server...");
            // Connecting to the server
            // Set the store depending on the parameter flag value
            String storeName = isImap ? "imaps" : "pop3";
            Store store = session.getStore(storeName);

            // Set the server depending on the parameter flag value
            String server = isImap ? "imap.gmail.com" : "pop.gmail.com";
            store.connect(server,address,password);

            System.out.println("Connected!");

            // Get the Inbox folder
            //Folder folder = store.getFolder("Inbox");
            Folder folder = store.getFolder(selectedFolder);

            // Set the mode to the read-only mode
            folder.open(Folder.READ_ONLY);

            // Get messages
            Message messages[] = folder.getMessages();

            System.out.println("Reading messages...");

            int numEmails = 0;

            // Display the messages
            for(Message message:messages) {
                numEmails++;
                for (Address a: message.getFrom())
                    System.out.println("From:" + a);

                System.out.println("Title: " + message.getSubject());
                System.out.println(message.getSentDate());
                System.out.println();
                System.out.println(message.getContent());
                System.out.println("------");


            }

            System.out.println("\nNumber of emails in '" + selectedFolder + "' folder: " +  numEmails);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static Folder[] printFolders(String address, String password) {

        try {
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", address, password);
            System.out.println(store);

            Folder[] folders = store.getDefaultFolder().list();
            for (int i= 0; i<folders.length; i++) {
                System.out.println(i + " " +  folders[i].getName());
            }
            return folders;

        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
            return null;
        }

    }

}   