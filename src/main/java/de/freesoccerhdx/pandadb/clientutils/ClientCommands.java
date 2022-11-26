package de.freesoccerhdx.pandadb.clientutils;

import de.freesoccerhdx.pandadb.DataResult;
import de.freesoccerhdx.pandadb.ListType;

public interface ClientCommands {

    public static interface SerializerFactory<T> {
        T create();
    }

    public void getMemberData(String key, DataResult.MemberDataResult memberData);

    public <T> void getSerializableMemberData(String key, SerializerFactory<T> factory, DataResult.ListStoredSerializableResult<T> memberData);

    public void getSerializableKeys(DataResult.KeysResult keysResult);

    public void getSerializableMemberKeys(String key, DataResult.KeysResult keysResult);

    public <T> void storeSerializable(String key, String member, PandaDataSerializer<T> serializer, DataResult.StatusResult statusResult);

    public <T> void getStoredSerializable(String key, String member, SerializerFactory<T> type, DataResult.StoredSerializableResult<T> serializableResult);

    /**
     * Adds a specific value to a ListType withing a Key with the Listkey
     * */
    public <T> void addListEntry(String key, String listkey, ListType listType, T value, DataResult.StatusResult addListResult);

    /**
     * Gets the specific list of the listtype is the same and the list exist or else null
     * */
    public <T> void getList(String key, String listkey, ListType listType, DataResult.ListResult<T> listResult);

    /**
     * Gets a specific text or null if not exist
     * */
    public void get(String key, String member, DataResult.TextResult textResult);

    /**
     * Sets a specific text
     * */
    public void set(String key, String member, String value, DataResult.StatusResult setResult);

    /**
     * Gets the specific value or null if not set
     * */
    public void getValue(String key, String member, DataResult.ValueResult future);

    /**
     * Sets the specific value
     * */
    public void setValue(String key, String member, double value, DataResult.ValueResult valueResult);

    /**
     * Add/Subtract the specific value
     * */
    public void addValue(String key, String member, double value, DataResult.ValueResult valueResult);

    /**
     * Remove a specific value
     * */
    public void removeValue(String key, String member, DataResult.StatusResult removeResult);

    /**
     * Remove a specific set text
     * */
    public void remove(String key, String member, DataResult.StatusResult removeResult);

    /**
     * Remove a specific list within a key
     * */
    public <T> void removeList(String key, String listkey, ListType listType, DataResult.StatusResult removeResult);

    /**
     * Remove a specific key that stores lists
     * */
    public <T> void removeList(String key, ListType listType, DataResult.StatusResult removeResult);

    /**
     * Remove a specific index within a list of an key
     * */
    public <T> void removeListIndex(String key, String listkey, ListType listType, int index, DataResult.StatusResult removeResult);

    /**
     * Gets all Keys stored under the specific ListType
     * */
    public <T> void getListKeys(ListType listType, DataResult.KeysResult keysResult);

    /**
     * Gets all ListKeys stored under the specific key
     * */
    public <T> void getListKeys(String key, ListType listType, DataResult.KeysResult keysResult);

    /**
     * Gets all Text-Keys stored under the key
     * */
    public void getKeys(String key, DataResult.KeysResult keysResult);

    /**
     * Gets all Text-Keys stored
     * */
    public void getKeys(DataResult.KeysResult keysResult);

    /**
     * Gets all Value-Keys stored
     * */
    public void getValueKeys(DataResult.KeysResult keysResult);

    /**
     * Gets all Value-Keys stored under the key
     * */
    public void getValueKeys(String key, DataResult.KeysResult keysResult);

    /**
     * Gets a small info about the current state of the values from the members under a specific key
     * */
    public void getValuesMemberInfo(String key, boolean withKeys, DataResult.ValuesInfoResult valuesInfoResult);

}
