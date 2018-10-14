package Engine;


import javax.mail.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

public class User {

    //------------------Declaring Variables------------------//
    private String                  USERNAME_FILE = "TextFiles/userNames.txt";
    private String                  email, password;
    private ArrayList<Email>        sentMail;
    private long                    lastLogin;
    private String                  folderName;
    private ArrayList<Email>        emails;
    private ArrayList<UserFolder>   folders;
    private int[][] dayOfWeekFrequency;
    private int frequencyDifference = -1;


    public User(String email, String password, Boolean runSentimentAnalysis) {
        this.email = email;
        this.password = password;

        try {
            serializeUser(runSentimentAnalysis);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
        }

        //recover all serialized emails right here
        emails = recoverSerializedEmails();
        folders = recoverFolders();

    }


    public ArrayList<Email> getEmailsFromFolder(String folderName, String subFolderName) {
        ArrayList<Email> filteredEmails = new ArrayList<>();
        for (Email e: this.emails) {
            if (e.getFolder().equalsIgnoreCase(folderName) && e.getSubFolder().equalsIgnoreCase(subFolderName)) {
                filteredEmails.add(e);
            }
        }
        return filteredEmails;
    }


    public ArrayList<Sender> getTopSendersForFolder(String folderName, String subFolderName) {
        boolean all = false;
        boolean subFolderBool = false;
        if (folderName.equals("AllFolders")) {
            all = true;
        }
        if (subFolderName.equals("")) {
            subFolderBool = true;
        }
        ArrayList<Sender> topSenders = new ArrayList<>();
        ArrayList<String> senderNames = new ArrayList<>();
        for (Email e : emails) {
            if ((e.getFolder().equals(folderName) && e.getSubFolder().equals(subFolderName))|| all || (e.getFolder().equals(folderName) && subFolderBool) ) {
                if (!senderNames.contains(e.getSender().getAddress())) {
                    senderNames.add(e.getSender().getAddress());
                    topSenders.add(new Sender(e.getSender().getAddress()));
                } else {
                    for (Sender s : topSenders) {
                        if (s.getAddress().equals(e.getSender().getAddress())) {
                            s.incrementNumEmails();
                        }
                    }
                }
            }
        }

        Collections.sort(topSenders);
        return topSenders;
    }



    public ArrayList<UserFolder> recoverFolders() {
        ArrayList<UserFolder> f = new ArrayList<>();
        ArrayList<String> folderNames = new ArrayList<>();

        for (Email e : emails) {

            if (!folderNames.contains(e.getFolder())) {
                UserFolder fold = new UserFolder(e.getFolder());
                f.add(fold);
                folderNames.add(e.getFolder());
            } else {
                for (UserFolder folds: f) {
                    if (e.getFolder().equals(folds.folderName) && !folds.subFolders.contains(e.getSubFolder())) {
                        //add the subfolder to the userFolder
                        folds.subFolders.add(e.getSubFolder());
                    }
                }
            }
        }
        return f;
    }


    public ArrayList<Email> recoverSerializedEmails() {
        File temp = new File("TextFiles/" + encrypt(email));
        File[] e = temp.listFiles();
        emails = new ArrayList<Email>();
        for (File f : e) {
            try {
                Email em = new Email(f);
                this.emails.add(em);
            } catch (Exception em) {
                System.out.println("Email cannot be properly read..");
                em.printStackTrace();
            }

        }

        return emails;

    }


    public void serializeUser(boolean runSentiment) throws IOException, javax.mail.MessagingException {

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
            numAccounts = numAccounts + 1;
            String[] newAccounts = new String[numAccounts];
            String numAccountString = Integer.toString(numAccounts);
            for (int j = 0; j < numAccounts - 1; j++) {
                newAccounts[j] = lines[j];
            }

            newAccounts[numAccounts - 1] = encryptedAddress + " " + System.currentTimeMillis();
            this.lastLogin = 0;
            bw.write(numAccountString);
            bw.newLine();
            for (int k = 0; k < newAccounts.length; k++) {
                bw.write(newAccounts[k]);
                bw.newLine();
            }
        } else {
            //user exists. simply update last login time
            lines[existingAccountIndex] = encryptedAddress + " " + System.currentTimeMillis();
            String n = Integer.toString(numAccounts);
            bw.write(n);
            bw.newLine();
            this.lastLogin = lastLoginDate;
            for (int q = 0; q < numAccounts; q++) {
                bw.write(lines[q]);
                bw.newLine();
            }

        }

        bw.close();

