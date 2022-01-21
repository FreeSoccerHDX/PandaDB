package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.ListType;
import de.freesoccerhdx.pandadb.PandaClientChannel;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONObject;

import java.util.List;

public class ListListener /*extends ServerListener*/ {

    private PandaServer pandaServer;

    public ListListener(PandaServer pandaServer){
        this.pandaServer = pandaServer;
        //pandaServer.getSimpleSocketServer().setServerListener("addlist", this);
       // pandaServer.getSimpleSocketServer().setServerListener("getlist", this);
    }
    /*
    @Override
    public void recive(SimpleSocketServer simpleSocketServer, ClientSocket clientSocket, String channel, String message) {
        JSONObject jsonObject = parseData(channel,message);
        if(jsonObject != null) {
            clientSocket.sendNewMessage("listfeedback", jsonObject.toString(), null);
        }
    }
    */

    public JSONObject parseData(PandaClientChannel channel, String data){
        JSONObject jsonObject = new JSONObject(data);
        String questid = jsonObject.has("q") ? jsonObject.getString("q") : null;
        String key = jsonObject.getString("k");
        String listkey = jsonObject.getString("listkey");
        int id = jsonObject.getInt("type");
        ListType listType = ListType.values()[id];

        if(channel == PandaClientChannel.GETLIST) {
            Pair<Status, List<Object>> pair = pandaServer.getDataStorage().getList(key, listkey, listType);
            return createTotalObject(questid, pair);
        }else if(channel == PandaClientChannel.ADDLIST) {
            Object value = jsonObject.get("value");
            Status status = pandaServer.getDataStorage().addListEntry(key, listkey, listType, value);
            return createTotalObject(questid, status);
        }

        return null;
    }

    private <T> JSONObject createTotalObject(String questid, Object info){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            if (info != null) {
                if(info instanceof Status){
                    jsonObject.put("s", ((Status) info).ordinal());
                }else{
                    Pair<Status, List<Object>> pair = (Pair<Status, List<Object>>) info;
                    jsonObject.put("s", pair.getFirst().ordinal());
                    if(pair.getSecond() != null) {
                        jsonObject.put("i", pair.getSecond());
                    }
                }

            }
            return jsonObject;
        }
        return null;
    }


}
