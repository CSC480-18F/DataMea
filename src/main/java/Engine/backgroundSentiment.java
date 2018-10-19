package Engine;

import Controllers.DashboardController;
import Controllers.DashboardLogin;
import javafx.concurrent.Task;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Properties;

public class backgroundSentiment extends Task<Void> {

        /*TODO
        *1. Look at the text file that the user has keeping track of how far sentiment analysis has gotten
        *2. based on how far sentiment analysis has gotten, start reading from there
        * text file will look like this
        *
        * Folder1 SubFolder1 numEmailsProcessed
        * Folder1 Subfolder2 numEmailsProcessed
        * Folder2 SubFolder1 numEmailsProcessed
        *...
        *
        * 3. go to each folder, and read the emails from 0- most recent.
        * 4. at every given email, go find the text file associated with that email, read the text,
        *       run sentiment analysis, and rewrite it to the textFile.
        * 5. once step 4 is done, update the text file explained in step 2, to update the lastReadEmailTime for that folder
        * 6. repeat 3-5 until all emails have been read
        *
        *
        * 7. Periodically refresh the current users list of emails by calling the  User.recoverSerializedEmails() folder
        *
        *
        */

    String lastReadSentimentFile = "TextFiles/sentimentProgress.txt";
    User currentUser = Main.getCurrentUser();


    @Override
    protected Void call() throws Exception {

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", currentUser.getEmail(), currentUser.getPassword());
        System.out.println("Getting sentiment.... for "+ store);
        Folder[] foldersList = store.getDefaultFolder().list();


        File f = new File(lastReadSentimentFile);

        if (!f.exists()) {
            //this is the users first time running the program. Create the text file for them, and write everything to have started at 0
            f.createNewFile();
            BufferedWriter br = new BufferedWriter(new FileWriter(f));

            ArrayList<UserFolder> folders = currentUser.recoverFolders();
            for (int i = 0; i<folders.size(); i++) {
                ArrayList<String> subFolders = folders.get(i).subFolders;
                for (String s: subFolders) {
                    String folderStructure = folders.get(i).folderName + " ---> " + s + " ---> " + 0;
                    br.write(folderStructure);
                    br.newLine();
                }
            }
            br.close();


            for (int i = 0; i<folders.size(); i++) {
                ArrayList<String> subFolders = folders.get(i).subFolders;
                for (String s: subFolders) {
                    //user is brand new so start serialization at 0th email....
                    continueSerialization(foldersList, folders.get(i).folderName, s, 0);
                }
            }









        } else {
            //the user is not new. Simply go ahead and start the sentiment analysis from where it left off






        }












        return null;
    }



    public void continueSerialization(Folder [] folders, String folder, String subFolder, int startIndex) {
        //basically just go to the folder and subfolder, start at the startindex, and process sentiment analysis
        //each time the sentiment is process, update the textFile through the updateEmailFile function



    }



    public void updateEmailFile(String fileName, int [] sentiment) {
        //  basically go find the email file, and then go rewrite the sentiment part



    }








}
