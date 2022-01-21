package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.clientlisteners.ListKeysListener;
import de.freesoccerhdx.pandadb.clientlisteners.ListListener;
import de.freesoccerhdx.pandadb.clientlisteners.RemoveListener;
import de.freesoccerhdx.pandadb.clientlisteners.TextListener;
import de.freesoccerhdx.pandadb.clientlisteners.ValueListener;
import de.freesoccerhdx.pandadb.interweb.ClientCommands;
import de.freesoccerhdx.pandadb.interweb.DatabaseReader;
import de.freesoccerhdx.pandadb.interweb.PandaDataSerializer;
import de.freesoccerhdx.simplesocket.client.ClientListener;
import de.freesoccerhdx.simplesocket.client.SimpleSocketClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PandaClient implements ClientCommands {


    protected HashMap<DataResult.Result, Object> extraListenerInfo = new HashMap<>();
    protected HashMap<String, DataResult.Result> futureListener = new HashMap<>();
    private SimpleSocketClient simpleSocketClient;
    private PipelineSupplier mainPipelineSupplier;

    public PandaClient(String name, String ip, int port){
        mainPipelineSupplier = new PipelineSupplier(this);
        this.simpleSocketClient = new SimpleSocketClient(name, ip, port);

        new ValueListener(this);
        new TextListener(this);
        new ListListener(this);
        new RemoveListener(this);
        new ListKeysListener(this);

        this.simpleSocketClient.setSocketListener("dbpiperesult", new ClientListener() {
            @Override
            public void recive(SimpleSocketClient simpleSocketClient, String channel, String source, String message) {
                //System.err.println(message);
                JSONObject jsonObject = new JSONObject(message);
                int size = jsonObject.getInt("size");
                String uuid = jsonObject.getString("uuid");

                for(int i = 0; i < size; i++){
                    if(jsonObject.has("r"+i)){
                        JSONObject resultData = jsonObject.getJSONObject("r"+i);
                        String id = resultData.getString("id");
                        int statusID = resultData.getInt("s");
                        Object info = resultData.has("i") ? resultData.get("i") : null;
                        handleResult(uuid+id,Status.values()[statusID],info);
                        //System.out.println("ResultData=" + resultData);
                    }
                }


            }
        });

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

    /**
     * USE THIS FUCKING WISELEY! Too many database-calls will not work since the transfer length-limit is reached.
     * */
    public PipelineSupplier createPipelineSupplier(){
        return new PipelineSupplier(this);
    }

    public void handleResult(String id, Status status, Object info){
        try {
            DataResult.Result listener = futureListener.get(id);
            if (listener != null) {
                if (listener instanceof DataResult.ValueResult) {
                    DataResult.ValueResult valueListener = (DataResult.ValueResult) listener;
                    if(info == null){
                        valueListener.result(null, status);
                    }else {
                        if(info instanceof BigDecimal){
                            BigDecimal bigDecimal = (BigDecimal) info;
                            valueListener.result(bigDecimal.doubleValue(), status);
                        }else if(info instanceof Integer){
                            Integer integer = (Integer) info;
                            valueListener.result(integer.doubleValue(), status);
                        }else{
                            valueListener.result((Double) info, status);
                        }
                    }
                }else if (listener instanceof DataResult.StoredSerializableResult) {
                    DataResult.StoredSerializableResult result = (DataResult.StoredSerializableResult) listener;
                    SerializerFactory factory = (SerializerFactory) extraListenerInfo.get(result);

                    PandaDataSerializer pandaDataSerializer = null;
                    if(info != null){
                        pandaDataSerializer = (PandaDataSerializer) factory.create();
                        JSONObject jsonObject = new JSONObject(""+info);
                        pandaDataSerializer.deserialize(new DatabaseReader(jsonObject));
                    }
                    extraListenerInfo.remove(result);
                    result.result(pandaDataSerializer,status);
                }else if (listener instanceof DataResult.ListStoredSerializableResult) {
                    DataResult.ListStoredSerializableResult result = (DataResult.ListStoredSerializableResult) listener;
                    SerializerFactory factory = (SerializerFactory) extraListenerInfo.get(result);

                    HashMap<String,PandaDataSerializer> data = null;
                    if(info != null){
                        data = new HashMap<>();
                        Map infoData = ((JSONObject) info).toMap();
                        Map<String, String> extractedData = infoData;

                        for(String key : extractedData.keySet()){
                            try {
                                JSONObject keyData = new JSONObject(extractedData.get(key));
                                PandaDataSerializer pandaDataSerializer = (PandaDataSerializer) factory.create();
                                pandaDataSerializer.deserialize(new DatabaseReader(keyData));
                                data.put(key, pandaDataSerializer);
                            }catch (Exception exception){
                                exception.printStackTrace();
                            }
                        }

                    }

                    extraListenerInfo.remove(result);
                    result.result(data, status);
                }else if (listener instanceof DataResult.ValuesInfoResult) {
                    DataResult.ValuesInfoResult valuesInfoResult = (DataResult.ValuesInfoResult) listener;

                    ValueDataStorage.ValueMembersInfo vmi = null;
                    if(info != null){
                        JSONObject vmiJSON = (JSONObject) info;
                        double avr = vmiJSON.getDouble("avr");
                        double low = vmiJSON.getDouble("low");
                        double high = vmiJSON.getDouble("high");
                        int size = vmiJSON.getInt("size");
                        List<String> members = null;

                        if(vmiJSON.has("mem")){
                            List objectList = vmiJSON.getJSONArray("mem").toList();
                            members = objectList;
                        }

                        vmi = new ValueDataStorage.ValueMembersInfo(low,high,avr,size,members);
                    }

                    valuesInfoResult.result(vmi, status);

                }else if (listener instanceof DataResult.StatusResult) {
                    DataResult.StatusResult setResult = (DataResult.StatusResult) listener;
                    setResult.result(status);

                }else if (listener instanceof DataResult.MemberDataResult) {
                    DataResult.MemberDataResult memberDataResult = (DataResult.MemberDataResult) listener;
                    if(info == null){
                        memberDataResult.resultData(null, status);
                    }else{
                        JSONObject jsonObject = (JSONObject) info;
                        Map hashMap = jsonObject.toMap();
                        memberDataResult.resultData((HashMap<String, String>) hashMap, status);
                    }

                }else if (listener instanceof DataResult.TextResult) {
                    DataResult.TextResult textResult = (DataResult.TextResult) listener;
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
                            ArrayList arrayList = (ArrayList) jsonArray.toList();

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

    @Override
    public void getMemberData(String key, DataResult.MemberDataResult memberData) {
        this.mainPipelineSupplier.getMemberData(key, memberData);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public <T> void getSerializableMemberData(String key, SerializerFactory<T> factory, DataResult.ListStoredSerializableResult<T> memberData) {
        this.mainPipelineSupplier.getSerializableMemberData(key, factory, memberData);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void getSerializableKeys(DataResult.KeysResult keysResult) {
        this.mainPipelineSupplier.getSerializableKeys(keysResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void getSerializableMemberKeys(String key, DataResult.KeysResult keysResult) {
        this.mainPipelineSupplier.getSerializableMemberKeys(key, keysResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public <T> void storeSerializable(String key, String member, PandaDataSerializer<T> serializer, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.storeSerializable(key,member,serializer,statusResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public <T> void getStoredSerializable(String key, String member, SerializerFactory<T> type, DataResult.StoredSerializableResult<T> serializableResult) {
        this.mainPipelineSupplier.getStoredSerializable(key,member,type,serializableResult);
        this.mainPipelineSupplier.sync();
    }

    public <T> void addListEntry(String key, String listkey, ListType listType, T value, DataResult.StatusResult addListResult){
        this.mainPipelineSupplier.addListEntry(key,listkey,listType,value,addListResult);
        this.mainPipelineSupplier.sync();
        /*
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
         */
    }

    public <T> void getList(String key, String listkey, ListType listType, DataResult.ListResult<T> listResult){
        this.mainPipelineSupplier.getList(key,listkey,listType,listResult);
        this.mainPipelineSupplier.sync();
        /*
        if(notnull(key, listType)) {
            JSONObject jsonObject = prepareValuePacket(key, null, listResult);
            jsonObject.put("type", listType.ordinal());
            jsonObject.put("listkey", listkey);
            this.simpleSocketClient.sendMessage("getlist", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and ListType can't be null!");
        }
         */
    }

    public void get(String key, String member, DataResult.TextResult textResult){
        this.mainPipelineSupplier.get(key,member,textResult);
        this.mainPipelineSupplier.sync();
        /*
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, textResult);
            this.simpleSocketClient.sendMessage("get", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
         */
    }

    public void set(String key, String member, String value, DataResult.StatusResult setResult){
        this.mainPipelineSupplier.set(key,member,value,setResult);
        this.mainPipelineSupplier.sync();
        /*
        if(notnull(key,member,value)) {
            JSONObject jsonObject = prepareValuePacket(key, member, setResult);
            jsonObject.put("value", value);
            this.simpleSocketClient.sendMessage("set","Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key, Member and Value can't be null!");
        }
         */
    }

    public void getValue(String key, String member, DataResult.ValueResult valueResult){
        this.mainPipelineSupplier.getValue(key,member,valueResult);
        this.mainPipelineSupplier.sync();
        /*
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, future);
            this.simpleSocketClient.sendMessage("getvalue","Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
         */
    }

    public void setValue(String key, String member, double value, DataResult.ValueResult valueResult){
        this.mainPipelineSupplier.setValue(key,member,value,valueResult);
        this.mainPipelineSupplier.sync();
        /*
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, valueResult);
            jsonObject.put("value", value);
            this.simpleSocketClient.sendMessage("setvalue", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
        */
    }

    public void addValue(String key, String member, double value, DataResult.ValueResult valueResult){
        this.mainPipelineSupplier.addValue(key,member,value,valueResult);
        this.mainPipelineSupplier.sync();
        /*
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, valueResult);
            jsonObject.put("value", value);
            this.simpleSocketClient.sendMessage("addvalue", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
         */
    }

    public void removeValue(String key, String member, DataResult.StatusResult removeResult){
        this.mainPipelineSupplier.removeValue(key,member,removeResult);
        this.mainPipelineSupplier.sync();
        /*
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, removeResult);
            jsonObject.put("removetype", 0);
            this.simpleSocketClient.sendMessage("remove", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
         */
    }

    public void remove(String key, String member, DataResult.StatusResult removeResult){
        this.mainPipelineSupplier.remove(key,member,removeResult);
        this.mainPipelineSupplier.sync();
        /*
        if(notnull(key,member)) {
            JSONObject jsonObject = prepareValuePacket(key, member, removeResult);
            jsonObject.put("removetype", 1);
            this.simpleSocketClient.sendMessage("remove", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and Member can't be null!");
        }
         */
    }

    public <T> void removeList(String key, String listkey, ListType listType, DataResult.StatusResult removeResult){
        this.mainPipelineSupplier.removeList(key,listkey,listType,removeResult);
        this.mainPipelineSupplier.sync();
        /*
        if(notnull(key,listkey,listType)) {
            JSONObject jsonObject = prepareValuePacket(key, null, removeResult);
            jsonObject.put("removetype", 2);
            jsonObject.put("type", listType.ordinal());
            jsonObject.put("listkey", listkey);
            this.simpleSocketClient.sendMessage("remove", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key, Listkey and ListType can't be null!");
        }
         */
    }

    public <T> void removeList(String key, ListType listType, DataResult.StatusResult removeResult){
        this.mainPipelineSupplier.removeList(key,listType,removeResult);
        this.mainPipelineSupplier.sync();
        /*
        if(notnull(key,listType)) {
            JSONObject jsonObject = prepareValuePacket(key, null, removeResult);
            jsonObject.put("removetype", 4);
            jsonObject.put("type", listType.ordinal());
            this.simpleSocketClient.sendMessage("remove", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and ListType can't be null!");
        }
         */
    }

    public <T> void removeListIndex(String key, String listkey, ListType listType, int index, DataResult.StatusResult removeResult){
        this.mainPipelineSupplier.removeListIndex(key,listkey,listType,index,removeResult);
        this.mainPipelineSupplier.sync();
        /*
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
         */
    }

    public <T> void getListKeys(ListType listType, DataResult.KeysResult keysResult){
        this.mainPipelineSupplier.getListKeys(listType,keysResult);
        this.mainPipelineSupplier.sync();
        /*
        if(notnull(listType)) {
            JSONObject jsonObject = prepareValuePacket(null,null, keysResult);
            jsonObject.put("gettype", 0);
            jsonObject.put("type", listType.ordinal());

            this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("ListType can't be null!");
        }
         */
    }

    public <T> void getListKeys(String key, ListType listType, DataResult.KeysResult keysResult){
        this.mainPipelineSupplier.getListKeys(key, listType,keysResult);
        this.mainPipelineSupplier.sync();
        /*
        if(notnull(key, listType)) {
            JSONObject jsonObject = prepareValuePacket(key,null, keysResult);
            jsonObject.put("gettype", 1);
            jsonObject.put("type", listType.ordinal());

            this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key and ListType can't be null!");
        }
        */
    }

    public void getKeys(String key, DataResult.KeysResult keysResult){
        this.mainPipelineSupplier.getKeys(key,keysResult);
        this.mainPipelineSupplier.sync();
        /*
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key,null, keysResult);
            jsonObject.put("gettype", 2);

            this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key can't be null!");
        }
         */
    }

    public void getKeys(DataResult.KeysResult keysResult){
        this.mainPipelineSupplier.getKeys(keysResult);
        this.mainPipelineSupplier.sync();
        /*
        JSONObject jsonObject = prepareValuePacket(null,null, keysResult);
        jsonObject.put("gettype", 3);

        this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
        */
    }

    public void getValueKeys(DataResult.KeysResult keysResult){
        this.mainPipelineSupplier.getValueKeys(keysResult);
        this.mainPipelineSupplier.sync();
        /*
        JSONObject jsonObject = prepareValuePacket(null,null, keysResult);
        jsonObject.put("gettype", 4);

        this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
        */
    }

    public void getValueKeys(String key, DataResult.KeysResult keysResult){
        this.mainPipelineSupplier.getValueKeys(key,keysResult);
        this.mainPipelineSupplier.sync();
        /*
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key,null, keysResult);
            jsonObject.put("gettype", 5);

            this.simpleSocketClient.sendMessage("listkeys", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key can't be null!");
        }
         */
    }

    public void getValuesMemberInfo(String key, boolean withKeys, DataResult.ValuesInfoResult valuesInfoResult){
        this.mainPipelineSupplier.getValuesMemberInfo(key,withKeys,valuesInfoResult);
        this.mainPipelineSupplier.sync();
        /*
        if(notnull(key)) {
            JSONObject jsonObject = prepareValuePacket(key,null, valuesInfoResult);
            if(withKeys){
                jsonObject.put("k", true);
            }
            this.simpleSocketClient.sendMessage("infovalues", "Server", jsonObject.toString());
        }else{
            throw new IllegalArgumentException("Key can't be null!");
        }
         */
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
