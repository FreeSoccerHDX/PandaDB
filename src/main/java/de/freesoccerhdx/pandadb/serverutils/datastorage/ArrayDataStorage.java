package de.freesoccerhdx.pandadb.serverutils.datastorage;

import de.freesoccerhdx.pandadb.ServerDataStorage;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class ArrayDataStorage<T> extends HashMap<String, T[]> {

    private final ServerDataStorage serverDataStorage;

    public ArrayDataStorage(ServerDataStorage serverDataStorage) {
        this.serverDataStorage = serverDataStorage;
    }

    public ServerDataStorage getServerDataStorage() {
        return serverDataStorage;
    }

    public Status createNewArray(String key, int size) {
        if(containsKey(key)) {
            return Status.KEY_ALREADY_EXISTS;
        }else{
            T[] bytes = (T[]) new Object[size];
            put(key, bytes);
            serverDataStorage.needSave();
            return Status.SUCCESSFUL_CREATED_NEW;
        }
    }

    public Status setIndex(String key, int index, T value) {
        T[] bytes = get(key);
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

    public Pair<Status,T> getIndex(String key, int index) {
        T[] bytes = get(key);
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

    public Pair<Status,T[]> getIndexes(String key, int[] indexes) {
        T[] bytes = get(key);
        if(bytes == null) {
            return Pair.of(Status.KEY_NOT_FOUND, null);
        }else{
            T[] data = (T[]) new Object[indexes.length];
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

    public Pair<Status,T[]> getKeyData(String key) {
        T[] bytes = get(key);
        if(bytes == null) {
            return Pair.of(Status.KEY_NOT_FOUND, null);
        }else{
            return Pair.of(Status.SUCCESSFUL_GET_DATA, bytes);
        }
    }

    public Pair<Status,Integer> getKeySize(String key) {
        T[] bytes = get(key);
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
        T[] bytes = get(key);
        if(bytes == null) {
            return Status.KEY_NOT_FOUND;
        }else{
            put(key, (T[]) new Object[bytes.length]);
            serverDataStorage.needSave();
            return Status.SUCCESSFUL_CLEARED;
        }
    }



}
