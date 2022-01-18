package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.Pair;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.pandadb.ValueDataStorage;
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
        pandaServer.getSimpleSocketServer().setServerListener("infovalues", this);
    }

    @Override
    public void recive(SimpleSocketServer simpleSocketServer, ClientSocket clientSocket, String channel, String message) {
        JSONObject jsonObject = new JSONObject(message);
        String questid = jsonObject.has("questid") ? jsonObject.getString("questid") : null;
        String key = jsonObject.getString("key");
        String member = jsonObject.has("member") ? jsonObject.getString("member") : null;

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
        }else if(channel.equals("infovalues")){
            Pair<Status, ValueDataStorage.ValueMembersInfo> pair = pandaServer.getDataStorage().getValuesInfo(key, jsonObject.has("k"));
            sendValueInfoFeedback(clientSocket,questid,pair);
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

    private void sendValueInfoFeedback(ClientSocket clientSocket, String questid, Pair<Status, ValueDataStorage.ValueMembersInfo> info){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            jsonObject.put("s", info.getFirst().ordinal());
            ValueDataStorage.ValueMembersInfo vmi = info.getSecond();
            if(vmi != null){
                jsonObject.put("avr", vmi.getAverageValue());
                jsonObject.put("low", vmi.getLowestValue());
                jsonObject.put("high", vmi.getHighestValue());
                jsonObject.put("size", vmi.getSize());
                if(vmi.getMembers() != null) {
                    jsonObject.put("mem", vmi.getMembers());
                }
            }

            clientSocket.sendNewMessage("valueinfofeedback", jsonObject.toString(), null);
        }
    }

}
