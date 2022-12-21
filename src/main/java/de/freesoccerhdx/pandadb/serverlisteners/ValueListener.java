package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.clientutils.PandaClientChannel;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class ValueListener {

    private final PandaServer pandaServer;

    public ValueListener(PandaServer pandaServer){
        this.pandaServer = pandaServer;
    }

    private JSONObject createTotalObject(String questid, Pair info){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            jsonObject.put("s", ((Status)info.getFirst()).ordinal());
            Object value = info.getSecond();
            if (value != null) {
                if(value instanceof MemberValueDataStorage) {
                    jsonObject.put("i", ((HashMap<String,Double>) value));
                }else {
                    jsonObject.put("i", value);
                }
            }
            return jsonObject;
        }
        return null;
    }

    private JSONObject createTotalInfoObject(String questid, Pair<Status, MemberValueDataStorage.ValueMembersInfo> info){
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            jsonObject.put("s", info.getFirst().ordinal());
            MemberValueDataStorage.ValueMembersInfo vmi = info.getSecond();
            if(vmi != null){
                JSONObject vmiJSON = new JSONObject();
                vmiJSON.put("avr", vmi.averageValue());
                vmiJSON.put("low", vmi.lowestValue());
                vmiJSON.put("high", vmi.highestValue());
                vmiJSON.put("size", vmi.size());
                if(vmi.members() != null) {
                    vmiJSON.put("mem", vmi.members());
                }
                jsonObject.put("i", vmiJSON);
            }

            return jsonObject;
        }
        return null;
    }

    public JSONObject parseData(PandaClientChannel channel, JSONObject jsonObject) {
        String questid = jsonObject.has("q") ? jsonObject.getString("q") : null;
        String key = jsonObject.has("k") ? jsonObject.getString("k") : null;
        String member = jsonObject.has("m") ? jsonObject.getString("m") : null;

        ValueDataStorage vds = pandaServer.getDataStorage().getValueData();
        if(channel == PandaClientChannel.VALUE_SET) {
            double value = jsonObject.getDouble("v");
            Pair<Status, Double> info = vds.setValue(key, member, value);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.VALUE_ADD) {
            double value = jsonObject.getDouble("v");
            Pair<Status, Double> info = vds.addValue(key, member, value);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.VALUE_GET_KEYS) {
            Pair<Status, List<String>> info = vds.getKeys();
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.VALUE_GET_MEMBER_KEYS) {
            Pair<Status, List<String>> info = vds.getMemberKeys(key);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.VALUE_GET_MEMBER_DATA) {
            Pair<Status, Double> info = vds.getMemberData(key, member);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.VALUE_GET_KEY_DATA) {
            //System.err.println("ValueListener: VALUE_GET_KEY_DATA");
            Pair<Status, HashMap<String, Double>> info = vds.getKeyData(key);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.VALUE_REMOVE_KEY) {
            Status info = vds.removeKey(key);
            return createTotalObject(questid, Pair.of(info, null));
        }else if(channel == PandaClientChannel.VALUE_REMOVE_MEMBER) {
            Pair<Status, Double> info = vds.removeMember(key, member);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.VALUE_GET_VALUE_INFO) {
            Pair<Status, MemberValueDataStorage.ValueMembersInfo> info = vds.getValuesInfo(key, jsonObject.has("km"));
            return createTotalInfoObject(questid, info);
        }else {
            System.out.println("[PandaServer] Unknown Channel for ValueListener: " + channel + " data="+jsonObject);
        }

        return null;
    }
}
