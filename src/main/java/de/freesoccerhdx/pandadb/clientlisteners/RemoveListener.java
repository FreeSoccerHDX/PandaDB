package de.freesoccerhdx.pandadb.clientlisteners;

import de.freesoccerhdx.pandadb.PandaClient;
import de.freesoccerhdx.simplesocket.client.ClientListener;
import de.freesoccerhdx.simplesocket.client.SimpleSocketClient;
import org.json.JSONObject;

public class RemoveListener extends ClientListener {

    private PandaClient pandaClient;

    public RemoveListener(PandaClient pandaClient) {
        this.pandaClient = pandaClient;
        pandaClient.getSimpleSocketClient().setSocketListener("removefeedback", this);
    }

    @Override
    public void recive(SimpleSocketClient simpleSocketClient, String channel, String source, String message) {
        JSONObject jsonObject = new JSONObject(message);
        String id = jsonObject.getString("id");
        Object info = null;
        if(jsonObject.has("info")){
            info = jsonObject.get("info");
        }
        this.pandaClient.handleResult(id,info);
    }

}
