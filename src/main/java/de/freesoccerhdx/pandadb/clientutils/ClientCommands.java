package de.freesoccerhdx.pandadb.clientutils;

import de.freesoccerhdx.pandadb.DataResult;
import de.freesoccerhdx.pandadb.ListType;

public interface ClientCommands {

    interface SerializerFactory<T> {
        T create();
    }

    void setText(String key, String member, String value, DataResult.StatusResult statusResult);
    void getTextKeys(DataResult.KeysResult keysResult);
    void getTextMemberKeys(String key, DataResult.KeysResult keysResult);
    void getTextMemberData(String key, String member, DataResult.TextResult textResult);
    void getTextKeyData(String key, DataResult.MemberDataResult memberDataResult);
    void removeTextKey(String key, DataResult.StatusResult statusResult);
    void removeTextMember(String key, String member, DataResult.TextResult textResult);


    <T> void setSerializable(String key, String member, PandaDataSerializer<T> serializer, DataResult.StatusResult statusResult);
    void getSerializableKeys(DataResult.KeysResult keysResult);
    void getSerializableMemberKeys(String key, DataResult.KeysResult keysResult);
    <T> void getSerializableMemberData(String key, String member, SerializerFactory<T> type, DataResult.SpecificResult<T> specificResult);
    <T> void getSerializableKeyData(String key, SerializerFactory<T> factory, DataResult.ListStoredSerializableResult listStoredSerializableResult);
    void removeSerializableKey(String key, DataResult.StatusResult statusResult);
    void removeSerializableMember(String key, String member, DataResult.StatusResult statusResult);


    void setValue(String key, String member, double value, DataResult.ValueResult valueResult);
    void addValue(String key, String member, double value, DataResult.ValueResult valueResult);
    void getValueKeys(DataResult.KeysResult keysResult);
    void getValueMemberKeys(String key, DataResult.KeysResult keysResult);
    void getValueMemberData(String key, String member, DataResult.ValueResult valueResult);
    void getValueKeyData(String key, DataResult.ValueMemberDataResult listStoredSerializableResult);
    void removeValueKey(String key, DataResult.StatusResult statusResult);
    void removeValueMember(String key, String member, DataResult.ValueResult valueResult);
    void getValueInfo(String key, boolean withKeys, DataResult.ValuesInfoResult valuesInfoResult);



    <T> void addList(ListType<T> listType, String key, T value, DataResult.StatusResult statusResult);
    void removeListtype(ListType listType, DataResult.StatusResult statusResult);
    void removeListKey(ListType listType, String key, DataResult.StatusResult statusResult);
    <T> void removeListIndex(ListType<T> listType, String key, int index, DataResult.ListTypeValueResult<T> specificResult);
    void getListKeys(ListType listType, DataResult.KeysResult keysResult);
    void getListTypes(DataResult.ListTypeResult listResult);
    <T> void getListData(ListType<T> listType, String key, DataResult.ListResult<T> listResult);
    <T> void getListIndex(ListType<T> listType, String key, int index, DataResult.ListTypeValueResult<T> specificResult);
    <T> void getListSize(ListType<T> listType, String key, DataResult.ListSizeResult sizeResult);



}
