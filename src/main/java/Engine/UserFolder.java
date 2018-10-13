package Engine;

import java.util.ArrayList;

public class UserFolder {

    String folderName;
    ArrayList<String> subFolders = new ArrayList<>();

    UserFolder(String n) {
        folderName = n;
        subFolders.add(n);
    }

    public String getFolderName () {
        return this.folderName;
    }
}


