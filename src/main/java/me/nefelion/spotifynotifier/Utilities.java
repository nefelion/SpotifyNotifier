package me.nefelion.spotifynotifier;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Utilities {

    private static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

    public static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }

    public static String showInputDialog(String message, String title) {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);
        String str = JOptionPane.showInputDialog(frame, message, "SpotifyNotifier: " + title, JOptionPane.PLAIN_MESSAGE);
        frame.dispose();
        return str;
    }

    public static void showMessageDialog(String message, String title, int type) {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(frame, message, "SpotifyNotifier: " + title, type);
        frame.dispose();
    }

    public static void showTextArea(String title, String... text) {
        JFrame frame = new JFrame("SpotifyNotifier: " + title);
        JPanel jPanel = new JPanel();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        for (String str : text) {
            JTextArea jTextArea = new JTextArea(str.trim());
            jTextArea.setEditable(false);
            jPanel.add(jTextArea);
        }
        JScrollPane scroll = new JScrollPane(jPanel);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        Dimension d = scroll.getPreferredSize();
        d.height = 600;
        d.width += 25;
        scroll.setPreferredSize(d);

        frame.add(scroll);
        frame.pack();
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
    }

    public static String convertMsToDuration(int ms) {
        String str = "";
        int sec = ms / 1000;

        int hours = sec / 3600;
        if (hours > 0) {
            str += hours + ":";
            sec %= 3600;
        }
        // w sec zostały tylko minuty i sekundy

        int min = sec / 60;
        str += hours > 0 && min > 9 ? "0" + min + ":" : min + ":";
        sec %= 60; // w sec zostały tylko sekundy

        str += sec > 9 ? sec : "0" + sec;

        return str;
    }

    public static String getTimeAgo(String str) {
        LocalDateTime date;

        try {
            date = LocalDateTime.parse(str, DateTimeFormatter.ofPattern(DATE_FORMAT_NOW));
        } catch (DateTimeParseException e) {
            return "-";
        }

        long seconds = SECONDS.between(date, LocalDateTime.now());
        if (seconds > 3600 * 24) return seconds / 3600 * 24 + " days ago";
        if (seconds > 3600) return seconds / 3600 + " hours ago";
        if (seconds > 60) return seconds / 60 + " minutes ago";

        return "< minute ago";
    }


}
