package me.nefelion.spotifynotifier;

public final class TempData {
    private static TempData instance;
    private FileData fileData;

    private TempData() {
        fileData = new FileData();
    }

    public static TempData getInstance() {
        if (instance == null) {
            instance = new TempData();
        }
        return instance;
    }

    public FileData getFileData() {
        return fileData;
    }

    public void setFileData(FileData fileData) {
        this.fileData = fileData;
    }

}