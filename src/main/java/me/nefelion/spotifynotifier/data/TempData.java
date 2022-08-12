package me.nefelion.spotifynotifier.data;

public final class TempData {
    private static TempData instance;
    private FileData fileData;
    private double volumeSliderValue = 33;
    private String typedCountry = "";

    private TempData() {
        fileData = new FileData();
    }

    public static TempData getInstance() {
        if (instance == null) {
            instance = new TempData();
        }
        return instance;
    }

    public static void setInstance(TempData instance) {
        TempData.instance = instance;
    }

    public FileData getFileData() {
        return fileData;
    }

    public void setFileData(FileData fileData) {
        this.fileData = fileData;
    }

    public double getVolumeSliderValue() {
        return volumeSliderValue;
    }

    public void setVolumeSliderValue(double volumeSliderValue) {
        this.volumeSliderValue = volumeSliderValue;
    }

    public String getTypedCountry() {
        return typedCountry;
    }

    public void setTypedCountry(String typedCountry) {
        this.typedCountry = typedCountry;
    }
}