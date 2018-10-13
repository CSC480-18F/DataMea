package Engine;

import java.util.ArrayList;

class Sender implements Comparable{

    //------------------Declaring Variables------------------//
    private String           address;
    private ArrayList<Email> emails;
    int                      numEmailsSent;

    public Sender(String address) {

        this.address = address;
        emails = new ArrayList<>();
        numEmailsSent = 1;
    }

    public void incrementNumEmails(){
        numEmailsSent++;
    }


    public String filterName() {
        String ret = this.address;
        int a=0, b=0;
        char [] c = ret.toCharArray();
        for (int i=0; i<c.length; i++) {
            if (c[i] == '<') {
                a = i;
            } else if (c[i] == '>') {
                b = i;
            }
        }

        if (a==0 && b == 0) {
            // no name attached, so just return the entire thing
            return ret;
        }

        ret = ret.substring(a+1,b);
        return ret;
    }


    /// Need to fix this function to take into account what folder is being looked at
    @Override
    public int compareTo(Object o) {
        return ((Sender)o).numEmailsSent - this.numEmailsSent;
    }

    public String toString() {
        return address + " number of emails sent: " + numEmailsSent;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ArrayList<Email> getEmails() {
        return emails;
    }

    public void setEmails(ArrayList<Email> emails) {
        this.emails = emails;
    }

    public void addEmail(Email e) {
        this.emails.add(e);
    }



}