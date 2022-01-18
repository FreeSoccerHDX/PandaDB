package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.Pair;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.server.ClientSocket;
import de.freesoccerhdx.simplesocket.server.ServerListener;
import de.freesoccerhdx.simplesocket.server.SimpleSocketServer;
import org.json.JSONObject;

public class TextListener extends ServerListener {

    private PandaServer pandaServer;

    public TextListener(PandaServer pandaServer){
        this.pandaServer = pandaServer;
        pandaServer.getSimpleSocketServer().setServerListener("set", this);
        pandaServer.getSimpleSocketServer().setServerListener("get", this);
    }

    @Override
    public void recive(SimpleSocketServer simpleSocketServer, ClientSocket clientSocket, String channel, String message) {
        JSONObject jsonObject = new JSONObject(message);
        String questid = jsonObject.has("questid") ? jsonObject.getString("questid") : null;
        String key = jsonObject.getString("key");
        String member = jsonObject.getString("member");

        if(channel.equals("get")) {
            Pair<Status, String> value = pandaServer.getDataStorage().get(key, member);
            sendTextFeedback(clientSocket, questid, value);
        }else if(channel.equals("set")) {
            String value = jsonObject.getString("value");
            Status erfolg = pandaServer.getDataStorage().set(key, member, value);
            sendTextFeedback(clientSocket, questid, erfolg);
        }
    }

    private void sendTextFeedback(ClientSocket clientSocket, String questid, Object info){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            if (info != null) {
                if(info instanceof Status) {
                    jsonObject.put("s", ((Status) info).ordinal());
                }else{
                    Pair<Status,String> pair = (Pair<Status, String>) info;
                    jsonObject.put("s", pair.getFirst().ordinal());
                    jsonObject.put("info", pair.getSecond());
                }
            }
            clientSocket.sendNewMessage("textfeedback", jsonObject.toString(), null);
        }
    }

}
