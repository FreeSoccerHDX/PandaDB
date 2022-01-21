package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.PandaClientChannel;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONObject;

public class SerializableListener {

    private PandaServer pandaServer;

    public SerializableListener(PandaServer pandaServer) {
        this.pandaServer = pandaServer;
    }

    public JSONObject parseData(PandaClientChannel channel, String data) {
        JSONObject jsonObject = new JSONObject(data);
        String questid = jsonObject.has("q") ? jsonObject.getString("q") : null;
        String key = jsonObject.getString("k");
        String member = jsonObject.getString("m");

        if(channel == PandaClientChannel.GET_STORED_SERIALIZABLE) {
            Pair<Status, String> value = pandaServer.getDataStorage().getStoredSerializable(key, member);
            return createTotalObject(questid, value);

        }else if(channel == PandaClientChannel.STORE_SERIALIZABLE) {
            String value = jsonObject.getString("pds");
            Status erfolg = pandaServer.getDataStorage().storeSerializable(key, member, value);
            return createTotalObject(questid, Pair.of(erfolg,null));
        }

        return null;
    }

    private JSONObject createTotalObject(String questid, Pair<Status, String> info){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            jsonObject.put("s", info.getFirst().ordinal());
            String jsonString = info.getSecond();
            if (jsonString != null) {
                jsonObject.put("i", jsonString);
            }
            return jsonObject;
        }
        return null;
    }

}
