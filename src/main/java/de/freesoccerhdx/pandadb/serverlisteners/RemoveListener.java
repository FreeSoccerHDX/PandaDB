package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.ListType;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.server.ClientSocket;
import de.freesoccerhdx.simplesocket.server.ServerListener;
import de.freesoccerhdx.simplesocket.server.SimpleSocketServer;
import org.json.JSONObject;

public class RemoveListener extends ServerListener {

    private PandaServer pandaServer;

    public RemoveListener(PandaServer pandaServer){
        this.pandaServer = pandaServer;
        pandaServer.getSimpleSocketServer().setServerListener("remove", this);
    }

    @Override
    public void recive(SimpleSocketServer simpleSocketServer, ClientSocket clientSocket, String channel, String message) {
        JSONObject jsonObject = new JSONObject(message);
        String questid = jsonObject.has("questid") ? jsonObject.getString("questid") : null;
        String key = jsonObject.getString("key");
        int removeTypeId = jsonObject.getInt("removetype"); //0=value, 1=text, 2=list(listkey), 3=listIndex(listkey), 4=list(list and all sub-listkeys)
        Status status = null;

        if(removeTypeId == 0){
            String member = jsonObject.getString("member");
            status = pandaServer.getDataStorage().removeValue(key, member);
        }else if(removeTypeId == 1){
            String member = jsonObject.getString("member");
            status = pandaServer.getDataStorage().remove(key, member);
        }else if(removeTypeId == 2){
            int id = jsonObject.getInt("type");
            String listkey = jsonObject.getString("listkey");
            ListType listType = ListType.values()[id];
            status = pandaServer.getDataStorage().removeList(key, listkey, listType);
        }else if(removeTypeId == 3){
            int id = jsonObject.getInt("type");
            int index = jsonObject.getInt("index");
            String listkey = jsonObject.getString("listkey");
            ListType listType = ListType.values()[id];
            status = pandaServer.getDataStorage().removeListIndex(key, listkey, listType, index);
        }else if(removeTypeId == 4){
            int id = jsonObject.getInt("type");
            ListType listType = ListType.values()[id];
            status = pandaServer.getDataStorage().removeList(key, listType);
        }

        sendRemoveFeedback(clientSocket, questid, status);
    }

    private void sendRemoveFeedback(ClientSocket clientSocket, String questid, Status status){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            jsonObject.put("s", status.ordinal());

            clientSocket.sendNewMessage("removefeedback", jsonObject.toString(), null);
        }
    }

}
