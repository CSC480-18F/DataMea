

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
        int [] sentiementScores;
        String content, title;
        Date date;
        Sender sender;

        public Email(Message m) {
            message = m;
            try{
                content = m.getContent().toString();
                title = m.getSubject();
                sender = new Sender(m.getFrom()[0].toString());
                date = m.getSentDate();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            
            sentiementScores = new int [5];
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

        String selectedFolderAsString = selectedFolder.toString();

        Flags seen = new Flags(Flags.Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
        Message unreadMessages[] = new Message[0];


        Flags answered = new Flags(Flags.Flag.ANSWERED);
        FlagTerm answeredFlagTerm = new FlagTerm(answered, false);
        Message answeredMessages[] = new Message[0];



        try {
            System.out.print("Connecting to the IMAP server...");
            // Connecting to the server
            // Set the store depending on the parameter flag value
            String storeName = "imaps";
            Store store = session.getStore(storeName);

            // Set the server depending on the parameter flag value
            String server =  "imap.gmail.com";
            store.connect(server, address, password);

            System.out.println("Connected!");

            // Get the Inbox folder

            // Set the mode to the read-only mode
            selectedFolder.open(Folder.READ_ONLY);

            // Get messages
            Message messages[] = selectedFolder.getMessages();

            unreadMessages = selectedFolder.search(unseenFlagTerm);
            answeredMessages = selectedFolder.search(answeredFlagTerm);

            if (unreadMessages.length == 0) System.out.println("No unread messages in " + selectedFolderAsString);
            else System.out.println("You have " + unreadMessages.length + " unread messages in " + selectedFolderAsString);

            if (answeredMessages.length == 0) System.out.println("You haven't answered any messages");
            else System.out.println("You have answered " + answeredMessages.length + " from " + selectedFolderAsString);

            System.out.println("Reading messages...");

            int numEmails = 0;

            // Display the messages
            for (Message message : messages) {
                numEmails++;
                for (Address a : message.getFrom()) {
                    System.out.println("From:" + a);


                    boolean found = false;
                    for (Sender s : senders) {
                        if (s.address.equals(a.toString())) {
                            s.count++;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        senders.add(new Sender(a.toString()));
                    }

                }
                System.out.println("Title: " + message.getSubject());

                System.out.println(message.getSentDate());

                System.out.println();

                String messageText;
                messageText = getTextFromMessage(message);

                analyzeSentiment(filter(messageText));

                System.out.println("------");


            }

            System.out.println("\nNumber of emails in '" + selectedFolderAsString + "' folder: " + numEmails);
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

    private static void analyzeSentiment(String message) {

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        System.out.println("Processing annotation");
        Annotation annotation = pipeline.process(message);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        System.out.println("Start Time: " + getCurrentTimeStamp());

        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            System.out.println("Sentiment: " + sentiment + "\t" + sentence);
        }

        System.out.println("End Time: " + getCurrentTimeStamp());
    }

    private static String getTextFromMessage(Message message) throws MessagingException, IOException {
        System.out.println("Getting text from message");
        String result = "";
        if (message.isMimeType("text/plain")) {
            System.out.println("Message is plain text");
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            System.out.println("Message is multipart");
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
            System.out.println("Body Part: " + (i + 1));
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                System.out.println("Body part is plain text");
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                System.out.println("Body part is HTML");
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                System.out.println("Body part is another MimeMultipart object");
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
        System.out.println("AFTER REGEX FILTER:\n" + newText);
        return newText;
    }

    public static void endTimer(long startTime) {
        long endTime = System.nanoTime();
        long totalTime = (endTime - startTime) / 1000000000;
        System.out.println("Total runtime: " + totalTime + " seconds");
    }

}