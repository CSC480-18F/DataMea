package datamea.backend;

import junit.framework.TestCase;

public class SenderTest extends TestCase {

    Sender tester = new Sender("<glin2@oswego.edu>");
    Sender tester1 = new Sender("<glin2@oswego.edu>123");
    Sender tester2 = new Sender("glin2@oswego.edu123");

    public void testIncrementNumEmails() {
    }

    public void testFilterName() {
        assertEquals(tester.filterName(), "glin2@oswego.edu");
        assertEquals(tester1.filterName(), "glin2@oswego.edu");
        assertEquals(tester2.filterName(), "glin2@oswego.edu123");

    }

    public void testFilterEmailAddress() {
        assertEquals(tester.filterEmailAddress("<glin2@oswego.edu>"), "glin2@oswego.edu");
    }

    public void testCompareTo() {
    }

    public void testToString() {
    }

    public void testGetAddress() {
    }

    public void testSetAddress() {
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