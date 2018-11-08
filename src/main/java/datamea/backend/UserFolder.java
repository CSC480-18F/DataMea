package datamea.backend;

import java.util.ArrayList;

public class UserFolder {

    public String folderName;
    public ArrayList<String> subFolders = new ArrayList<>();

    UserFolder(String n) {
        folderName = n;
        subFolders.add(n);
    }

    public String getFolderName () {
        return this.folderName;
    }
}


