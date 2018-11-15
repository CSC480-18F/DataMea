package datamea.backend;


import junit.framework.TestCase;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class EmailTest extends TestCase {
    private final File TEXT_FILE = new File("TextFiles/xiqrptkgn2ygx{Fiqdmq0hvs/1234788438000.txt");
    private final Email EMAIL = new Email(TEXT_FILE);

    public void testRecoverEmail() {
        ;
    }

    public void testGetOverallSentimentDbl() {
        int[] testArray = new int[]{1,1,1,1,1};
        assertEquals(Email.getOverallSentimentDbl(testArray), 50.0);
    }

    //ask how to test
    public void testAnalyzeSentiment() {
    }

    //ask what to send filter
    public void testFilter() {
    }

    public void testGetDayOfWeek() {
        assertEquals(EMAIL.getDayOfWeek(), 2);
    }

    //need help
    public void testExtractAttachments() throws MessagingException, IOException {
        ArrayList<String> testAttachments = new ArrayList<>();
        ArrayList<String> expected = new ArrayList<>();
        try{
            testAttachments = EMAIL.extractAttachments();
        }catch (StringIndexOutOfBoundsException s){
            System.out.println(s.getMessage());
        }

        assertEquals(testAttachments, expected);

    }

    public void testIsAnswered() {
        assertFalse(EMAIL.isAnswered());
    }




    public void testGetSentimentScores() {
        boolean test = true;
        for(int i: EMAIL.getSentimentScores()){
            if(i != 0){
                test = false;
            }
        }
        assertTrue(test);
    }


    public void testGetSentimentPctStr() {

    }

    public void testGetDate() {
        Date date = new Date(1234788438000L);
        assertEquals(EMAIL.getDate(), date);
    }

    public void testGetSender() {
        Sender testSender = new Sender(User.decrypt("[jdwljcowmtp%6&Cvqi%Tjuykrj$AyjixghdoyktuFwrl1iwj5jgB"));
        assertEquals(EMAIL.getSender(),testSender);
    }

    public void testDetectLanguage(){
        String testLanguage = "Hello, World. This is a test";
        assertEquals(EMAIL.detectLanguage(testLanguage), "en");
    }

    public void testGetFlags() {
        assertEquals(EMAIL.getFlags(), ("\\" + "Seen"));
    }

    public void testGetFolder() {
        assertEquals(EMAIL.getFolder(), User.decrypt("Ewemp|\"YqmIwJ'Gfvhwxg"));
    }

    public void testGetSubFolder() {
        assertEquals(EMAIL.getSubFolder(), User.decrypt("Ewemp|\"YqmIwJ'Gfvhwxg"));
    }

    public void testGetAttachments() {
        ArrayList<String> testAttachments = new ArrayList<>();
        assertEquals(EMAIL.getAttachments(), testAttachments);
    }

    public void testGetLanguage() {
        assertEquals(EMAIL.getLanguage(),"unk");
    }

    public void testGetDomain() {
        assertEquals(EMAIL.getDomain(false), "[@uni-due.de]");
    }


}