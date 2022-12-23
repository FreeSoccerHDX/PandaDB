package de.freesoccerhdx.pandadb.clientutils;

import de.freesoccerhdx.pandadb.DataResult;
import de.freesoccerhdx.pandadb.ListType;
import de.freesoccerhdx.pandadb.serverlisteners.MemberValueDataStorage;
import de.freesoccerhdx.pandadb.PandaClient;
import de.freesoccerhdx.pandadb.PipelineSupplier;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockingPipelineSupplier {

    private final HashMap<Integer, Object> results = new HashMap<>();
    private final AtomicInteger reusltId = new AtomicInteger(0);
    private final PandaClient client;
    private final PipelineSupplier pipelineSupplier;

    public BlockingPipelineSupplier(PandaClient pandaClient) {
        this.client = pandaClient;
        pipelineSupplier = client.createPipelineSupplier();
    }

    private int getID() {
        reusltId.compareAndSet(Integer.MAX_VALUE-1, 0);
        return reusltId.getAndIncrement();
    }

    private Object waitForResult(int id, long maxMillis) {
        long maxTime = System.currentTimeMillis() + maxMillis;
        while(!results.containsKey(id) && System.currentTimeMillis() <= maxTime) {
            /*try {
                Thread.sleep(0, 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
        return results.remove(id);
    }

    public Pair<Status,String> setText(String key, String member, String value, long maxMillis) {
        int id = getID();
        DataResult.TextResult dataresult = new DataResult.TextResult() {
            @Override
            public void result(String data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.setText(key, member, value, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, String>) result;
    }

    public Pair<Status, List<String>> getTextKeys(long maxMillis) {
        int id = getID();
        DataResult.KeysResult keysResult = new DataResult.KeysResult() {
            @Override
            public void resultList(ArrayList<String> data, Status status) {
                results.put(id, new Pair<>(status, data));
            }
        };
        pipelineSupplier.getTextKeys(keysResult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, List<String>>) result;
    }

    public Pair<Status, List<String>> getTextMemberKeys(String key, long maxMillis) {
        int id = getID();
        DataResult.KeysResult keysResult = new DataResult.KeysResult() {
            @Override
            public void resultList(ArrayList<String> data, Status status) {
                results.put(id, new Pair<>(status, data));
            }
        };
        pipelineSupplier.getTextMemberKeys(key, keysResult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, List<String>>) result;
    }

    public Pair<Status, String> getTextMemberData(String key, String member, long maxMillis) {
        int id = getID();
        DataResult.TextResult textResult = new DataResult.TextResult() {
            @Override
            public void result(String data, Status status) {
                results.put(id, new Pair<>(status, data));
            }
        };
        pipelineSupplier.getTextMemberData(key, member, textResult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, String>) result;
    }

    public Pair<Status,HashMap<String,String>> getTextKeyData(String key, long maxMillis) {
        int id = getID();
        DataResult.MemberDataResult memberDataResult = new DataResult.MemberDataResult() {
            @Override
            public void resultData(HashMap<String, String> memberData, Status status) {
                results.put(id, new Pair<>(status, memberData));
            }
        };
        pipelineSupplier.getTextKeyData(key, memberDataResult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status,HashMap<String,String>>) result;
    }

    public Status removeTextKey(String key, long maxMillis) {
        int id = getID();
        DataResult.StatusResult dataresult = new DataResult.StatusResult() {
            @Override
            public void result(Status status) {
                results.put(id, status);
            }
        };
        pipelineSupplier.removeTextKey(key, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Status) result;
    }

    public Pair<Status,String> removeTextMember(String key, String member, long maxMillis) {
        int id = getID();
        DataResult.TextResult dataresult = new DataResult.TextResult() {
            @Override
            public void result(String data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.removeTextMember(key, member, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, String>) result;
    }






    public Pair<Status,Double> setValue(String key, String member, double value, long maxMillis) {
        int id = getID();
        DataResult.ValueResult dataresult = new DataResult.ValueResult() {
            @Override
            public void result(Double data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.setValue(key, member, value, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, Double>) result;
    }

    public Pair<Status,Double> addValue(String key, String member, double value, long maxMillis) {
        int id = getID();
        DataResult.ValueResult dataresult = new DataResult.ValueResult() {
            @Override
            public void result(Double data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.addValue(key, member, value, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, Double>) result;
    }

    public Pair<Status,List<String>> getValueKeys(long maxMillis) {
        int id = getID();
        DataResult.KeysResult dataresult = new DataResult.KeysResult() {
            @Override
            public void resultList(ArrayList<String> data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.getValueKeys(dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, List<String>>) result;
    }

    public Pair<Status,List<String>> getValueMemberKeys(String key, long maxMillis) {
        int id = getID();
        DataResult.KeysResult dataresult = new DataResult.KeysResult() {
            @Override
            public void resultList(ArrayList<String> data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.getValueMemberKeys(key, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, List<String>>) result;
    }

    public  Pair<Status,Double> getValueMemberData(String key, String member, long maxMillis) {
        int id = getID();
        DataResult.ValueResult dataresult = new DataResult.ValueResult() {
            @Override
            public void result(Double data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.getValueMemberData(key, member, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, Double>) result;
    }

    public Pair<Status,HashMap<String,Double>> getValueKeyData(String key, long maxMillis) {
        int id = getID();
        DataResult.ValueMemberDataResult dataresult = new DataResult.ValueMemberDataResult() {
            @Override
            public void result(HashMap<String, Double> object, Status status) {
                results.put(id, Pair.of(status,object));
            }
        };
        pipelineSupplier.getValueKeyData(key, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, HashMap<String, Double>>) result;
    }

    public Status removeValueKey(String key, long maxMillis) {
        int id = getID();
        DataResult.StatusResult dataresult = new DataResult.StatusResult() {
            @Override
            public void result(Status status) {
                results.put(id, status);
            }
        };
        pipelineSupplier.removeValueKey(key, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Status) result;
    }

    public Pair<Status,Double> removeValueMember(String key, String member, long maxMillis) {
        int id = getID();
        DataResult.ValueResult dataresult = new DataResult.ValueResult() {
            @Override
            public void result(Double data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.removeValueMember(key, member, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, Double>) result;
    }

    public Pair<Status, MemberValueDataStorage.ValueMembersInfo> getValueInfo(String key, boolean withKeys, long maxMillis) {
        int id = getID();
        DataResult.ValuesInfoResult dataresult = new DataResult.ValuesInfoResult() {
            @Override
            public void result(MemberValueDataStorage.ValueMembersInfo data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.getValueInfo(key, withKeys, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, MemberValueDataStorage.ValueMembersInfo>) result;
    }


    public Pair<Status,Pair<String,Double>[]> getValueLowestTop(String key, int maxMembers, long maxMillis) {
        int id = getID();
        DataResult.SortedValueMemberDataResult sortedValueMemberDataResult = new DataResult.SortedValueMemberDataResult() {
            @Override
            public void result(Pair<String, Double>[] sorted, Status status) {
                results.put(id, Pair.of(status,sorted));
            }
        };
        pipelineSupplier.getValueLowestTop(key, maxMembers, sortedValueMemberDataResult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, Pair<String, Double>[]>) result;
    }


    public Pair<Status,Pair<String,Double>[]> getValueHighestTop(String key, int maxMembers, long maxMillis) {
        int id = getID();
        DataResult.SortedValueMemberDataResult sortedValueMemberDataResult = new DataResult.SortedValueMemberDataResult() {
            @Override
            public void result(Pair<String, Double>[] sorted, Status status) {
                results.put(id, Pair.of(status,sorted));
            }
        };
        pipelineSupplier.getValueHighestTop(key, maxMembers, sortedValueMemberDataResult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, Pair<String, Double>[]>) result;
    }












    public <T> Status setSerializable(String key, String member, PandaDataSerializer<T> serializer, long maxMillis) {
        int id = getID();
        DataResult.StatusResult dataresult = new DataResult.StatusResult() {
            @Override
            public void result(Status status) {
                results.put(id, status);
            }
        };
        pipelineSupplier.setSerializable(key, member, serializer, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Status) result;
    }

    public Pair<Status,ArrayList<String>> getSerializableKeys(long maxMillis) {
        int id = getID();
        DataResult.KeysResult dataresult = new DataResult.KeysResult() {
            @Override
            public void resultList(ArrayList<String> data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.getSerializableKeys(dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, ArrayList<String>>) result;
    }

    public Pair<Status,ArrayList<String>> getSerializableMemberKeys(String key, long maxMillis) {
        int id = getID();
        DataResult.KeysResult dataresult = new DataResult.KeysResult() {
            @Override
            public void resultList(ArrayList<String> data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.getSerializableMemberKeys(key, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, ArrayList<String>>) result;
    }

    public <T> Pair<Status,T> getSerializableMemberData(String key, String member, ClientCommands.SerializerFactory<T> type, long maxMillis) {
        int id = getID();
        DataResult.SpecificResult<T> dataresult = new DataResult.SpecificResult<T>() {
            @Override
            public void result(T object, Status status) {
                results.put(id, Pair.of(status,object));
            }
        };
        pipelineSupplier.getSerializableMemberData(key, member, type, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, T>) result;
    }

    public <T> Pair<Status, HashMap<String, T>> getSerializableKeyData(String key, ClientCommands.SerializerFactory<T> factory, long maxMillis) {
        int id = getID();
        DataResult.ListStoredSerializableResult<T> dataresult = new DataResult.ListStoredSerializableResult<>() {
            @Override
            public void result(HashMap<String, T> object, Status status) {
                results.put(id, Pair.of(status, object));
            }

        };
        pipelineSupplier.getSerializableKeyData(key, factory, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, HashMap<String, T>>) result;
    }

    public Status removeSerializableKey(String key, long maxMillis) {
        int id = getID();
        DataResult.StatusResult dataresult = new DataResult.StatusResult() {
            @Override
            public void result(Status status) {
                results.put(id, status);
            }
        };
        pipelineSupplier.removeSerializableKey(key, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Status) result;
    }

    public Status removeSerializableMember(String key, String member, long maxMillis) {
        int id = getID();
        DataResult.StatusResult dataresult = new DataResult.StatusResult() {
            @Override
            public void result(Status status) {
                results.put(id, status);
            }
        };
        pipelineSupplier.removeSerializableMember(key, member, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Status) result;
    }












    public <T> Status addList(ListType<T> listType, String key, T value, long maxMillis) {
        int id = getID();
        DataResult.StatusResult dataresult = new DataResult.StatusResult() {
            @Override
            public void result(Status status) {
                results.put(id, status);
            }
        };
        pipelineSupplier.addList(listType, key, value, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Status) result;
    }

    public Status removeListtype(ListType listType, long maxMillis) {
        int id = getID();
        DataResult.StatusResult dataresult = new DataResult.StatusResult() {
            @Override
            public void result(Status status) {
                results.put(id, status);
            }
        };
        pipelineSupplier.removeListtype(listType, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Status) result;
    }

    public Status removeListKey(ListType listType, String key, long maxMillis) {
        int id = getID();
        DataResult.StatusResult dataresult = new DataResult.StatusResult() {
            @Override
            public void result(Status status) {
                results.put(id, status);
            }
        };
        pipelineSupplier.removeListKey(listType, key, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Status) result;
    }

    public <T> Pair<Status,T> removeListIndex(ListType<T> listType, String key, int index, long maxMillis) {
        int id = getID();
        DataResult.ListTypeValueResult dataresult = new DataResult.ListTypeValueResult() {
            @Override
            public void result(Object data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.removeListIndex(listType, key, index, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, T>) result;
    }

    public Pair<Status, ArrayList<String>> getListKeys(ListType listType, long maxMillis) {
        int id = getID();
        DataResult.KeysResult dataresult = new DataResult.KeysResult() {
            @Override
            public void resultList(ArrayList<String> data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.getListKeys(listType, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, ArrayList<String>>) result;
    }

    public Pair<Status,ArrayList<ListType>> getListTypes(long maxMillis) {
        int id = getID();
        DataResult.ListTypeResult dataresult = new DataResult.ListTypeResult() {
            @Override
            public void resultList(ArrayList<ListType> list, Status status) {
                results.put(id, Pair.of(status,list));
            }
        };
        pipelineSupplier.getListTypes(dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, ArrayList<ListType>>) result;
    }

    public <T> Pair<Status,ArrayList<T>> getListData(ListType<T> listType, String key, long maxMillis) {
        int id = getID();
        DataResult.ListResult<T> dataresult = new DataResult.ListResult<T>() {
            @Override
            public void resultList(ArrayList<T> data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.getListData(listType, key, dataresult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, ArrayList<T>>) result;
    }

    public <T> Pair<Status,T> getListIndex(ListType<T> listType, String key, int index, long maxMillis) {
        int id = getID();
        DataResult.ListTypeValueResult<T> specificResult = new DataResult.ListTypeValueResult<T>() {
            @Override
            public void result(T data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.getListIndex(listType, key, index, specificResult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, T>) result;
    }

    public Pair<Status,Integer> getListSize(ListType listType, String key, long maxMillis) {
        int id = getID();
        DataResult.ListSizeResult sizeResult = new DataResult.ListSizeResult() {
            @Override
            public void result(Integer size, Status status) {
                results.put(id, Pair.of(status,size));
            }
        };
        pipelineSupplier.getListSize(listType, key, sizeResult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, Integer>) result;
    }




    public Pair<Status,String> setSimple(String key, String value, long maxMillis) {
        int id = getID();
        DataResult.TextResult textResult = new DataResult.TextResult() {
            @Override
            public void result(String data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.setSimple(key, value, textResult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, String>) result;
    }

    public Pair<Status,String> getSimple(String key, long maxMillis) {
        int id = getID();
        DataResult.TextResult textResult = new DataResult.TextResult() {
            @Override
            public void result(String data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.getSimple(key, textResult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, String>) result;
    }

    public Pair<Status,String> removeSimple(String key, long maxMillis) {
        int id = getID();
        DataResult.TextResult textResult = new DataResult.TextResult() {
            @Override
            public void result(String data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.removeSimple(key, textResult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, String>) result;
    }

    public Pair<Status,ArrayList<String>> getSimpleKeys(long maxMillis) {
        int id = getID();
        DataResult.KeysResult keysResult = new DataResult.KeysResult() {
            @Override
            public void resultList(ArrayList<String> data, Status status) {
                results.put(id, Pair.of(status,data));
            }
        };
        pipelineSupplier.getSimpleKeys(keysResult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, ArrayList<String>>) result;
    }

    public Pair<Status,HashMap<String,String>> getSimpleData(long maxMillis) {
        int id = getID();
        DataResult.MemberDataResult memberDataResult = new DataResult.MemberDataResult() {
            @Override
            public void resultData(HashMap<String, String> memberData, Status status) {
                results.put(id, Pair.of(status,memberData));
            }
        };
        pipelineSupplier.getSimpleData(memberDataResult);
        pipelineSupplier.sync();
        Object result = waitForResult(id, maxMillis);
        return result == null ? null : (Pair<Status, HashMap<String, String>>) result;
    }








}
