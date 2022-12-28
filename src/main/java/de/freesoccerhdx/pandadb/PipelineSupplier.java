package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.clientutils.ClientCommands;
import de.freesoccerhdx.pandadb.clientutils.DatabaseReader;
import de.freesoccerhdx.pandadb.clientutils.DatabaseWriter;
import de.freesoccerhdx.pandadb.clientutils.PandaClientChannel;
import de.freesoccerhdx.pandadb.clientutils.PandaDataSerializer;
import de.freesoccerhdx.simplesocket.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.freesoccerhdx.pandadb.DataResult.*;

public class PipelineSupplier implements ClientCommands {
    
    
    private final PandaClient pandaClient;
    private final HashMap<String, Result> waitingFutureListener = new HashMap<>();
    private final List<Pair<PandaClientChannel, JSONObject>> waitingCalls = new ArrayList<>();
    private String currentUUID;

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

    private JSONObject prepareValuePacket(String key, String member, Result future) {
        String uuid = this.currentUUID + "#" + waitingCalls.size();
        JSONObject jsonObject = new JSONObject();

        if(future != null) {
            waitingFutureListener.put(uuid, future);
            jsonObject.put("q", "#" + waitingCalls.size());
        }
        if(key != null) {
            jsonObject.put("k", key);
        }
        if(member != null) {
            jsonObject.put("m", member);
        }

        return jsonObject;
    }


    @Override
    public void setText(@NotNull String key, @NotNull String member, @NotNull String value, @Nullable TextResult result) {
        if(notnull(key, member, value)) {
            JSONObject jsonObject = prepareValuePacket(key, member, result);
            jsonObject.put("v", value);
            this.waitingCalls.add(Pair.of(PandaClientChannel.TEXT_SET, jsonObject));
        }else {
            throw new IllegalArgumentException("Key, member or value is null");
        }
    }

    @Override
    public void getTextKeys(@NotNull KeysResult result) {
        JSONObject jsonObject = prepareValuePacket(null, null, result);
        this.waitingCalls.add(Pair.of(PandaClientChannel.TEXT_GET_KEYS, jsonObject));
    }

