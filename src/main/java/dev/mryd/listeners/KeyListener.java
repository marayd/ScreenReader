package dev.mryd.listeners;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import dev.mryd.swing.Frame;

import static dev.mryd.swing.Frame.isSelectionEnabled;
import static dev.mryd.swing.Frame.showSelectionWindow;

public class KeyListener implements NativeKeyListener {
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_L &&
                (e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) {
            if (!isSelectionEnabled) {
                showSelectionWindow();
                isSelectionEnabled = true;
            }
        } else if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
            Frame.hideWindow();

        }
    }
}
