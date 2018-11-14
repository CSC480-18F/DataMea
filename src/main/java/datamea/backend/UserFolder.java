package datamea.backend;

import java.util.ArrayList;

public class UserFolder {

    public String folderName;
    public ArrayList<String> subFolders = new ArrayList<>();

    UserFolder(String n) {
        folderName = n;
        subFolders.add(n);
    }

    public boolean containsFolder(ArrayList<String> filteredMail) {
        for (String s : filteredMail) {
            if (s.split(" -> ")[0].equals(folderName)) {
                return true;
            }
        }

        return false;
    }

    public boolean containsSubFolder(ArrayList<String> filteredMail) {
        for (String s : filteredMail) {
            for (String sub : subFolders)
            if (s.split(" -> ")[1].equals(sub)) {
                return true;
            }
        }

        return false;
    }

    public String getFolderName () {
        return this.folderName;
    }
}


