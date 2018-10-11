package Engine;

import javax.mail.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.io.*;
import java.util.Date.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User {
    private String USERNAME_FILE = "TextFiles/userNames.txt";

    private String email, password;
    //private ArrayList<UserFolder> folders;
    private ArrayList<Email> sentMail;
    private long lastLogin;
    private String folderName;
    private ArrayList<Email> emails;
    private ArrayList<String> folders;


    public User (String email, String password, Boolean runSentimentAnalysis){
        this.email = email;
        this.password = password;

        try {
            serializeUser(runSentimentAnalysis);
        } catch (IOException e){
            e.printStackTrace();
        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
        }

        //recover all serialized emails right here
        emails = recoverSerializedEmails();
        folders = recoverFolders();

    }


    public ArrayList<Sender> getTopSendersForFolder(String folderName) {
        ArrayList<Sender> topSenders = new ArrayList<>();
        ArrayList<String> senderNames = new ArrayList<>();
        for (Email e : emails) {
            if (e.folder.equals(folderName)) {
                if (!senderNames.contains(e.sender.getAddress())) {
                    senderNames.add(e.sender.getAddress());
                    topSenders.add(new Sender(e.sender.getAddress()));
                } else {
                    for (Sender s : topSenders) {
                        if (s.getAddress().equals(e.sender.getAddress())) {
                            s.incrementNumEmails();
                        }
                    }
                }
            }
        }

        Collections.sort(topSenders);
        return topSenders;
    }



    public ArrayList<String> recoverFolders(){
        ArrayList<String> f = new ArrayList<>();
        for (Email e : emails) {
            if (!f.contains(e.folder)) {
                f.add(e.folder);
            }
        }
        return f;
    }


    public ArrayList<Email> recoverSerializedEmails() {
        File temp = new File ("TextFiles/" + encrypt(email));
        File [] e = temp.listFiles();
        emails = new ArrayList<Email>();
        for (File f: e) {
            Email em = new Email(f);
            this.emails.add(em);
        }

        return emails;

    }



    public void serializeUser(boolean runSentiment) throws IOException, javax.mail.MessagingException{

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
        String encryptedAddress = encrypt(email);
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
            String[] accountHashes = new String[numAccounts];
            String s;
            int i = 0;

            while ((s = br.readLine()) != null) {
                lines[i] = s;
                String[] stuff = s.split(" ");
                accountHashes[i] = stuff[0];
                String decryptedSavedEmail = decrypt(accountHashes[i]);
                String userTypedEmail = decrypt(encryptedAddress);
                if (decryptedSavedEmail.equals(userTypedEmail)) {
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

            newAccounts[numAccounts-1] = encryptedAddress + " " + System.currentTimeMillis();
            this.lastLogin = 0;
            bw.write(numAccountString);
            bw.newLine();
            for (int k = 0; k<newAccounts.length; k++) {
                bw.write(newAccounts[k]);
                bw.newLine();
            }
        } else {
            //user exists. simply update last login time
            lines[existingAccountIndex] = encryptedAddress + " "  + System.currentTimeMillis();
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
        updateSerializedFolders(runSentiment);

    }

    public void createSerializedUserFolder() throws IOException {
        File temp = new File ("TextFiles/" + encrypt(email));
        File [] users = (new File("TextFiles/")).listFiles();
        boolean exists = false;
        for (File user: users) {
            if (!user.getName().contains(".")) {
                if (decrypt(user.getName()).equals(email)) {
                    exists = true;
                    folderName = user.getName();
                    break;
                }
            }

        }

        if (!exists) {
            String name = encrypt(email);
            File dir = new File("TextFiles/" + name);
            folderName = name;
            dir.mkdir();
        }

    }



    public void readFolderAndSerializeEmails(Folder f, boolean runSentiment){

        //to do
        //create email objects to serialize

        String originPath = "TextFiles/" + encrypt(email) + "/";
        int numMessages;


        try {
            f.open(f.READ_ONLY);
            Message[] messages = f.getMessages();
            numMessages = messages.length;
            System.out.println("Serializing " + f.getName());

        for (int i= numMessages -1; i>=0; i--) {
            System.out.println("processing: " + i);
            Message m = messages[i];
            String sender = "";
            try {
                sender = m.getFrom()[0].toString();
            } catch (ArrayIndexOutOfBoundsException e) {
                sender = "Unknown";
                System.out.println("The sender is invalid..... not processing email - for now");

            }


            Long receivedDate = m.getReceivedDate().getTime();
            if (this.getLastLogin() < receivedDate) {

                Email e = new Email(messages[i], new Sender(sender), runSentiment);
                System.out.println(e.toString());

                //serialize email
                File currentEmail = new File(originPath + receivedDate + ".txt");
                currentEmail.createNewFile();

                BufferedWriter bw = new BufferedWriter(new FileWriter(currentEmail));

                //write all necessary components here
                bw.write(encrypt(f.getName()));
                bw.newLine();
                bw.write(Long.toString(receivedDate));
                bw.newLine();
                bw.write(encrypt(sender));
                bw.newLine();
                bw.write(m.getFlags().toString());
                bw.newLine();

                //add sentiment analysis below
                bw.write(Integer.toString(e.sentimentScores[0]));
                bw.newLine();
                bw.write(Integer.toString(e.sentimentScores[1]));
                bw.newLine();
                bw.write(Integer.toString(e.sentimentScores[2]));
                bw.newLine();
                bw.write(Integer.toString(e.sentimentScores[3]));
                bw.newLine();
                bw.write(Integer.toString(e.sentimentScores[4]));
                bw.newLine();





                //
                bw.close();


            } else {
                break;
            }

        }



        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }




    public void updateSerializedFolders(boolean runSentiment) throws javax.mail.MessagingException {
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


        for (int i = 0; i<folders.length; i++) {
            String name = folders[i].getName();
            if (!name.equalsIgnoreCase("[Gmail]") && !name.equalsIgnoreCase("inbox")){
                readFolderAndSerializeEmails(folders[i], runSentiment);
            }

        }

    }


    // have a set of random keys to cycle through to make the encryption more secure
    private static int [] randomizer = {4,5,2,5,7,6,2,4,78,8};



    public static String encrypt(String strToBeEncrypted) {
        String result = "";
        int length = strToBeEncrypted.length();
        char ch;
        int ck=0;
        for (int i = 0; i< length; i++) {
            if (ck >= randomizer.length -1) {
                ck = 0;
            }

            ch = strToBeEncrypted.charAt(i);
            ch += randomizer[ck];
            result += ch;
            ck++;
        }


        return result;
    }


    public static String decrypt(String strEncrypted) {
        String result = "";
        int length = strEncrypted.length();
        char ch;
        int ck=0;
        for (int i = 0; i<length; i++) {
            if (ck >= randomizer.length-1 ) {
                ck = 0;
            }

            ch = strEncrypted.charAt(i);
            ch -= randomizer[ck];
            result += ch;
            ck++;
        }

        return result;
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


}
