package Controllers;

public class Filter {
    private String name;
    private boolean isTopSender = false;
    private boolean isFolder = false;
    private boolean isDomain = false;
    private boolean isAttachment = false;
    private boolean isLanguage = false;
    private boolean isStartDate = false;
    private boolean isEndDate = false;

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

    public boolean isStartDate() {
        return isStartDate;
    }

    public void setStartDate(boolean startDate) {
        isStartDate = startDate;
    }

    public boolean isEndDate() {
        return isEndDate;
    }

    public void setEndDate(boolean endDate) {
        isEndDate = endDate;
    }
}
