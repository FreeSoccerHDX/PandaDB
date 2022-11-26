package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.ListType;
import de.freesoccerhdx.pandadb.PandaClientChannel;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONObject;

import java.util.List;

public class ListKeysListener {

    private PandaServer pandaServer;

    public ListKeysListener(PandaServer pandaServer){
        this.pandaServer = pandaServer;
    }


    public JSONObject parseData(PandaClientChannel channel, String data){
        JSONObject jsonObject = new JSONObject(data);
        String questid = jsonObject.has("q") ? jsonObject.getString("q") : null;
        int gettype = jsonObject.getInt("gettype");
        Pair<Status, List<String>> list = null;

        if(gettype == 0){ // getListKeys
            int listtypeID = jsonObject.getInt("type");
            ListType listType = ListType.values()[listtypeID];
            list = pandaServer.getDataStorage().getListKeys(listType);
        }else if(gettype == 1) { // getListKeys
            String key = jsonObject.getString("k");
            int listtypeID = jsonObject.getInt("type");
            ListType listType = ListType.values()[listtypeID];
            list = pandaServer.getDataStorage().getListMemberKeys(key, listType);
        }else if(gettype == 2) { // getKeys
            String key = jsonObject.getString("k");
            list = pandaServer.getDataStorage().getMemberKeys(key);
        }else if(gettype == 3) { // getKeys
            list = pandaServer.getDataStorage().getKeys();
        }else if(gettype == 4) { // getValueKeys
            list = pandaServer.getDataStorage().getValueKeys();
        }else if(gettype == 5) { // getValueKeys
            String key = jsonObject.getString("k");
            list = pandaServer.getDataStorage().getValueKeys(key);
        }else if(gettype == 10) {
            String key = jsonObject.getString("k");
            list = pandaServer.getDataStorage().getStoredSerializableMemberKeys(key);
        }else if(gettype == 11) {
            list = pandaServer.getDataStorage().getStoredSerializableKeys();
        }

        return createTotalObject(questid, list);
    }

    private <T> JSONObject createTotalObject(String questid, Pair<Status,List<String>> pair){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            jsonObject.put("s", pair.getFirst().ordinal());
            if (pair.getSecond() != null) {
                jsonObject.put("i", pair.getSecond());
            }
            return jsonObject;
        }
        return null;
    }

}
