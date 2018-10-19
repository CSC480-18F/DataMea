package Engine;

import Controllers.DashboardController;
import Controllers.DashboardLogin;
import javafx.concurrent.Task;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class BackgroundSentiment extends Task<Void> {

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

    User currentUser = Main.getCurrentUser();
    String lastReadSentimentFile = "TextFiles/" + User.encrypt(currentUser.getEmail()) + "/sentimentProgress.txt";


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
            ArrayList<UserFolder> folders = currentUser.recoverFolders();

            for (UserFolder fold : folders) {
                for (String s: fold.subFolders) {
                    continueSerialization(foldersList, fold.folderName, s, getStartingPointForFolder(fold.folderName,s));
                }

            }

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


    public int getStartingPointForFolder(String folder, String subFolder) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(lastReadSentimentFile)));
            for (String line = ""; line!= null; line=br.readLine()) {
                String [] parts = line.split(" ---> ");
                if (parts[0].equals(folder) && parts[1].equals(subFolder)) {

                    //the folder has been found, return the starting location (aka, the last email it read)
                    return Integer.parseInt(parts[2]);
                }
            }

            br.close();

            //folder is not found, meaning it is probably a new folder, so write it into the file
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(lastReadSentimentFile), true));
            //this might need some debugging but basically append the folder to the end
            String toWrite =  folder + " ---> " + subFolder + " ---> " + 0;
            bw.write(toWrite);
            bw.newLine();
            bw.close();

            return 0;

        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

    }








}