        createSerializedUserFolder();
        updateSerializedFolders(runSentiment);

    }

    public void createSerializedUserFolder() throws IOException {
        File temp = new File("TextFiles/" + encrypt(email));
        File[] users = (new File("TextFiles/")).listFiles();
        boolean exists = false;
        for (File user : users) {
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


    public void readFolderAndSerializeEmails(Folder f, boolean runSentiment) {

        //to do
        //create email objects to serialize

        String originPath = "TextFiles/" + encrypt(email) + "/";

        try {
            f.open(f.READ_ONLY);
            System.out.println("Serializing " + f.getName());

            writeMessages(f, f, runSentiment, originPath);

            Folder [] subFolders = f.list();
            for (Folder sub: subFolders) {
                writeMessages(f, sub, runSentiment, originPath);
            }


        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
        }
    }


    public void writeMessages(Folder f, Folder sub, boolean runSentiment, String originPath) {
        System.out.println("Currently reading/writing: " + f.getName() + "    Subfolder: " + sub.getName());
        Message [] messages = new Message[0];


        try {
            if (!f.getName().equals(sub.getName())) {
                sub.open(1);
            }
            messages = sub.getMessages();

        } catch (Exception e) {
            e.printStackTrace();
        }


        int numMessages = messages.length;
        for (int i = numMessages - 1; i >= 0; i--) {

            System.out.println("processing: " + i);
            Message m = messages[i];
            String sender = "Unknown";
            Long receivedDate = 0l;
            try {
                sender = m.getFrom()[0].toString();
                receivedDate = m.getReceivedDate().getTime();
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("The sender is invalid..... processing email as sender 'unknown'");
            } catch (MessagingException e) {
                System.out.println("Messaging exception");
            }

            if (this.getLastLogin() < receivedDate) {

                //serialize email
                try {
                    Email e = new Email(messages[i], new Sender(sender), runSentiment);
                    File currentEmail = new File(originPath + receivedDate + ".txt");
                    currentEmail.createNewFile();

                    BufferedWriter bw = new BufferedWriter(new FileWriter(currentEmail));

                    //write all necessary components here
                    bw.write(encrypt(f.getName()));
                    bw.newLine();
                    bw.write(encrypt(sub.getName()));
                    bw.newLine();
                    bw.write(Long.toString(receivedDate));
                    bw.newLine();
                    bw.write(encrypt(sender));
                    bw.newLine();
                    bw.write(m.getFlags().toString());
                    bw.newLine();

                    //add sentiment analysis below
                    bw.write(Integer.toString(e.getSentimentScores()[0]));
                    bw.newLine();
                    bw.write(Integer.toString(e.getSentimentScores()[1]));
                    bw.newLine();
                    bw.write(Integer.toString(e.getSentimentScores()[2]));
                    bw.newLine();
                    bw.write(Integer.toString(e.getSentimentScores()[3]));
                    bw.newLine();
                    bw.write(Integer.toString(e.getSentimentScores()[4]));
                    bw.newLine();

                    //
                    bw.close();
                } catch (IOException e) {
                    System.out.println("uh oh, something went wrong");
                } catch (MessagingException e) {
                    System.out.println("cannot get flags");
                }

            } else {
                break;
            }


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


        for (int i = 0; i < folders.length; i++) {
            String name = folders[i].getName();
            if (!name.equalsIgnoreCase("[Gmail]") /*&& !name.equalsIgnoreCase("inbox")*/) {
                readFolderAndSerializeEmails(folders[i], runSentiment);
            }

        }

    }


    // have a set of random keys to cycle through to make the encryption more secure
    private static int[] randomizer = {4, 5, 2, 5, 7, 6, 2, 4, 78, 8};


    public static String encrypt(String strToBeEncrypted) {
        String result = "";
        int length = strToBeEncrypted.length();
        char ch;
        int ck = 0;
        for (int i = 0; i < length; i++) {
            if (ck >= randomizer.length - 1) {
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
        int ck = 0;
        for (int i = 0; i < length; i++) {
            if (ck >= randomizer.length - 1) {
                ck = 0;
            }

            ch = strEncrypted.charAt(i);
            ch -= randomizer[ck];
            result += ch;
            ck++;
        }

        return result;
    }



    //add a paramater to specify which emails to populate for the heatmap
    public int[][] generateDayOfWeekFrequency(ArrayList<Email> emailsOfIntrest) {

        int ZERO = 0;
        int ONE = 100;
        int TWO = 200;
        int THREE = 300;
        int FOUR = 400;
        int FIVE = 500;
        int SIX = 600;
        int SEVEN = 700;
        int EIGHT = 800;
        int NINE = 900;
        int TEN = 1000;
        int ELEVEN = 1100;
        int TWELVE = 1200;
        int THIRT = 1300;
        int FOURT = 1400;
        int FIFT = 1500;
        int SIXT = 1600;
        int SEVENT = 1700;
        int EIGHTT = 1800;
        int NINET = 1900;
        int TWENTY = 2000;
        int TWENTONE = 2100;
        int TWENTTWO = 2200;
        int TWENTTHREE = 2300;
        int TWENTFOUR = 2400;

        SimpleDateFormat dateFormatter = new SimpleDateFormat("kkmm");

        dayOfWeekFrequency = new int[7][24];

        for (Email e : emailsOfIntrest) {
            int dayOfWeek = e.getDayOfWeek() - 1;
            Date d = e.getDate();
            String time = dateFormatter.format(d);
            int t = Integer.parseInt(time);

            if (t >= ZERO && t < ONE) dayOfWeekFrequency[dayOfWeek][0]++;
            else if (t >= ONE && t < TWO) dayOfWeekFrequency[dayOfWeek][1]++;
            else if (t >= TWO && t < THREE) dayOfWeekFrequency[dayOfWeek][2]++;
            else if (t >= THREE && t < FOUR) dayOfWeekFrequency[dayOfWeek][3]++;
            else if (t >= FOUR && t < FIVE) dayOfWeekFrequency[dayOfWeek][4]++;
            else if (t >= FIVE && t < SIX) dayOfWeekFrequency[dayOfWeek][5]++;
            else if (t >= SIX && t < SEVEN) dayOfWeekFrequency[dayOfWeek][6]++;
            else if (t >= SEVEN && t < EIGHT) dayOfWeekFrequency[dayOfWeek][7]++;
            else if (t >= EIGHT && t < NINE) dayOfWeekFrequency[dayOfWeek][8]++;
            else if (t >= NINE && t < TEN) dayOfWeekFrequency[dayOfWeek][9]++;
            else if (t >= TEN && t < ELEVEN) dayOfWeekFrequency[dayOfWeek][10]++;
            else if (t >= ELEVEN && t < TWELVE) dayOfWeekFrequency[dayOfWeek][11]++;
            else if (t >= TWELVE && t < THIRT) dayOfWeekFrequency[dayOfWeek][12]++;
            else if (t >= THIRT && t < FOURT) dayOfWeekFrequency[dayOfWeek][13]++;
            else if (t >= FOURT && t < FIFT) dayOfWeekFrequency[dayOfWeek][14]++;
            else if (t >= FIFT && t < SIXT) dayOfWeekFrequency[dayOfWeek][15]++;
            else if (t >= SIXT && t < SEVENT) dayOfWeekFrequency[dayOfWeek][16]++;
            else if (t >= SEVENT && t < EIGHTT) dayOfWeekFrequency[dayOfWeek][17]++;
            else if (t >= EIGHTT && t < NINET) dayOfWeekFrequency[dayOfWeek][18]++;
            else if (t >= NINET && t < TWENTY) dayOfWeekFrequency[dayOfWeek][19]++;
            else if (t >= TWENTY && t < TWENTONE) dayOfWeekFrequency[dayOfWeek][20]++;
            else if (t >= TWENTONE && t < TWENTTWO) dayOfWeekFrequency[dayOfWeek][21]++;
            else if (t >= TWENTTWO && t < TWENTTHREE) dayOfWeekFrequency[dayOfWeek][22]++;
            else if (t >= TWENTTHREE && t < TWENTFOUR) dayOfWeekFrequency[dayOfWeek][23]++;
        }

        return dayOfWeekFrequency;
    }

    public void differenceMinMax(){

        if(this.frequencyDifference < 0) {

            int[][] heatMap = getDayOfWeekFrequency();
            int min = heatMap[0][0];
            int max = heatMap[0][0];
            for (int i = 0; i < heatMap.length; i++) {
                for (int j = 0; j < heatMap[i].length; j++) {
                    if (heatMap[i][j] < min) min = heatMap[i][j];
                    else if (heatMap[i][j] > max) max = heatMap[i][j];
                }
            }

            this.frequencyDifference = max - min;
        }

    }

    public String getColorForHeatMap(int i){

        differenceMinMax();

        int diff = this.frequencyDifference;

        float h = .75f;
        float s = 1f;
        float b = 1f / (float)diff * i;


        if(i == 0)
            return "-fx-background-color: transparent;";
        else
            return "-fx-background-color: #" + Integer.toHexString((Color.HSBtoRGB(h, s, b)));
    }

    public int[][] getDayOfWeekFrequency() { return dayOfWeekFrequency; }

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

    public ArrayList<Email> getEmails() {
        return emails;
    }

    public static String getDay(int i){
        switch(i) {
            case 0:
                return "Sun";
            case 1:
                return "Mon";
            case 2:
                return "Tue";
            case 3:
                return "Wed";
            case 4:
                return "Thu";
            case 5:
                return "Fri";
            case 6:
                return "Sat";
            default:
                return "";
        }
    }
}


