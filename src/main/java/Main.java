

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.*;
import org.apache.xpath.operations.Bool;

public class Main {


    static class Sender {

        String address;
        int count = 1;

        public Sender(String address) {
            this.address = address;
        }

        public String toString() {
            return address + " number of emails sent: " + count;
        }

    }



    static class Email {

        Message message;
        int [] sentimentScores;
        int overallSentiment;
        String content, title;
        Date date;
        Sender sender;
        Flags flags;
        int VNEG = 0;
        int NEG = 1;
        int NEU = 2;
        int POS = 3;
        int VPOS = 4;
        int VMULT = 3;

        public Email(Message m, Sender s, Boolean runSentiment) {

            message = m;

            try{
                content = getTextFromMessage(m);
                title = m.getSubject();
                sender = new Sender(s.toString());
                date = m.getSentDate();
                flags = m.getFlags();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (MessagingException e) {
                e.printStackTrace();
            }


            if(runSentiment){
                sentimentScores = analyzeSentiment(filter(content));
                overallSentiment = sentimentScores[VPOS] * VMULT + sentimentScores[POS] -
                        sentimentScores[NEG] - sentimentScores[VNEG] * VMULT;
            }
        }




        public String toString() {
            return "From: " + this.sender + "\nTitle:" + this.title + "\nDate: " + date + "\nFlags: " + flags.toString()
                    + "\n" + content;
        }


    }


    public static void main(String[] args) throws FileNotFoundException {

        // get user Email address, password
        String password, address, selectedFolderAsString;
        Scanner kb = new Scanner(System.in);
        System.out.println("Enter email address");
        address = kb.nextLine();
        System.out.println("Enter password for " + address);
        password = kb.nextLine();

        //recover the list of folders in the users email account
        Folder[] folders = printFolders(address, password);

        System.out.println("Select which folder to get emails from (type 0-" + (folders.length - 1) + ")");

        //grab selected folder from user
        Folder selectedFolder = folders[Integer.parseInt(kb.nextLine())];

        long startTime = System.nanoTime();
        //read and print all emails from the selected folder
        readEmails(password, address, selectedFolder);
        endTimer(startTime);
    }




/*

This function was modified from an existing function by ItsCuties from the site below

http://www.itcuties.com/java/javamail-read-email/

Some additional notes:
-whenever something like javax.mail.internet.MimeMultipart@396f6598 appears as the message content,
it appears to be whenever there is a thread of replies

-anything (from what i've checked) that is html, is a mass email

 */

    private static void readEmails(String password, String address, Folder selectedFolder) {
        // Create all the needed properties - empty!
        Properties connectionProperties = new Properties();
        // Create the session
        Session session = Session.getDefaultInstance(connectionProperties, null);

        ArrayList<Sender> senders = new ArrayList<>();
        ArrayList<Email> emails = new ArrayList<>();

        String selectedFolderAsString = selectedFolder.toString();

        try {
            System.out.print("Connecting to the IMAP server...");
            String storeName = "imaps";
            Store store = session.getStore(storeName);

            // Set the server depending on the parameter flag value
            String server =  "imap.gmail.com";
            store.connect(server, address, password);

            System.out.println("Connected!");


            // Set the mode to the read-only mode
            selectedFolder.open(Folder.READ_ONLY);

            // Get messages
            Message messages[] = selectedFolder.getMessages();

            System.out.println("Reading messages...");

            // Display the messages
            for (Message message : messages) {

                Sender current = null;

                for (Address a : message.getFrom()) {
                    current = null;
                    boolean found = false;
                    for (Sender s : senders) {
                        if (s.address.equals(a.toString())) {
                            s.count++;
                            found = true;
                            current = s;
                            break;
                        }
                    }
                    if (!found) {
                        current = new Sender(a.toString());
                        senders.add(current);
                    }

                }


                emails.add(new Email(message, current, true));

                System.out.println("Email sentiment score: " + emails.get(emails.size() - 1).overallSentiment);

            }

            System.out.println("\nNumber of emails in '" + selectedFolderAsString + "' folder: " + emails.size());
            System.out.println("\nsummary of senders: \n");
            for (Sender s: senders) {
                System.out.println(s.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static Folder[] printFolders(String address, String password) {

        try {
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", address, password);
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

    private static int[] analyzeSentiment(String message) {

        System.out.println("Start Time: " + getCurrentTimeStamp());

        int[] score = new int[5];

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        //System.out.println("Processing annotation");
        Annotation annotation = pipeline.process(message);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);


        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            //System.out.println("Sentiment: " + sentiment + "\t" + sentence);
            switch (sentiment){
                case "Very Negative":
                    score[0]++;
                    break;
                case "Negative":
                    score[1]++;
                    break;
                case "Neutral":
                    score[2]++;
                    break;
                case "Positive":
                    score[3]++;
                    break;
                case "Very Positive":
                    score[4]++;
                    break;
            }
        }

        System.out.println("End Time: " + getCurrentTimeStamp());

        return score;
    }

    private static String getTextFromMessage(Message message) throws MessagingException, IOException {
        //System.out.println("Getting text from message");
        String result = "";
        if (message.isMimeType("text/plain")) {
            //System.out.println("Message is plain text");
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            //System.out.println("Message is multipart");
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private static String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            //System.out.println("Body Part: " + (i + 1));
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                //System.out.println("Body part is plain text");
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                //System.out.println("Body part is HTML");
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                //System.out.println("Body part is another MimeMultipart object");
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public static String filter(String text){
        String regex = "[`,~,*,#,^,\\n,\\t]";
        String newText = text.replaceAll(regex, "");
        //System.out.println("AFTER REGEX FILTER:\n" + newText);
        return newText;
    }

    public static void endTimer(long startTime) {
        long endTime = System.nanoTime();
        long totalTime = (endTime - startTime) / 1000000000;
        System.out.println("Total runtime: " + totalTime + " seconds");
    }

}