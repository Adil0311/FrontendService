package org.uniupo.it.service;

import com.google.gson.Gson;
import javafx.application.Platform;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.uniupo.it.model.Selection;
import org.uniupo.it.util.Topics;

public class FrontendService {

    String machineId;
    String instituteId;
    MqttClient mqttClient;
    Gson gson = new Gson();


    public FrontendService(String instituteId, String machineId, MqttClient mqttClient) throws MqttException {
        this.machineId = machineId;
        this.mqttClient = mqttClient;
        this.instituteId = instituteId;
        mqttClient.subscribe(String.format(Topics.KILL_SERVICE_TOPIC, instituteId, machineId), this::killServiceHandler);

    }

    private void killServiceHandler(String topic, MqttMessage message) {
        System.out.println("Service killed, hello darkness my old friend :(");
        stop();

    }

    public void publishNewSelection(Selection selection) throws MqttException {

        try {
            String json = gson.toJson(selection, Selection.class);
            mqttClient.publish(String.format(Topics.TRANSACTION_NEW_SELECTION_TOPIC, instituteId, machineId), new MqttMessage(json.getBytes()));
        } catch (MqttException e) {
            System.err.println("Error while publishing message: " + e.getMessage());
            e.printStackTrace();
        }


    }

    public void publishNewCoinInserted() throws MqttException {

        mqttClient.publish(String.format(Topics.TRANSACTION_NEW_COIN_INSERTED_TOPIC, instituteId, machineId), new MqttMessage("New coin inserted".getBytes()));

    }

    public void publishCancelSelection() throws MqttException {

        mqttClient.publish(String.format(Topics.TRANSACTION_CANCEL_SELECTION_TOPIC, instituteId, machineId), new MqttMessage("Cancel selection".getBytes()));

    }

    public void stop() {
        Platform.runLater(() -> {
            try {
                mqttClient.disconnect();
                Platform.exit();
                System.exit(0);
            } catch (MqttException e) {
                System.err.println("Error during shutdown: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
