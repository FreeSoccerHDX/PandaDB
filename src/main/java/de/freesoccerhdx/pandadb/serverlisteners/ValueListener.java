package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.Pair;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.server.ClientSocket;
import de.freesoccerhdx.simplesocket.server.ServerListener;
import de.freesoccerhdx.simplesocket.server.SimpleSocketServer;
import org.json.JSONObject;

public class ValueListener extends ServerListener {

    private PandaServer pandaServer;

    public ValueListener(PandaServer pandaServer){
        this.pandaServer = pandaServer;
        pandaServer.getSimpleSocketServer().setServerListener("getvalue", this);
        pandaServer.getSimpleSocketServer().setServerListener("setvalue", this);
        pandaServer.getSimpleSocketServer().setServerListener("addvalue", this);
    }

    @Override
    public void recive(SimpleSocketServer simpleSocketServer, ClientSocket clientSocket, String channel, String message) {
        JSONObject jsonObject = new JSONObject(message);
        String questid = jsonObject.has("questid") ? jsonObject.getString("questid") : null;
        String key = jsonObject.getString("key");
        String member = jsonObject.getString("member");

        if(channel.equals("getvalue")) {
            Pair<Status, Double> value = pandaServer.getDataStorage().getValue(key, member);
            sendValueFeedback(clientSocket, questid, value);
        }else if(channel.equals("setvalue")) {
            Double value = jsonObject.getDouble("value");
            Pair<Status, Double> erfolg = pandaServer.getDataStorage().setValue(key, member, value);
            sendValueFeedback(clientSocket, questid, erfolg);
        }else if(channel.equals("addvalue")){
            Double value = jsonObject.getDouble("value");
            Pair<Status, Double> erfolg = pandaServer.getDataStorage().addValue(key, member, value);
            sendValueFeedback(clientSocket, questid, erfolg);
        }
    }

    private void sendValueFeedback(ClientSocket clientSocket, String questid, Pair<Status, Double> info){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            jsonObject.put("s", info.getFirst().ordinal());
            Double value = info.getSecond();
            if (value != null) {
                jsonObject.put("info", value);
            }
            clientSocket.sendNewMessage("valuefeedback", jsonObject.toString(), null);
        }
    }

}
