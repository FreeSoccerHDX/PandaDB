package de.freesoccerhdx.pandadb.serverutils;

import de.freesoccerhdx.pandadb.Triple;
import de.freesoccerhdx.pandadb.clientutils.PandaClientChannel;
import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.pandadb.clientutils.changelistener.ChangeReason;
import de.freesoccerhdx.pandadb.serverutils.datastorage.TextsDataStorage;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TextListener extends DataChannelListener {

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
            Pair<Status, String> status = tds.set(key, member, value);
            String oldvalue = status.getSecond();

            if(oldvalue == null){
                onChange(ChangeReason.SET, key, member, null, value);
            }else{
                onChange(ChangeReason.OVERWRITE, key, member, oldvalue, value);
            }

            return createTotalObject(questid, status);
        }else if(channel == PandaClientChannel.TEXT_GET_KEYS) {
            Pair<Status, List<String>> info = tds.getKeys();
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.TEXT_GET_MEMBER_KEYS) {
            Pair<Status, List<String>> info = tds.getMemberKeys(key);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.TEXT_GET_MEMBER_DATA) {
            Pair<Status, String> info = tds.getMemberData(key, member);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.TEXT_GET_KEY_DATA) {
            Pair<Status, HashMap<String, String>> info = tds.getKeyData(key);
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.TEXT_REMOVE_KEY) {
            Status info = tds.removeKey(key);
            if(info == Status.SUCCESSFUL_REMOVED_KEY) {
                onChange(ChangeReason.REMOVE_KEY, key, null, null, null);
            }
            return createTotalObject(questid, info);
        }else if(channel == PandaClientChannel.TEXT_REMOVE_MEMBER) {
            Pair<Status, String> info = tds.removeMember(key, member);

            if(info.getFirst() == Status.SUCCESSFUL_REMOVED_MEMBER) {
                onChange(ChangeReason.REMOVE_MEMBER, key, member, info.getSecond(), null);
            }


            return createTotalObject(questid, info);
        }else {
            System.out.println("[PandaServer] Unknown Channel for TextListener: " + channel + " data="+jsonObject);
        }

        return null;
    }

    // UUID -> ClientName,Key,Member
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

    private void onChange(ChangeReason changeReason, String key, String member, String oldvalue, String newvalue) {
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
