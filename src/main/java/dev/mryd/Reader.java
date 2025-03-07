package dev.mryd;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.mryd.Main.*;

public class Reader {

    protected static void ensureTessdataExists() {
        File tessdataFolder = new File(TESSDATA_DIR);
        if (!tessdataFolder.exists()) {
            tessdataFolder.mkdirs();
        }

        for (String lang : LANGUAGES) {
            File trainedData = new File(tessdataFolder, lang + ".traineddata");
            if (!trainedData.exists()) {
                downloadTessdata(lang);
            }
        }
    }
    public static String recognizeTextFromImage(BufferedImage image) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESSDATA_DIR);
        tesseract.setLanguage(getAvailableLanguages());

        try {
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String getAvailableLanguages() {
        File tessdataFolder = new File(TESSDATA_DIR);
        return Arrays.stream(Objects.requireNonNull(tessdataFolder.listFiles((dir, name) -> name.endsWith(".traineddata"))))
                .map(file -> file.getName().replace(".traineddata", ""))
                .collect(Collectors.joining("+"));
    }

}
