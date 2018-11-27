package datamea.backend;

import junit.framework.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class UserTest extends TestCase {

    User testUser = new User("tdominick.test@gmail.com", "DataMeacsc480", false);
    private final File TEXT_FILE1 = new File("TestEmails/1239784613000.txt");
    private final Email EMAIL1 = new Email(TEXT_FILE1);
    private final File TEXT_FILE2 = new File("TestEmails/1284278477000.txt");
    private final Email EMAIL2 = new Email(TEXT_FILE2);
    private final File TEXT_FILE3 = new File("TestEmails/1322486502000.txt");
    private final Email EMAIL3 = new Email(TEXT_FILE3);
    private final File TEXT_FILE4 = new File("TestEmails/1428001431000.txt");
    private final Email EMAIL4 = new Email(TEXT_FILE4);
    ArrayList<Email> testEmails = new ArrayList<>();
    public void setUp() throws Exception {
        super.setUp();
        testEmails.add(EMAIL1);
        testEmails.add(EMAIL2);
        testEmails.add(EMAIL3);
        testEmails.add(EMAIL4);
        testUser.setEmails(testEmails);
        //manually create info for testUser
    }


    // priority low
    public void testGetOverallSentiment() {

    }

    public void testGetSentimentForFilteredEmails() {

    }

    //Done
    public void testGetReplyFrequency() {
        assertEquals(testUser.getReplyFrequency(testUser.getEmails()),25.0);
    }

    //Done
    public void testGetDomainFreq() {
        Map<String, Long> mapTest1 = testUser.getDomainFreq(testUser.getEmails(),false);
        for(Map.Entry<String, Long> entry: mapTest1.entrySet()){
            if(entry.getKey().equals("@uni-due.de")){
                assertEquals(entry.getValue().intValue(), 2);
            }else if(entry.getKey().equals("@ibes.uni-due.de")){
                assertEquals(entry.getValue().intValue(), 1);
            }else if(entry.getKey().equals("paluno.uni-due.de")){
                assertEquals(entry.getValue().intValue(), 1);
            }
        }
    }

    //Done
    public void testGetAttachmentFreq() {
        Map<String, Long> mapTest1 = testUser.getAttachmentFreq(testUser.getEmails(), false);
        for(Map.Entry<String, Long> entry: mapTest1.entrySet()){
            if(entry.getKey().equals(".pdf")){
                assertEquals(entry.getValue().intValue(), 5);
            }
            else if (entry.getKey().equals(".txt")){
                assertEquals(entry.getValue().intValue(), 3);
            }
            else if (entry.getKey().equals(".htm")){
               assertEquals(entry.getValue().intValue(), 3);
            }
            else if (entry.getKey().equals(".docx")){
                 assertEquals(entry.getValue().intValue(), 1);
            }
            else if (entry.getKey().equals(".eap")){
                assertEquals(entry.getValue().intValue(),1);
            }
        }
    }

    //Done
    public void testGetSendersFreq() {
        Map<String, Long> mapTest1 = testUser.getSendersOrRecipientsFreq(testUser.getEmails(), false);
        for(Map.Entry<String, Long> entry: mapTest1.entrySet()){
            if(entry.getKey().equals("Christoph.Helbach@ibes.uni-due.de")){
                assertEquals(entry.getValue().intValue(), 1);
            }
            else if(entry.getKey().equals("Kevin.Marten@paluno.uni-due.de")){
                assertEquals(entry.getValue().intValue(), 1);
            }
            else if(entry.getKey().equals("fortbildungsteam@uni-due.de")){
                assertEquals(entry.getValue().intValue(),1);
            }
            else if(entry.getKey().equals("profor@uni-due.de")){
                assertEquals(entry.getValue().intValue(), 1);
            }
        }
    }

    //Done
    public void testGetLanguageFreq() {
        Map<String, Long> maptest = testUser.getLanguageFreq(testUser.getEmails(), false);
        for(Map.Entry<String, Long> entry: maptest.entrySet()){
            if(entry.getKey().equals("unk")){
                assertEquals(entry.getValue().intValue(), 2);
            }else if(entry.getKey().equals("en")){
                assertEquals(entry.getValue().intValue(), 1);
            }else if(entry.getKey().equals("de")){
                assertEquals(entry.getValue().intValue(), 1);
            }
        }
    }

    // ask for help
    public void testSortByValues() {
    }

    //Done
    public void testFilterByFolder() {
        ArrayList<Email> filteredEmails1 = testUser.filterByFolder("Archiv UniDuE Adresse","Archiv UniDuE Adresse", testEmails);
        ArrayList<Email> filteredEmails2 = testUser.filterByFolder("Betreute Arbeiten","Betreute Arbeiten", testEmails);
        for(Email e: filteredEmails1){
            assertEquals(e.getFolder(), "Archiv UniDuE Adresse");
        }
        for(Email e: filteredEmails2){
            assertEquals(e.getFolder(), "Betreute Arbeiten");
        }
    }

    //Done
    public void testFilterbySender() {
        ArrayList<Email> filteredEmails1 = testUser.filterbySender("\"Helbach, Christoph\" <Christoph.Helbach@ibes.uni-due.de>", testEmails);
        ArrayList<Email> filteredEmails2 = testUser.filterbySender("Promovierendenforum <profor@uni-due.de>", testEmails);
        ArrayList<Email> filteredEmails3 = testUser.filterbySender("fortbildungsteam@uni-due.de", testEmails);
        ArrayList<Email> filteredEmails4 = testUser.filterbySender("\"Marten, Kevin\" <Kevin.Marten@paluno.uni-due.de>", testEmails);

        for(Email e: filteredEmails1){
            assertEquals(e.getSender().filterName(),"\"Helbach, Christoph\" <Christoph.Helbach@ibes.uni-due.de>" );
        }
        for(Email e: filteredEmails2){
            assertEquals(e.getSender().filterName(), "Promovierendenforum <profor@uni-due.de>");
        }
        for(Email e: filteredEmails3){
            assertEquals(e.getSender().filterName(), "fortbildungsteam@uni-due.de");
        }
        for(Email e: filteredEmails4){
            assertEquals(e.getSender().filterName(), "\"Marten, Kevin\" <Kevin.Marten@paluno.uni-due.de>");
        }
    }

    //not sure if ever used
    public void testFilterByDate() {
    }

    //Done
    public void testFilterByDomain() {
        ArrayList<Email> filteredEmails = testUser.filterByDomain("@uni-due.de", testEmails);
        ArrayList<Email> filteredEmails1 = testUser.filterByDomain("@paluno.uni-due.de", testEmails);
        ArrayList<Email> filteredEmails2 = testUser.filterByDomain("@ibes.uni-due.de", testEmails);


        for(Email e: filteredEmails){
            assertEquals(e.getDomain(false).get(0), "@uni-due.de");
        }
        for(Email e: filteredEmails1){
            assertEquals(e.getDomain(false).get(0), "@paluno.uni-due.de");
        }
        for(Email e: filteredEmails2){
            assertEquals(e.getDomain(false).get(0), "@ibes.uni-due.de");
        }

    }

    //Done
    public void testFilterByAttachmentType() {
        ArrayList<Email> filteredEmails = testUser.filterByAttachmentType(".txt", testEmails);
        ArrayList<Email> filteredEmails2 = testUser.filterByAttachmentType(".pdf", testEmails);

        for(Email e: filteredEmails){
            assertTrue(e.hasAttachment(".txt"));
        }
        for(Email e: filteredEmails2){
            assertTrue(e.hasAttachment(".pdf"));
        }
    }

    //Done
    public void testFilter() {
        //filtered by Folder & Sender
        ArrayList<Email> filteredEmails = testUser.filter("Archiv UniDuE Adresse", "Archiv UniDuE Adresse", null, null, "\"Helbach, Christoph\" <Christoph.Helbach@ibes.uni-due.de>", null,null,null);
        //filtered by Domain & Attachment
        ArrayList<Email> filteredEmails2 = testUser.filter(null, null, null, null, null, "@uni-due.de", ".pdf", null);

        for(Email e: filteredEmails){
            assertEquals(e.getFolder(), "Archiv UniDuE Adresse");
            assertEquals(e.getSender().filterName(), "\"Helbach, Christoph\" <Christoph.Helbach@ibes.uni-due.de>");
        }
        for(Email e: filteredEmails2){
            assertEquals(e.getDomain(false).get(0), "@uni-due.de");
            assertTrue(e.hasAttachment(".pdf"));
        }



    }

    //Ask for help
    public void testGetFoldersCountForSunburst() {
    }

    //Done
    public void testGetNumEmailsInFolder() {
        assertEquals(testUser.getNumEmailsInFolder("Archiv UniDuE Adresse",testEmails), 3);
        assertEquals(testUser.getNumEmailsInFolder("Betreute Arbeiten", testEmails), 1);
    }

    //Done
    public void testGetNumEmailsInSubFolder() {
        assertEquals(testUser.getNumEmailsInSubFolder("Archiv UniDuE Adresse","Archiv UniDuE Adresse",testEmails), 3);
        assertEquals(testUser.getNumEmailsInSubFolder("Betreute Arbeiten","Betreute Arbeiten", testEmails), 1);
    }
    //somethings wrong with compareTo for sender
    public void testGetTopSendersForFolder() {
        ArrayList<Sender> actualTopSenders1 = new ArrayList<>();
        for(int i = 0; i<4;i++){
            actualTopSenders1.add(testUser.getEmails().get(i).getSender());
        }
        ArrayList<Sender> actualTopSenders2 = new ArrayList<>();
        for(int i = 0; i<3;i++){
            actualTopSenders2.add(testUser.getEmails().get(i).getSender());
        }
        ArrayList<Sender> actualTopSenders3 = new ArrayList<>();
        actualTopSenders3.add(testUser.getEmails().get(3).getSender());

        ArrayList<Sender> testTopSenders1 = testUser.getTopSendersForFolder("AllFolders", "AllFolders");
        ArrayList<Sender> testTopSenders2 = testUser.getTopSendersForFolder("Archiv UniDuE Adresse", "Archiv UniDuE Adresse");
        ArrayList<Sender> testTopSenders3 = testUser.getTopSendersForFolder("Betreute Arbeiten", "Betreute Arbeiten");

        assertEquals(actualTopSenders1,testTopSenders1);
        assertEquals(actualTopSenders2, testTopSenders2);
        assertEquals(actualTopSenders3, testTopSenders3);


    }

    //Done
    public void testRecoverFolders() {
        ArrayList<UserFolder> testFolders = testUser.recoverFolders();
        ArrayList<UserFolder> actualFolders = new ArrayList<>();
        actualFolders.add(new UserFolder("Archiv UniDuE Adresse"));
        actualFolders.add(new UserFolder("Betreute Arbeiten"));

        for(int i = 0; i<actualFolders.size();i++){
            assertEquals(testFolders.get(i).getFolderName(), actualFolders.get(i).getFolderName());
        }
    }

    //Recover serialized user needs to take file path as parameter, cant test currently
    public void testRecoverSerializedEmails() {

    }

    //ask cedric
    public void testSerializeUser() {
    }

    //ask cedric
    public void testCreateSerializedUserFolder() {
    }

    //ask cedric reads from text files generated at runtime cant test properly
    public void testReadFolderAndSerializeEmails() {
    }

    //ask cedric
    public void testResetUser() {
    }

    // ask cedric
    public void testWriteMessages() {
    }

    //ask cedric
    public void testUpdateSerializedFolders() {
    }

    public void testEncrypt() {
        String test1 = "abcdefg";
        String test2 = "mno12A";
        String test3 = "AbCjj+aabbcc";

        assertEquals("egeilli", testUser.encrypt(test1));
        assertEquals("qsq69G", testUser.encrypt(test2));
        assertEquals("EgEoq1ceefhe", testUser.encrypt(test3));
    }

    public void testDecrypt() {
        String test1 = "egeilli";
        String test2 = "qsq69G";
        String test3 = "EgEoq1ceefhe";

        assertEquals("abcdefg", testUser.decrypt(test1));
        assertEquals("mno12A", testUser.decrypt(test2));
        assertEquals("AbCjj+aabbcc", testUser.decrypt(test3));
    }

    public void testGenerateDayOfWeekFrequency() {
    }

    //Done
    public void testDifferenceMinMax() {
        int[][] ary = new int[][]{
                { 0,1,2,3,4 },
                { 0,1,2,3,4 }};

        assertEquals(4, testUser.differenceMinMax(ary));
    }

    public void testGetColorForHeatMap() {
    }

    public void testGetDayOfWeekFrequency() {
    }

    public void testGetLastLogin() {
    }

    public void testSetLastLogin() {
    }

    public void testGetEmail() {
    }

    public void testSetEmail() {
    }

    public void testGetPassword() {
    }

    public void testSetPassword() {
    }

    public void testGetEmails() {
    }

    public void testGetTotalNumberOfEmails() {
    }

    public void testGetDay() {
        int i = 0;
        assertEquals(User.getDay(i),"Sun");
    }
    public void testGetFolders(){
        int i = 0;
        float h = .75f;
        float s = 1f;
        //float b = 1f / (float) diff * i;
    }
}