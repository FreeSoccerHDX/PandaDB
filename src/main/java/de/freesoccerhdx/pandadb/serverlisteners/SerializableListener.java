package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.clientutils.PandaClientChannel;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class SerializableListener {

    private final PandaServer pandaServer;

    public SerializableListener(PandaServer pandaServer) {
        this.pandaServer = pandaServer;
    }

    public JSONObject parseData(PandaClientChannel channel, JSONObject jsonObject) {
        String questid = jsonObject.has("q") ? jsonObject.getString("q") : null;
        String key = jsonObject.has("k") ? jsonObject.getString("k") : null;
        String member = jsonObject.has("m") ? jsonObject.getString("m") : null;


        TextsDataStorage sds = pandaServer.getDataStorage().getSerializableData();

        if(channel == PandaClientChannel.SERIALIZABLE_SET) {
            String value = jsonObject.getString("v");
            Pair<Status, String> status = sds.set(key, member, value);
            return createTotalObject(questid, status);
        }else if(channel == PandaClientChannel.SERIALIZABLE_GET_KEYS){
            Pair<Status, List<String>> info = sds.getKeys();
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.SERIALIZABLE_GET_MEMBER_KEYS){
            Pair<Status, List<String>> info = sds.getMemberKeys(key);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.SERIALIZABLE_GET_MEMBER_DATA){
            Pair<Status, String> info = sds.getMemberData(key, member);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.SERIALIZABLE_GET_KEY_DATA){
            Pair<Status, HashMap<String, String>> info = sds.getKeyData(key);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.SERIALIZABLE_REMOVE_KEY){
            Status info = sds.removeKey(key);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.SERIALIZABLE_REMOVE_MEMBER){
            Pair<Status, String> info = sds.removeMember(key, member);
            return createTotalObject(questid, info.getFirst());
        }else {
            System.out.println("[PandaServer] Unknown Channel for TextListener: " + channel + " data="+jsonObject);
        }

        return null;
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

}
