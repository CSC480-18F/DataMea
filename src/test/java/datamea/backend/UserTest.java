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

    public void testGetFoldersCountForSunburst() {
    }

    public void testGetNumEmailsInFolder() {
    }

    public void testGetNumEmailsInSubFolder() {
    }

    public void testGetTopSendersForFolder() {
    }

    public void testGetTopSendersForFilter() {
    }

    public void testRecoverFolders() {
    }

    public void testRecoverSerializedEmails() {
    }

    public void testSerializeUser() {
    }

    public void testCreateSerializedUserFolder() {
    }

    public void testReadFolderAndSerializeEmails() {
    }

    public void testResetUser() {
    }

    public void testWriteMessages() {
    }

    public void testUpdateSerializedFolders() {
    }

    public void testEncrypt() {
    }

    public void testDecrypt() {
    }

    public void testGenerateDayOfWeekFrequency() {
    }

    public void testDifferenceMinMax() {
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