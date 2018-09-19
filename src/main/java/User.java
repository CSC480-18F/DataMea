import javax.mail.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

public class User {


    private String email, password;
    private Folder [] folders;
    private ArrayList<Sender> senders;
    private ArrayList<Email> sentMail;


    User (String email, String password){
        this.email = email;
        this.password = password;
        folders = fetchFolders();
        senders = new ArrayList<>();
    }


    public Folder[] fetchFolders() {
        try {
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", this.getEmail(), this.getPassword());
            System.out.println(store);

            Folder[] folders = store.getDefaultFolder().list();

            for (int i = 0; i < folders.length; i++) {
                System.out.println(i + " " + folders[i].getName());
            }

            return folders;

        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
            return null;
        }

    }


    public ArrayList<Sender> readFolder(Folder selectedFolder, boolean runSentiment) {

        Properties connectionProperties = new Properties();
        Session session = Session.getDefaultInstance(connectionProperties, null);

        String selectedFolderAsString = selectedFolder.toString();

        try {
            System.out.print("Connecting to the IMAP server...");
            String storeName = "imaps";
            Store store = session.getStore(storeName);

            // Set the server depending on the parameter flag value
            String server =  "imap.gmail.com";
            store.connect(server, this.getEmail(), this.getPassword());

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
                        this.senders.add(current);
                    }

                }

                Email e = new Email(message, current, runSentiment);
                current.addEmail(e);

                System.out.println("Email sentiment score: " + e.overallSentiment);

            }

            System.out.println("\nNumber of emails in '" + selectedFolderAsString + "' folder: " + numEmails);
            System.out.println("\nsummary of senders: \n");

            Collections.sort(sendersListFromFolder);
            for (Sender s: sendersListFromFolder) {
                System.out.println(s.toString());
            }

            return sendersListFromFolder;

        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }

    }



    public int numEmailsInFolder(ArrayList <Sender> senders) {
        int count = 0;
        for (int i = 0; i<senders.size(); i++) {
            Sender s = senders.get(i);
            for (int j = 0; j<s.getEmails().size(); j++) {
                count++;
            }
        }
        return count;
    }





    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Folder [] getFolders() {
        return folders;
    }

    public void setFolders(Folder [] folders) {
        this.folders = folders;
    }

    public ArrayList<Sender> getSenders() {
        return senders;
    }

    public void setSenders(ArrayList<Sender> senders) {
        this.senders = senders;
    }

}
