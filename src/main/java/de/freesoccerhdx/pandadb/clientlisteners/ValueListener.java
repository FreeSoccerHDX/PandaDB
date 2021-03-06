package de.freesoccerhdx.pandadb.clientlisteners;

import de.freesoccerhdx.pandadb.PandaClient;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.pandadb.ValueDataStorage;
import de.freesoccerhdx.simplesocket.client.ClientListener;
import de.freesoccerhdx.simplesocket.client.SimpleSocketClient;
import org.json.JSONObject;

import java.util.List;

public class ValueListener extends ClientListener {

    private PandaClient pandaClient;

    public ValueListener(PandaClient pandaClient){
        this.pandaClient = pandaClient;
        pandaClient.getSimpleSocketClient().setSocketListener("valuefeedback", this);
        pandaClient.getSimpleSocketClient().setSocketListener("valueinfofeedback", this);
    }

    @Override
    public void recive(SimpleSocketClient simpleSocketClient, String channel, String source, String message) {
        JSONObject jsonObject = new JSONObject(message);
        String id = jsonObject.getString("id");
        int statusID = jsonObject.getInt("s");
        Status status = Status.values()[statusID];

        if(channel.equals("valuefeedback")) {
            Double info = null;
            if (jsonObject.has("info")) {
                info = jsonObject.getDouble("info");
            }
            this.pandaClient.handleResult(id, status, info);
        }else if(channel.equals("valueinfofeedback")) {
            ValueDataStorage.ValueMembersInfo vmi = null;
            if(jsonObject.has("info")){
                JSONObject vmiJSON = jsonObject.getJSONObject("info");
                double avr = vmiJSON.getDouble("avr");
                double low = vmiJSON.getDouble("low");
                double high = vmiJSON.getDouble("high");
                int size = vmiJSON.getInt("size");
                List<String> members = null;

                if(vmiJSON.has("mem")){
                    List objectList = vmiJSON.getJSONArray("mem").toList();
                    members = objectList;
                }

                vmi = new ValueDataStorage.ValueMembersInfo(low,high,avr,size,members);
            }
            this.pandaClient.handleResult(id,status,vmi);
        }
    }
}
