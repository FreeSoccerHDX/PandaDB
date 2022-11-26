package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.clientutils.ClientCommands;
import de.freesoccerhdx.pandadb.clientutils.DatabaseReader;
import de.freesoccerhdx.pandadb.clientutils.PandaDataSerializer;
import de.freesoccerhdx.simplesocket.client.ClientListener;
import de.freesoccerhdx.simplesocket.client.SimpleSocketClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PandaClient extends SimpleSocketClient implements ClientCommands{


    protected HashMap<DataResult.Result, Object> extraListenerInfo = new HashMap<>();
    protected HashMap<String, DataResult.Result> futureListener = new HashMap<>();
    private PipelineSupplier mainPipelineSupplier;


    public PandaClient(String name, String ip, int port){
        super(name, ip, port);

        mainPipelineSupplier = new PipelineSupplier(this);

        setSocketListener("dbpiperesult", new ClientListener() {
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

        setSocketListener("testping", new ClientListener() {
            @Override
            public void recive(SimpleSocketClient simpleSocketClient, String channel, String source, String message) {
                testping = message;
            }
        });
    }

    private String testping = null;
    private String getTestPing(){
        String s = testping;
        if(s == null){
            testping = null;
        }
        return s;
    }


    public String testPing(int max_ns){

        String testping = null;

        this.sendMessage("testping", "Server", "get me this");

        long maxTime = System.nanoTime()+max_ns;
        long start = System.nanoTime();
        while(System.nanoTime() < maxTime){
            String s = getTestPing();
            if(s != null){
                testping = s;
                break;
            }
        }

        long end = System.nanoTime();

        System.out.println("Test: " + (end-start)+"ns" + " ("+(((double)(end-start))/1000000.0)+"ms)");

        return testping;
    }


    public boolean isReady(){
        return this.isRunning() && this.isLogin_succesfull();
    }

    public void makeDataTree(){
        this.sendMessage("datatree","Server", "");
    }

    /**
     * USE THIS FUCKING WISELY! Too many database-calls will not work since the transfer length-limit is reached.
     * Anyway... this can handle up to 99`999`999 chars while sending messages... safe until maybe 20k Database-calls
     * */
    public PipelineSupplier createPipelineSupplier(){
        return new PipelineSupplier(this);
    }

    protected void handleResult(String id, Status status, Object info){
        try {
            DataResult.Result listener = futureListener.get(id);
            if (listener != null) {
                if (listener instanceof DataResult.ValueResult result) {
                    if(info == null){
                        result.result(null, status);
                    }else {
                        if(info instanceof Number number){
                            result.result(number.doubleValue(), status);
                        }else{
                            result.result(null, status);
                        }
                        
                    }
                }else if (listener instanceof DataResult.StoredSerializableResult result) {
                    SerializerFactory factory = (SerializerFactory) extraListenerInfo.get(result);

                    PandaDataSerializer pandaDataSerializer = null;
                    if(info != null){
                        pandaDataSerializer = (PandaDataSerializer) factory.create();
                        JSONObject jsonObject = new JSONObject(""+info);
                        pandaDataSerializer.deserialize(new DatabaseReader(jsonObject));
                    }
                    extraListenerInfo.remove(result);
                    result.result(pandaDataSerializer,status);
                }else if (listener instanceof DataResult.ListStoredSerializableResult result) {
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
                }else if (listener instanceof DataResult.ValuesInfoResult result) {

                    ValueDataStorage.ValueMembersInfo vmi = null;
                    if(info != null){
                        JSONObject vmiJSON = (JSONObject) info;
                        double avr = vmiJSON.getDouble("avr");
                        double low = vmiJSON.getDouble("low");
                        double high = vmiJSON.getDouble("high");
                        int size = vmiJSON.getInt("size");
                        List<String> members = null;

                        if(vmiJSON.has("mem")){
                            members = (List) vmiJSON.getJSONArray("mem").toList();
                        }

                        vmi = new ValueDataStorage.ValueMembersInfo(low,high,avr,size,members);
                    }

                    result.result(vmi, status);

                }else if (listener instanceof DataResult.StatusResult result) {
                    result.result(status);

                }else if (listener instanceof DataResult.MemberDataResult result) {
                    if(info == null){
                        result.resultData(null, status);
                    }else{
                        JSONObject jsonObject = (JSONObject) info;
                        Map hashMap = jsonObject.toMap();
                        result.resultData((HashMap<String, String>) hashMap, status);
                    }

                }else if (listener instanceof DataResult.TextResult result) {
                    if(info == null){
                        result.result(null, status);
                    }else {
                        result.result((String) info, status);
                    }
                }else if (listener instanceof DataResult.ListResult result) {
                    if(info != null){
                        if(info instanceof JSONArray){
                            JSONArray jsonArray = (JSONArray) info;
                            ArrayList<Object> arrayList = (ArrayList<Object>) jsonArray.toList();
                            result.resultList(arrayList, status);
                        }
                    }else{
                        result.resultList(null, status);
                    }
                }else if (listener instanceof DataResult.KeysResult result) {
                    if(info != null){
                        if(info instanceof JSONArray){
                            JSONArray jsonArray = (JSONArray) info;
                            ArrayList arrayList = (ArrayList) jsonArray.toList();

                            result.resultList(arrayList, status);
                        }
                    }else{
                        result.resultList(null, status);
                    }

                }
            }else{
                System.err.println("###########################");
                System.err.println("#");
                System.err.println("# NO Handler for: id="+id + ", status="+status + ", info="+info );
                System.err.println("#");
                System.err.println("###########################");
                throw new IllegalStateException();

            }
        }finally {
            futureListener.remove(id);
        }
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
    }

    public <T> void getList(String key, String listkey, ListType listType, DataResult.ListResult<T> listResult){
        this.mainPipelineSupplier.getList(key,listkey,listType,listResult);
        this.mainPipelineSupplier.sync();
    }

    public void get(String key, String member, DataResult.TextResult textResult){
        this.mainPipelineSupplier.get(key,member,textResult);
        this.mainPipelineSupplier.sync();
    }

    public void set(String key, String member, String value, DataResult.StatusResult setResult){
        this.mainPipelineSupplier.set(key,member,value,setResult);
        this.mainPipelineSupplier.sync();
    }

    public void getValue(String key, String member, DataResult.ValueResult valueResult){
        this.mainPipelineSupplier.getValue(key,member,valueResult);
        this.mainPipelineSupplier.sync();
    }

    public void setValue(String key, String member, double value, DataResult.ValueResult valueResult){
        this.mainPipelineSupplier.setValue(key,member,value,valueResult);
        this.mainPipelineSupplier.sync();
    }

    public void addValue(String key, String member, double value, DataResult.ValueResult valueResult){
        this.mainPipelineSupplier.addValue(key,member,value,valueResult);
        this.mainPipelineSupplier.sync();
    }

    public void removeValue(String key, String member, DataResult.StatusResult removeResult){
        this.mainPipelineSupplier.removeValue(key,member,removeResult);
        this.mainPipelineSupplier.sync();
    }

    public void remove(String key, String member, DataResult.StatusResult removeResult){
        this.mainPipelineSupplier.remove(key,member,removeResult);
        this.mainPipelineSupplier.sync();
    }

    public <T> void removeList(String key, String listkey, ListType listType, DataResult.StatusResult removeResult){
        this.mainPipelineSupplier.removeList(key,listkey,listType,removeResult);
        this.mainPipelineSupplier.sync();
    }

    public <T> void removeList(String key, ListType listType, DataResult.StatusResult removeResult){
        this.mainPipelineSupplier.removeList(key,listType,removeResult);
        this.mainPipelineSupplier.sync();
    }

    public <T> void removeListIndex(String key, String listkey, ListType listType, int index, DataResult.StatusResult removeResult){
        this.mainPipelineSupplier.removeListIndex(key,listkey,listType,index,removeResult);
        this.mainPipelineSupplier.sync();
    }

    public <T> void getListKeys(ListType listType, DataResult.KeysResult keysResult){
        this.mainPipelineSupplier.getListKeys(listType,keysResult);
        this.mainPipelineSupplier.sync();
    }

    public <T> void getListKeys(String key, ListType listType, DataResult.KeysResult keysResult){
        this.mainPipelineSupplier.getListKeys(key, listType,keysResult);
        this.mainPipelineSupplier.sync();
    }

    public void getKeys(String key, DataResult.KeysResult keysResult){
        this.mainPipelineSupplier.getKeys(key,keysResult);
        this.mainPipelineSupplier.sync();
    }

    public void getKeys(DataResult.KeysResult keysResult){
        this.mainPipelineSupplier.getKeys(keysResult);
        this.mainPipelineSupplier.sync();
    }

    public void getValueKeys(DataResult.KeysResult keysResult){
        this.mainPipelineSupplier.getValueKeys(keysResult);
        this.mainPipelineSupplier.sync();
    }

    public void getValueKeys(String key, DataResult.KeysResult keysResult){
        this.mainPipelineSupplier.getValueKeys(key,keysResult);
        this.mainPipelineSupplier.sync();
    }

    public void getValuesMemberInfo(String key, boolean withKeys, DataResult.ValuesInfoResult valuesInfoResult){
        this.mainPipelineSupplier.getValuesMemberInfo(key,withKeys,valuesInfoResult);
        this.mainPipelineSupplier.sync();
    }
}
