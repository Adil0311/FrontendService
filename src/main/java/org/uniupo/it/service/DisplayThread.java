package org.uniupo.it.service;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import org.uniupo.it.model.DisplayMessageFormat;

public class DisplayThread extends Thread {
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String CLEAR_LINE = "\u001B[2K";
    private static final int MESSAGE_AREA_HEIGHT = 7; // Aumentato per dare più spazio

    private final String instituteId;
    private final String machineId;
    private final MqttClient mqttClient;
    private final Gson gson;
    private volatile boolean running;
    private volatile String lastMessage = "";
    private volatile boolean isLastMessageError = false;

    public DisplayThread(String instituteId, String machineId, MqttClient mqttClient) {
        this.instituteId = instituteId;
        this.machineId = machineId;
        this.mqttClient = mqttClient;
        this.gson = new Gson();
        this.running = true;

        initializeDisplay();
    }

    private void initializeDisplay() {
        // Pulisci lo schermo
        System.out.print("\u001B[2J");
        // Vai in alto a sinistra
        System.out.print("\u001B[H");
        // Crea l'area messaggi
        System.out.println("=== Area Messaggi ===");
        System.out.println("==========================================");
        // Aggiungi linee vuote per l'area messaggi
        for (int i = 0; i < MESSAGE_AREA_HEIGHT - 4; i++) {
            System.out.println();
        }
        // Linea di separazione finale
        System.out.println("==========================================");
    }

    @Override
    public void run() {
        try {
            String displayTopic = String.format("istituto/%s/macchina/%s/frontend/screen/update",
                    instituteId, machineId);

            mqttClient.subscribe(displayTopic, (topic, message) -> {
                try {
                    String payload = new String(message.getPayload());
                    DisplayMessageFormat displayMessage = gson.fromJson(payload, DisplayMessageFormat.class);

                    // Aggiorna il messaggio corrente
                    lastMessage = displayMessage.getMessage();
                    isLastMessageError = displayMessage.isError();

                    updateDisplay();

                    // Aggiungi una piccola pausa per permettere la lettura
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("Error processing display message: " + e.getMessage());
                }
            });

            while (running) {
                Thread.sleep(100);
            }
        } catch (Exception e) {
            System.err.println("Error in DisplayThread: " + e.getMessage());
        }
    }

    private void updateDisplay() {
        // Salva la posizione corrente del cursore
        System.out.print("\u001B[s");

        System.out.print("\u001B[3;1H");

        for (int i = 0; i < MESSAGE_AREA_HEIGHT - 4; i++) {
            System.out.print("\u001B[" + (3 + i) + ";1H");
            System.out.print(CLEAR_LINE);
        }

        // Stampa il messaggio con il colore appropriato
        if (isLastMessageError) {
            System.out.print(ANSI_RED + "ERROR: " + lastMessage + ANSI_RESET);
        } else if (!lastMessage.isEmpty()) {
            System.out.print(ANSI_GREEN + "INFO: " + lastMessage + ANSI_RESET);
        }

        // Ripristina la posizione del cursore
        System.out.print("\u001B[u");
    }

    public void stopThread() {
        this.running = false;
    }

    public static int getMessageAreaHeight() {
        return MESSAGE_AREA_HEIGHT;
    }
}