package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.PandaClientChannel;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONObject;

import java.util.HashMap;

public class MemberDataListener {

    private PandaServer pandaServer;

    public MemberDataListener(PandaServer pandaServer){
        this.pandaServer = pandaServer;
    }

    public JSONObject parseData(PandaClientChannel channel, String data){
        JSONObject jsonObject = new JSONObject(data);
        String questid = jsonObject.has("q") ? jsonObject.getString("q") : null;
        String key = jsonObject.getString("k");

        if(channel == PandaClientChannel.GET_MEMBER_DATA) {
            return createTotalObject(questid, pandaServer.getDataStorage().getTextMemberData(key));
        }else if(channel == PandaClientChannel.GET_SERIALIZABLE_MEMBER_DATA){
            return createTotalObject(questid, pandaServer.getDataStorage().getStoredSerializableMemberData(key));
        }

        return null;
    }

    private JSONObject createTotalObject(String questid, Pair<Status, HashMap<String, String>> data){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            jsonObject.put("s", data.getFirst().ordinal());
            if(data.getSecond() != null){
                jsonObject.put("i", data.getSecond());
            }

            return jsonObject;
        }
        return null;
    }

}
