import javax.mail.Folder;
import java.util.ArrayList;

public class User {
    String email, password;
    ArrayList<Folder> folders;
    ArrayList<Sender> senders;


    User (String email, String password){
        this.email = email;
        this.password = password;
        folders = new ArrayList<>();
        senders = new ArrayList<>();
    }


}
