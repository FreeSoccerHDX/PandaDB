package de.freesoccerhdx.pandadb.clientutils.changelistener;

import de.freesoccerhdx.pandadb.PandaClient;
import de.freesoccerhdx.pandadb.clientutils.changelistener.ChangeListener;
import de.freesoccerhdx.pandadb.clientutils.changelistener.ChangeReason;
import de.freesoccerhdx.pandadb.clientutils.changelistener.TextChangeListener;
import de.freesoccerhdx.simplesocket.client.ClientListener;
import de.freesoccerhdx.simplesocket.client.SimpleSocketClient;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;

public class ChangeListenerHandler {

    private final PandaClient client;
    private final HashMap<UUID, ChangeListener> listeners = new HashMap<>();
    public ChangeListenerHandler(PandaClient pandaClient) {
        this.client = pandaClient;

        pandaClient.setSocketListener("changeresponse", new ClientListener() {
            @Override
            public void recive(SimpleSocketClient simpleSocketClient, String channel, String source, String message) {
                JSONObject jsonObject = new JSONObject(message);
                UUID uuid = UUID.fromString(jsonObject.getString("uuid"));

                ChangeListener listener = listeners.get(uuid);
                if(listener != null) {
                    if(listener instanceof TextChangeListener textChangeListener){
                        ChangeReason cr = ChangeReason.values()[jsonObject.getInt("cr")];
                        String oldvalue = jsonObject.has("old") ? jsonObject.getString("old") : null;
                        String newvalue = jsonObject.has("new") ? jsonObject.getString("new") : null;
                        textChangeListener.onChange(cr, oldvalue, newvalue);
                    }else if(listener instanceof ValueChangeListener valueChangeListener) {
                        ChangeReason cr = ChangeReason.values()[jsonObject.getInt("cr")];
                        Double oldvalue = jsonObject.has("old") ? jsonObject.getDouble("old") : null;
                        Double newvalue = jsonObject.has("new") ? jsonObject.getDouble("new") : null;
                        valueChangeListener.onChange(cr, oldvalue, newvalue);
                    }
                }else{
                    System.out.println("Listener not found!\nRemoving from Client and Server...");
                    send(new JSONObject(), uuid, "remove");
                }
            }
        });

    }

    public boolean removeListener(UUID uuid){
        if(listeners.containsKey(uuid)){
            listeners.remove(uuid);
            return send(new JSONObject(), uuid, "remove") != null;
        }
        return false;
    }

    public UUID addTextListener(String key, String member, TextChangeListener listener) {
        if(notnull(key,member,listener)){
            JSONObject json = new JSONObject();
            UUID uuid = UUID.randomUUID();
            listeners.put(uuid, listener);

            json.put("key", key);
            json.put("member", member);

            return send(json, uuid, "text");
        }else{
            throw new IllegalArgumentException("key, member or listener is null");
        }
    }

    public UUID addValueListener(String key, String member, ValueChangeListener listener) {
        if(notnull(key,member,listener)){
            JSONObject json = new JSONObject();
            UUID uuid = UUID.randomUUID();
            listeners.put(uuid, listener);

            json.put("key", key);
            json.put("member", member);

            return send(json, uuid, "value");
        }else{
            throw new IllegalArgumentException("key, member or listener is null");
        }
    }

    private UUID send(JSONObject jsonObject, UUID uuid, String type){
        jsonObject.put("uuid", uuid.toString());
        jsonObject.put("type", type);
        client.sendMessage("changelistener", "Server", jsonObject);
        return uuid;
    }


    private static boolean notnull(Object... objects){
        for(Object obj : objects){
            if(obj == null) {
                return false;
            }
        }
        return true;
    }
}
