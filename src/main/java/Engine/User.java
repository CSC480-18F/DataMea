package Engine;

import Controllers.DashboardController;
import Controllers.DashboardLoading;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.tools.TreeNode;
import javafx.application.Platform;
import javax.mail.*;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public static ArrayList<javafx.scene.paint.Color> colors = new ArrayList<>();
    private static int totalNumberOfEmails = 0;
    private DashboardLoading dashboardLoading;
    private int totalProgress = 0;


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



    //TODO | have the sentiment guage take in a paramater - notably, the filtered list of emails so a user can
    //TODO | filter sentiment scores while background sentimenet analysis is running
    public void updateSentimentGaugeFiltered(ArrayList<Email> emails) {

        Platform.runLater(()->{
            DashboardController.sentimentGauge.setValue(Email.getOverallSentimentDbl(getOverallSentiment()));
        });
    }



    public int [] getOverallSentiment() {
        ArrayList <Email> emails = recoverSerializedEmails();
        int [] sentiment = {0,0,0,0,0};
        for (Email e: emails) {
            for (int i = 0; i<sentiment.length;i++) {
                sentiment[i] += e.getSentimentScores()[i];
            }
        }
        return sentiment;
    }

    //TODO
    public int getReplyFrequency(ArrayList<Email> emails){
        int replied = 0;
        for(Email e : emails){
            if(e.getFlags().contains(Flags.Flag.ANSWERED))
                replied++;
        }


        return 0;
    }


    public Map<String, Long> getDomainFreq(ArrayList<Email> emails){
        //TODO; refine filters to remove weird chars

        ArrayList<String> domains = new ArrayList<>();
        for (Email e: emails) {
                String address = e.getSender().getAddress().substring(e.getSender().getAddress().indexOf("@"));
                int quoteLocation = address.indexOf("\"" /*,address.indexOf("\"")+1*/);
                int caratLocation = address.indexOf(">");
                String d;

                int earlierLocation = -1;

                if (quoteLocation < caratLocation && quoteLocation!=-1) {
                    earlierLocation = quoteLocation;
                } else {
                    if (caratLocation != -1) {
                        earlierLocation = caratLocation;
                    }
                }

                if (earlierLocation == -1) {
                    //none of the weird characters are found
                    domains.add(address);
                } else {
                    //some weird characters are found
                    d = address.substring(address.indexOf("@"), earlierLocation);
                    domains.add(d);
                }

        }

        String [] doms = new String[domains.size()];
        doms = domains.toArray(doms);


        Map<String, Long> freqs =
                Stream.of(doms)
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        freqs = new TreeMap<String, Long>(freqs);

        Map sorted = sortByValues(freqs);

        return sorted;
    }

    public Map<String, Long> getAttachmentFreq(ArrayList<Email> emails){
        ArrayList<String> aTypes = new ArrayList<>();
        for (Email e: emails) {
            //get everything after the @ symbol
            ArrayList<String> atts = e.getAttachments();
            if(atts != null){
                aTypes.addAll(atts);
            }
        }
        String [] aTypesAry = new String[aTypes.size()];
        aTypesAry = aTypes.toArray(aTypesAry);
        String [] whiteSpaceRemoved = new String[aTypes.size()];

        //this below removes whitespace before and after... Temporary fix
        for (int i = 0; i<aTypes.size(); i++) {
            String attachment = aTypes.get(i).trim();
            whiteSpaceRemoved[i] = attachment;
        }


        Map<String, Long> freqs =
                Stream.of(whiteSpaceRemoved)
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        freqs = new TreeMap<String, Long>(freqs);

        Map sorted = sortByValues(freqs);

        return sorted;
    }


    public Map<String, Long> getSendersFreq(ArrayList<Email> emails) {
        ArrayList<String> senders = new ArrayList<>();
        for (Email e : emails) {
            senders.add(e.getSender().getAddress());
        }

        String [] sendersArray = new String[senders.size()];
        sendersArray = senders.toArray(sendersArray);

        Map<String, Long> freqs =
                Stream.of(sendersArray)
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        freqs = new TreeMap<String, Long>(freqs);

        Map sorted = sortByValues(freqs);
        return sorted;


    }

    public Map<String, Long> getLanguageFreq(ArrayList<Email> emails){
        ArrayList<String> langs = new ArrayList<>();
        for (Email e: emails) {
            //get everything after the @ symbol
            String l = e.getLanguage();
            if(!l.equals("unk")){
                langs.add(l);
            }
        }
        String [] langsAry = new String[langs.size()];
        langsAry = langs.toArray(langsAry);


        Map<String, Long> freqs =
                Stream.of(langsAry)
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        freqs = new TreeMap<String, Long>(freqs);

        Map sorted = sortByValues(freqs);

        return sorted;
    }

    public static <K, V extends Comparable<V>> Map<K, V>
    sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator =
                new Comparator<K>() {
                    public int compare(K k1, K k2) {
                        int compare =
                                map.get(k2).compareTo(map.get(k1));
                        if (compare == 0)
                            return 1;
                        else
                            return compare;
                    }
                };

        Map<K, V> sortedByValues =
                new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }


    /**
     *
     * @param folderName Name of the folder to filter by
     * @param subFolderName Name of the subfolder to filter by
     * @param emailsToFilter ArrayList of emails to filter
     * @return ArrayList of filtered emails
     */
    public ArrayList<Email> filterByFolder(String folderName, String subFolderName, ArrayList<Email> emailsToFilter) {
        ArrayList<Email> filteredEmails = new ArrayList<>();

        if (subFolderName == null && folderName.equals("All")) {
            return emailsToFilter;
        } else if (subFolderName == null) {
            for (Email e: emailsToFilter) {
                if (e.getFolder().equalsIgnoreCase(folderName)) {
                    filteredEmails.add(e);
                }
            }
            return filteredEmails;
        }

        for (Email e: emailsToFilter) {
            if (e.getFolder().equalsIgnoreCase(folderName) && e.getSubFolder().equalsIgnoreCase(subFolderName)) {
                filteredEmails.add(e);
            }
        }
        return filteredEmails;
    }
    /** filter for returning emails from a specific sender
     * @param sender
     * @param emailsToFilter current list of emails
     * @return  ArrayList of filtered emails
     **/
    public ArrayList<Email> filterbySender(String sender, ArrayList<Email> emailsToFilter){
        ArrayList<Email> filteredEmails = new ArrayList<Email>();
        for(Email e: emailsToFilter){
            String s = e.getSender().filterName();
            if (s.equalsIgnoreCase(sender)){
                filteredEmails.add(e);
            }
        }
        return filteredEmails;
    }

    /**
     * Filter for returning emails within a date range
     * @param startDate only emails after this date will be returned
     * @param endDate only emails before this date will be returned
     * @param emailsToFilter ArrayList of emails to filter
     * @return ArrayList of filtered emails
     */
    public ArrayList<Email> filterByDate(Date startDate, Date endDate, ArrayList<Email> emailsToFilter){
        ArrayList<Email> filteredEmails = new ArrayList<>();
        for (Email e: emailsToFilter){
            if(e.getDate().after(startDate) && e.getDate().before(endDate)){
                filteredEmails.add(e);
            }
        }
        return filteredEmails;
    }

    /**
     * Filter for returning emails sent from specified domain
     * @param domain domain to filter by
     * @param emailsToFilter ArrayList of emails to filter
     * @return ArrayList of filtered emails
     */
    public ArrayList<Email> filterByDomain(String domain, ArrayList<Email> emailsToFilter){
        ArrayList<Email> filteredEmails = new ArrayList<>();
        for (Email e: emailsToFilter){
            if(e.getDomain().equalsIgnoreCase(domain)){
                filteredEmails.add(e);
            }
        }
        return filteredEmails;
    }

    /**
     * Filter for returning emails sent with specified attachment
     * @param attachmentType attachment to filter by
     * @param emailsToFilter ArrayList of emails to filter
     * @return ArrayList of filtered emails
     */

    public ArrayList<Email> filterByAttachmentType(String attachmentType, ArrayList<Email> emailsToFilter){
        ArrayList<Email> filteredEmails = new ArrayList<>();
        for (Email e: emailsToFilter){
            if(e.getAttachments().size() != 0){
                for(String attachment: e.getAttachments()){
                    if(attachmentType.equalsIgnoreCase(attachment))
                        filteredEmails.add(e);
                }
            }
        }
        return filteredEmails;
    }

    /**
     * function to control the filters
     * @param folder folder to filter by
     * @param subfolder subfolder to filter by
     * @param startDate beginning date to filter by
     * @param endDate ending date to filter by
     * @param sender sender to filter by
     * @param domain domain to filter by
     * @param attachment attachment to filter by
     * @return ArrayList of emails after each of the filters
     */

    public ArrayList<Email> filter(String folder, String subfolder, Date startDate, Date endDate, String sender, String domain, String attachment){
        ArrayList<Email> filteredEmails = new ArrayList<>();
        if(folder != null || subfolder != null)
            filteredEmails = filterByFolder(folder, subfolder, this.emails);
        if(startDate != null && endDate != null){
            if (filteredEmails.size() == 0)
                filteredEmails = filterByDate(startDate, endDate, this.emails);
            else filteredEmails = filterByDate(startDate, endDate, filteredEmails);
        }
        if(sender != null){
            if(filteredEmails.size() == 0){
                filteredEmails = filterbySender(sender, this.emails);
            }else filteredEmails = filterbySender(sender, filteredEmails);
        }
        if(domain != null){
            if(filteredEmails.size()==0){
                filteredEmails = filterByDomain(domain, this.emails);
            }else filteredEmails = filterByDomain(domain, filteredEmails);
        }
        if(attachment != null){
            if(filteredEmails.size()==0){
                filteredEmails = filterByAttachmentType(attachment, this.emails);
            }else filteredEmails = filterByAttachmentType(attachment, filteredEmails);
        }

        if (folder == null && subfolder == null && startDate == null && endDate==null && sender == null && domain == null && attachment==null) {
            //no folder was selected so just return all of the emails
            return this.emails;
        }

        return filteredEmails;
    }

    // domain filter, attachment filter,

    public TreeNode getFoldersCountForSunburst(){

        colors.add(javafx.scene.paint.Color.valueOf("#fc5c65"));
        colors.add(javafx.scene.paint.Color.valueOf("#fd9644"));
        colors.add(javafx.scene.paint.Color.valueOf("#fed330"));
        colors.add(javafx.scene.paint.Color.valueOf("#26de81"));
        colors.add(javafx.scene.paint.Color.valueOf("#2bcbba"));
        colors.add(javafx.scene.paint.Color.valueOf("#eb3b5a"));
        colors.add(javafx.scene.paint.Color.valueOf("#fa8231"));
        colors.add(javafx.scene.paint.Color.valueOf("#f7b731"));
        colors.add(javafx.scene.paint.Color.valueOf("#20bf6b"));
        colors.add(javafx.scene.paint.Color.valueOf("#0fb9b1"));
        colors.add(javafx.scene.paint.Color.valueOf("#45aaf2"));
        colors.add(javafx.scene.paint.Color.valueOf("#4b7bec"));
        colors.add(javafx.scene.paint.Color.valueOf("#a55eea"));
        colors.add(javafx.scene.paint.Color.valueOf("#d1d8e0"));
        colors.add(javafx.scene.paint.Color.valueOf("#778ca3"));
        colors.add(javafx.scene.paint.Color.valueOf("#2d98da"));
        colors.add(javafx.scene.paint.Color.valueOf("#3867d6"));
        colors.add(javafx.scene.paint.Color.valueOf("#8854d0"));
        colors.add(javafx.scene.paint.Color.valueOf("#a5b1c2"));
        colors.add(javafx.scene.paint.Color.valueOf("#4b6584"));

        TreeNode treeRoot   = new TreeNode(new ChartData("ROOT"));

        int colorCount = 0;

        for (UserFolder uf: folders) {
            int numEmailsInFolder = getNumEmailsInFolder(uf.getFolderName());
            TreeNode temp = new TreeNode(new ChartData(uf.folderName, numEmailsInFolder, colors.get(colorCount)), treeRoot);

            for (String f: uf.subFolders) {
                if (!f.equals(uf.folderName)) {
                    //if the folder does not match the subfolder name, add the node normally
                    int numEmailsInSubFolder = getNumEmailsInSubFolder(uf.getFolderName(), f);
                    TreeNode subfold = new TreeNode(new ChartData(f, numEmailsInSubFolder, colors.get(colorCount)), temp);
                } else {
                    //else, add the node but make it invisible so it takes up the correct section of the pie
                    int numEmailsInSubFolder = getNumEmailsInSubFolder(uf.getFolderName(), f);
                    TreeNode subfold = new TreeNode(new ChartData("", numEmailsInSubFolder, javafx.scene.paint.Color.TRANSPARENT), temp);
                }
            }
            if(colorCount < 19) {
                colorCount++;
            }else{
                colorCount = 0;
            }
        }
        return treeRoot;
    }


    public int getNumEmailsInFolder(String folderName) {
        int count = 0;
        for (Email e: getEmails()) {
            if (e.getFolder().equals(folderName)) {
                count++;
            }
        }
        return count;
    }

    public int getNumEmailsInSubFolder(String folderName, String subFolderName) {
        int count = 0;
        for (Email e: getEmails()) {
            if (e.getFolder().equals(folderName) && e.getSubFolder().equals(subFolderName)) {
                count++;
            }
        }
        return count;
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


    public ArrayList<Sender> getTopSendersForFilter(ArrayList<Email> emails) {
        ArrayList<Sender> topSenders = new ArrayList<>();
        ArrayList<String> senderNames = new ArrayList<>();

        for (Email e: emails) {
            if (!senderNames.contains(e.getSender())) {
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
            if (!f.getName().equals("sentimentProgress.txt"))
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

    public void resetUser() throws IOException {

        File user = new File("TextFiles/" + encrypt(email) + "/");

        File[] emails = user.listFiles();
        for(File e : emails){
            e.delete();
        }
        System.out.println(user.delete());

        File userNamesFile = new File("TextFiles/userNames.txt");

        BufferedReader br = new BufferedReader(new FileReader(userNamesFile));


        int count = Integer.parseInt(br.readLine());
        ArrayList<String> userNames = new ArrayList<>();
        System.out.println("Email: " + email);

        for(int i = 0; i < count; i ++){
            String[] name = br.readLine().split(" ");
            String n = name[0];
            if(name.length > 2){
                n = name[0] + " " + name[1];
            }
            System.out.println("decrypted: " + decrypt(n));
            if(!decrypt(n).contains(email)){
                userNames.add(n);
            }
        }
        br.close();

        BufferedWriter bw = new BufferedWriter(new FileWriter(userNamesFile));

        bw.write(Integer.toString(count - 1));

        System.out.println(userNames.toString());

        for(String name : userNames){
            bw.newLine();
            bw.write(encrypt(name));
        }
        bw.close();

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

            totalProgress += i;
            /*Platform.runLater(()->{
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Loading_Screen.fxml"));
                dashboardLoading = loader.getController();
                dashboardLoading.progressBar.setProgress(totalProgress/getTotalNumberOfEmails());
                    });*/
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

                    //write attachments
                    bw.write(e.getAttachments().toString());
                    bw.newLine();

                    //write language
                    String l = e.getLanguage();
                    if(l != null)
                        bw.write(l);
                    else
                        bw.write("unk");

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

        //this right below is simply used to calculate how many emails are in total in the account (look at all folders)
        for (int i = 0; i<folders.length; i++) {
            String name = folders[i].getName();
            if (!name.equalsIgnoreCase("[Gmail]")) {
                folders[i].open(Folder.READ_ONLY);
                Message [] messages = folders[i].getMessages();
                totalNumberOfEmails += messages.length;
                folders[i].close();
            }

        }



        //this right below is simply used to calculate how many emails are in total in the account (look at all folders)
        for (int i = 0; i<folders.length; i++) {
            String name = folders[i].getName();
            if (!name.equalsIgnoreCase("[Gmail]")) {
                folders[i].open(Folder.READ_ONLY);
                Message [] messages = folders[i].getMessages();
                totalNumberOfEmails += messages.length;
                folders[i].close();
            }

        }

        for (int i = 0; i < folders.length; i++) {
            String name = folders[i].getName();
            if (!name.equalsIgnoreCase("[Gmail]") /*&& !name.equalsIgnoreCase("inbox")*/ ) {
                /// Create a new thread to do this!!!!!!!
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
        try {
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
        }
        catch(NullPointerException e){
            System.out.println("Null Pointer encountered while decrypting in User.decrypt()");
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

    public int differenceMinMax(int[][] heatMap){


        int min = heatMap[0][0];
        int max = heatMap[0][0];
        for (int i = 0; i < heatMap.length; i++) {
            for (int j = 0; j < heatMap[i].length; j++) {
                if (heatMap[i][j] < min) min = heatMap[i][j];
                else if (heatMap[i][j] > max) max = heatMap[i][j];
            }
        }

        int frequencyDifference = max - min;

        return frequencyDifference;

    }

    public String getColorForHeatMap(int i, int[][] heatMap){

        int diff = differenceMinMax(heatMap);

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

    public static int getTotalNumberOfEmails() {
        return totalNumberOfEmails;
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


