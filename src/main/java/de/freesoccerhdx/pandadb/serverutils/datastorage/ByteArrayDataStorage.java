package de.freesoccerhdx.pandadb.serverutils.datastorage;

import de.freesoccerhdx.pandadb.ServerDataStorage;

public class ByteArrayDataStorage extends ArrayDataStorage<Byte> {


    public ByteArrayDataStorage(ServerDataStorage serverDataStorage) {
        super(serverDataStorage);
    }

/*
    public Status createNewArray(String key, int size) {
        if(containsKey(key)) {
            return Status.KEY_ALREADY_EXISTS;
        }else{
            Byte[] bytes = new Byte[size];
            put(key, bytes);
            serverDataStorage.needSave();
            return Status.SUCCESSFUL_CREATED_NEW;
        }
    }


    public Status setIndex(String key, int index, byte value) {
        Byte[] bytes = get(key);
        if(bytes == null) {
            return Status.KEY_NOT_FOUND;
        }else{
            if(index >= bytes.length) {
                return Status.INDEX_NOT_FOUND;
            }else{
                bytes[index] = value;
                serverDataStorage.needSave();
                return Status.SUCCESSFUL_SET;
            }
        }
    }

    public Pair<Status,Byte> getIndex(String key, int index) {
        Byte[] bytes = get(key);
        if(bytes == null) {
            return Pair.of(Status.KEY_NOT_FOUND, null);
        }else{
            if(index >= bytes.length) {
                return Pair.of(Status.INDEX_NOT_FOUND, null);
            }else{
                return Pair.of(Status.SUCCESSFUL_GET_DATA, bytes[index]);
            }
        }
    }

    public Pair<Status,Byte[]> getIndexes(String key, int[] indexes) {
        Byte[] bytes = get(key);
        if(bytes == null) {
            return Pair.of(Status.KEY_NOT_FOUND, null);
        }else{
            Byte[] data = new Byte[indexes.length];
            for(int i = 0; i < indexes.length; i++) {
                int index = indexes[i];
                if (index >= bytes.length) {
                    return Pair.of(Status.INDEX_NOT_FOUND, null);
                } else {
                    data[i] = bytes[index];
                }
            }
            return Pair.of(Status.SUCCESSFUL_GET_DATA, data);
        }
    }

    public Pair<Status,Byte[]> getKeyData(String key) {
        Byte[] bytes = get(key);
        if(bytes == null) {
            return Pair.of(Status.KEY_NOT_FOUND, null);
        }else{
            return Pair.of(Status.SUCCESSFUL_GET_DATA, bytes);
        }
    }

    public Pair<Status,Integer> getKeySize(String key) {
        Byte[] bytes = get(key);
        if(bytes == null) {
            return Pair.of(Status.KEY_NOT_FOUND, null);
        }else{
            return Pair.of(Status.SUCCESSFUL_GET_DATA, bytes.length);
        }
    }

    public Pair<Status, ArrayList<String>> getKeys() {
        return size() == 0 ? new Pair<>(Status.NO_KEYS_AVAILABLE, null) : new Pair<>(Status.SUCCESSFUL_GET_KEYS, new ArrayList<>(keySet()));
    }

    public Status deleteKey(String key) {
        if(containsKey(key)) {
            remove(key);
            serverDataStorage.needSave();
            return Status.SUCCESSFUL_REMOVED_KEY;
        }else{
            return Status.KEY_NOT_FOUND;
        }
    }

    public Status clearKey(String key) {
        Byte[] bytes = get(key);
        if(bytes == null) {
            return Status.KEY_NOT_FOUND;
        }else{
            put(key, new Byte[bytes.length]);
            serverDataStorage.needSave();
            return Status.SUCCESSFUL_CLEARED;
        }
    }
*/


}
