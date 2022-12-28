package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.clientutils.BlockingPipelineSupplier;
import de.freesoccerhdx.pandadb.clientutils.changelistener.ChangeListenerHandler;
import de.freesoccerhdx.pandadb.clientutils.ClientCommands;
import de.freesoccerhdx.pandadb.clientutils.PandaDataSerializer;
import de.freesoccerhdx.pandadb.serverutils.datastorage.MemberValueDataStorage;
import de.freesoccerhdx.simplesocket.Pair;
import de.freesoccerhdx.simplesocket.client.ClientListener;
import de.freesoccerhdx.simplesocket.client.SimpleSocketClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PandaClient extends SimpleSocketClient implements ClientCommands {


    protected final HashMap<String, DataResult.Result> futureListener = new HashMap<>();
    private final PipelineSupplier mainPipelineSupplier;
    private final ChangeListenerHandler changeListener;


    public PandaClient(String name, String ip, int port) {
        super(name, ip, port);

        mainPipelineSupplier = new PipelineSupplier(this);
        changeListener = new ChangeListenerHandler(this);

        setSocketListener("dbpiperesult", new ClientListener() {
            @Override
            public void recive(SimpleSocketClient simpleSocketClient, String channel, String source, String message) {
                //System.err.println("Pandaclient got msg: " + message);
                try {
                    JSONObject jsonObject = new JSONObject(message);
                    String uuid = jsonObject.getString("uuid");
                    JSONArray array = jsonObject.getJSONArray("data");

                    array.forEach(o -> {
                        if(o instanceof JSONObject resultData) {
                            String id = resultData.getString("id");
                            int statusID = resultData.getInt("s");
                            Object info = resultData.has("i") ? resultData.get("i") : null;
                            handleResult(uuid + id, Status.values()[statusID], info);
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public ChangeListenerHandler getChangeListenerHandler() {
        return changeListener;
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
     * Anyway... this can handle up to 99`999`999 chars while sending messages...
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
                if(listener instanceof DataResult.SortedValueMemberDataResult result) {
                    if (info == null) {
                        result.result(null, status);
                    } else {
                        JSONArray jsonArray = (JSONArray) info;
                        Pair<String, Double>[] pairs = new Pair[jsonArray.length() / 2];
                        for (int i = 0; i < jsonArray.length(); i += 2) {
                            pairs[i / 2] = new Pair<>(jsonArray.getString(i), jsonArray.getDouble(i + 1));
                        }
                        result.result(pairs, status);
                    }

                }else if(listener instanceof DataResult.KeyByteDataResult result) {
                    if (info == null) {
                        result.result(null, status);
                    } else {
                        JSONArray jsonArray = (JSONArray) info;
                        Byte[] bytes = new Byte[jsonArray.length()];
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Object o = jsonArray.get(i);
                            if(o instanceof Number) {
                                bytes[i] = ((Number) o).byteValue();
                            }
                            //bytes[i] = o == null ? null : ((Number) o).byteValue();
                        }
                        result.result(bytes, status);
                    }
                }else if(listener instanceof DataResult.ByteResult result) {
                    if (info == null) {
                        result.result(null, status);
                    } else {
                        result.result(((Number)info).byteValue(), status);
                    }
                }else if (listener instanceof DataResult.ValueResult result) {
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

                }else if(listener instanceof DataResult.ListTypeValueResult result) {
                    result.result(info,status);
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
                        result.result(null, status);
                    }else{
                        JSONObject jsonObject = (JSONObject) info;
                        Map hashMap = jsonObject.toMap();
                        result.result((HashMap<String, String>) hashMap, status);
                    }

                }else if (listener instanceof DataResult.TextResult result) {
                    if(info == null){
                        result.result(null, status);
                    }else {
                        result.result(info.toString(), status);
                    }
                }else if (listener instanceof DataResult.ListTypeResult result) {
                    if(info != null){
                        if(info instanceof JSONArray jsonArray){
                            ArrayList<Object> arrayList = (ArrayList<Object>) jsonArray.toList();
                            ArrayList<ListType> listTypes = new ArrayList<>();
                            for(Object object : arrayList){
                                if(object instanceof Number number){
                                    listTypes.add(ListType.values()[number.intValue()]);
                                }
                            }
                            result.result(listTypes, status);
                        }
                    }else{
                        result.result(null, status);
                    }
                }else if (listener instanceof DataResult.ListResult result) {
                    if(info != null){
                        if(info instanceof JSONArray jsonArray){
                            ArrayList<Object> arrayList = (ArrayList<Object>) jsonArray.toList();
                            result.result(arrayList, status);
                        }
                    }else{
                        result.result(null, status);
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
                        if(info instanceof JSONArray jsonArray){
                            ArrayList arrayList = (ArrayList) jsonArray.toList();

                            result.result(arrayList, status);
                        }
                    }else{
                        result.result(null, status);
                    }
                }else {
                    System.err.println("Unknown Result-Type: " + listener.getClass().getName());
                }
            }else if(info != null) {
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


    public void setText(@NotNull String key, @NotNull String member, @NotNull String value, @Nullable DataResult.TextResult result) {
        this.mainPipelineSupplier.setText(key, member, value, result);
        this.mainPipelineSupplier.sync();
    }


    public void getTextKeys(DataResult.@NotNull KeysResult keysResult) {
        this.mainPipelineSupplier.getTextKeys(keysResult);
        this.mainPipelineSupplier.sync();
    }


    public void getTextMemberKeys(@NotNull String key, DataResult.@NotNull KeysResult keysResult) {
        this.mainPipelineSupplier.getTextMemberKeys(key, keysResult);
        this.mainPipelineSupplier.sync();
    }


    public void getTextMemberData(@NotNull String key, @NotNull String member, DataResult.@NotNull TextResult textResult) {
        this.mainPipelineSupplier.getTextMemberData(key, member, textResult);
        this.mainPipelineSupplier.sync();
    }


    public void getTextKeyData(@NotNull String key, DataResult.@NotNull MemberDataResult memberDataResult) {
        this.mainPipelineSupplier.getTextKeyData(key, memberDataResult);
        this.mainPipelineSupplier.sync();
    }


    public void removeTextKey(@NotNull String key, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.removeTextKey(key, statusResult);
        this.mainPipelineSupplier.sync();
    }


    public void removeTextMember(@NotNull String key, @NotNull String member, DataResult.TextResult textResult) {
        this.mainPipelineSupplier.removeTextMember(key, member, textResult);
        this.mainPipelineSupplier.sync();
    }







    public <T> void setSerializable(@NotNull String key, @NotNull String member, @NotNull PandaDataSerializer<T> serializer, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.setSerializable(key, member, serializer, statusResult);
        this.mainPipelineSupplier.sync();
    }


    public void getSerializableKeys(DataResult.@NotNull KeysResult keysResult) {
        this.mainPipelineSupplier.getSerializableKeys(keysResult);
        this.mainPipelineSupplier.sync();
    }


    public void getSerializableMemberKeys(@NotNull String key, DataResult.@NotNull KeysResult keysResult) {
        this.mainPipelineSupplier.getSerializableMemberKeys(key, keysResult);
        this.mainPipelineSupplier.sync();
    }


    public <T> void getSerializableMemberData(@NotNull String key, @NotNull String member, @NotNull SerializerFactory<T> type, DataResult.@NotNull SpecificResult<T> specificResult) {
        this.mainPipelineSupplier.getSerializableMemberData(key, member, type, specificResult);
        this.mainPipelineSupplier.sync();
    }


    public <T> void getSerializableKeyData(@NotNull String key, @NotNull SerializerFactory<T> factory, DataResult.@NotNull ListStoredSerializableResult listStoredSerializableResult) {
        this.mainPipelineSupplier.getSerializableKeyData(key, factory, listStoredSerializableResult);
        this.mainPipelineSupplier.sync();
    }


    public void removeSerializableKey(@NotNull String key, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.removeSerializableKey(key, statusResult);
        this.mainPipelineSupplier.sync();
    }


    public void removeSerializableMember(@NotNull String key, @NotNull String member, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.removeSerializableMember(key, member, statusResult);
        this.mainPipelineSupplier.sync();
    }




    public void setValue(@NotNull String key, @NotNull String member, double value, DataResult.ValueResult valueResult) {
        this.mainPipelineSupplier.setValue(key, member, value, valueResult);
        this.mainPipelineSupplier.sync();
    }


    public void addValue(@NotNull String key, @NotNull String member, double value, DataResult.ValueResult valueResult) {
        this.mainPipelineSupplier.addValue(key, member, value, valueResult);
        this.mainPipelineSupplier.sync();
    }


    public void getValueKeys(DataResult.@NotNull KeysResult keysResult) {
        this.mainPipelineSupplier.getValueKeys(keysResult);
        this.mainPipelineSupplier.sync();
    }


    public void getValueMemberKeys(@NotNull String key, DataResult.@NotNull KeysResult keysResult) {
        this.mainPipelineSupplier.getValueMemberKeys(key, keysResult);
        this.mainPipelineSupplier.sync();
    }


    public void getValueMemberData(@NotNull String key, @NotNull String member, DataResult.@NotNull ValueResult valueResult) {
        this.mainPipelineSupplier.getValueMemberData(key, member, valueResult);
        this.mainPipelineSupplier.sync();
    }


    public void getValueKeyData(@NotNull String key, DataResult.@NotNull ValueMemberDataResult listStoredSerializableResult) {
        this.mainPipelineSupplier.getValueKeyData(key, listStoredSerializableResult);
        this.mainPipelineSupplier.sync();
    }


    public void removeValueKey(@NotNull String key, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.removeValueKey(key, statusResult);
        this.mainPipelineSupplier.sync();
    }


    public void removeValueMember(@NotNull String key, @NotNull String member, DataResult.ValueResult valueResult) {
        this.mainPipelineSupplier.removeValueMember(key, member, valueResult);
        this.mainPipelineSupplier.sync();
    }


    public void getValueInfo(@NotNull String key, boolean withKeys, DataResult.@NotNull ValuesInfoResult valuesInfoResult) {
        this.mainPipelineSupplier.getValueInfo(key, withKeys, valuesInfoResult);
        this.mainPipelineSupplier.sync();
    }


    public void getValueLowestTop(@NotNull String key, int maxMembers, DataResult.@NotNull SortedValueMemberDataResult sortedValueMemberDataResult) {
       this.mainPipelineSupplier.getValueLowestTop(key, maxMembers, sortedValueMemberDataResult);
       this.mainPipelineSupplier.sync();
    }


    public void getValueHighestTop(@NotNull String key, int maxMembers, DataResult.@NotNull SortedValueMemberDataResult sortedValueMemberDataResult) {
        this.mainPipelineSupplier.getValueHighestTop(key, maxMembers, sortedValueMemberDataResult);
        this.mainPipelineSupplier.sync();
    }





    public <T> void addList(@NotNull ListType<T> listType, @NotNull String key, @NotNull T value, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.addList(listType, key, value, statusResult);
        this.mainPipelineSupplier.sync();
    }


    public void removeListType(@NotNull ListType listType, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.removeListType(listType, statusResult);
        this.mainPipelineSupplier.sync();
    }


    public void removeListKey(@NotNull ListType listType, @NotNull String key, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.removeListKey(listType, key, statusResult);
        this.mainPipelineSupplier.sync();
    }


    public void removeListIndex(@NotNull ListType listType, @NotNull String key, int index, DataResult.ListTypeValueResult specificResult) {
        this.mainPipelineSupplier.removeListIndex(listType, key, index, specificResult);
        this.mainPipelineSupplier.sync();
    }


    public void getListKeys(@NotNull ListType listType, DataResult.@NotNull KeysResult keysResult) {
        this.mainPipelineSupplier.getListKeys(listType, keysResult);
        this.mainPipelineSupplier.sync();
    }


    public void getListTypes(DataResult.@NotNull ListTypeResult listResult) {
        this.mainPipelineSupplier.getListTypes(listResult);
        this.mainPipelineSupplier.sync();
    }


    public <T> void getListData(@NotNull ListType<T> listType, @NotNull String key, DataResult.@NotNull ListResult<T> listResult) {
        this.mainPipelineSupplier.getListData(listType, key, listResult);
        this.mainPipelineSupplier.sync();
    }


    public <T> void getListIndex(@NotNull ListType<T> listType, @NotNull String key, int index, DataResult.@NotNull ListTypeValueResult<T> specificResult) {
        this.mainPipelineSupplier.getListIndex(listType, key, index, specificResult);
        this.mainPipelineSupplier.sync();
    }


    public void getListSize(@NotNull ListType listType, @NotNull String key, DataResult.@NotNull ListSizeResult listSizeResult) {
        this.mainPipelineSupplier.getListSize(listType, key, listSizeResult);
        this.mainPipelineSupplier.sync();
    }






    public void setSimple(@NotNull String key, @NotNull String value, DataResult.TextResult textResult) {
        this.mainPipelineSupplier.setSimple(key, value, textResult);
        this.mainPipelineSupplier.sync();
    }

    public void getSimple(@NotNull String key, DataResult.@NotNull TextResult textResult) {
        this.mainPipelineSupplier.getSimple(key, textResult);
        this.mainPipelineSupplier.sync();
    }


    public void removeSimple(@NotNull String key, DataResult.TextResult textResult) {
        this.mainPipelineSupplier.removeSimple(key, textResult);
        this.mainPipelineSupplier.sync();
    }


    public void getSimpleKeys(DataResult.@NotNull KeysResult keysResult) {
        this.mainPipelineSupplier.getSimpleKeys(keysResult);
        this.mainPipelineSupplier.sync();
    }


    public void getSimpleData(DataResult.@NotNull MemberDataResult memberDataResult) {
        this.mainPipelineSupplier.getSimpleData(memberDataResult);
        this.mainPipelineSupplier.sync();
    }




    public void createNewByteArray(@NotNull String key, int size, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.createNewByteArray(key, size, statusResult);
        this.mainPipelineSupplier.sync();
    }


    public void setByteArrayIndex(@NotNull String key, int index, Byte value, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.setByteArrayIndex(key, index, value, statusResult);
        this.mainPipelineSupplier.sync();
    }


    public void getByteArrayIndex(@NotNull String key, int index, DataResult.@NotNull ByteResult byteResult) {
        this.mainPipelineSupplier.getByteArrayIndex(key, index, byteResult);
        this.mainPipelineSupplier.sync();
    }


    public void getByteArrayIndexes(@NotNull String key, int[] indexes, DataResult.@NotNull KeyByteDataResult byteResult) {
        this.mainPipelineSupplier.getByteArrayIndexes(key, indexes, byteResult);
        this.mainPipelineSupplier.sync();
    }


    public void getByteArrayKeyData(@NotNull String key, DataResult.@NotNull KeyByteDataResult keyByteDataResult) {
        this.mainPipelineSupplier.getByteArrayKeyData(key, keyByteDataResult);
        this.mainPipelineSupplier.sync();
    }


    public void getByteArrayKeySize(@NotNull String key, DataResult.@NotNull ListSizeResult listSizeResult) {
        this.mainPipelineSupplier.getByteArrayKeySize(key, listSizeResult);
        this.mainPipelineSupplier.sync();
    }


    public void getByteArrayKeys(DataResult.@NotNull KeysResult keysResult) {
        this.mainPipelineSupplier.getByteArrayKeys(keysResult);
        this.mainPipelineSupplier.sync();
    }


    public void removeByteArrayKey(@NotNull String key, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.removeByteArrayKey(key, statusResult);
        this.mainPipelineSupplier.sync();
    }


    public void clearByteArrayKey(@NotNull String key, DataResult.StatusResult statusResult) {
        this.mainPipelineSupplier.clearByteArrayKey(key, statusResult);
        this.mainPipelineSupplier.sync();
    }

}
