package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.ListType;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.simplesocket.server.ClientSocket;
import de.freesoccerhdx.simplesocket.server.ServerListener;
import de.freesoccerhdx.simplesocket.server.SimpleSocketServer;
import org.json.JSONObject;

import java.util.List;

public class ListKeysListener extends ServerListener {

    private PandaServer pandaServer;

    public ListKeysListener(PandaServer pandaServer){
        this.pandaServer = pandaServer;
        pandaServer.getSimpleSocketServer().setServerListener("listkeys", this);
    }


    @Override
    public void recive(SimpleSocketServer simpleSocketServer, ClientSocket clientSocket, String channel, String message) {
        JSONObject jsonObject = new JSONObject(message);
        String questid = jsonObject.has("questid") ? jsonObject.getString("questid") : null;
        int gettype = jsonObject.getInt("gettype");
        List<String> list = null;

        if(gettype == 0){ // getListKeys
            int listtypeID = jsonObject.getInt("type");
            ListType listType = ListType.values()[listtypeID];
            list = pandaServer.getDataStorage().getListKeys(listType);
        }else if(gettype == 1) { // getListKeys
            String key = jsonObject.getString("key");
            int listtypeID = jsonObject.getInt("type");
            ListType listType = ListType.values()[listtypeID];
            list = pandaServer.getDataStorage().getListKeys(key, listType);
        }else if(gettype == 2) { // getKeys
            String key = jsonObject.getString("key");
            list = pandaServer.getDataStorage().getKeys(key);
        }else if(gettype == 3) { // getKeys
            list = pandaServer.getDataStorage().getKeys();
        }else if(gettype == 4) { // getValueKeys
            list = pandaServer.getDataStorage().getValueKeys();
        }else if(gettype == 5) { // getValueKeys
            String key = jsonObject.getString("key");
            list = pandaServer.getDataStorage().getValueKeys(key);
        }

        sendListKeysFeedback(clientSocket, questid, list);

    }

    private <T> void sendListKeysFeedback(ClientSocket clientSocket, String questid, List<String> info){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            if (info != null) {
                jsonObject.put("info", info);
            }
            clientSocket.sendNewMessage("listkeysfeedback", jsonObject.toString(), null);
        }
    }

}
