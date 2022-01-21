package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.interweb.ClientCommands;
import de.freesoccerhdx.pandadb.interweb.DatabaseWriter;
import de.freesoccerhdx.pandadb.interweb.PandaDataSerializer;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PipelineSupplier implements ClientCommands {
    
    
    private PandaClient pandaClient;
    private HashMap<String, DataResult.Result> waitingFutureListener = new HashMap<>();
    private List<Pair<PandaClientChannel, JSONObject>> waitingCalls = new ArrayList<>();
    private String currentUUID;
    private HashMap<DataResult.Result,Object> extraStuff = new HashMap<>();

    protected PipelineSupplier(PandaClient pandaClient){
        this.pandaClient = pandaClient;
        currentUUID = UUID.randomUUID().toString();
    }

    /**
     * Sends the queued calls to the Server and waits for the answer
     *
     * */
    public void sync(){
        if(size() > 0) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("size", waitingCalls.size());
            jsonObject.put("uuid", currentUUID);
            int i = 0;
            for (Pair<PandaClientChannel, JSONObject> pair : waitingCalls) {
                PandaClientChannel channel = pair.getFirst();
                JSONObject data = pair.getSecond();

                jsonObject.put("c" + i, channel.ordinal());
                jsonObject.put("d" + i, data);

                i++;
            }
            for(String id : waitingFutureListener.keySet()){
                pandaClient.futureListener.put(id,waitingFutureListener.get(id));
            }
            for(DataResult.Result result : extraStuff.keySet()) {
                pandaClient.extraListenerInfo.put(result, extraStuff.get(result));
            }

            extraStuff.clear();
            waitingFutureListener.clear();
            waitingCalls.clear();
            currentUUID = UUID.randomUUID().toString();

            pandaClient.getSimpleSocketClient().sendMessage("dbpipeline", "Server", jsonObject);
        }else{
            throw new IllegalStateException("There are no waiting Calls for the Server left.");
        }
    }

    /**
     * @return The size of the currently waiting calls
     * */
    public int size(){
        return waitingCalls.size();
    }

    private JSONObject prepareValuePacket(String key, String member, DataResult.Result future) {
        String uuid = this.currentUUID + "#" + waitingCalls.size();
        if(future != null) {
            waitingFutureListener.put(uuid.toString(), (DataResult.Result) future);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("q", "#" + waitingCalls.size());
        if(key != null) {
            jsonObject.put("k", key);
        }
        if(member != null) {
            jsonObject.put("m", member);
        }

        return jsonObject;
    }

    @Override
    public void getMemberData(String key, DataResult.MemberDataResult memberData) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key,null, memberData);
            this.waitingCalls.add(Pair.of(PandaClientChannel.GET_MEMBER_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("Key can't be null!");
        }
    }

    @Override
    public <T> void getSerializableMemberData(String key, SerializerFactory<T> factory, DataResult.ListStoredSerializableResult<T> memberData) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key,null, memberData);
            extraStuff.put(memberData, factory);
            this.waitingCalls.add(Pair.of(PandaClientChannel.GET_SERIALIZABLE_MEMBER_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("Key can't be null!");
        }
    }

    @Override
    public void getSerializableKeys(DataResult.KeysResult keysResult) {
        JSONObject jsonObject = prepareValuePacket(null,null, keysResult);
        jsonObject.put("gettype", 11);
        this.waitingCalls.add(Pair.of(PandaClientChannel.LISTKEYS, jsonObject));
    }

    @Override
    public void getSerializableMemberKeys(String key, DataResult.KeysResult keysResult) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key,null, keysResult);
            jsonObject.put("gettype", 10);
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTKEYS, jsonObject));
        }else{
            throw new IllegalArgumentException("Key can't be null!");
        }
    }

    @Override
    public <T> void storeSerializable(String key, String member, PandaDataSerializer<T> serializer, DataResult.StatusResult statusResult) {
        if(notnull(key,member,serializer)) {
            JSONObject jsonObject = prepareValuePacket(key, member, statusResult);
            DatabaseWriter writer = new DatabaseWriter();
            serializer.serialize(writer);
            jsonObject.put("pds", writer.toJSON());
            this.waitingCalls.add(Pair.of(PandaClientChannel.STORE_SERIALIZABLE, jsonObject));
        }else{
            throw new IllegalArgumentException("Key, Member and DataSerializer can't be null!");
        }
    }

    @Override
    public <T> void getStoredSerializable(String key, String member, SerializerFactory<T> factory, DataResult.StoredSerializableResult<T> serializableResult) {
        if(notnull(key,member,factory)) {
            JSONObject jsonObject = prepareValuePacket(key, member, serializableResult);
            this.extraStuff.put(serializableResult,factory);
            this.waitingCalls.add(Pair.of(PandaClientChannel.GET_STORED_SERIALIZABLE, jsonObject));
        }else{
            throw new IllegalArgumentException("Key, Member and SerializerFactor can't be null!");
        }
    }

    @Override
    public <T> void addListEntry(String key, String listkey, ListType listType, T value, DataResult.StatusResult addListResult){
        if(notnull(key,listType,value)) {
            if (validateListType(listType, value)) {
                JSONObject jsonObject = prepareValuePacket(key, null, addListResult);
                jsonObject.put("type", listType.ordinal());
                jsonObject.put("value", value);
                jsonObject.put("listkey", listkey);
                //this.simpleSocketClient.sendMessage("addlist", "Server", jsonObject.toString());
                this.waitingCalls.add(Pair.of(PandaClientChannel.ADDLIST, jsonObject));
            } else {
                throw new IllegalArgumentException("ListType(" + listType + ") did not allow the value(" + value + ")");
            }
        }else{
            throw new IllegalArgumentException("Key, ListType and Value can't be null!");
        }
    }

    @Override
    public <T> void getList(String key, String listkey, ListType listType, DataResult.ListResult<T> listResult){
        if(notnull(key, listType)) {
            JSONObject jsonObject = prepareValuePacket(key, null, listResult);
            jsonObject.put("type", listType.ordinal());
            jsonObject.put("listkey", listkey);
            //this.simpleSocketClient.sendMessage("getlist", "Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.GETLIST, jsonObject));
        }else{
            throw new IllegalArgumentException("Key and ListType can't be null!");
        }
    }

    @Override
    public void get(String key, String member, DataResult.TextResult textResult){
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, textResult);
            //this.simpleSocketClient.sendMessage("get", "Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.GET, jsonObject));
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
    }

    @Override
    public void set(String key, String member, String value, DataResult.StatusResult setResult){
        if(notnull(key,member,value)) {
            JSONObject jsonObject = prepareValuePacket(key, member, setResult);
            jsonObject.put("value", value);
            //this.simpleSocketClient.sendMessage("set","Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.SET, jsonObject));
        }else{
            throw new IllegalArgumentException("Key, Member and Value can't be null!");
        }
    }

    @Override
    public void getValue(String key, String member, DataResult.ValueResult future) {
        if (notnull(key, member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, future);
            //this.simpleSocketClient.sendMessage("getvalue","Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.GETVALUE, jsonObject));
        } else {
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
    }

    @Override
    public void setValue(String key, String member, double value, DataResult.ValueResult valueResult){
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, valueResult);
            jsonObject.put("value", value);
            //this.simpleSocketClient.sendMessage("setvalue", "Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.SETVALUE, jsonObject));
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
    }

    @Override
    public void addValue(String key, String member, double value, DataResult.ValueResult valueResult){
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, valueResult);
            jsonObject.put("value", value);
            //this.simpleSocketClient.sendMessage("addvalue", "Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.ADDVALUE, jsonObject));
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
    }

    @Override
    public void removeValue(String key, String member, DataResult.StatusResult removeResult){
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, removeResult);
            jsonObject.put("removetype", 0);
            //this.simpleSocketClient.sendMessage("remove", "Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.REMOVE, jsonObject));
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
    }

    @Override
    public void remove(String key, String member, DataResult.StatusResult removeResult){
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, removeResult);
            jsonObject.put("removetype", 1);
            //this.simpleSocketClient.sendMessage("remove", "Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.REMOVE, jsonObject));
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
    }

    @Override
    public <T> void removeList(String key, String listkey, ListType listType, DataResult.StatusResult removeResult){
        if(notnull(key,listkey,listType)) {
            JSONObject jsonObject = prepareValuePacket(key, null, removeResult);
            jsonObject.put("removetype", 2);
            jsonObject.put("type", listType.ordinal());
            jsonObject.put("listkey", listkey);
            //this.simpleSocketClient.sendMessage("remove", "Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.REMOVE, jsonObject));
        }else{
            throw new IllegalArgumentException("Key, Listkey and ListType can't be null!");
        }
    }

    @Override
    public <T> void removeList(String key, ListType listType, DataResult.StatusResult removeResult){
        if(notnull(key,listType)) {
            JSONObject jsonObject = prepareValuePacket(key, null, removeResult);
            jsonObject.put("removetype", 4);
            jsonObject.put("type", listType.ordinal());
            //this.simpleSocketClient.sendMessage("remove", "Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.REMOVE, jsonObject));
        }else{
            throw new IllegalArgumentException("Key and ListType can't be null!");
        }
    }

    @Override
    public <T> void removeListIndex(String key, String listkey, ListType listType, int index, DataResult.StatusResult removeResult){
        if(notnull(key,listkey,listType)) {
            JSONObject jsonObject = prepareValuePacket(key, null, removeResult);
            jsonObject.put("removetype", 3);
            jsonObject.put("type", listType.ordinal());
            jsonObject.put("listkey", listkey);
            jsonObject.put("index", index);
            //this.simpleSocketClient.sendMessage("remove", "Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.REMOVE, jsonObject));
        }else{
            throw new IllegalArgumentException("Key, ListKey and ListType can't be null!");
        }
    }

    @Override
    public <T> void getListKeys(ListType listType, DataResult.KeysResult keysResult){
        if(notnull(listType)) {
            JSONObject jsonObject = prepareValuePacket(null,null, keysResult);
            jsonObject.put("gettype", 0);
            jsonObject.put("type", listType.ordinal());

            //this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTKEYS, jsonObject));

        }else{
            throw new IllegalArgumentException("ListType can't be null!");
        }
    }

    @Override
    public <T> void getListKeys(String key, ListType listType, DataResult.KeysResult keysResult){
        if(notnull(key, listType)) {
            JSONObject jsonObject = prepareValuePacket(key,null, keysResult);
            jsonObject.put("gettype", 1);
            jsonObject.put("type", listType.ordinal());
            //this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTKEYS, jsonObject));
        }else{
            throw new IllegalArgumentException("Key and ListType can't be null!");
        }
    }

    @Override
    public void getKeys(String key, DataResult.KeysResult keysResult){
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key,null, keysResult);
            jsonObject.put("gettype", 2);
            //this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTKEYS, jsonObject));
        }else{
            throw new IllegalArgumentException("Key can't be null!");
        }
    }

    @Override
    public void getKeys(DataResult.KeysResult keysResult){

        JSONObject jsonObject = prepareValuePacket(null,null, keysResult);
        jsonObject.put("gettype", 3);

       //this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
        this.waitingCalls.add(Pair.of(PandaClientChannel.LISTKEYS, jsonObject));
    }

    @Override
    public void getValueKeys(DataResult.KeysResult keysResult){

        JSONObject jsonObject = prepareValuePacket(null,null, keysResult);
        jsonObject.put("gettype", 4);
        //this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
        this.waitingCalls.add(Pair.of(PandaClientChannel.LISTKEYS, jsonObject));
    }

    @Override
    public void getValueKeys(String key, DataResult.KeysResult keysResult){
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key,null, keysResult);
            jsonObject.put("gettype", 5);
            //this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTKEYS, jsonObject));
        }else{
            throw new IllegalArgumentException("Key can't be null!");
        }
    }

    @Override
    public void getValuesMemberInfo(String key, boolean withKeys, DataResult.ValuesInfoResult valuesInfoResult){
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key,null, valuesInfoResult);
            if(withKeys){
                jsonObject.put("km", true);
            }
            //this.simpleSocketClient.sendMessage("infovalues", "Server", jsonObject.toString());
            this.waitingCalls.add(Pair.of(PandaClientChannel.INFOVALUES, jsonObject));
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
    
    
    
}
