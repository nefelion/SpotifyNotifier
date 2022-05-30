package me.nefelion.spotifynotifier;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;

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
        new Timer(30000, (evt) -> frame.dispose()).start();
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
        str += (hours > 0 && min < 10) ? "0" + min + ":" : min + ":";
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

    public static boolean tryAgainMSGBOX(String errMsg) {
        final FutureTask<Boolean> query = new FutureTask<>(() -> showTryAgainMSGBOX(errMsg));
        Platform.runLater(query);
        try {
            query.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(-929);
        }

        return true;
    }

    private static boolean showTryAgainMSGBOX(String errMsg) {
        ButtonType try_again = new ButtonType("Try again");
        ButtonType exit = new ButtonType("Exit");

        Alert alert = new Alert(Alert.AlertType.ERROR, errMsg, try_again, exit);
        Window window = alert.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        alert.setTitle("");
        ((Stage) (alert.getDialogPane().getScene().getWindow())).getIcons()
                .add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/images/icon.png"))));
        alert.setHeaderText(null);
        alert.showAndWait();
        if (alert.getResult() == exit) System.exit(-1000);

        return true;
    }

    public static boolean okUndoMSGBOX(String errMsg) {
        ButtonType ok = new ButtonType("Ok");
        ButtonType undo = new ButtonType("Undo");

        Alert alert = new Alert(Alert.AlertType.INFORMATION, errMsg, ok, undo);
        Window window = alert.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        alert.setTitle("");
        ((Stage) (alert.getDialogPane().getScene().getWindow())).getIcons()
                .add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/images/icon.png"))));
        alert.setHeaderText(null);
        alert.showAndWait();
        return alert.getResult() == undo;
    }

    public static Predicate<String> haveWordsThatStartWith(final String startsWith) {
        return s -> Arrays.stream(s.split(" ")).anyMatch(p -> p.toLowerCase().startsWith(startsWith.toLowerCase()));
    }
}
