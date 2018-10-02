package Engine;

import javax.mail.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

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
            for (Message message : messages) {
                numEmails++;
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

                Email e = new Email(message, current, runSentiment);
                current.addEmail(e);

                System.out.println("Engine.Email sentiment score: " + e.overallSentiment);

            }

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


    public ArrayList<Sender> getSenders() {
        return senders;
    }

    public void setSenders(ArrayList<Sender> senders) {
        this.senders = senders;
    }


}
