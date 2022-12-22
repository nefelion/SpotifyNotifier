package me.nefelion.spotifynotifier;

import me.nefelion.spotifynotifier.data.TempData;

import java.util.List;

public class DiscordMessage {

    public static void copy(ReleasedAlbum album) {
        final String TEMPLATE_ALBUM = TempData.getInstance().getFileData().getDiscordTemplateAlbum();

        Utilities.copyToClipboard(getForRelease(album, TEMPLATE_ALBUM));
    }

    public static void copy(List<ReleasedAlbum> newReleases) {
        final String TEMPLATE_ALBUM = TempData.getInstance().getFileData().getDiscordTemplateAlbum();

        StringBuilder builder = new StringBuilder();
        for (ReleasedAlbum album : newReleases) builder.append(getForRelease(album, TEMPLATE_ALBUM)).append("\n");
        Utilities.copyToClipboard(builder.toString());
    }

    public static void copyTodays(List<ReleasedAlbum> allReleases) {
        final String PREFIX_LINE_TODAY = TempData.getInstance().getFileData().getDiscordPrefixLineToday();

        copySpecific(allReleases, PREFIX_LINE_TODAY, Utilities.getTodayDate());
    }

    public static void copyTomorrows(List<ReleasedAlbum> allReleases) {
        final String PREFIX_LINE_TOMORROW = TempData.getInstance().getFileData().getDiscordPrefixLineTomorrow();

        copySpecific(allReleases, PREFIX_LINE_TOMORROW, Utilities.getTomorrowDate());
    }


    private static void copySpecific(List<ReleasedAlbum> allReleases, String prefixLine, String date) {
        final String TEMPLATE_ALBUM_SPECIFIC_DAY = TempData.getInstance().getFileData().getDiscordTemplateAlbumSpecificDay();

        StringBuilder builder = new StringBuilder();

        if (prefixLine != null && !prefixLine.isEmpty())
            builder.append(prefixLine.replace("%DATE%", date)).append("\n\n");

        for (ReleasedAlbum album : allReleases.stream().filter(p -> p.getReleaseDate().equals(date)).toList()) {
            builder.append(getForRelease(album, TEMPLATE_ALBUM_SPECIFIC_DAY)).append("\n");
        }

        Utilities.copyToClipboard(builder.toString());
    }


    private static String getForRelease(ReleasedAlbum album, String templateMessage) {
        final String WORD_SINGLE = "Single";

        String s = templateMessage;
        if (!album.isSingle()) s = removeOptional(s, "%OPTIONAL_SINGLE%");

        return s.replace("[", "").replace("]", "")
                .replace("%ALBUMNAME%", album.getAlbumName())
                .replace("%OPTIONAL_SINGLE%", album.isSingle() ? WORD_SINGLE : "")
                .replace("%ARTISTS%", String.join(", ", Utilities.getFormattedArtists(album)))
                .replace("%DATE%", album.getExtendedReleaseDate())
                .replace("%LINK%", album.getLink())
                + "\n";
    }

    private static String removeOptional(String string, String optional) {
        return string.replaceAll("\\[.*" + optional + ".*]", "");
    }


}
