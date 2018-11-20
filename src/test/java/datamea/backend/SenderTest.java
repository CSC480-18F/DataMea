package datamea.backend;

import junit.framework.TestCase;

public class SenderTest extends TestCase {

    Sender tester = new Sender("<glin2@oswego.edu>");
    Sender tester1 = new Sender("glin2@oswego.edu");
    Sender tester2 = new Sender("<glin2@oswego.edu>");

    public void testIncrementNumEmails() {
    }

    public void testFilterName() {
        assertEquals(tester.filterName(), "glin2@oswego.edu");
        assertEquals(tester1.filterName(), "glin2@oswego.edu");

    }

    public void testFilterEmailAddress() {
        assertEquals(tester.filterEmailAddress("<glin2@oswego.edu>"), "glin2@oswego.edu");
    }

    public void testCompareTo() {
    }

    public void testToString() {
        assertEquals(tester.toString(),"<glin2@oswego.edu> number of emails sent: 1");
    }

    public void testGetAddress() {
        assertEquals(tester.getAddress(),"<glin2@oswego.edu>");
    }

    public void testSetAddress() {
        tester2.setAddress("<glin@oswego.edu>");
        assertEquals(tester2.getAddress(),"<glin@oswego.edu>");
    }

    public void testGetEmails() {
    }

    public void testSetEmails() {
    }

    public void testAddEmail() {
    }

    public void testAddMessage() {
    }

}