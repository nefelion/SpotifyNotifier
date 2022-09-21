package me.nefelion.spotifynotifier;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static java.time.temporal.ChronoUnit.DAYS;
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

    public static void showSwingMessageDialog(String message, String title, int type) {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);
        new Timer(30000, (evt) -> frame.dispose()).start();
        JOptionPane.showMessageDialog(frame, message, "SpotifyNotifier: " + title, type);
        frame.dispose();
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
        if (seconds > (3600 * 24)) {
            long days = seconds / (3600 * 24);
            return days + (days > 1 ? " days" : " day") + " ago";
        }
        if (seconds > 3600) {
            long hours = seconds / 3600;
            return hours + (hours > 1 ? " hours" : " hour") + " ago";
        }
        if (seconds > 60) {
            long minutes = seconds / 60;
            return minutes + (minutes > 1 ? " minutes" : " minute") + " ago";
        }

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
        showAlert(alert);
        if (alert.getResult() == exit) System.exit(-1000);

        return true;
    }

    public static boolean okUndoMSGBOX(String message) {
        ButtonType ok = new ButtonType("Ok");
        ButtonType undo = new ButtonType("Undo");

        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ok, undo);
        showAlert(alert);
        return alert.getResult() == undo;
    }

    public static void okMSGBOX(String message) {
        ButtonType ok = new ButtonType("Ok");
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ok);
        showAlert(alert);
    }

    public static void showAlert(Alert alert) {
        Window window = alert.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        alert.setTitle("");
        ((Stage) (alert.getDialogPane().getScene().getWindow())).getIcons()
                .add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/images/icon.png"))));
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static String convertDateToAgo(String str) {
        LocalDate date = findLocalDate(str);
        if (date == null) return "-";

        long days = DAYS.between(date, LocalDate.now());
        if (days > 365) {
            int years = (int) (days / 365);
            if (years > 1) return years + " years ago";
            return "1 year ago";
        }
        if (days > 30) {
            int months = (int) (days / 30);
            if (months > 1) return months + " months ago";
            return "1 month ago";
        }
        if (days > 0) {
            if (days > 1) return days + " days ago";
            return "1 day ago";
        }
        if (days == 0) return "Today";
        if (days == -1) return "Tomorrow";
        return "In " + (-days) + " days";
    }

    public static int convertDateToDaysAgo(String str) {
        LocalDate date = findLocalDate(str);
        if (date == null) return -9999;

        return (int) DAYS.between(date, LocalDate.now());
    }

    public static LocalDate findLocalDate(String str) {
        java.util.List<SimpleDateFormat> knownFormatters = new ArrayList<>(Arrays.asList(
                new SimpleDateFormat("yyyy-MM-dd"),
                new SimpleDateFormat("yyyy-MM"),
                new SimpleDateFormat("yyyy")
        ));


        for (SimpleDateFormat formatter : knownFormatters) {
            try {
                return formatter.parse(str).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (ParseException ignored) {
            }
        }

        return null;
    }

    public static String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        return sdf.format(c.getTime());
    }

    public static String getTomorrowDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        return sdf.format(c.getTime());
    }
}
