package me.grothgar.spotifynotifier.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Stack;


public abstract class StandardGUI extends GUIFrame {

    protected static final Stack<GUIData> GUI_STACK = new Stack<>();
    private static JFrame ONE_FRAME;

    public StandardGUI() {
        super();

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JButton backButton = new JButton();
        backButton.addActionListener(e -> {
            GUI_STACK.pop();
            replaceGUIWith(GUI_STACK.peek());
        });

        if (!GUI_STACK.isEmpty()) {
            backButton.setText("< " + GUI_STACK.peek().getTitle());
            JPanel backButtonPanel = new JPanel();
            backButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
            backButtonPanel.add(backButton);
            container.add(backButtonPanel);
            //frame.setLocation(CONTAINER_STACK.peek().frame.getLocation());
        }


    }

    @Override
    public void show() {
        pushToStack();
        if (ONE_FRAME == null) ONE_FRAME = this.frame;
        replaceGUIWith(new GUIData(this.container, this.frame.getTitle(), this.frame.getSize(), this.hashCode()));
    }

    private void replaceGUIWith(GUIData guiData) {
        ONE_FRAME.setContentPane(guiData.getContainer());
        ONE_FRAME.setSize(guiData.getSize());
        ONE_FRAME.setTitle(guiData.getTitle());
        ONE_FRAME.revalidate();
        ONE_FRAME.repaint();
        ONE_FRAME.setVisible(true);
    }


    protected void pushToStack() {
        GUI_STACK.push(new GUIData(this.container, this.frame.getTitle(), this.frame.getSize(), this.hashCode()));
    }

}
