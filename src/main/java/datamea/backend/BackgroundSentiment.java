package datamea.backend;

import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javax.mail.*;
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
    int totalSentimentRan = 0;



    @Override
    protected Void call() throws Exception {

        this.updateProgress(getTotalSentimentRan(), User.getTotalNumberOfEmails());

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", currentUser.getEmail(), currentUser.getPassword());
        System.out.println("Getting sentiment.... for "+ store);
        long startTime = System.nanoTime();
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

        System.out.println("Done sentiment analysis.... for now");
        Main.endTimer(startTime);

        return null;
    }



    public void continueSerialization(Folder [] folders, String folder, String subFolder, int startIndex) {
        Timeline updateGauge = null;
        //basically just go to the folder and subfolder, start at the startindex, and process sentiment analysis
        //each time the sentiment is process, update the textFile through the updateEmailFile function
        try {

            for (Folder f : folders) {
                if (f.getName().equals(folder)) {
                    f.open(Folder.READ_ONLY);
                    Folder [] subFolders = f.list();
                    if (f.getName().equals(subFolder)) {
                        Message [] messages = f.getMessages();

                        for (int i = startIndex; i<messages.length; i++) {

                            //do sentiment stuff and update file with the correct info
                            Message currentMessage = messages[i];
                            Email tempEmail = new Email(currentMessage, new Sender(currentMessage.getFrom()[0].toString()), true);
                            String fileName = "TextFiles/" + User.encrypt(currentUser.getEmail()) + "/" + currentMessage.getReceivedDate().getTime() + ".txt";
                            System.out.println("Analysing email: " + i + " -- " + fileName + "\n" + f.getName());

                            this.updateProgress(getTotalSentimentRan(), User.getTotalNumberOfEmails());
                            System.out.println("total number of emails: " + User.getTotalNumberOfEmails() + "\nEmails Processed: " + getTotalSentimentRan());

                            try {
                                updateEmailFile(fileName, tempEmail.getSentimentScores(), tempEmail.getLanguage());
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("\t\tcaught the messed up email!!!!");
                                deleteEmail(fileName);
                                incrementFolder(f.getName(), f.getName());
                                break;
                            }

                        }

                    }
                    for (Folder sub : subFolders) {
                        if (sub.getName().equals(subFolder)) {
                            sub.open(Folder.READ_ONLY);
                            Message [] messages = sub.getMessages();

                            //start at the starting index and go from there
                            for (int i = startIndex; i<messages.length; i++) {

                                //do sentiment stuff and update file with the correct info
                                Message currentMessage = messages[i];
                                Email tempEmail = null;
                                try {
                                    tempEmail = new Email(currentMessage, new Sender(currentMessage.getFrom()[0].toString()), true);
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                    System.out.println("email has been deleted...");
                                }
                                String fileName = "TextFiles/" + User.encrypt(currentUser.getEmail()) + "/" + currentMessage.getReceivedDate().getTime() + ".txt";
                                System.out.println("Analysing email: " + i + " -- " + fileName + "\n" + f.getName() + " -- " + sub.getName());
                                this.updateProgress(getTotalSentimentRan(), User.getTotalNumberOfEmails());
                                System.out.println("total number of emails: " + User.getTotalNumberOfEmails() + "\nEmails Processed: " + getTotalSentimentRan());

                                try {
                                    updateEmailFile(fileName, tempEmail.getSentimentScores(), tempEmail.getLanguage());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    System.out.println("\t\tcaught the messed up email in the subfolder!!!");
                                    deleteEmail(fileName);
                                    incrementFolder(f.getName(), sub.getName());
                                    break;
                                }
                            }
                        }
                    }
                    f.close();
                }
            }

        } catch (MessagingException e) {
             e.printStackTrace();
            System.out.println("Cannot open folder..");
        }

    }

    //delete emails that arent serialized properly (happens 2 in 6000 tested emails.......)
    public void deleteEmail(String fileName) {
        File f = new File(fileName);
        f.delete();
    }

    public int getTotalSentimentRan() {
        try {
            int processed = 0;
            BufferedReader br = new BufferedReader(new FileReader(new File(lastReadSentimentFile)));
            ArrayList<String> fileStrings = new ArrayList<>();

            for (String line = ""; line!=null ; line = br.readLine()) {
                if (line != "") {
                    fileStrings.add(line);
                }
            }

            br.close();


            for (String l: fileStrings) {
                String[] parts = l.split(" ---> ");
                processed += Integer.parseInt(parts[2]);
            }
            totalSentimentRan = processed;
            return processed;
        } catch (FileNotFoundException e) {
            return 0;
        } catch (IOException e) {
            return 0;
        }

    }



    public void updateEmailFile(String fileName, int [] sentiment, String language) throws IOException {
        //  basically go find the email file, and then go rewrite the sentiment part

        File f = new File(fileName);


            BufferedReader br = new BufferedReader(new FileReader(f));
            ArrayList<String> fileStrings = new ArrayList<>();

            for (String line = ""; line!=null ; line = br.readLine()) {
                if (line != "") {
                    fileStrings.add(line);
                }
            }

            br.close();

            try {
                int sentimentCount = 0;
                for (int i = 5  ; i<10; i++) {
                    //replace the old sentiment ones to be the new ones given from the sentiment int [];
                    //if this line below breaks, the something was wrong with the file, so just pass on it
                    int test = Integer.parseInt(fileStrings.get(i));
                    fileStrings.set(i, Integer.toString(sentiment[sentimentCount]));
                    sentimentCount++;
                }
            } catch (NumberFormatException e) {
                System.out.println("Something went wrong with fixing the email..... Cannot be processed");
            }

            if(language == null) language = "unk";

            fileStrings.set(11, language);

            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));

            for (String s: fileStrings) {
                bw.write(s);
                bw.newLine();
            }

            bw.close();


            ///TODO: update the file which keeps track of the last email that was processed in the folder.
            incrementFolder(fileStrings.get(0), fileStrings.get(1));


    }

    public void incrementFolder (String folder, String subFolder) {
        ArrayList<String> lines = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(lastReadSentimentFile));
            //store a copy of the lines
            for (String line = ""; line!= null; line=br.readLine()) {
                if (line != "") {
                    lines.add(line);
                }
            }

            br.close();

            for (String l: lines) {
                String [] parts = l.split(" ---> ");
                if (User.encrypt(parts[0]).equals(folder) && User.encrypt(parts[1]).equals(subFolder)) {
                    int location = Integer.parseInt(parts[2]) + 1;
                    lines.set(lines.indexOf(l), parts[0] + " ---> " + parts[1] + " ---> " + location);
                    break;
                }
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(lastReadSentimentFile));

            for (String s : lines) {
                bw.write(s);
                bw.newLine();
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }



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
