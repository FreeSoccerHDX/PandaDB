package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.ListType;
import de.freesoccerhdx.pandadb.PandaClientChannel;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import org.json.JSONObject;

public class RemoveListener {

    private PandaServer pandaServer;

    public RemoveListener(PandaServer pandaServer){
        this.pandaServer = pandaServer;
    }

    public JSONObject parseData(PandaClientChannel channel, String data){
        JSONObject jsonObject = new JSONObject(data);
        String questid = jsonObject.has("q") ? jsonObject.getString("q") : null;
        String key = jsonObject.getString("k");
        int removeTypeId = jsonObject.getInt("removetype"); //0=value, 1=text, 2=list(listkey), 3=listIndex(listkey), 4=list(list and all sub-listkeys)
        Status status = null;

        if(removeTypeId == 0){
            String member = jsonObject.getString("m");
            status = pandaServer.getDataStorage().removeValue(key, member);
        }else if(removeTypeId == 1){
            String member = jsonObject.getString("m");
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

        return createTotalObject(questid, status);
    }

    private JSONObject createTotalObject(String questid, Status status){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            jsonObject.put("s", status.ordinal());

           return jsonObject;
        }
        return null;
    }

}
