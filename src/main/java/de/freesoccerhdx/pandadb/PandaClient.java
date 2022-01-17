    package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.clientlisteners.ListListener;
import de.freesoccerhdx.pandadb.clientlisteners.RemoveListener;
import de.freesoccerhdx.pandadb.clientlisteners.TextListener;
import de.freesoccerhdx.pandadb.clientlisteners.ValueListener;
import de.freesoccerhdx.simplesocket.client.ClientListener;
import de.freesoccerhdx.simplesocket.client.SimpleSocketClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PandaClient {

    private HashMap<String, DataResult.Result> futureListener = new HashMap<>();
    private SimpleSocketClient simpleSocketClient;

    public PandaClient(String name, String ip, int port){
        this.simpleSocketClient = new SimpleSocketClient(name, ip, port);

        new ValueListener(this);
        new TextListener(this);
        new ListListener(this);
        new RemoveListener(this);

    }
    public boolean isReady(){
        return this.simpleSocketClient.isRunning() && this.simpleSocketClient.isLogin_succesfull();
    }
    public void stop(){
        this.simpleSocketClient.stop();
    }

    public void handleResult(String id, Object info){
        try {
            DataResult.Result listener = futureListener.get(id);
            if (listener != null) {
                if (listener instanceof DataResult.ValueResult) {
                    DataResult.ValueResult<Double> valueListener = (DataResult.ValueResult<Double>) listener;
                    valueListener.result((Double) info, info != null);

                }else if (listener instanceof DataResult.SetResult) {
                    DataResult.SetResult<Boolean> setResult = (DataResult.SetResult<Boolean>) listener;
                    setResult.result((Boolean) info);

                }else if (listener instanceof DataResult.TextResult) {
                    DataResult.TextResult<String> textResult = (DataResult.TextResult<String>) listener;
                    textResult.result((String) info, info != null);

                }else if (listener instanceof DataResult.AddListResult) {
                    DataResult.AddListResult<Boolean> addListResult = (DataResult.AddListResult<Boolean>) listener;
                    addListResult.result(info != null);

                }else if (listener instanceof DataResult.RemoveResult) {
                    DataResult.RemoveResult<Boolean> removeResult = (DataResult.RemoveResult<Boolean>) listener;
                    removeResult.result((Boolean)info);

                }else if (listener instanceof DataResult.ListResult) {
                    DataResult.ListResult listResult = (DataResult.ListResult) listener;
                    if(info != null){
                        if(info instanceof JSONArray){
                            JSONArray jsonArray = (JSONArray) info;
                            ArrayList<Object> arrayList = (ArrayList<Object>) jsonArray.toList();
                            listResult.result(arrayList, true);
                        }
                    }else{
                        listResult.result(null, false);
                    }


                }
            }
        }finally {
            futureListener.remove(id);
        }
    }

    private JSONObject prepareValuePacket(String key, String member, DataResult.Result<?> future) {
        UUID uuid = UUID.randomUUID();
        if(future != null) {
            futureListener.put(uuid.toString(), (DataResult.Result) future);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("questid", uuid.toString());
        jsonObject.put("key", key);
        if(member != null) {
            jsonObject.put("member", member);
        }

        return jsonObject;
    }

    /**
     * Adds the value to the list(or new created list), if the type is the same or the list new
     * */
    public <T> void addListEntry(String key, String listkey, ListType listType, T value, DataResult.AddListResult addListResult){
        if(notnull(key,listType,value)) {
            if (validateListType(listType, value)) {
                JSONObject jsonObject = prepareValuePacket(key, null, addListResult);
                jsonObject.put("type", listType.ordinal());
                jsonObject.put("value", value);
                jsonObject.put("listkey", listkey);
                this.simpleSocketClient.sendMessage("addlist", "Server", jsonObject.toString());
            } else {
                throw new IllegalArgumentException("ListType(" + listType + ") did not allow the value(" + value + ")");
            }
        }else{
            throw new IllegalArgumentException("Key, ListType and Value can't be null!");
        }
    }


    /**
     * Gets the specific list of the listtype is the same and the list exist or else null
     * */
    public <T> void getList(String key, String listkey, ListType listType, DataResult.ListResult<T> listResult){
        if(notnull(key, listType)) {
            JSONObject jsonObject = prepareValuePacket(key, null, listResult);
            jsonObject.put("type", listType.ordinal());
            jsonObject.put("listkey", listkey);
            this.simpleSocketClient.sendMessage("getlist", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and ListType can't be null!");
        }
    }

    /**
     * Gets a specific text or null if not exist
     * */
    public void get(String key, String member, DataResult.TextResult textResult){
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, textResult);
            this.simpleSocketClient.sendMessage("get", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
    }

    /**
     * Sets a specific text
     * */
    public void set(String key, String member, String value, DataResult.SetResult setResult){
        if(notnull(key,member,value)) {
            JSONObject jsonObject = prepareValuePacket(key, member, setResult);
            jsonObject.put("value", value);
            this.simpleSocketClient.sendMessage("set","Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key, Member and Value can't be null!");
        }
    }

    /**
     * Gets the specific value or null if not set
     * */
    public void getValue(String key, String member, DataResult.ValueResult<Double> future){
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, future);
            this.simpleSocketClient.sendMessage("getvalue","Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
    }

    /**
     * Sets the specific value
     * */
    public void setValue(String key, String member, double value, DataResult.ValueResult<Double> valueResult){
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, valueResult);
            jsonObject.put("value", value);
            this.simpleSocketClient.sendMessage("setvalue", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
    }

    /**
     * Add/Subtract the specific value
     * */
    public void addValue(String key, String member, double value, DataResult.ValueResult<Double> valueResult){
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, valueResult);
            jsonObject.put("value", value);
            this.simpleSocketClient.sendMessage("addvalue", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
    }

    /**
     * Remove a specific value
     * */
    public void removeValue(String key, String member, DataResult.RemoveResult removeResult){
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, removeResult);
            jsonObject.put("removetype", 0);
            this.simpleSocketClient.sendMessage("remove", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
    }

    /**
     * Remove a specific set text
     * */
    public void remove(String key, String member, DataResult.RemoveResult removeResult){
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, removeResult);
            jsonObject.put("removetype", 1);
            this.simpleSocketClient.sendMessage("remove", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
    }

    /**
     * Remove a specific list within a key
     * */
    public void removeList(String key, String listkey, ListType listType, DataResult.RemoveResult removeResult){
        if(notnull(key,listkey,listType)) {
            JSONObject jsonObject = prepareValuePacket(key, null, removeResult);
            jsonObject.put("removetype", 2);
            jsonObject.put("type", listType.ordinal());
            jsonObject.put("listkey", listkey);
            this.simpleSocketClient.sendMessage("remove", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key, Listkey and ListType can't be null!");
        }
    }

    /**
     * Remove a specific key that stores lists
     * */
    public void removeList(String key, ListType listType, DataResult.RemoveResult removeResult){
        if(notnull(key,listType)) {
            JSONObject jsonObject = prepareValuePacket(key, null, removeResult);
            jsonObject.put("removetype", 4);
            jsonObject.put("type", listType.ordinal());
            this.simpleSocketClient.sendMessage("remove", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and ListType can't be null!");
        }
    }

    /**
     * Remove a specific index within a list of an key
     * */
    public void removeListIndex(String key, String listkey, ListType listType, int index, DataResult.RemoveResult removeResult){
        if(notnull(key,listkey,listType)) {
            JSONObject jsonObject = prepareValuePacket(key, null, removeResult);
            jsonObject.put("removetype", 3);
            jsonObject.put("type", listType.ordinal());
            jsonObject.put("listkey", listkey);
            jsonObject.put("index", index);

            this.simpleSocketClient.sendMessage("remove", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key, ListKey and ListType can't be null!");
        }
    }

    private boolean notnull(Object... objects){

        for(Object obj : objects){
            if(obj == null) {
                return false;
            }
        }

        return true;
    }

    private <T> boolean validateListType(ListType listType, T value) {
        if(listType != null && value != null){
            return listType.check(value);
        }
        return false;
    }

    public SimpleSocketClient getSimpleSocketClient() {
        return this.simpleSocketClient;
    }
}
