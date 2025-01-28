package org.uniupo.it;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.uniupo.it.model.Selection;
import org.uniupo.it.mqttConfig.MqttOptions;
import org.uniupo.it.service.FrontendService;

import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    static Gson gson = new Gson();

    public static void main(String[] args) {

        String mqttUrl = "";
        String machineId = "";

        Properties properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("config.properties"));
            mqttUrl = properties.getProperty("mqttUrl");
            machineId = properties.getProperty("machineId");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            MqttClient mqttClient = new MqttClient(mqttUrl, UUID.randomUUID() + " " + machineId);
            MqttConnectOptions mqttOptions = new MqttOptions().getOptions();
            mqttClient.connect(mqttOptions);
            FrontendService frontendService = new FrontendService(machineId, mqttClient);
            startMenu(mqttClient,machineId);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void startMenu(MqttClient mqttClient, String machineId) {
        Scanner scanner = new Scanner(System.in);
        int zucchero = 0;
        boolean running = true;

        while (running) {
            System.out.println("\n--- Menu Macchina ---");
            System.out.println("1. Aggiungi un'unità di zucchero");
            System.out.println("2. Rimuovi un'unità di zucchero");
            System.out.println("3. Visualizza elenco bevande");
            System.out.println("4. Annulla selezione");
            System.out.println("5. Esci");
            System.out.print("Seleziona un'opzione: ");

            int scelta = scanner.nextInt();

            switch (scelta) {
                case 1 -> {
                    zucchero++;
                    System.out.println("Zucchero aggiunto. Totale zucchero: " + zucchero);
                }
                case 2 -> {
                    if (zucchero > 0) {
                        zucchero--;
                        System.out.println("Zucchero rimosso. Totale zucchero: " + zucchero);
                    } else {
                        System.out.println("Nessuna unità di zucchero da rimuovere.");
                    }
                }
                case 3 -> {
                    System.out.println("Elenco Bevande Disponibili:");
                    System.out.println("- Caffè");
                    System.out.println("- Cappuccino");
                    System.out.println("- Tè");
                    System.out.println("- Cioccolata calda");
                    System.out.print("Seleziona una bevanda: ");
                    scanner.nextLine(); // Consuma il newline
                    String bevanda = scanner.nextLine();
                    String message= gson.toJson(new Selection(bevanda,zucchero));
                    try {
                        mqttClient.publish(String.format("macchina/%s/transaction/newSelection",machineId), new MqttMessage(message.getBytes()));
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Bevanda selezionata: " + bevanda);
                }
                case 4 -> {
                    System.out.println("Selezione annullata.");
                    zucchero = 0;
                }
                case 5 -> {
                    System.out.println("Uscita dal programma.");
                    running = false;
                }
                default -> System.out.println("Scelta non valida, riprova.");
            }
        }
    }
}

