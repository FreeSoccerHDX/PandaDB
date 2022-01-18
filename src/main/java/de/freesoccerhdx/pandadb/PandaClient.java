    package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.clientlisteners.ListKeysListener;
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
        new ListKeysListener(this);

    }
    public boolean isReady(){
        return this.simpleSocketClient.isRunning() && this.simpleSocketClient.isLogin_succesfull();
    }
    public void stop(){
        this.simpleSocketClient.stop();
    }

    public void makeDataTree(){
        this.simpleSocketClient.sendMessage("datatree","Server", "");
    }

    public void handleResult(String id, Status status, Object info){
        try {
            DataResult.Result listener = futureListener.get(id);
            if (listener != null) {
                if (listener instanceof DataResult.ValueResult) {
                    DataResult.ValueResult<Double> valueListener = (DataResult.ValueResult<Double>) listener;
                    if(info == null){
                        valueListener.result(null, status);
                    }else {
                        valueListener.result((Double) info, status);
                    }

                }else if (listener instanceof DataResult.StatusResult) {
                    DataResult.StatusResult setResult = (DataResult.StatusResult) listener;
                    setResult.result(status);

                }else if (listener instanceof DataResult.TextResult) {
                    DataResult.TextResult<String> textResult = (DataResult.TextResult<String>) listener;
                    if(info == null){
                        textResult.result(null, status);
                    }else {
                        textResult.result((String) info, status);
                    }
                }else if (listener instanceof DataResult.ListResult) {
                    DataResult.ListResult listResult = (DataResult.ListResult) listener;
                    if(info != null){
                        if(info instanceof JSONArray){
                            JSONArray jsonArray = (JSONArray) info;
                            ArrayList<Object> arrayList = (ArrayList<Object>) jsonArray.toList();
                            listResult.resultList(arrayList, status);
                        }
                    }else{
                        listResult.resultList(null, status);
                    }
                }else if (listener instanceof DataResult.KeysResult) {
                    DataResult.KeysResult listResult = (DataResult.KeysResult) listener;
                    if(info != null){
                        if(info instanceof JSONArray){
                            JSONArray jsonArray = (JSONArray) info;
                            ArrayList<Object> arrayList = (ArrayList<Object>) jsonArray.toList();
                            listResult.resultList(arrayList, status);
                        }
                    }else{
                        listResult.resultList(null, status);
                    }

                }
            }
        }finally {
            futureListener.remove(id);
        }
    }

    private JSONObject prepareValuePacket(String key, String member, DataResult.Result future) {
        UUID uuid = UUID.randomUUID();
        if(future != null) {
            futureListener.put(uuid.toString(), (DataResult.Result) future);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("questid", uuid.toString());
        if(key != null) {
            jsonObject.put("key", key);
        }
        if(member != null) {
            jsonObject.put("member", member);
        }

        return jsonObject;
    }

    /**
     * Adds the value to the list(or new created list), if the type is the same or the list new
     * */
    public <T> void addListEntry(String key, String listkey, ListType listType, T value, DataResult.StatusResult addListResult){
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
    public void set(String key, String member, String value, DataResult.StatusResult setResult){
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
    public void removeValue(String key, String member, DataResult.StatusResult removeResult){
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
    public void remove(String key, String member, DataResult.StatusResult removeResult){
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
    public void removeList(String key, String listkey, ListType listType, DataResult.StatusResult removeResult){
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
    public void removeList(String key, ListType listType, DataResult.StatusResult removeResult){
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
    public void removeListIndex(String key, String listkey, ListType listType, int index, DataResult.StatusResult removeResult){
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

    /**
     * Gets all Keys stored under the specific ListType
     * */
    public void getListKeys(ListType listType, DataResult.KeysResult keysResult){
        if(notnull(listType)) {
            JSONObject jsonObject = prepareValuePacket(null,null, keysResult);
            jsonObject.put("gettype", 0);
            jsonObject.put("type", listType.ordinal());

            this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("ListType can't be null!");
        }
    }

    /**
     * Gets all ListKeys stored under the specific key
     * */
    public void getListKeys(String key, ListType listType, DataResult.KeysResult keysResult){
        if(notnull(key, listType)) {
            JSONObject jsonObject = prepareValuePacket(key,null, keysResult);
            jsonObject.put("gettype", 1);
            jsonObject.put("type", listType.ordinal());

            this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and ListType can't be null!");
        }
    }

    /**
     * Gets all Text-Keys stored under the key
     * */
    public void getKeys(String key, DataResult.KeysResult keysResult){
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key,null, keysResult);
            jsonObject.put("gettype", 2);

            this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key can't be null!");
        }
    }

    /**
     * Gets all Text-Keys stored
     * */
    public void getKeys(DataResult.KeysResult keysResult){

        JSONObject jsonObject = prepareValuePacket(null,null, keysResult);
        jsonObject.put("gettype", 3);

        this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());

    }

    /**
     * Gets all Value-Keys stored
     * */
    public void getValueKeys(DataResult.KeysResult keysResult){

        JSONObject jsonObject = prepareValuePacket(null,null, keysResult);
        jsonObject.put("gettype", 4);

        this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());

    }

    /**
     * Gets all Value-Keys stored under the key
     * */
    public void getValueKeys(String key, DataResult.KeysResult keysResult){
            if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key,null, keysResult);
            jsonObject.put("gettype", 5);

            this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key can't be null!");
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
