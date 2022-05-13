package me.nefelion.spotifynotifier;

import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import javax.swing.*;
import java.io.IOException;

public class TokenGetter {
    private final String clientId;
    private final String clientSecret;

    public TokenGetter(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getToken() {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();

        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();

        try {
            final ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            // Set access token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            System.out.println("Expires in: " + clientCredentials.getExpiresIn());
            return spotifyApi.getAccessToken();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Utilities.showMessageDialog(e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            System.exit(-1001);
            return null;
        }
    }


}
