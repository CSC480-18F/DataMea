import javax.mail.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.io.*;
import java.util.Date.*;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User {
    private String USERNAME_FILE = "TextFiles/userNames.txt";
    private int USER_SIZE = 512;

    private String email, password;
    private ArrayList<UserFolder> folders;
    private ArrayList<Email> sentMail;
    private long lastLogin;


    User (String email, String password, Boolean runSentimentAnalysis){
        this.email = email;
        this.password = password;

        try {
            serializeUser();
            folders = fetchFolders(runSentimentAnalysis);
        } catch (IOException e){
            e.printStackTrace();
        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
        }

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
                if (!(f.getName().equalsIgnoreCase("[Gmail]") /*|| f.getName().equalsIgnoreCase("Inbox")*/)  ) {
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


    @Override
    public int hashCode() {
        int hash = 7;
        char [] c = this.getEmail().toCharArray();
        for (int i = 0; i<c.length; i++) {
            hash = (hash * 137)  + c[i];
        }
        return hash;
    }



    public void printUserEmails(){
        File directory = new File("TextFiles/"+this.hashCode());
        File[] userFolders = directory.listFiles();
        for (File f: userFolders) {
                File uF = new File(directory+"/"+f.getName());
                File[] folderSenders = uF.listFiles();

                    System.out.println("        folder : " + f.getName());

                for (File sender : folderSenders) {
                    File s = new File(uF + "/" + sender.getName());
                    File[] emails = s.listFiles();

                    System.out.println("        sender: " + sender.getName());

                    for (File email: emails) {
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(email));
                            String line = null;
                            while ((line = br.readLine()) != null) {
                                System.out.println(line);
                            }
                            System.out.println();
                        } catch (Exception e) {
                            System.out.println(email.getAbsolutePath());
                            e.printStackTrace();
                        }
                    }

                }
        }
    }




    public void serializeUser() throws IOException, javax.mail.MessagingException{
        /*  We need to include the users email address, along with the last log in
         *  ex. (without the info being scrambled/encrypted)
         *  chansen@oswego.edu 2018-09-25T21:01:04.894
         *  After encryption, will look like
         *  2384798278923 2018-09-25T21:01:04.894
         *
         *  if it is a new user, add the actual user hash and login date, and increment the number of accounts
         *  else, just update the last login date for that specific users hash
         *
         *  example output in the text file:
         *  3
            2080537423 2018-09-25T21:01:04.894
            -949398889 2018-09-25T20:59:54.483
            -1323952787 2018-09-25T21:01:49.573
         */



        /// need to authenticate user credentials before everything below is done

        File f = new File(USERNAME_FILE);
        boolean found = f.exists();
        if (!found) {
            f.createNewFile();
        }

        BufferedReader br = new BufferedReader(new FileReader(f));
        int numAccounts;

        String numString = br.readLine();
        Integer existingAccountIndex = null;
        String[] lines = new String[0];
        int hash = this.hashCode();
        long lastLoginDate = 0;
        if (numString == null) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write("0");
            bw.close();
            numAccounts = 0;
        } else {
            //look for existing user index
            numAccounts = Integer.parseInt(numString);
            lines = new String[numAccounts];
            int[] accountHashes = new int[numAccounts];
            String s;
            int i = 0;

            while ((s = br.readLine()) != null) {
                lines[i] = s;
                String[] stuff = s.split(" ");
                accountHashes[i] = Integer.parseInt(stuff[0]);
                if (accountHashes[i] == hash) {
                    existingAccountIndex = i;
                    lastLoginDate = Long.parseLong(stuff[1]);
                    this.lastLogin = lastLoginDate;
                }
                i++;
            }

            br.close();
        }


        BufferedWriter bw = new BufferedWriter(new FileWriter(f));

        //user does not exists
        if (existingAccountIndex == null) {
            //one account has been created. Increase number of accounts, and add the new user to the end of list
            numAccounts = numAccounts+1;
            String [] newAccounts = new String[numAccounts];
            String numAccountString = Integer.toString(numAccounts);
            for (int j = 0; j<numAccounts-1; j++){
                newAccounts[j] = lines[j];
            }

            newAccounts[numAccounts-1] = hash + " " + System.currentTimeMillis();
            this.lastLogin = 0;
            bw.write(numAccountString);
            bw.newLine();
            for (int k = 0; k<newAccounts.length; k++) {
                bw.write(newAccounts[k]);
                bw.newLine();
            }
        } else {
            //user exists. simply update last login time
            lines[existingAccountIndex] = hash + " "  + System.currentTimeMillis();
            String n = Integer.toString(numAccounts);
            bw.write(n);
            bw.newLine();
            this.lastLogin = lastLoginDate;
            for (int q = 0; q<numAccounts; q++) {
                bw.write(lines[q]);
                bw.newLine();
            }

        }

        bw.close();

        createSerializedUserFolder();
        updateSerializedFolders();

    }

    public void createSerializedUserFolder() throws IOException {
        File temp = new File ("TextFiles/" + this.hashCode());
        boolean exists = temp.exists();

        if (!exists) {
            File dir = new File("TextFiles/" + this.hashCode());
            dir.mkdir();
        }
    }


    public void updateSerializedFolders() throws javax.mail.MessagingException {
        /*
         *
         * look at the folders the user has in their account, and add the folders to their list of folders under their
         * hashed id folder (if necessary)
         *
         */
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", this.getEmail(), this.getPassword());
        System.out.println(store);

        Folder[] folders = store.getDefaultFolder().list();
        store.close();

        File temp;
        boolean exists;

        for (int i = 0; i<folders.length; i++) {
            if (!folders[i].getName().equalsIgnoreCase("[Gmail]")){
                String dirName = "TextFiles/" + this.hashCode() +"/" + folders[i].getName().hashCode();
                temp = new File(dirName);
                exists = temp.exists();
                if (!exists) {
                    File dir = new File(dirName);
                    dir.mkdir();
                }
            }

        }

    }


    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
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

    public void setFolders(ArrayList<UserFolder> folders) {
        this.folders = folders;
    }

}
