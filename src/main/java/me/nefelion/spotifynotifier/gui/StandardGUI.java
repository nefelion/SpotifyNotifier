package me.nefelion.spotifynotifier.gui;

import javax.swing.*;
import java.util.Stack;


public abstract class StandardGUI extends GUIFrame {

    private static final Stack<GUIData> GUI_STACK = new Stack<>();
    private static JFrame ONE_FRAME;
    protected JButton backButton;

    public StandardGUI() {
        super();

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        if (!GUI_STACK.isEmpty()) {
            backButton = new JButton();
            backButton.addActionListener(e -> {
                GUI_STACK.pop();
                replaceGUIWith(GUI_STACK.peek());
            });
            backButton.setText("<");
            setSmallButtonMargins(backButton);
            backButton.setToolTipText(GUI_STACK.peek().getTitle());
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

    protected int getCurrentGUIHashCode() {
        return GUI_STACK.peek().getHashCode();
    }
}
