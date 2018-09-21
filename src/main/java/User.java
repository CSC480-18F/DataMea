import javax.mail.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

public class User {


    private String email, password;
    private ArrayList<UserFolder> folders;
    private ArrayList<Email> sentMail;


    User (String email, String password, Boolean runSentimentAnalysis){
        this.email = email;
        this.password = password;
        folders = fetchFolders(runSentimentAnalysis);
    }

    public void printFolders(){
        for (int i = 0; i<folders.size(); i++){
            System.out.println(i + " " + folders.get(i).folderName);
        }

    }


    public ArrayList<UserFolder> fetchFolders(Boolean runSentimentAnalysis) {
        try {
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", this.getEmail(), this.getPassword());
            System.out.println(store);

            Folder[] folders = store.getDefaultFolder().list();
            ArrayList<UserFolder> userFolders = new ArrayList<>();
            int numEmails=0;
            for (int i = 0; i<folders.length; i++) {
                Folder f = folders[i];

                //Ignore reading inbox for now because it is the biggest folder and the invalid [Gmail] folder
                if (!(f.getName().equalsIgnoreCase("[Gmail]") || f.getName().equalsIgnoreCase("Inbox")) ) {
                    UserFolder uf = new UserFolder(f, this, runSentimentAnalysis );
                    userFolders.add(uf);
                    //System.out.println(numEmails + " " +  userFolders.get(numEmails).folderName);
                    numEmails++;
                }

            }

            return userFolders;

        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
            return null;
        }

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

    public ArrayList<UserFolder> getFolders() {
        return folders;
    }

    public void setFolders(ArrayList<UserFolder> folders)
    {
        this.folders = folders;
    }

}
