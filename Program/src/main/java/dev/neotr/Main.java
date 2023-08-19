package dev.neotr;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import javax.swing.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class Main implements NativeKeyListener {
    private int keyPressCount = 0;
    private int exitCount = 0;
    private int x1, y1, x2, y2;
    private static boolean webEnabled = true;

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public void GUI() {
        JFrame frame = new JFrame("OCR");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(0, 0);
        frame.setVisible(false);
    }

    public static void main(String[] args) throws AWTException {
        Main main = new Main();
        main.GUI();

        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        init();

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            logger.error(e.getMessage(), e);
            System.exit(-1);
        }

        GlobalScreen.addNativeKeyListener(main);
    }
    public static void getText() throws TesseractException, IOException {
        Tesseract tesseract = new Tesseract();
        String appPath = System.getProperty("user.dir");
        String tessdataPath = appPath + "/tesseract-5.3.2/tessdata";
        tesseract.setDatapath(tessdataPath);
        tesseract.setLanguage("eng");
        String text = tesseract.doOCR(new File("PartialScreenshot.png"));
        StringSelection stringSelection = new StringSelection(text);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        if (webEnabled) {
            sendRequest(text);
        }
    }

    public static void takeScreenshot(Integer x1, Integer y1, Integer x2, Integer y2) throws AWTException, IOException {
        Robot robot = new Robot();
        String format = "png";
        String fileName = "PartialScreenshot." + format;

        int width = Math.abs(x2 - x1);
        int height = Math.abs(y2 - y1);
        int captureX = Math.min(x1, x2);
        int captureY = Math.min(y1, y2);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        Rectangle captureRect = new Rectangle(captureX, captureY, width, height);

        BufferedImage screenFullImage = robot.createScreenCapture(captureRect);
        try {
            ImageIO.write(screenFullImage, format, new File(fileName));
            Toolkit.getDefaultToolkit().beep();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void init() {

        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);

        logger.setUseParentHandlers(false);
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
            String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
            if (keyText.equals("F8")) {
                Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
                int mouseY = MouseInfo.getPointerInfo().getLocation().y;
                int mouseX = MouseInfo.getPointerInfo().getLocation().x;
                keyPressCount++;
                switch (keyPressCount) {
                    case 1:
                        x1 = mouseX;
                        y1 = mouseY;
                        break;
                    case 2:
                        x2 = mouseX;
                        y2 = mouseY;
                        keyPressCount = 0;
                        try {
                            takeScreenshot(x1, y1, x2, y2);
                            Thread.sleep(300);
                            getText();
                            break;
                        } catch (AWTException | IOException ex) {
                            ex.printStackTrace();
                        } catch (InterruptedException | TesseractException ex) {
                            throw new RuntimeException(ex);
                        }
                        break;
                }
            } else if (keyText.equals("F9")) {
                try {
                    exitCount++;
                    if (exitCount == 2) {
                        System.exit(0);
                    } else {
                        displayTray("Press F9 again to exit", "WARNING");
                    }
                } catch (AWTException ex) {
                    ex.printStackTrace();
                }
            } else if (keyText.equals("F10")) {
                try {
                    displayTray("Web is disabled.", "INFO");
                    webEnabled = false;
                } catch (AWTException ex) {
                    ex.printStackTrace();
                }
            }
    }

    public static void sendRequest(String text) throws IOException {
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        URL url = new URL("http://localhost:3000/api");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        String escapedText = text.replaceAll("\\r?\\n", " ")
                .replaceAll("\\\\", "\\\\\\\\")
                .replaceAll("\\\"", "\\\\\"");
        String json = "{\"text\":\"" + escapedText + "\",\"date\":\"" + date.format(formatter) + "\"}";
        connection.getOutputStream().write(json.getBytes());

        int responseCode = connection.getResponseCode();
        connection.disconnect();
    }
    public void displayTray(String text, String type) throws AWTException {
        SystemTray tray = SystemTray.getSystemTray();

        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("System tray icon demo");
        tray.add(trayIcon);
        trayIcon.displayMessage("SnapText", text, MessageType.valueOf(type));
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        // Nothing
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
        // Nothing here
    }
}