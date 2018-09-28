import javax.mail.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Date;

public class UserFolder {


    javax.mail.Folder folder;
    String folderName;
    ArrayList<Sender> senders;
    User user;

    public UserFolder(javax.mail.Folder f, User u, boolean runSentiment){
        folder = f;
        folderName = f.getName();
        user = u;
        senders = readFolder(runSentiment);
    }



    public ArrayList<Sender> readFolder(boolean runSentiment) {

        Folder selectedFolder = this.folder;

        Properties connectionProperties = new Properties();
        Session session = Session.getDefaultInstance(connectionProperties, null);

        String selectedFolderAsString = selectedFolder.toString();
        ArrayList<Email> newEmails = new ArrayList<>();
        ArrayList<Sender> newSenders = new ArrayList<>();
        try {
            System.out.print("Connecting to the IMAP server...");
            String storeName = "imaps";
            Store store = session.getStore(storeName);

            // Set the server depending on the parameter flag value
            String server = "imap.gmail.com";
            store.connect(server, this.user.getEmail(), this.user.getPassword());

            System.out.println("Connected!");
            selectedFolder.open(Folder.READ_ONLY);

            // Get messages
            Message messages[] = selectedFolder.getMessages();

            System.out.println("Reading messages...");

            ArrayList<Sender> sendersListFromFolder = new ArrayList<>();
            int numEmails = 0;

            // Display the messages
            // read messages backwards to know when to stop
            for (int i = messages.length-1; i>=0; i--) {
                Message message = messages[i];
                Sender current = new Sender(null);
                boolean found = false;

                for (Address a : message.getFrom()) {
                    for (Sender s : sendersListFromFolder) {
                        if (s.getAddress().equals(a.toString())) {
                            found = true;
                            current = s;
                            break;
                        }
                    }
                    if (!found) {
                        current = new Sender(a.toString());
                        sendersListFromFolder.add(current);
                    }

                }


                //Only read the emails that have been sent since the last time the user logged in
                Date rDate = message.getReceivedDate();
                Long receivedDate = rDate.getTime();
                if (this.user.getLastLogin() < receivedDate) {
                    newEmails.add(new Email(message, current, runSentiment));
                    numEmails++;
                    if (!containsSender(newSenders, current.getAddress())) {
                        newSenders.add(current);
                    }
                } else {
                    break;
                }
                //

                Email e = new Email(message, current, runSentiment);
                current.addEmail(e);



                System.out.println("Email sentiment score: " + e.overallSentiment);

            }

            //add a line right here to serialize the new emails
            serializeEmails(newSenders);

            System.out.println("\nNumber of emails in '" + selectedFolderAsString + "' folder: " + numEmails);
            System.out.println("\nsummary of senders: \n");

            Collections.sort(sendersListFromFolder);
            for (Sender s : sendersListFromFolder) {
                System.out.println(s.toString());
            }

            return sendersListFromFolder;

        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }


    public boolean containsSender(ArrayList<Sender> senders, String name) {
        for (Sender s: senders) {
            if (s.getAddress().equals(name)) {
                return true;
            }
        }
        return false;
    }


    public void serializeEmails(ArrayList<Sender> senderList){
        for (Sender currentSender: senderList) {
            String originPath = "TextFiles/" + this.user.hashCode() + "/" + this.folderName.hashCode() + "/";
            String newDir = currentSender.getAddress();
            File temp = new File(originPath + newDir.hashCode());
            boolean exists = temp.exists();

            if (!exists) {
                File dir = new File(originPath+ newDir.hashCode());
                dir.mkdir();
            }
            for (Email currentEmail: currentSender.getEmails()) {
                //figure out what I want to serialize right here
                String path = originPath + newDir.hashCode() + "/" + currentEmail.hashCode() + ".txt";

                try {
                    File f = new File(path);
                    f.createNewFile();

                    BufferedWriter bw = new BufferedWriter(new FileWriter(f));

                    //write everything we need to write to the text file in lines below

                    bw.write(Long.toString(currentEmail.date.getTime()));
                    bw.newLine();
                    bw.write(currentEmail.flags.toString());
                    bw.newLine();

                    //add sentiment analysis part here

                    bw.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println(path);
            }
        }

    }



    public ArrayList<Sender> getSenders() {
        return senders;
    }

    public void setSenders(ArrayList<Sender> senders) {
        this.senders = senders;
    }


}