    @Override
    public void getTextMemberKeys(@NotNull String key, @NotNull KeysResult result) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.TEXT_GET_MEMBER_KEYS, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void getTextMemberData(@NotNull String key, @NotNull String member, @NotNull TextResult result) {
        if(notnull(key, member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.TEXT_GET_MEMBER_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or member is null");
        }
    }

    @Override
    public void getTextKeyData(@NotNull String key, @NotNull MemberDataResult result) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.TEXT_GET_KEY_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void removeTextKey(@NotNull String key, StatusResult result) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.TEXT_REMOVE_KEY, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void removeTextMember(@NotNull String key, @NotNull String member, TextResult result) {
        if(notnull(key, member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.TEXT_REMOVE_MEMBER, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or member is null");
        }
    }

    @Override
    public <T> void setSerializable(@NotNull String key, @NotNull String member, @NotNull PandaDataSerializer<T> serializer, StatusResult result) {
        if(notnull(key, member, serializer)) {
            JSONObject jsonObject = prepareValuePacket(key, member, result);
            DatabaseWriter writer = new DatabaseWriter();
            serializer.serialize(writer);
            jsonObject.put("v", writer.toJSON());
            this.waitingCalls.add(Pair.of(PandaClientChannel.SERIALIZABLE_SET, jsonObject));
        }else {
            throw new IllegalArgumentException("Key, member or serializer is null");
        }
    }
    @Override
    public void getSerializableKeys(@NotNull KeysResult result) {
        JSONObject jsonObject = prepareValuePacket(null, null, result);
        this.waitingCalls.add(Pair.of(PandaClientChannel.SERIALIZABLE_GET_KEYS, jsonObject));
    }

    @Override
    public void getSerializableMemberKeys(@NotNull String key, @NotNull KeysResult result) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SERIALIZABLE_GET_MEMBER_KEYS, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public <T> void getSerializableMemberData(@NotNull String key, @NotNull String member, @NotNull SerializerFactory<T> factory, DataResult.@NotNull SpecificResult<T> result) {
        if(notnull(key, member, factory)) {
            JSONObject jsonObject = prepareValuePacket(key, member, new TextResult() {
                @Override
                public void result(String text, Status status) {
                    if(text != null) {
                        PandaDataSerializer pandaDataSerializer = (PandaDataSerializer) factory.create();
                        pandaDataSerializer.deserialize(new DatabaseReader(new JSONObject(text)));
                        result.result((T) pandaDataSerializer, status); //TODO: CHECK IF WORKING
                    }else{
                        result.result(null, status);
                    }
                }
            });
            this.waitingCalls.add(Pair.of(PandaClientChannel.SERIALIZABLE_GET_MEMBER_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("Key, member or factory is null");
        }
    }


    @Override
    public <T> void getSerializableKeyData(@NotNull String key, @NotNull SerializerFactory<T> factory, @NotNull ListStoredSerializableResult result) {
        if(notnull(key, factory)) {
            JSONObject jsonObject = prepareValuePacket(key, null, new TextResult() {
                @Override
                public void result(String data, Status status) {
                    if(data != null) {
                        HashMap<String, T> map = new HashMap<>();
                        Map<String, String> extractedData = (Map) ((JSONObject) new JSONObject(data)).toMap();
                        for(String key : extractedData.keySet()){
                            try {
                                JSONObject keyData = new JSONObject(extractedData.get(key));
                                PandaDataSerializer pandaDataSerializer = (PandaDataSerializer) factory.create();
                                pandaDataSerializer.deserialize(new DatabaseReader(keyData));
                                map.put(key, (T) pandaDataSerializer);
                            }catch (Exception exception){
                                exception.printStackTrace();
                            }
                        }
                        result.result(map, status);
                    }else{
                        result.result(null, status);
                    }
                }
            });
            this.waitingCalls.add(Pair.of(PandaClientChannel.SERIALIZABLE_GET_KEY_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or factory is null");
        }
    }

    @Override
    public void removeSerializableKey(@NotNull String key, StatusResult result) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SERIALIZABLE_REMOVE_KEY, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void removeSerializableMember(@NotNull String key, @NotNull String member, StatusResult result) {
        if(notnull(key, member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SERIALIZABLE_REMOVE_MEMBER, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or member is null");
        }
    }

    @Override
    public void setValue(@NotNull String key, @NotNull String member, double value, ValueResult result) {
        if(notnull(key, member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, result);
            jsonObject.put("v", value);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_SET, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or member is null");
        }
    }

    @Override
    public void addValue(@NotNull String key, @NotNull String member, double value, ValueResult result) {
        if(notnull(key, member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, result);
            jsonObject.put("v", value);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_ADD, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or member is null");
        }
    }

    @Override
    public void getValueKeys(@NotNull KeysResult result) {
        JSONObject jsonObject = prepareValuePacket(null, null, result);
        this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_GET_KEYS, jsonObject));
    }

    @Override
    public void getValueMemberKeys(@NotNull String key, @NotNull KeysResult result) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_GET_MEMBER_KEYS, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void getValueMemberData(@NotNull String key, @NotNull String member, @NotNull ValueResult result) {
        if(notnull(key, member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_GET_MEMBER_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or member is null");
        }
    }

    @Override
    public void getValueKeyData(@NotNull String key, @NotNull ValueMemberDataResult result) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_GET_KEY_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void removeValueKey(@NotNull String key, StatusResult result) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_REMOVE_KEY, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void removeValueMember(@NotNull String key, @NotNull String member, ValueResult result) {
        if(notnull(key, member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_REMOVE_MEMBER, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or member is null");
        }
    }

    @Override
    public void getValueInfo(@NotNull String key, boolean withKeys, @NotNull ValuesInfoResult result) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            if(withKeys) {
                jsonObject.put("km", true);
            }
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_GET_VALUE_INFO, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void getValueLowestTop(@NotNull String key, int maxMembers, @NotNull SortedValueMemberDataResult result) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            jsonObject.put("mm", maxMembers);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_GET_LOWEST_TOP, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    @Override
    public void getValueHighestTop(@NotNull String key, int maxMembers, @NotNull SortedValueMemberDataResult result) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            jsonObject.put("mm", maxMembers);
            this.waitingCalls.add(Pair.of(PandaClientChannel.VALUE_GET_HIGHEST_TOP, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }




    @Override
    public <T> void addList(@NotNull ListType<T> listType, @NotNull String key, @NotNull T value, StatusResult result) {
        if(notnull(listType, key, value)) {
            if (validateListType(listType, value)) {
                JSONObject jsonObject = prepareValuePacket(key, null, result);
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
    public void removeListType(@NotNull ListType listType, StatusResult result) {
        if(notnull(listType)) {
            JSONObject jsonObject = prepareValuePacket(null, null, result);
            jsonObject.put("t", listType.ordinal());
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_REMOVE_LISTTYPE, jsonObject));
        }else{
            throw new IllegalArgumentException("ListType is null");
        }
    }

    @Override
    public void removeListKey(@NotNull ListType listType, @NotNull String key, StatusResult result) {
        if(notnull(listType, key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            jsonObject.put("t", listType.ordinal());
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_REMOVE_LISTKEY, jsonObject));
        }else{
            throw new IllegalArgumentException("ListType or key is null");
        }
    }

    @Override
    public void removeListIndex(@NotNull ListType listType, @NotNull String key, int index, ListTypeValueResult result) {
        if(notnull(listType, key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            jsonObject.put("t", listType.ordinal());
            jsonObject.put("in", index);
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_REMOVE_INDEX, jsonObject));
        }else{
            throw new IllegalArgumentException("ListType or key is null");
        }
    }

    @Override
    public void getListKeys(@NotNull ListType listType, @NotNull KeysResult result) {
        if(notnull(listType)) {
            JSONObject jsonObject = prepareValuePacket(null, null, result);
            jsonObject.put("t", listType.ordinal());
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_GET_LISTTYPE_KEYS, jsonObject));
        }else{
            throw new IllegalArgumentException("ListType is null");
        }
    }

    @Override
    public void getListTypes(@NotNull ListTypeResult result) {
        JSONObject jsonObject = prepareValuePacket(null, null, result);
        this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_GET_LISTTYPES, jsonObject));
    }

    @Override
    public <T> void getListData(@NotNull ListType<T> listType, @NotNull String key, DataResult.@NotNull ListResult<T> result) {
        if(notnull(listType, key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            jsonObject.put("t", listType.ordinal());
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_GET_LISTKEY_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("ListType or key is null");
        }
    }

    public <T> void getListIndex(@NotNull ListType<T> listType, @NotNull String key, int index, DataResult.@NotNull ListTypeValueResult<T> result) {
        if(notnull(listType, key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            jsonObject.put("t", listType.ordinal());
            jsonObject.put("in", index);
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_GET_LISTINDEX, jsonObject));
        }else{
            throw new IllegalArgumentException("ListType or key is null");
        }
    }

    public void getListSize(@NotNull ListType listType, @NotNull String key, @NotNull ListSizeResult result) {
        if(notnull(listType, key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            jsonObject.put("t", listType.ordinal());
            this.waitingCalls.add(Pair.of(PandaClientChannel.LISTDATA_GET_LISTSIZE, jsonObject));
        }else{
            throw new IllegalArgumentException("ListType or key is null");
        }
    }






    public void setSimple(@NotNull String key, @NotNull String value, TextResult result) {
        if(notnull(key, value)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            jsonObject.put("v", value);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SIMPLE_SET, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or value is null");
        }
    }

    public void getSimple(@NotNull String key, @NotNull TextResult result) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SIMPLE_GET, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    public void removeSimple(@NotNull String key, TextResult result) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SIMPLE_REMOVE, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or value is null");
        }
    }

    public void getSimpleKeys(@NotNull KeysResult result) {
        if(notnull(result)) {
            JSONObject jsonObject = prepareValuePacket(null, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SIMPLE_GET_KEYS, jsonObject));
        }else{
            throw new IllegalArgumentException("KeysResult or value is null");
        }
    }

    public void getSimpleData(@NotNull MemberDataResult result) {
        if(notnull(result)) {
            JSONObject jsonObject = prepareValuePacket(null, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.SIMPLE_GET_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("KeysResult or value is null");
        }
    }



    public void createNewByteArray(@NotNull String key, int size, StatusResult result) {
        if(notnull(key)) {
            if(size < 1) {
                throw new IllegalArgumentException("Size must be greater than 0");
            }
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            jsonObject.put("s", size);
            this.waitingCalls.add(Pair.of(PandaClientChannel.BYTEARRAY_CREATE_NEW, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    public void setByteArrayIndex(@NotNull String key, int index, Byte value, StatusResult result) {
        if(notnull(key)) {
            if(index < 0) {
                throw new IllegalArgumentException("Index must be greater than 0");
            }
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            jsonObject.put("i", index);
            jsonObject.put("v", value);
            this.waitingCalls.add(Pair.of(PandaClientChannel.BYTEARRAY_SET_INDEX, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    public void getByteArrayIndex(@NotNull String key, int index, @NotNull ByteResult result) {
        if(notnull(key, result)) {
            if(index < 0) {
                throw new IllegalArgumentException("Index must be greater than 0");
            }
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            jsonObject.put("i", index);
            this.waitingCalls.add(Pair.of(PandaClientChannel.BYTEARRAY_GET_INDEX, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    public void getByteArrayIndexes(@NotNull String key, int[] indexes, @NotNull KeyByteDataResult result) {
        if(notnull(key, result)) {
            if (indexes.length < 1) {
                throw new IllegalArgumentException("Size of Indexes must be greater than 0");
            }
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            JSONArray jsonArray = new JSONArray();
            for (int i : indexes) {
                jsonArray.put(i);
            }
            jsonObject.put("i", jsonArray);
            this.waitingCalls.add(Pair.of(PandaClientChannel.BYTEARRAY_GET_INDEXES, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or Result is null");
        }
    }


    public void getByteArrayKeyData(@NotNull String key, @NotNull KeyByteDataResult result) {
        if(notnull(key, result)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.BYTEARRAY_GET_KEY_DATA, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or Result is null");
        }
    }

    public void getByteArrayKeySize(@NotNull String key, @NotNull ListSizeResult result) {
        if(notnull(key, result)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.BYTEARRAY_GET_KEY_SIZE, jsonObject));
        }else{
            throw new IllegalArgumentException("Key or Result is null");
        }
    }

    public void getByteArrayKeys(@NotNull KeysResult result) {
        if(notnull(result)) {
            JSONObject jsonObject = prepareValuePacket(null, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.BYTEARRAY_GET_KEYS, jsonObject));
        }else{
            throw new IllegalArgumentException("KeysResult or value is null");
        }
    }

    public void removeByteArrayKey(@NotNull String key, StatusResult result) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.BYTEARRAY_REMOVE_KEY, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
        }
    }

    public void clearByteArrayKey(@NotNull String key, StatusResult result) {
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key, null, result);
            this.waitingCalls.add(Pair.of(PandaClientChannel.BYTEARRAY_CLEAR_KEY, jsonObject));
        }else{
            throw new IllegalArgumentException("Key is null");
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
