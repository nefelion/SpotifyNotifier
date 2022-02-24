package me.nefelion.spotifynotifier;

import com.goxr3plus.streamplayer.stream.StreamPlayer;
import com.goxr3plus.streamplayer.stream.StreamPlayerEvent;
import com.goxr3plus.streamplayer.stream.StreamPlayerException;
import com.goxr3plus.streamplayer.stream.StreamPlayerListener;
import me.nefelion.spotifynotifier.gui.StandardGUI;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;


public class Player {

    private final StreamPlayer streamPlayer;
    private boolean lockToStandardGUI = false;
    private int standardGUIHash = -1;

    public Player(String audioAbsolutePath) {

        streamPlayer = new StreamPlayer();

        try {
            streamPlayer.addStreamPlayerListener(new StreamPlayerListener() {
                @Override
                public void opened(Object o, Map<String, Object> map) {

                }

                @Override
                public void progress(int i, long l, byte[] bytes, Map<String, Object> map) {
                    if (lockToStandardGUI && StandardGUI.getCurrentGUIHashCode() != standardGUIHash) stop();
                }

                @Override
                public void statusUpdated(StreamPlayerEvent streamPlayerEvent) {

                }
            });

            URL url = new URL(audioAbsolutePath);
            InputStream bufferedIn = new BufferedInputStream(url.openStream());
            streamPlayer.open(bufferedIn);

        } catch (final Exception ex) {
            ex.printStackTrace();
        }

    }

    public void play() {
        try {
            streamPlayer.play();
            streamPlayer.setGain(0.5);
        } catch (StreamPlayerException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        streamPlayer.stop();
    }

    public void lockToStandardGUI(int hash) {
        lockToStandardGUI = true;
        standardGUIHash = hash;
    }

}