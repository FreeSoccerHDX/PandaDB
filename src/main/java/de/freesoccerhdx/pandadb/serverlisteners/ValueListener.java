package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.PandaClientChannel;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.pandadb.ValueDataStorage;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONObject;

public class ValueListener {

    private PandaServer pandaServer;

    public ValueListener(PandaServer pandaServer){
        this.pandaServer = pandaServer;
    }

    private JSONObject createTotalObject(String questid, Pair<Status, Double> info){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            jsonObject.put("s", info.getFirst().ordinal());
            Double value = info.getSecond();
            if (value != null) {
                jsonObject.put("i", value);
            }
            return jsonObject;
        }
        return null;
    }

    private JSONObject createTotalInfoObject(String questid, Pair<Status, ValueDataStorage.ValueMembersInfo> info){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            jsonObject.put("s", info.getFirst().ordinal());
            ValueDataStorage.ValueMembersInfo vmi = info.getSecond();
            if(vmi != null){
                JSONObject vmiJSON = new JSONObject();
                vmiJSON.put("avr", vmi.getAverageValue());
                vmiJSON.put("low", vmi.getLowestValue());
                vmiJSON.put("high", vmi.getHighestValue());
                vmiJSON.put("size", vmi.getSize());
                if(vmi.getMembers() != null) {
                    vmiJSON.put("mem", vmi.getMembers());
                }
                jsonObject.put("i", vmiJSON);
            }

            return jsonObject;
        }
        return null;
    }

    public JSONObject parseData(PandaClientChannel channel, String data) {

        JSONObject jsonObject = new JSONObject(data);
        String questid = jsonObject.has("q") ? jsonObject.getString("q") : null;
        String key = jsonObject.getString("k");
        String member = jsonObject.has("m") ? jsonObject.getString("m") : null;

        if(channel == PandaClientChannel.GETVALUE) {
            Pair<Status, Double> value = pandaServer.getDataStorage().getValue(key, member);
            return createTotalObject(questid, value);
        }else if(channel == PandaClientChannel.SETVALUE) {
            Double value = jsonObject.getDouble("value");
            Pair<Status, Double> erfolg = pandaServer.getDataStorage().setValue(key, member, value);
            return createTotalObject(questid, erfolg);
        }else if(channel == PandaClientChannel.ADDVALUE){
            Double value = jsonObject.getDouble("value");
            Pair<Status, Double> erfolg = pandaServer.getDataStorage().addValue(key, member, value);
            return createTotalObject(questid, erfolg);
        }else if(channel == PandaClientChannel.INFOVALUES){
            Pair<Status, ValueDataStorage.ValueMembersInfo> pair = pandaServer.getDataStorage().getValuesInfo(key, jsonObject.has("km"));
           return createTotalInfoObject(questid,pair);
        }

        return null;
    }
}
