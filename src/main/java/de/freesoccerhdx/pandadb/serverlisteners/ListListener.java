package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.ListType;
import de.freesoccerhdx.pandadb.clientutils.PandaClientChannel;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONObject;

import java.util.List;

public class ListListener {

    private final PandaServer pandaServer;

    public ListListener(PandaServer pandaServer){
        this.pandaServer = pandaServer;
    }

    public JSONObject parseData(PandaClientChannel channel, JSONObject jsonObject){
        String questid = jsonObject.has("q") ? jsonObject.getString("q") : null;
        String key = jsonObject.has("k") ? jsonObject.getString("k") : null;
        Integer id = jsonObject.has("t") ? jsonObject.getInt("t") : null;
        ListType listType = id != null ? ListType.values()[id] : null;

        ListTypeDataStorage storage = pandaServer.getDataStorage().getListData();

        if(channel == PandaClientChannel.LISTDATA_ADD_LIST_ENTRY) {
            Status info = storage.addListEntry(key, listType, jsonObject.get("v"));
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.LISTDATA_REMOVE_LISTTYPE) {
            Status info = storage.removeListType(listType);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.LISTDATA_REMOVE_LISTKEY) {
            Status info = storage.removeListKey(listType, key);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.LISTDATA_REMOVE_INDEX) {
            Pair<Status, Object> info = storage.removeListIndex(listType, key, jsonObject.getInt("in"));
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.LISTDATA_GET_LISTTYPE_KEYS) {
            Pair<Status, List<String>> info = storage.getListKeys(listType);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.LISTDATA_GET_LISTTYPES) {
            Pair<Status, List<Integer>> info = storage.getListTypes();
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.LISTDATA_GET_LISTKEY_DATA) {
            Pair<Status, List<Object>> info = storage.getList(listType, key);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.LISTDATA_GET_LISTINDEX) {
            Pair<Status, Object> info = storage.getListIndex(listType, key, jsonObject.getInt("in"));
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.LISTDATA_GET_LISTSIZE) {
            Pair<Status, Integer> info = storage.getListSize(listType, key);
            return createTotalObject(questid, info);
        }else {
            System.out.println("[PandaServer] Unknown Channel for ListListener: " + channel + " data="+jsonObject);
        }

        return null;
    }

    private JSONObject createTotalObject(String questid, Object info){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            if (info != null) {
                if(info instanceof Status s){
                    jsonObject.put("s", s.ordinal());
                }else if(info instanceof Pair p){
                    //Pair<Status, List<Object>> pair = (Pair<Status, List<Object>>) info;
                    jsonObject.put("s", ((Status)p.getFirst()).ordinal());
                    if(p.getSecond() != null) {
                        jsonObject.put("i", p.getSecond());
                    }
                }

            }
            return jsonObject;
        }
        return null;
    }


}
