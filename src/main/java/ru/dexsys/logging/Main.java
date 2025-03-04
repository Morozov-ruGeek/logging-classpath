package ru.dexsys.logging;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        var resultFile = new File(args.length > 0 ? args[1] : "out.docx");

        byte[] pictureData = null;

        try (var httpClient = HttpClients.createDefault()) {
            var response = httpClient.execute(new HttpGet("https://cataas.com/cat/says/I%20love%20Java"));
            pictureData = EntityUtils.toByteArray(response.getEntity());
        } catch (IOException e) {
            LOGGER.error("Couldn't download image", e);
            System.exit(1);
        }

        LOGGER.info("Picture downloaded, size={}", pictureData.length);

        try (var document = new XWPFDocument();
             var output = new FileOutputStream(resultFile)) {
            var paragraph = document.createParagraph();
            var run = paragraph.createRun();

            run.addPicture(new ByteArrayInputStream(pictureData), XWPFDocument.PICTURE_TYPE_JPEG, "image.jpg", Units.toEMU(400), Units.toEMU(400));

            document.write(output);
        } catch (InvalidFormatException e) {
            LOGGER.error("Couldn't add picture", e);
            System.exit(1);
        } catch (IOException e) {
            LOGGER.error("Couldn't save document", e);
            System.exit(1);
        }

        LOGGER.info("Result file {} saved, opening...", resultFile);

        try {
            Desktop.getDesktop().open(resultFile);
        } catch (IOException e) {
            LOGGER.error("Couldn't open {} in default viewer", resultFile, e);
        }
    }
}
