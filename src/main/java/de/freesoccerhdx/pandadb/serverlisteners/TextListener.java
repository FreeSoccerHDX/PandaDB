package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.clientutils.PandaClientChannel;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class TextListener {

    private final PandaServer pandaServer;

    public TextListener(PandaServer pandaServer){
        this.pandaServer = pandaServer;
    }

    public JSONObject parseData(PandaClientChannel channel, JSONObject jsonObject){
        String questid = jsonObject.has("q") ? jsonObject.getString("q") : null;
        String key = jsonObject.has("k") ? jsonObject.getString("k") : null;
        String member = jsonObject.has("m") ? jsonObject.getString("m") : null;


        TextsDataStorage tds = pandaServer.getDataStorage().getTextData();

        if(channel == PandaClientChannel.TEXT_SET) {
            String value = jsonObject.getString("v");
            Status status = tds.set(key, member, value);
            return createTotalObject(questid, status);
        }else if(channel == PandaClientChannel.TEXT_GET_KEYS){
            Pair<Status, List<String>> info = tds.getKeys();
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.TEXT_GET_MEMBER_KEYS){
            Pair<Status, List<String>> info = tds.getMemberKeys(key);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.TEXT_GET_MEMBER_DATA){
            Pair<Status, String> info = tds.getMemberData(key, member);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.TEXT_GET_KEY_DATA){
            Pair<Status, HashMap<String, String>> info = tds.getKeyData(key);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.TEXT_REMOVE_KEY){
            Status info = tds.removeKey(key);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.TEXT_REMOVE_MEMBER){
            Pair<Status, String> info = tds.removeMember(key, member);
            return createTotalObject(questid, info);
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
                    if(pair.getSecond() instanceof HashMap map) {
                        jsonObject.put("i", ((HashMap<String, String>)map));
                    }else {
                        jsonObject.put("i", pair.getSecond());
                    }
                }
            }
            return jsonObject;
        }
        return null;
    }

}
