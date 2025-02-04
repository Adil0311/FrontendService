package org.uniupo.it.util;

import java.util.Locale;

public class Topics {

    private final static String BASE_TOPIC = "istituto/%s/macchina/%s";
    public static final String DISPLAY_TOPIC_UPDATE = BASE_TOPIC+"/frontend/screen/update";

    public static final String TRANSACTION_NEW_SELECTION_TOPIC = BASE_TOPIC+"/transaction/newSelection";
    public static final String TRANSACTION_NEW_COIN_INSERTED_TOPIC = BASE_TOPIC+"/transaction/newCoinInserted";
    public static final String TRANSACTION_CANCEL_SELECTION_TOPIC = BASE_TOPIC+"/transaction/cancelSelection";

    public static final String KILL_SERVICE_TOPIC = "macchinette/%s/%s/killService";
}
