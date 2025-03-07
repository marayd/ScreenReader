package dev.mryd;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import dev.mryd.listeners.KeyListener;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static dev.mryd.Reader.ensureTessdataExists;

public class Main implements NativeKeyListener {
    protected static final String TESSDATA_DIR = "tessdata";
    protected static final String[] LANGUAGES = {"eng", "rus"};
    public static TrayIcon trayIcon;
    public static void main(String[] args) {
        setupSystemTray();

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
            return;
        }
        ensureTessdataExists();
        GlobalScreen.addNativeKeyListener(new KeyListener());
    }

    private static void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            System.err.println("Системный трей не поддерживается.");
            return;
        }

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

        PopupMenu popup = new PopupMenu();
        MenuItem exitItem = new MenuItem("Выход");
        exitItem.addActionListener((ActionEvent e) -> {
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (NativeHookException ex) {
                throw new RuntimeException(ex);
            }
            System.out.println("Goodbye!");
            System.exit(0);
        });

        popup.add(exitItem);
        trayIcon = new TrayIcon(image, "Screen Reader", popup);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("Ошибка добавления в трей: " + e.getMessage());
        }
    }

    protected static void downloadTessdata(String lang) {
        String url = "https://github.com/tesseract-ocr/tessdata_best/raw/main/" + lang + ".traineddata";
        File outputFile = new File(TESSDATA_DIR, lang + ".traineddata");

        System.out.println("Загрузка " + lang + " данных...");
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Файл " + lang + " успешно загружен.");
        } catch (IOException e) {
            System.err.println("Ошибка загрузки " + lang + " данных: " + e.getMessage());
        }
    }
}