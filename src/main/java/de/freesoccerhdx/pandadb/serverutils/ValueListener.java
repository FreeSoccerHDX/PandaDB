package de.freesoccerhdx.pandadb.serverutils;

import de.freesoccerhdx.pandadb.Triple;
import de.freesoccerhdx.pandadb.clientutils.PandaClientChannel;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.pandadb.clientutils.changelistener.ChangeReason;
import de.freesoccerhdx.pandadb.serverutils.datastorage.MemberValueDataStorage;
import de.freesoccerhdx.pandadb.serverutils.datastorage.ValueDataStorage;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ValueListener extends DataChannelListener {

    private final PandaServer pandaServer;

    public ValueListener(PandaServer pandaServer){
        this.pandaServer = pandaServer;
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
            Double oldvalue = info.getSecond();
            if(oldvalue == null){
                onChange(ChangeReason.SET, key, member, null, value);
            }else{
                onChange(ChangeReason.OVERWRITE, key, member, oldvalue, value);
            }
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.VALUE_ADD) {
            double value = jsonObject.getDouble("v");
            Pair<Status, Double> info = vds.addValue(key, member, value);
            Double newvalue = info.getSecond();
            if(info.getFirst() == Status.SUCCESSFUL_CREATED_NEW) {
                onChange(ChangeReason.SET, key, member, null, newvalue);
            }else {
                onChange(ChangeReason.ADD, key, member, newvalue - value, newvalue);
            }
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
            if(info == Status.SUCCESSFUL_REMOVED_KEY) {
                onChange(ChangeReason.REMOVE_KEY, key, null, null, null);
            }
            return createTotalObject(questid, Pair.of(info, null));
        }else if(channel == PandaClientChannel.VALUE_REMOVE_MEMBER) {
            Pair<Status, Double> info = vds.removeMember(key, member);
            if(info.getFirst() == Status.SUCCESSFUL_REMOVED_MEMBER) {
                onChange(ChangeReason.REMOVE_KEY, key, member, info.getSecond(), null);
            }
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.VALUE_GET_VALUE_INFO) {
            Pair<Status, MemberValueDataStorage.ValueMembersInfo> info = vds.getValuesInfo(key, jsonObject.has("km"));
            return createTotalInfoObject(questid, info);
        }else if(channel == PandaClientChannel.VALUE_GET_LOWEST_TOP) {
            Pair<Status,Pair<String,Double>[]> info = vds.getLowestTop(key, jsonObject.getInt("mm"));
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.VALUE_GET_HIGHEST_TOP) {
            Pair<Status,Pair<String,Double>[]> info = vds.getHighestTop(key, jsonObject.getInt("mm"));
            return createTotalObject(questid, info);
        }else {
            System.out.println("[PandaServer] Unknown Channel for ValueListener: " + channel + " data="+jsonObject);
        }

        return null;
    }


    // UUID -> CleintName,Key,Member
    private HashMap<UUID, Triple<String,String,String>> changeListener = new HashMap<>();

    public void addChangeListener(UUID uuid, String clientName, String key, String member) {
        if(!changeListener.containsKey(uuid)) {
            changeListener.put(uuid, Triple.of(clientName, key, member));
        }
    }

    public void removeChangeListener(UUID uuid) {
        if(changeListener.containsKey(uuid)) {
            changeListener.remove(uuid);
        }
    }

    private void onChange(ChangeReason changeReason, String key, String member, Double oldvalue, Double newvalue) {
        for(UUID uuid : changeListener.keySet()) {
            Triple<String, String, String> triple = changeListener.get(uuid);
            String tripleKey = triple.getSecond();
            String tripleMember = triple.getThird();
            if(tripleKey.equals(key) && (member == null || tripleMember.equals(member))) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uuid", uuid.toString());
                jsonObject.put("cr", changeReason.ordinal());
                if(oldvalue != null) jsonObject.put("old", oldvalue);
                if(newvalue != null) jsonObject.put("new", newvalue);
                pandaServer.getSimpleSocketServer().sendNewMessage(triple.getFirst(),"changeresponse",jsonObject.toString());
            }
        }
    }

}
