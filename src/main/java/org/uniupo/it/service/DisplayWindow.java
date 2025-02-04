package org.uniupo.it.service;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.uniupo.it.model.DisplayMessageFormat;
import com.google.gson.Gson;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DisplayWindow extends Application {
    private static final Logger LOGGER = Logger.getLogger(DisplayWindow.class.getName());
    private static Label messageLabel;
    private static String instituteId;
    private static String machineId;
    private static MqttClient mqttClient;
    private static final Gson gson = new Gson();
    private static final String WELCOME_MESSAGE = "Benvenuto!";
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: black;");

        Label titleLabel = new Label("=== Area Messaggi ===");
        titleLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.LIGHTBLUE);

        messageLabel = new Label(WELCOME_MESSAGE);
        messageLabel.setFont(Font.font("Monospace", 14));
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setWrapText(true);

        root.getChildren().addAll(titleLabel, messageLabel);

        Scene scene = new Scene(root, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Display Macchina " + machineId);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(200);

        setupMqttListener();

        primaryStage.show();
    }

    private void showMessage(String message, Color color) {
        Platform.runLater(() -> {
            messageLabel.setTextFill(color);
            messageLabel.setText(message);

        });
    }

    private void setupMqttListener() {
        try {
            String displayTopic = String.format("istituto/%s/macchina/%s/frontend/screen/update",
                    instituteId, machineId);

            mqttClient.subscribe(displayTopic, (topic, message) -> {
                String payload = new String(message.getPayload());
                DisplayMessageFormat displayMessage = gson.fromJson(payload, DisplayMessageFormat.class);

                if (displayMessage.isError()) {
                    showMessage("ERROR: " + displayMessage.getMessage(), Color.RED);
                } else {
                    showMessage("INFO: " + displayMessage.getMessage(), Color.GREEN);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up MQTT listener", e);
            showMessage("Error setting up message listener: " + e.getMessage(), Color.RED);
        }
    }

    public static void launchDisplay(String instId, String machId, MqttClient client) {
        if (instId == null || machId == null || client == null) {
            LOGGER.log(Level.SEVERE, "Cannot launch display with null parameters");
            throw new IllegalArgumentException("InstId, MachId, and Client cannot be null");
        }

        instituteId = instId;
        machineId = machId;
        mqttClient = client;
        new Thread(() -> Application.launch(DisplayWindow.class)).start();
    }

    @Override
    public void stop() {
        scheduler.shutdown();
        Platform.exit();
    }
}