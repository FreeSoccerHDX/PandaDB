package de.freesoccerhdx.pandadb.serverutils;

import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.pandadb.clientutils.PandaClientChannel;
import de.freesoccerhdx.pandadb.clientutils.changelistener.ChangeReason;
import de.freesoccerhdx.pandadb.serverutils.datastorage.SimpleDataStorage;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;

public class SimpleListener {


    private final PandaServer server;
    public SimpleListener(PandaServer pandaServer) {
        this.server = pandaServer;

    }

    public JSONObject parseData(PandaClientChannel channel, JSONObject jsonObject) {
        String questid = jsonObject.has("q") ? jsonObject.getString("q") : null;
        String key = jsonObject.has("k") ? jsonObject.getString("k") : null;

        Object info = null;

        SimpleDataStorage sds = server.getDataStorage().getSimpleData();
        if(channel == PandaClientChannel.SIMPLE_SET) {
            String value = jsonObject.getString("v");
            Pair<Status,String> info0 = sds.setSimple(key, value);
            info = info0;
            if(info0.getFirst() == Status.SUCCESSFUL_OVERWRITE_OLD) {
                onChange(ChangeReason.OVERWRITE, key, info0.getSecond(), value);
            }else if(info0.getFirst() == Status.SUCCESSFUL_CREATED_NEW) {
                onChange(ChangeReason.SET, key, null, value);
            }

        }else if(channel == PandaClientChannel.SIMPLE_GET) {
            info = sds.getSimple(key);
        }else if(channel == PandaClientChannel.SIMPLE_REMOVE) {
            Pair<Status,String> info0 = sds.removeSimple(key);
            info = info0;
            if(info0.getFirst() == Status.SUCCESSFUL_REMOVED_KEY) {
                onChange(ChangeReason.REMOVE_KEY, key, info0.getSecond(), null);
            }
        }else if(channel == PandaClientChannel.SIMPLE_GET_KEYS) {
            info = sds.getSimpleKeys();
        }else if(channel == PandaClientChannel.SIMPLE_GET_DATA) {
            info = sds.getSimpleData();
        }


        return createTotalObject(questid, info);
    }

    private JSONObject createTotalObject(String questid, Object info){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            if (info != null) {
                if(info instanceof Status s) {
                    jsonObject.put("s", s.ordinal());
                }else if(info instanceof Pair pair) {
                    jsonObject.put("s", ((Status)pair.getFirst()).ordinal());
                    jsonObject.put("i", pair.getSecond());
                }
            }
            return jsonObject;
        }
        return null;
    }

    // UUID -> CleintName,Key
    private HashMap<UUID, Pair<String,String>> changeListener = new HashMap<>();

    public void addChangeListener(UUID uuid, String clientName, String key) {
        if(!changeListener.containsKey(uuid)) {
            changeListener.put(uuid, Pair.of(clientName, key));
        }
    }

    public void removeChangeListener(UUID uuid) {
        if(changeListener.containsKey(uuid)) {
            changeListener.remove(uuid);
        }
    }

    private void onChange(ChangeReason changeReason, String key, String oldvalue, String newvalue) {
        for(UUID uuid : changeListener.keySet()) {
            Pair<String, String> triple = changeListener.get(uuid);
            String tripleKey = triple.getSecond();
            if(tripleKey.equals(key)) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uuid", uuid.toString());
                jsonObject.put("cr", changeReason.ordinal());
                if(oldvalue != null) jsonObject.put("old", oldvalue);
                if(newvalue != null) jsonObject.put("new", newvalue);
                server.getSimpleSocketServer().sendNewMessage(triple.getFirst(),"changeresponse",jsonObject.toString());
            }
        }
    }
}
