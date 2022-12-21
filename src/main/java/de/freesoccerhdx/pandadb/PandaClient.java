package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.clientutils.BlockingPipelineSupplier;
import de.freesoccerhdx.pandadb.clientutils.ClientCommands;
import de.freesoccerhdx.pandadb.clientutils.DatabaseReader;
import de.freesoccerhdx.pandadb.clientutils.PandaDataSerializer;
import de.freesoccerhdx.pandadb.serverlisteners.MemberValueDataStorage;
import de.freesoccerhdx.simplesocket.client.ClientListener;
import de.freesoccerhdx.simplesocket.client.SimpleSocketClient;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PandaClient extends SimpleSocketClient implements ClientCommands {


    protected HashMap<DataResult.Result, Object> extraListenerInfo = new HashMap<>();
    protected HashMap<String, DataResult.Result> futureListener = new HashMap<>();
    private PipelineSupplier mainPipelineSupplier;


    public PandaClient(String name, String ip, int port){
        super(name, ip, port);

        mainPipelineSupplier = new PipelineSupplier(this);

        setSocketListener("dbpiperesult", new ClientListener() {
            @Override
            public void recive(SimpleSocketClient simpleSocketClient, String channel, String source, String message) {
                //System.err.println("Pandaclient got msg: " + message);
                try {
                    JSONObject jsonObject = new JSONObject(message);
                    int size = jsonObject.getInt("size");
                    String uuid = jsonObject.getString("uuid");

                    for (int i = 0; i < size; i++) {
                        if (jsonObject.has("r" + i)) {
                            JSONObject resultData = jsonObject.getJSONObject("r" + i);
                            String id = resultData.getString("id");
                            int statusID = resultData.getInt("s");
                            Object info = resultData.has("i") ? resultData.get("i") : null;
                            handleResult(uuid + id, Status.values()[statusID], info);
                            //System.out.println("ResultData=" + resultData);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public BlockingPipelineSupplier getBlockingPipelineSupplier() {
        return new BlockingPipelineSupplier(this);
    }

    public boolean isReady(){
        return this.isRunning() && this.isLoginSuccesfull();
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

    protected void handleResult(String id, Status status, Object info) {
        try {
            DataResult.Result listener = futureListener.get(id);
            //System.out.println("####### " + id + " " + status + " " + info);
            if (listener != null) {
                //System.out.println("Listener found for id=" + id + " -> " + listener.getClass().getSimpleName());
                if (listener instanceof DataResult.ValueResult result) {
                    if (info == null) {
                        result.result(null, status);
                    } else {
                        if (info instanceof Number number) {
                            result.result(number.doubleValue(), status);
                        } else {
                            result.result(null, status);
                        }

                    }
                } else if(listener instanceof DataResult.ValueMemberDataResult result) {
                    //System.out.println("ValueMemberDataResult: " + info);
                    if(info != null) {
                        HashMap<String, Double> data = null;
                        if (info instanceof JSONObject jsonObject) {
                            data = new HashMap<>();
                            for (String key : jsonObject.keySet()) {
                                data.put(key, jsonObject.getDouble(key));
                            }
                        }
                        result.result(data, status);
                    } else {
                        result.result(null, status);
                    }

                }else if(listener instanceof DataResult.ListTypeValueResult result){
                    if(info == null){
                        result.result(null,status);
                    }else{
                        result.result(info,status);
                    }
                }else if (listener instanceof DataResult.SpecificResult result) {
                    ClientCommands.SerializerFactory factory = (ClientCommands.SerializerFactory) extraListenerInfo.get(result);

                    PandaDataSerializer pandaDataSerializer = null;
                    if(info != null){
                        pandaDataSerializer = (PandaDataSerializer) factory.create();
                        JSONObject jsonObject = new JSONObject(info.toString());
                        pandaDataSerializer.deserialize(new DatabaseReader(jsonObject));
                    }
                    extraListenerInfo.remove(result);
                    result.result(pandaDataSerializer,status);
                }else if (listener instanceof DataResult.ListStoredSerializableResult result) {
                    ClientCommands.SerializerFactory factory = (ClientCommands.SerializerFactory) extraListenerInfo.get(result);

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

                    MemberValueDataStorage.ValueMembersInfo vmi = null;
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

                        vmi = new MemberValueDataStorage.ValueMembersInfo(low,high,avr,size,members);
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
                }else if (listener instanceof DataResult.ListTypeResult result) {
                    if(info != null){
                        if(info instanceof JSONArray){
                            JSONArray jsonArray = (JSONArray) info;
                            ArrayList<Object> arrayList = (ArrayList<Object>) jsonArray.toList();
                            ArrayList<ListType> listTypes = new ArrayList<>();
                            for(Object object : arrayList){
                                if(object instanceof Number number){
                                    listTypes.add(ListType.values()[number.intValue()]);
                                }
                            }
                            result.resultList(listTypes, status);
                        }
                    }else{
                        result.resultList(null, status);
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
                }else if (listener instanceof DataResult.ListSizeResult result) {
                    if(info != null){
                        if(info instanceof Number number){
                            result.result(number.intValue(), status);
                        }
                    }else{
                        result.result(0, status);
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
                }else {
                    System.err.println("Unknown Result-Type: " + listener.getClass().getName());
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
    public void setText(String key, String member, String value, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.setText(key, member, value, statusResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void getTextKeys(DataResult.KeysResult keysResult) {
        this.mainPipelineSupplier.getTextKeys(keysResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void getTextMemberKeys(String key, DataResult.KeysResult keysResult) {
        this.mainPipelineSupplier.getTextMemberKeys(key, keysResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void getTextMemberData(String key, String member, DataResult.TextResult textResult) {
        this.mainPipelineSupplier.getTextMemberData(key, member, textResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void getTextKeyData(String key, DataResult.MemberDataResult memberDataResult) {
        this.mainPipelineSupplier.getTextKeyData(key, memberDataResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void removeTextKey(String key, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.removeTextKey(key, statusResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void removeTextMember(String key, String member, DataResult.TextResult textResult) {
        this.mainPipelineSupplier.removeTextMember(key, member, textResult);
        this.mainPipelineSupplier.sync();
    }






    @Override
    public <T> void setSerializable(String key, String member, PandaDataSerializer<T> serializer, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.setSerializable(key, member, serializer, statusResult);
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
    public <T> void getSerializableMemberData(String key, String member, SerializerFactory<T> type, DataResult.SpecificResult<T> specificResult) {
        this.mainPipelineSupplier.getSerializableMemberData(key, member, type, specificResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public <T> void getSerializableKeyData(String key, SerializerFactory<T> factory, DataResult.ListStoredSerializableResult listStoredSerializableResult) {
        this.mainPipelineSupplier.getSerializableKeyData(key, factory, listStoredSerializableResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void removeSerializableKey(String key, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.removeSerializableKey(key, statusResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void removeSerializableMember(String key, String member, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.removeSerializableMember(key, member, statusResult);
        this.mainPipelineSupplier.sync();
    }










    @Override
    public void setValue(String key, String member, double value, DataResult.ValueResult valueResult) {
        this.mainPipelineSupplier.setValue(key, member, value, valueResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void addValue(String key, String member, double value, DataResult.ValueResult valueResult) {
        this.mainPipelineSupplier.addValue(key, member, value, valueResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void getValueKeys(DataResult.KeysResult keysResult) {
        this.mainPipelineSupplier.getValueKeys(keysResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void getValueMemberKeys(String key, DataResult.KeysResult keysResult) {
        this.mainPipelineSupplier.getValueMemberKeys(key, keysResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void getValueMemberData(String key, String member, DataResult.ValueResult valueResult) {
        this.mainPipelineSupplier.getValueMemberData(key, member, valueResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void getValueKeyData(String key, DataResult.ValueMemberDataResult listStoredSerializableResult) {
        this.mainPipelineSupplier.getValueKeyData(key, listStoredSerializableResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void removeValueKey(String key, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.removeValueKey(key, statusResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void removeValueMember(String key, String member, DataResult.ValueResult valueResult) {
        this.mainPipelineSupplier.removeValueMember(key, member, valueResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void getValueInfo(String key, boolean withKeys, DataResult.ValuesInfoResult valuesInfoResult) {
        this.mainPipelineSupplier.getValueInfo(key, withKeys, valuesInfoResult);
        this.mainPipelineSupplier.sync();
    }






    @Override
    public void addList(ListType listType, String key, Object value, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.addList(listType, key, value, statusResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void removeListtype(ListType listType, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.removeListtype(listType, statusResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void removeListKey(ListType listType, String key, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.removeListKey(listType, key, statusResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void removeListIndex(ListType listType, String key, int index, DataResult.ListTypeValueResult specificResult) {
        this.mainPipelineSupplier.removeListIndex(listType, key, index, specificResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void getListKeys(ListType listType, DataResult.KeysResult keysResult) {
        this.mainPipelineSupplier.getListKeys(listType, keysResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void getListTypes(DataResult.ListTypeResult listResult) {
        this.mainPipelineSupplier.getListTypes(listResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public <T> void getListData(ListType<T> listType, String key, DataResult.ListResult<T> listResult) {
        this.mainPipelineSupplier.getListData(listType, key, listResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public <T> void getListIndex(ListType<T> listType, String key, int index, DataResult.ListTypeValueResult<T> specificResult) {
        this.mainPipelineSupplier.getListIndex(listType, key, index, specificResult);
        this.mainPipelineSupplier.sync();
    }

    @Override
    public void getListSize(ListType listType, String key, DataResult.ListSizeResult listSizeResult) {
        this.mainPipelineSupplier.getListSize(listType, key, listSizeResult);
        this.mainPipelineSupplier.sync();
    }


}
