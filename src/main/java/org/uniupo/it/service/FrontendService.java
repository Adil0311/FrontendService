package org.uniupo.it.service;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
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


    }

    public void publishNewSelection(Selection selection) throws MqttException {

        try {
            String json = gson.toJson(selection, Selection.class);
            mqttClient.publish(String.format(Topics.TRANSACTION_NEW_SELECTION_TOPIC, instituteId, machineId), new MqttMessage(json.getBytes()));
        }
        catch (MqttException e) {
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
}
