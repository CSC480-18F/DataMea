package Controllers;

public class Filter {
    private String name;
    private boolean isTopSender = false, isFolder = false, isDomain = false, isAttachment = false, isLanguage = false;

    public Filter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isTopSender() {
        return isTopSender;
    }

    public void setTopSender(boolean topSender) {
        isTopSender = topSender;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public boolean isDomain() {
        return isDomain;
    }

    public void setDomain(boolean domain) {
        isDomain = domain;
    }

    public boolean isAttachment() {
        return isAttachment;
    }

    public void setAttachment(boolean attachment) {
        isAttachment = attachment;
    }

    public boolean isLanguage() {
        return isLanguage;
    }

    public void setLanguage(boolean language) {
        isLanguage = language;
    }
}
