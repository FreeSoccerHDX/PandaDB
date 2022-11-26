package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.PandaClientChannel;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONObject;

public class TextListener {

    private PandaServer pandaServer;

    public TextListener(PandaServer pandaServer){
        this.pandaServer = pandaServer;
    }

    public JSONObject parseData(PandaClientChannel channel, String data){
        JSONObject jsonObject = new JSONObject(data);
        String questid = jsonObject.has("q") ? jsonObject.getString("q") : null;
        String key = jsonObject.getString("k");
        String member = jsonObject.getString("m");

        if(channel == PandaClientChannel.GET) {
            Pair<Status, String> value = pandaServer.getDataStorage().get(key, member);
            return createTotalObject(questid, value);
        }else if(channel == PandaClientChannel.SET) {
            String value = jsonObject.getString("value");
            Status erfolg = pandaServer.getDataStorage().set(key, member, value);
            return createTotalObject(questid, erfolg);
        }
        return null;
    }


    private JSONObject createTotalObject(String questid, Object info){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            if (info != null) {
                if(info instanceof Status) {
                    jsonObject.put("s", ((Status) info).ordinal());
                }else{
                    Pair<Status,String> pair = (Pair<Status, String>) info;
                    jsonObject.put("s", pair.getFirst().ordinal());
                    jsonObject.put("i", pair.getSecond());
                }
            }
            return jsonObject;
        }
        return null;
    }

}
