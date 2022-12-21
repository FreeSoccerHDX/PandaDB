package de.freesoccerhdx.pandadb.clientutils;

import de.freesoccerhdx.pandadb.DataResult;
import de.freesoccerhdx.pandadb.ListType;

public interface ClientCommands {

    public static interface SerializerFactory<T> {
        T create();
    }

    public void setText(String key, String member, String value, DataResult.StatusResult statusResult);
    public void getTextKeys(DataResult.KeysResult keysResult);
    public void getTextMemberKeys(String key, DataResult.KeysResult keysResult);
    public void getTextMemberData(String key, String member, DataResult.TextResult textResult);
    public void getTextKeyData(String key, DataResult.MemberDataResult memberDataResult);
    public void removeTextKey(String key, DataResult.StatusResult statusResult);
    public void removeTextMember(String key, String member, DataResult.TextResult textResult);


    public <T> void setSerializable(String key, String member, PandaDataSerializer<T> serializer, DataResult.StatusResult statusResult);
    public void getSerializableKeys(DataResult.KeysResult keysResult);
    public void getSerializableMemberKeys(String key, DataResult.KeysResult keysResult);
    public <T> void getSerializableMemberData(String key, String member, SerializerFactory<T> type, DataResult.SpecificResult<T> specificResult);
    public <T> void getSerializableKeyData(String key, SerializerFactory<T> factory, DataResult.ListStoredSerializableResult listStoredSerializableResult);
    public void removeSerializableKey(String key, DataResult.StatusResult statusResult);
    public void removeSerializableMember(String key, String member, DataResult.StatusResult statusResult);


    public void setValue(String key, String member, double value, DataResult.ValueResult valueResult);
    public void addValue(String key, String member, double value, DataResult.ValueResult valueResult);
    public void getValueKeys(DataResult.KeysResult keysResult);
    public void getValueMemberKeys(String key, DataResult.KeysResult keysResult);
    public void getValueMemberData(String key, String member, DataResult.ValueResult valueResult);
    public void getValueKeyData(String key, DataResult.ValueMemberDataResult listStoredSerializableResult);
    public void removeValueKey(String key, DataResult.StatusResult statusResult);
    public void removeValueMember(String key, String member, DataResult.ValueResult valueResult);
    public void getValueInfo(String key, boolean withKeys, DataResult.ValuesInfoResult valuesInfoResult);



    public void addList(ListType listType, String key, Object value, DataResult.StatusResult statusResult);
    public void removeListtype(ListType listType, DataResult.StatusResult statusResult);
    public void removeListKey(ListType listType, String key, DataResult.StatusResult statusResult);
    public <T> void removeListIndex(ListType<T> listType, String key, int index, DataResult.ListTypeValueResult<T> specificResult);
    public void getListKeys(ListType listType, DataResult.KeysResult keysResult);
    public void getListTypes(DataResult.ListTypeResult listResult);
    public <T> void getListData(ListType<T> listType, String key, DataResult.ListResult<T> listResult);
    public <T> void getListIndex(ListType<T> listType, String key, int index, DataResult.ListTypeValueResult<T> specificResult);
    public <T> void getListSize(ListType<T> listType, String key, DataResult.ListSizeResult sizeResult);



}
