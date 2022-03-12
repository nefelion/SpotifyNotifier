package me.nefelion.spotifynotifier;

import me.nefelion.spotifynotifier.gui.ProgressGUI;

import javax.swing.*;
import java.util.List;

public class Test2 extends JFrame {

    private final ProgressGUI progressBar;

    public Test2() {
        progressBar = new ProgressGUI(0, 100);
        progressBar.setTitle("zesralem sie");
        progressBar.show();
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("guwno");
        JPanel container = new JPanel();

        String[] tab = new String[]{"xD", "xD2"};
        JList<String> lista = new JList<>(tab);

        container.add(lista);

        frame.add(container);

        frame.pack();
        frame.setVisible(true);


    }

    public void runCalc() {
        TwoWorker task = new TwoWorker();
        task.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("progress")) {
                progressBar.setValue((int) e.getNewValue());
            }
        });
        task.execute();
    }

    private class TwoWorker extends SwingWorker<Double, Double> {

        private static final int N = 5;
        double x = 1;

        @Override
        protected Double doInBackground() throws Exception {
            for (int i = 1; i <= N; i++) {
                //x = x - (((x * x - 2) / (2 * x)));
                //setProgress(i * (100 / N));
                //publish(x);
                progressBar.increment();
                Thread.sleep(1000); // simulate latency
            }
            return x;
        }

        @Override
        protected void process(List<Double> chunks) {
            for (double d : chunks) {

            }
        }
    }
}