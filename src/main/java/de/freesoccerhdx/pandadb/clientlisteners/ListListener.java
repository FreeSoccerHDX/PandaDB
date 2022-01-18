package de.freesoccerhdx.pandadb.clientlisteners;

import de.freesoccerhdx.pandadb.PandaClient;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.client.ClientListener;
import de.freesoccerhdx.simplesocket.client.SimpleSocketClient;
import org.json.JSONObject;

public class ListListener extends ClientListener {

    private PandaClient pandaClient;

    public ListListener(PandaClient pandaClient){
        this.pandaClient = pandaClient;
        pandaClient.getSimpleSocketClient().setSocketListener("listfeedback", this);
    }

    @Override
    public void recive(SimpleSocketClient simpleSocketClient, String channel, String source, String message) {
        JSONObject jsonObject = new JSONObject(message);
        String id = jsonObject.getString("id");
        int statusID = jsonObject.getInt("s");
        Status status = Status.values()[statusID];

        Object info = null;
        if(jsonObject.has("info")){
            info = jsonObject.get("info");
        }
        this.pandaClient.handleResult(id,status,info);
    }

}
