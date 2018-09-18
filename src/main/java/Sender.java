
class Sender {

    String address;
    int count = 1;

    public Sender(String address) {
        this.address = address;
    }

    public String toString() {
        return address + " number of emails sent: " + count;
    }

}