package datamea.backend;

import junit.framework.TestCase;

import java.util.ArrayList;

public class UserFolderTest extends TestCase {

    UserFolder userFolderTrue = new UserFolder("folder# 5");
    UserFolder userFolderFalse = new UserFolder("folder# 10");
    ArrayList<String> filteredMail = new ArrayList<>();

    public void setUp() throws Exception{
        super.setUp();
        for (int i = 0; i<10; i++){
            filteredMail.add("folder# " +String.valueOf(i)+ " -> folder# "+String.valueOf(i));
        }
    }


    public void testContainsFolder() {
        assertEquals("Test should be true", true,userFolderTrue.containsFolder(filteredMail));
        assertEquals("Test should be false", false, userFolderFalse.containsFolder(filteredMail));

        /**
         * Explain what a subfolder is in the code because every time a new user folder is created, It is added
         * in it's sub subfolder list? how come?
         */
    }

    public void testContainsSubFolder() {
        assertEquals("Expecting true", true, userFolderTrue.containsSubFolder(filteredMail));
        assertEquals("Expecting false",false, userFolderFalse.containsSubFolder(filteredMail));
    }

    public void testGetFolderName() {
        assertEquals("Expecting true",userFolderTrue.getFolderName(), "folder# 5");
        assertEquals("Expecting true", userFolderFalse.getFolderName(), "folder# 10");
    }
}