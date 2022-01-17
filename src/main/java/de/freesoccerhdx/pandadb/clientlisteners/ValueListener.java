package de.freesoccerhdx.pandadb.clientlisteners;

import de.freesoccerhdx.pandadb.PandaClient;
import de.freesoccerhdx.simplesocket.client.ClientListener;
import de.freesoccerhdx.simplesocket.client.SimpleSocketClient;
import org.json.JSONObject;

public class ValueListener extends ClientListener {

    private PandaClient pandaClient;

    public ValueListener(PandaClient pandaClient){
        this.pandaClient = pandaClient;
        pandaClient.getSimpleSocketClient().setSocketListener("valuefeedback", this);
    }

    @Override
    public void recive(SimpleSocketClient simpleSocketClient, String channel, String source, String message) {
        JSONObject jsonObject = new JSONObject(message);
        String id = jsonObject.getString("id");
        Double info = null;
        if(jsonObject.has("info")){
            info = jsonObject.getDouble("info");
        }
        this.pandaClient.handleResult(id, info);
    }
}
