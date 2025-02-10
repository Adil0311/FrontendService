package org.uniupo.it;

import javafx.application.Platform;
import org.eclipse.paho.client.mqttv3.*;
import org.uniupo.it.dao.DrinkDaoImpl;
import org.uniupo.it.model.Drink;
import org.uniupo.it.model.Selection;
import org.uniupo.it.mqttConfig.MqttOptions;
import org.uniupo.it.service.DisplayWindow;
import org.uniupo.it.service.FrontendService;

import java.util.*;

public class Main {

    public static DrinkDaoImpl drinkDao;
    private static final Set<Double> MONETE_ACCETTATE = Set.of(0.05, 0.10, 0.20, 0.50, 1.00, 2.00);

    public static void main(String[] args) {
        String mqttUrl= "ssl://localhost:8883";
        String machineId;
        String instituteId;
        if (args.length != 2) {
            System.out.println("Parametri non validi");
            System.exit(1);
        }
        instituteId = args[0];
        machineId = args[1];
        drinkDao = new DrinkDaoImpl(instituteId, machineId);

        try {

            MqttClient mqttClient = new MqttClient(mqttUrl, UUID.randomUUID() + " " + machineId);
            mqttClient.connect(new MqttOptions().getOptions());


            Platform.setImplicitExit(true);
            DisplayWindow.launchDisplay(instituteId, machineId, mqttClient);

            Thread.sleep(1000);

            FrontendService frontendService=new FrontendService(instituteId,machineId, mqttClient);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    frontendService.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
            startMenu(frontendService);

        } catch (MqttException e) {
            System.out.println("Errore nella connessione al broker MQTT.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Errore nell'attesa della terminazione del thread di visualizzazione.");
            throw new RuntimeException(e);
        }
    }

    private static void startMenu(FrontendService frontendService) {
        Scanner scanner = new Scanner(System.in);
        int zucchero = 0;
        boolean running = true;

        while (running) {

            System.out.println("\n=== Menu Macchina Bevande ===");
            System.out.println("1. Inserisci moneta");
            System.out.println("2. Gestisci zucchero (" + zucchero + "/5)");
            System.out.println("3. Seleziona bevanda");
            System.out.println("4. Annulla selezione");
            System.out.println("5. Esci");
            System.out.print("Seleziona un'opzione: ");

            int scelta = scanner.nextInt();
            try {
                switch (scelta) {
                    case 1 -> {
                        inserisciMoneta(scanner, frontendService);
                        Thread.sleep(1000);
                    }
                    case 2 -> {
                        zucchero = gestisciZucchero(scanner, zucchero);
                        Thread.sleep(1000);
                    }
                    case 3 -> {
                        selezionaBevanda(scanner, frontendService, zucchero);
                        Thread.sleep(1000);
                    }
                    case 4 -> {
                        try {
                            frontendService.publishCancelSelection();
                            System.out.println("Selezione annullata.");
                            zucchero = 0;
                            Thread.sleep(1000);
                        } catch (MqttException e) {
                            System.out.println("Errore nell'annullamento della selezione.");
                            e.printStackTrace();
                        }
                    }
                    case 5 -> {
                        System.out.println("Arrivederci!");
                        running = false;
                    }
                    default -> {
                        System.out.println("Scelta non valida, riprova.");
                        Thread.sleep(1000); // Pausa di 1 secondo
                    }
                }
            } catch (InterruptedException e) {
                System.err.println("Errore durante la pausa: " + e.getMessage());
            }
        }
    }

    private static void inserisciMoneta(Scanner scanner, FrontendService frontendService) {
        System.out.println("\nMonete accettate:");
        List<Double> moneteList = new ArrayList<>(MONETE_ACCETTATE);
        Collections.sort(moneteList);
        for (int i = 0; i < moneteList.size(); i++) {
            System.out.printf("%d. %.2f€%n", i + 1, moneteList.get(i));
        }
        System.out.print("Seleziona la moneta (1-" + moneteList.size() + "): ");

        int sceltaMoneta = scanner.nextInt();
        if (sceltaMoneta >= 1 && sceltaMoneta <= moneteList.size()) {
            double moneta = moneteList.get(sceltaMoneta - 1);
            try {
                drinkDao.insertCoin(moneta);
                frontendService.publishNewCoinInserted();
                System.out.printf("Moneta da %.2f€ inserita.%n", moneta);
            } catch (MqttException e) {
                System.out.println("Errore nell'inserimento della moneta.");
                e.printStackTrace();
            }
        } else {
            System.out.println("Selezione moneta non valida.");
        }
    }

    private static int gestisciZucchero(Scanner scanner, int zucchero) {
        System.out.println("\n--- Gestione Zucchero ---");
        System.out.println("1. Aggiungi zucchero");
        System.out.println("2. Rimuovi zucchero");
        System.out.println("3. Torna al menu principale");
        System.out.print("Scelta: ");

        int scelta = scanner.nextInt();
        switch (scelta) {
            case 1 -> {
                if (zucchero < 5) {
                    zucchero++;
                    System.out.println("Zucchero aggiunto. Livello: " + zucchero);
                } else {
                    System.out.println("Livello massimo di zucchero raggiunto.");
                }
            }
            case 2 -> {
                if (zucchero > 0) {
                    zucchero--;
                    System.out.println("Zucchero rimosso. Livello: " + zucchero);
                } else {
                    System.out.println("Livello minimo di zucchero raggiunto.");
                }
            }
            case 3 -> System.out.println("Ritorno al menu principale.");
            default -> System.out.println("Scelta non valida.");
        }
        return zucchero;
    }

    private static void selezionaBevanda(Scanner scanner, FrontendService frontendService, int zucchero) {
        try {
            List<Drink> drinks = drinkDao.getAllDrinks();
            System.out.println("\nBevande disponibili:");
            for (int i = 0; i < drinks.size(); i++) {
                Drink drink = drinks.get(i);
                System.out.printf("%d. %s - %s (%.2f€)%n",
                        i + 1,
                        drink.getName(),
                        drink.getDescription(),
                        drink.getPrice()
                );
            }
            System.out.print("Seleziona una bevanda (1-" + drinks.size() + "): ");

            int sceltaBevanda = scanner.nextInt();
            if (sceltaBevanda >= 1 && sceltaBevanda <= drinks.size()) {
                Drink selectedDrink = drinks.get(sceltaBevanda - 1);
                Selection selection = new Selection(selectedDrink.getCode(), zucchero);
                frontendService.publishNewSelection(selection);
                System.out.printf("Bevanda selezionata: %s (Zucchero: %d)%n",
                        selectedDrink.getName(), zucchero);
            } else {
                System.out.println("Selezione bevanda non valida.");
            }
        } catch (Exception e) {
            System.out.println("Errore nel recupero delle bevande dal database.");
            e.printStackTrace();
        }
    }
}