package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.pandadb.clientutils.PandaClientChannel;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONObject;

import java.util.HashMap;

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
            info = sds.setSimple(key, value);
        }else if(channel == PandaClientChannel.SIMPLE_GET) {
            info = sds.getSimple(key);
        }else if(channel == PandaClientChannel.SIMPLE_REMOVE) {
            info = sds.removeSimple(key);
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

}
