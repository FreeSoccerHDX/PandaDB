package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.ListType;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.simplesocket.server.ClientSocket;
import de.freesoccerhdx.simplesocket.server.ServerListener;
import de.freesoccerhdx.simplesocket.server.SimpleSocketServer;
import org.json.JSONObject;

import java.util.List;

public class ListListener extends ServerListener {

    private PandaServer pandaServer;

    public ListListener(PandaServer pandaServer){
        this.pandaServer = pandaServer;
        pandaServer.getSimpleSocketServer().setServerListener("addlist", this);
        pandaServer.getSimpleSocketServer().setServerListener("getlist", this);
    }

    @Override
    public void recive(SimpleSocketServer simpleSocketServer, ClientSocket clientSocket, String channel, String message) {
        JSONObject jsonObject = new JSONObject(message);
        String questid = jsonObject.has("questid") ? jsonObject.getString("questid") : null;
        String key = jsonObject.getString("key");
        String listkey = jsonObject.getString("listkey");
        int id = jsonObject.getInt("type");
        ListType listType = ListType.values()[id];

        if(channel.equals("getlist")) {
            List<?> value = pandaServer.getDataStorage().getList(key, listkey, listType);
            sendListFeedback(clientSocket, questid, value);
        }else if(channel.equals("addlist")) {
            Object value = jsonObject.get("value");
            boolean erfolg = pandaServer.getDataStorage().addListEntry(key, listkey, listType, value);
            sendListFeedback(clientSocket, questid, erfolg);
        }
    }

    private <T> void sendListFeedback(ClientSocket clientSocket, String questid, Object info){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            if (info != null) {
                jsonObject.put("info", info);
            }
            clientSocket.sendNewMessage("listfeedback", jsonObject.toString(), null);
        }
    }


}
