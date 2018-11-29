package datamea.backend;


import junit.framework.TestCase;

import javax.mail.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class EmailTest extends TestCase {
    private final File TEXT_FILE1 = new File("TestEmails/1239784613000.txt");
    private final Email EMAIL = new Email(TEXT_FILE1);
    private final File TEXT_FILE2 = new File("TestEmails/1284278477000.txt");
    private final Email EMAIL2 = new Email(TEXT_FILE2);
    private final File TEXT_FILE3 = new File("TestEmails/1322486502000.txt");
    private final Email EMAIL3 = new Email(TEXT_FILE3);
    private final File TEXT_FILE4 = new File("TestEmails/1428001431000.txt");
    private final Email EMAIL4 = new Email(TEXT_FILE4);

    public void setUp() throws Exception {
        super.setUp();
        ArrayList<Email> testEmails = new ArrayList<>();
        testEmails.add(EMAIL);
        testEmails.add(EMAIL2);
        testEmails.add(EMAIL3);
        testEmails.add(EMAIL4);

        //manually create info for testUser
    }

    //still needs work
    public void testRecoverEmail() {
        assertEquals(EMAIL.getSentimentScores()[0],2 );
        assertEquals(EMAIL.getSentimentScores()[4], 4);

        //check for changed values in txt file

    }

    //Done
    public void testGetOverallSentimentDbl() {
        int[] testArray = new int[]{1,1,1,1,1};
        int [] testArray2 = new int[]{2,1,3,4,2};
        assertEquals(Email.getOverallSentimentDbl(testArray), 50.0);
        assertEquals(Email.getOverallSentimentDbl(testArray2), 62.5);
    }

    //Done
    public void testAnalyzeSentiment() {
        String text = "This is a test string to be used for sentiment analysis.";

        Sentiment sentiment = Email.analyzeSentiment(text);

        assertFalse(sentiment == null);
    }

    //ask Andy what to send filter
    //mentioned using real email body for this test
    public void testFilter() {
    }

    //Done
    public void testGetDayOfWeek() {
        assertEquals(EMAIL.getDayOfWeek(), 4);
        assertEquals(EMAIL2.getDayOfWeek(), 1);
        assertEquals(EMAIL3.getDayOfWeek(), 2);
        assertEquals(EMAIL4.getDayOfWeek(), 5);
    }

    //Done
    public void testExtractAttachments() throws MessagingException, IOException {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        Message[] messages = null;
        Email e = null;

        ArrayList<String> expected = new ArrayList<>(3);
        expected.add(".png");
        expected.add(".jpg");
        expected.add(".jpg");

        try {
            store.connect("imap.gmail.com", "tdominick.test@gmail.com", "DataMeacsc480");
            Folder folder = store.getFolder("TEST");
            folder.open(Folder.READ_ONLY);
            messages = folder.getMessages();
            e = new Email(messages[0], null, false);
            folder.close();

            assertEquals(expected, e.extractAttachments());

        } catch (MessagingException er) {
            er.printStackTrace();
        }

    }

    //test fails when it shouldn't - bug
    public void testIsAnswered() {
        assertFalse(EMAIL.isAnswered());
        assertTrue(EMAIL4.isAnswered());
    }

    //Done
    public void testGetSentimentScores() {
        int[] testScores = new int[]{2,3,4,5,4};
        for(int i = 0; i < 5; i++){
            assertEquals(EMAIL.getSentimentScores()[i], testScores[i]);
        }
    }

    //Done
    public void testGetDate() {
        Date date = new Date(1239784613000L);
        Date date2 = new Date(1284278477000L);
        Date date3 = new Date(1322486502000L);
        Date date4 = new Date(1428001431000L);

        assertEquals(EMAIL.getDate(), date);
        assertEquals(EMAIL2.getDate(), date2);
        assertEquals(EMAIL3.getDate(), date3);
        assertEquals(EMAIL4.getDate(), date4);
    }

    //Done
    public void testGetSender() {
        Sender testSender1 = new Sender("\"Helbach, Christoph\" <Christoph.Helbach@ibes.uni-due.de>");
        Sender testSender2 = new Sender("Promovierendenforum <profor@uni-due.de>");
        Sender testSender3 = new Sender("fortbildungsteam@uni-due.de");
        Sender testSender4 = new Sender("\"Marten, Kevin\" <Kevin.Marten@paluno.uni-due.de>");

        boolean test_1 = EMAIL.getSender().filterName().equals(testSender1.filterName());
        boolean test_2 = EMAIL2.getSender().filterName().equals(testSender2.filterName());
        boolean test_3 = EMAIL3.getSender().filterName().equals(testSender3.filterName());
        boolean test_4 = EMAIL4.getSender().filterName().equals(testSender4.filterName());

        assertTrue(test_1);
        assertTrue(test_2);
        assertTrue(test_3);
        assertTrue(test_4);
    }

    // needs to be done better
    public void testDetectLanguage(){
        String testLanguage = "Hello, World. This is a test";
        assertEquals(EMAIL.detectLanguage(testLanguage), "en");
    }

    // flags are broken?
    public void testGetFlags() {
        assertEquals(EMAIL.getFlags(), ("\\" + "Seen"));
    }

    //Done
    public void testGetFolder() {
        assertEquals(EMAIL.getFolder(), "Archiv UniDuE Adresse");
        assertEquals(EMAIL4.getFolder(),"Betreute Arbeiten");
    }

    //Done
    public void testGetSubFolder() {
        assertEquals(EMAIL.getSubFolder(), "Archiv UniDuE Adresse");
        assertEquals(EMAIL4.getSubFolder(), "Betreute Arbeiten");
    }

    //Done
    public void testGetAttachments() {
        ArrayList<String> testAttachments = new ArrayList<>();
        testAttachments.add(".pdf");
        testAttachments.add(" .pdf");
        testAttachments.add(" .txt");

        for(int i = 0; i< testAttachments.size(); i++){
            assertEquals(testAttachments.get(i), EMAIL3.getAttachments().get(i));
        }

    }

    //Done
    public void testGetLanguage() {
        boolean test_1 = EMAIL.getLanguage().equals("unk");
        boolean test_2 = EMAIL2.getLanguage().equals("en");
        assertTrue(test_1);
        assertTrue(test_2);
    }

    //Done
    public void testGetDomain() {
        boolean same = EMAIL.getDomain(false).get(0).trim().equals("@ibes.uni-due.de");
        boolean same1 = EMAIL2.getDomain(false).get(0).trim().equals("@uni-due.de");
        boolean same2 = EMAIL3.getDomain(false).get(0).trim().equals("@uni-due.de");
        boolean same3 = EMAIL4.getDomain(false).get(0).trim().equals("@paluno.uni-due.de");
        assertTrue(same);
        assertTrue(same1);
        assertTrue(same2);
        assertTrue(same3);
    }


}