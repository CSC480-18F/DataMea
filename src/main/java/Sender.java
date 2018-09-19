import java.util.ArrayList;

class Sender implements Comparable{

    private String address;
    private ArrayList<Email> emails;

    public Sender(String address) {

        this.address = address;
        emails = new ArrayList<>();
    }

    /// Need to fix this function to take into account what folder is being looked at
    @Override
    public int compareTo(Object o) {
        return ((Sender)o).getEmails().size() - this.emails.size();
    }

    public String toString() {
        return address + " number of emails sent: " + emails.size();
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