package me.nefelion.spotifynotifier.settings;

import me.nefelion.spotifynotifier.data.FileData;
import me.nefelion.spotifynotifier.data.TempData;

public class DiscordMessageSettings {

    public static final String
            DEFAULT_TEMPLATE_ALBUM = """
            > **%ALBUMNAME%**[ _(%OPTIONAL_SINGLE%)_]
            > by %ARTISTS%
            > %DATE%
            > %LINK%""",
            DEFAULT_TEMPLATE_ALBUM_SPECIFIC_DAY = """
                    > **%ALBUMNAME%**[ _(%OPTIONAL_SINGLE%)_]
                    > by %ARTISTS%
                    > %LINK%""",
            DEFAULT_PREFIX_LINE_TODAY = "Today's (_%DATE%_) new releases:",
            DEFAULT_PREFIX_LINE_TOMORROW = "Tomorrow's (_%DATE%_) new releases:";

    private String TemplateAlbum, TemplateAlbumSpecificDay, PrefixLineToday, PrefixLineTomorrow;

    public DiscordMessageSettings() {
        TemplateAlbum = TempData.getInstance().getFileData().getDiscordTemplateAlbum();
        TemplateAlbumSpecificDay = TempData.getInstance().getFileData().getDiscordTemplateAlbumSpecificDay();
        PrefixLineToday = TempData.getInstance().getFileData().getDiscordPrefixLineToday();
        PrefixLineTomorrow = TempData.getInstance().getFileData().getDiscordPrefixLineTomorrow();
    }

    public String getTemplateAlbum() {
        return TemplateAlbum;
    }

    public void setTemplateAlbum(String templateAlbum) {
        this.TemplateAlbum = templateAlbum;
    }

    public String getTemplateAlbumSpecificDay() {
        return TemplateAlbumSpecificDay;
    }

    public void setTemplateAlbumSpecificDay(String templateAlbumSpecificDay) {
        this.TemplateAlbumSpecificDay = templateAlbumSpecificDay;
    }

    public String getPrefixLineToday() {
        return PrefixLineToday;
    }

    public void setPrefixLineToday(String prefixLineToday) {
        this.PrefixLineToday = prefixLineToday;
    }

    public String getPrefixLineTomorrow() {
        return PrefixLineTomorrow;
    }

    public void setPrefixLineTomorrow(String prefixLineTomorrow) {
        this.PrefixLineTomorrow = prefixLineTomorrow;
    }

    public void saveSettings(FileData fd) {
        fd.setDiscordTemplateAlbum(TemplateAlbum);
        fd.setDiscordTemplateAlbumSpecificDay(TemplateAlbumSpecificDay);
        fd.setDiscordPrefixLineToday(PrefixLineToday);
        fd.setDiscordPrefixLineTomorrow(PrefixLineTomorrow);
    }

}
