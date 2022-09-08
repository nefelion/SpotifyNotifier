package me.nefelion.spotifynotifier;

import me.nefelion.spotifynotifier.data.FileManager;
import me.nefelion.spotifynotifier.data.TempData;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import javax.swing.*;
import java.io.IOException;

public class ClientManager {
    private String clientId, clientSecret;
    private SpotifyApi spotifyApi;
    private boolean good = false;

    public ClientManager() {
        clientId = TempData.getInstance().getFileData().getClientId();
        clientSecret = TempData.getInstance().getFileData().getClientSecret();

        if (clientId.equals("-") || clientSecret.equals("-")) {
            askForCredentials();
        }

        checkCredentials();
    }

    public static void resetCredentials() {
        TempData.getInstance().getFileData().setClientId("-");
        TempData.getInstance().getFileData().setClientSecret("-");
        FileManager.saveFileData(TempData.getInstance().getFileData());
    }

    private void askForCredentials() {
        // create swing window
        JFrame frame = new JFrame("Spotify Notifier");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 280);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        JPanel panel = new JPanel();


        // add textbox
        JTextArea textArea = new JTextArea("""
                Please enter your Spotify API credentials.
                1. Go to: https://developer.spotify.com/dashboard/applications
                2. Create an app. The name does not matter.
                3. Use the Client ID and Client Secret of the app.
                """);
        JLabel clientIdLabel = new JLabel("Client ID:");
        JTextField clientIdField = new JTextField(32);
        JLabel clientSecretLabel = new JLabel("Client Secret:");
        JTextField clientSecretField = new JTextField(32);
        JButton submitButton = new JButton("OK");
        JButton defaultButton = new JButton("SKIP AND USE DEFAULT");
        JLabel defaultWhyNotLabel = new JLabel("Skipping is not recommended - may not work or lag");

        panel.add(textArea);
        panel.add(clientIdLabel);
        panel.add(clientIdField);
        panel.add(clientSecretLabel);
        panel.add(clientSecretField);
        panel.add(submitButton);
        panel.add(defaultButton);
        panel.add(defaultWhyNotLabel);

        frame.add(panel);
        frame.setVisible(true);

        // add action listener
        submitButton.addActionListener(e -> {
            clientId = clientIdField.getText();
            clientSecret = clientSecretField.getText();
            frame.dispose();
            checkCredentials();
        });

        defaultButton.addActionListener(e -> {
            frame.dispose();
            useDefault();
            checkCredentials();
        });

        // wait for user input
        while (!good) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }

        saveCredentials();
        frame.dispose();
    }

    private void useDefault() {
        clientId = "d31c4bbf7a6c41e4b1ce6c47656de668";
        clientSecret = "c1f3be4db5104a54bc2fac060fdbdf14";
    }

    private void saveCredentials() {
        TempData.getInstance().getFileData().setClientId(clientId);
        TempData.getInstance().getFileData().setClientSecret(clientSecret);
        FileManager.saveFileData(TempData.getInstance().getFileData());
    }

    private void checkCredentials() {
        spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(getToken())
                .build();

        if (getToken() == null) System.exit(100000);
        else good = true;
    }

    private String getToken() {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();

        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();

        try {
            final ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            System.out.println("Expires in: " + clientCredentials.getExpiresIn());
            return spotifyApi.getAccessToken();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Utilities.showSwingMessageDialog(e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public SpotifyApi getSpotifyApi() {
        return spotifyApi;
    }

}