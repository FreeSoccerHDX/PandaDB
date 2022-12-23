package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.clientutils.ClientCommands;
import de.freesoccerhdx.pandadb.clientutils.DatabaseWriter;
import de.freesoccerhdx.pandadb.clientutils.PandaClientChannel;
import de.freesoccerhdx.pandadb.clientutils.PandaDataSerializer;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PipelineSupplier implements ClientCommands {
    
    
    private final PandaClient pandaClient;
    private final HashMap<String, DataResult.Result> waitingFutureListener = new HashMap<>();
    private final List<Pair<PandaClientChannel, JSONObject>> waitingCalls = new ArrayList<>();
    private String currentUUID;
    private final HashMap<DataResult.Result,Object> extraStuff = new HashMap<>();

    protected PipelineSupplier(PandaClient pandaClient){
        this.pandaClient = pandaClient;
        currentUUID = UUID.randomUUID().toString();
    }

    /**
     * Sends the queued calls to the Server and waits for the answer
     *
     * @return If the message was sended or not
     * */
    public boolean sync() {
        if(size() > 0) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uuid", currentUUID);
            JSONArray array = new JSONArray();
            for (Pair<PandaClientChannel, JSONObject> pair : waitingCalls) {
                PandaClientChannel channel = pair.getFirst();
                JSONObject data = pair.getSecond();

                JSONObject obj = new JSONObject();
                obj.put("c", channel.ordinal());
                obj.put("d", data);

                array.put(obj);
            }

            jsonObject.put("data", array);
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

            return pandaClient.sendMessage("dbpipeline", "Server", jsonObject);
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
            waitingFutureListener.put(uuid, future);
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
    public void setText(String key, String member, String value, DataResult.TextResult textResult) {
        if(notnull(key, member, value)) {
            JSONObject jsonObject = prepareValuePacket(key, member, textResult);
            jsonObject.put("v", value);
            this.waitingCalls.add(Pair.of(PandaClientChannel.TEXT_SET, jsonObject));
        }else {
            throw new IllegalArgumentException("Key, member or value is null");
        }
    }

    @Override
    public void getTextKeys(DataResult.KeysResult keysResult) {
        JSONObject jsonObject = prepareValuePacket(null, null, keysResult);
        this.waitingCalls.add(Pair.of(PandaClientChannel.TEXT_GET_KEYS, jsonObject));
    }

    @Override
    public void getTextMemberKeys(String key, DataResult.KeysResult keysResult) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, keysResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.TEXT_GET_MEMBER_KEYS, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void getTextMemberData(String key, String member, DataResult.TextResult textResult) {
        if(notnull(key, member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, textResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.TEXT_GET_MEMBER_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or member is null");
        }
    }

    @Override
    public void getTextKeyData(String key, DataResult.MemberDataResult memberDataResult) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, memberDataResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.TEXT_GET_KEY_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void removeTextKey(String key, DataResult.StatusResult statusResult) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, statusResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.TEXT_REMOVE_KEY, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void removeTextMember(String key, String member, DataResult.TextResult textResult) {
        if(notnull(key, member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, textResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.TEXT_REMOVE_MEMBER, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or member is null");
        }
    }

    @Override
    public <T> void setSerializable(String key, String member, PandaDataSerializer<T> serializer, DataResult.StatusResult statusResult) {
        if(notnull(key, member, serializer)) {
            JSONObject jsonObject = prepareValuePacket(key, member, statusResult);
            DatabaseWriter writer = new DatabaseWriter();
            serializer.serialize(writer);
            jsonObject.put("v", writer.toJSON());
            this.waitingCalls.add(Pair.of(PandaClientChannel.SERIALIZABLE_SET, jsonObject));
        }else {
            throw new IllegalArgumentException("Key, member or serializer is null");
        }
    }
    @Override
    public void getSerializableKeys(DataResult.KeysResult keysResult) {
        JSONObject jsonObject = prepareValuePacket(null, null, keysResult);
        this.waitingCalls.add(Pair.of(PandaClientChannel.SERIALIZABLE_GET_KEYS, jsonObject));
    }

    @Override
    public void getSerializableMemberKeys(String key, DataResult.KeysResult keysResult) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, keysResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SERIALIZABLE_GET_MEMBER_KEYS, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public <T> void getSerializableMemberData(String key, String member, SerializerFactory<T> factory, DataResult.SpecificResult<T> specificResult) {
        if(notnull(key, member, factory)) {
            JSONObject jsonObject = prepareValuePacket(key, member, specificResult);
            extraStuff.put(specificResult, factory);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SERIALIZABLE_GET_MEMBER_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("Key, member or factory is null");
        }
    }


    @Override
    public <T> void getSerializableKeyData(String key, SerializerFactory<T> factory, DataResult.ListStoredSerializableResult listStoredSerializableResult) {
        if(notnull(key, factory)) {
            JSONObject jsonObject = prepareValuePacket(key, null, listStoredSerializableResult);
            extraStuff.put(listStoredSerializableResult, factory);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SERIALIZABLE_GET_KEY_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or factory is null");
        }
    }

    @Override
    public void removeSerializableKey(String key, DataResult.StatusResult statusResult) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, statusResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SERIALIZABLE_REMOVE_KEY, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void removeSerializableMember(String key, String member, DataResult.StatusResult statusResult) {
        if(notnull(key, member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, statusResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SERIALIZABLE_REMOVE_MEMBER, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or member is null");
        }
    }

    @Override
    public void setValue(String key, String member, double value, DataResult.ValueResult valueResult) {
        if(notnull(key, member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, valueResult);
            jsonObject.put("v", value);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_SET, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or member is null");
        }
    }

    @Override
    public void addValue(String key, String member, double value, DataResult.ValueResult valueResult) {
        if(notnull(key, member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, valueResult);
            jsonObject.put("v", value);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_ADD, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or member is null");
        }
    }

    @Override
    public void getValueKeys(DataResult.KeysResult keysResult) {
        JSONObject jsonObject = prepareValuePacket(null, null, keysResult);
        this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_GET_KEYS, jsonObject));
    }

    @Override
    public void getValueMemberKeys(String key, DataResult.KeysResult keysResult) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, keysResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_GET_MEMBER_KEYS, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void getValueMemberData(String key, String member, DataResult.ValueResult valueResult) {
        if(notnull(key, member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, valueResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_GET_MEMBER_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or member is null");
        }
    }

    @Override
    public void getValueKeyData(String key, DataResult.ValueMemberDataResult listStoredSerializableResult) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, listStoredSerializableResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_GET_KEY_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void removeValueKey(String key, DataResult.StatusResult statusResult) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, statusResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_REMOVE_KEY, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void removeValueMember(String key, String member, DataResult.ValueResult valueResult) {
        if(notnull(key, member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, valueResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_REMOVE_MEMBER, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or member is null");
        }
    }

    @Override
    public void getValueInfo(String key, boolean withKeys, DataResult.ValuesInfoResult valuesInfoResult) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, valuesInfoResult);
            if(withKeys) {
                jsonObject.put("km", true);
            }
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_GET_VALUE_INFO, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void getValueLowestTop(String key, int maxMembers, DataResult.SortedValueMemberDataResult sortedValueMemberDataResult) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, sortedValueMemberDataResult);
            jsonObject.put("mm", maxMembers);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_GET_LOWEST_TOP, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void getValueHighestTop(String key, int maxMembers, DataResult.SortedValueMemberDataResult sortedValueMemberDataResult) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, sortedValueMemberDataResult);
            jsonObject.put("mm", maxMembers);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_GET_HIGHEST_TOP, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }




    @Override
    public <T> void addList(ListType<T> listType, String key, T value, DataResult.StatusResult statusResult) {
        if(notnull(listType, key, value)) {
            if (validateListType(listType, value)) {
                JSONObject jsonObject = prepareValuePacket(key, null, statusResult);
                jsonObject.put("t", listType.ordinal());
                jsonObject.put("v", value);
                this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_ADD_LIST_ENTRY, jsonObject));
            }else{
                throw new IllegalArgumentException("Value is not valid for list type");
            }
        }else{
            throw new IllegalArgumentException("ListType, key or value is null");
        }
    }

    @Override
    public void removeListtype(ListType listType, DataResult.StatusResult statusResult) {
        if(notnull(listType)) {
            JSONObject jsonObject = prepareValuePacket(null, null, statusResult);
            jsonObject.put("t", listType.ordinal());
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_REMOVE_LISTTYPE, jsonObject));
        }else{
            throw new IllegalArgumentException("ListType is null");
        }
    }

    @Override
    public void removeListKey(ListType listType, String key, DataResult.StatusResult statusResult) {
        if(notnull(listType, key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, statusResult);
            jsonObject.put("t", listType.ordinal());
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_REMOVE_LISTKEY, jsonObject));
        }else{
            throw new IllegalArgumentException("ListType or key is null");
        }
    }

    @Override
    public void removeListIndex(ListType listType, String key, int index, DataResult.ListTypeValueResult specificResult) {
        if(notnull(listType, key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, specificResult);
            jsonObject.put("t", listType.ordinal());
            jsonObject.put("in", index);
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_REMOVE_INDEX, jsonObject));
        }else{
            throw new IllegalArgumentException("ListType or key is null");
        }
    }

    @Override
    public void getListKeys(ListType listType, DataResult.KeysResult keysResult) {
        if(notnull(listType)) {
            JSONObject jsonObject = prepareValuePacket(null, null, keysResult);
            jsonObject.put("t", listType.ordinal());
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_GET_LISTTYPE_KEYS, jsonObject));
        }else{
            throw new IllegalArgumentException("ListType is null");
        }
    }

    @Override
    public void getListTypes(DataResult.ListTypeResult listResult) {
        JSONObject jsonObject = prepareValuePacket(null, null, listResult);
        this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_GET_LISTTYPES, jsonObject));
    }

    @Override
    public <T> void getListData(ListType<T> listType, String key, DataResult.ListResult<T> listResult) {
        if(notnull(listType, key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, listResult);
            jsonObject.put("t", listType.ordinal());
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_GET_LISTKEY_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("ListType or key is null");
        }
    }

    public <T> void getListIndex(ListType<T> listType, String key, int index, DataResult.ListTypeValueResult<T> specificResult) {
        if(notnull(listType, key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, specificResult);
            jsonObject.put("t", listType.ordinal());
            jsonObject.put("in", index);
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_GET_LISTINDEX, jsonObject));
        }else{
            throw new IllegalArgumentException("ListType or key is null");
        }
    }

    public <T> void getListSize(ListType<T> listType, String key, DataResult.ListSizeResult sizeResult) {
        if(notnull(listType, key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, sizeResult);
            jsonObject.put("t", listType.ordinal());
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_GET_LISTSIZE, jsonObject));
        }else{
            throw new IllegalArgumentException("ListType or key is null");
        }
    }






    public void setSimple(String key, String value, DataResult.TextResult textResult) {
        if(notnull(key, value)) {
            JSONObject jsonObject = prepareValuePacket(key, null, textResult);
            jsonObject.put("v", value);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SIMPLE_SET, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or value is null");
        }
    }

    public void getSimple(String key, DataResult.TextResult textResult) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, textResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SIMPLE_GET, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    public void removeSimple(String key, DataResult.TextResult textResult) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, textResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SIMPLE_REMOVE, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or value is null");
        }
    }

    public void getSimpleKeys(DataResult.KeysResult keysResult) {
        if(notnull(keysResult)) {
            JSONObject jsonObject = prepareValuePacket(null, null, keysResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SIMPLE_GET_KEYS, jsonObject));
        }else{
            throw new IllegalArgumentException("KeysResult or value is null");
        }
    }

    public void getSimpleData(DataResult.MemberDataResult memberDataResult) {
        if(notnull(memberDataResult)) {
            JSONObject jsonObject = prepareValuePacket(null, null, memberDataResult);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SIMPLE_GET_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("KeysResult or value is null");
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

    private <T> boolean validateListType(ListType<T> listType, T value) {
        if(listType != null && value != null){
            return listType.check(value);
        }
        return false;
    }

    
    
}
