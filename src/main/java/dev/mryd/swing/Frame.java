package dev.mryd.swing;

import dev.mryd.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static dev.mryd.Main.trayIcon;
import static dev.mryd.Reader.recognizeTextFromImage;

public class Frame {
    private static Point startPoint;
    private static Rectangle selection;
    public static boolean isSelectionEnabled;
    private static JWindow window;
    private static JPanel panel;
    private static BufferedImage backgroundImage;
    private static Rectangle screenBounds;

    public static void showSelectionWindow() {
        SwingUtilities.invokeLater(() -> {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();

            Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
            GraphicsDevice activeScreen = null;

            for (GraphicsDevice screen : screens) {
                if (screen.getDefaultConfiguration().getBounds().contains(mouseLocation)) {
                    activeScreen = screen;
                    break;
                }
            }

            if (activeScreen == null) return;

            screenBounds = activeScreen.getDefaultConfiguration().getBounds();

            try {
                backgroundImage = captureScreen(screenBounds);
            } catch (AWTException e) {
                e.printStackTrace();
                return;
            }

            window = new JWindow();
            window.setAlwaysOnTop(true);
            window.setBounds(screenBounds);

            panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (backgroundImage != null) {
                        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
                    }
                    g.setColor(new Color(0, 0, 0, 100));
                    g.fillRect(0, 0, getWidth(), getHeight());

                    if (selection != null) {
                        g.setColor(new Color(88, 89, 87, 100));
                        g.fillRect(selection.x - screenBounds.x, selection.y - screenBounds.y, selection.width, selection.height);
                        g.setColor(Color.darkGray);
                        g.drawRect(selection.x - screenBounds.x, selection.y - screenBounds.y, selection.width, selection.height);
                    }
                }
            };
            panel.setOpaque(false);

            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    startPoint = new Point(e.getX() + screenBounds.x, e.getY() + screenBounds.y);
                    selection = new Rectangle(startPoint);
                    panel.repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (selection != null && selection.width > 0 && selection.height > 0) {
                        try {
                            panel.repaint();
                            window.dispose();
                            BufferedImage screenshot = new Robot().createScreenCapture(selection);
                            String recognizedText = recognizeTextFromImage(screenshot);
                            displayTray("Успешно скопировано в буфер обмена.", "ImageReader");
                            copyToClipboard(recognizedText);
                            System.out.println("Распознанный текст: " + recognizedText);
                        } catch (AWTException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        System.out.println("Выделенная область слишком мала.");
                    }
                    selection = null;
                    if (window.isVisible()) {
                        window.dispose();
                        panel.repaint();
                    }
                    isSelectionEnabled = false;
                }
            });

            panel.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int x = Math.min(startPoint.x, e.getX() + screenBounds.x);
                    int y = Math.min(startPoint.y, e.getY() + screenBounds.y);
                    int width = Math.abs(e.getX() + screenBounds.x - startPoint.x);
                    int height = Math.abs(e.getY() + screenBounds.y - startPoint.y);
                    selection.setBounds(x, y, width, height);
                    panel.repaint();
                }
            });

            window.add(panel);
            window.setVisible(true);
        });
    }

    private static BufferedImage captureScreen(Rectangle bounds) throws AWTException {
        return new Robot().createScreenCapture(bounds);
    }

    public static void hideWindow() {
        window.dispose();
        panel.repaint();
        window.setVisible(false);
        selection = null;
        isSelectionEnabled = false;
    }

    public static void displayTray(String message, String title) throws AWTException {
        if (trayIcon == null) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image;
            try {
                InputStream is = Main.class.getResourceAsStream("/icon.png");
                if (is == null) {
                    System.err.println("Иконка не найдена!");
                    return;
                }
                image = ImageIO.read(is);
            } catch (IOException e) {
                System.err.println("Ошибка загрузки иконки: " + e.getMessage());
                return;
            }

            trayIcon = new TrayIcon(image, "ImageReader");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("ImageReader");

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("Ошибка добавления в трей: " + e.getMessage());
                return;
            }
        }

        trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
    }

    private static void copyToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
}
