package de.freesoccerhdx.pandadb.serverutils.datastorage;

import de.freesoccerhdx.pandadb.ServerDataStorage;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class SimpleDataStorage extends HashMap<String,String> {

    private final ServerDataStorage serverDataStorage;
    public SimpleDataStorage(ServerDataStorage serverDataStorage){
        this.serverDataStorage = serverDataStorage;
    }

    public Pair<Status,String> setSimple(String key, String value) {
        String oldvalue = this.put(key, value);
        serverDataStorage.needSave();
        if(oldvalue != null) {
            return new Pair<>(Status.SUCCESSFUL_OVERWRITE_OLD, oldvalue);
        }
        return Pair.of(Status.SUCCESSFUL_CREATED_NEW, null);
    }

    public Pair<Status,String> getSimple(String key) {
        return this.containsKey(key) ? Pair.of(Status.SUCCESSFUL_GET_DATA, this.get(key)) : Pair.of(Status.KEY_NOT_FOUND, null);
    }

    public Pair<Status,String> removeSimple(String key) {
        String removed = this.remove(key);
        if(removed != null) {
            serverDataStorage.needSave();
        }
        return removed != null ? Pair.of(Status.SUCCESSFUL_REMOVED_KEY, removed) : Pair.of(Status.KEY_NOT_FOUND, null);
    }

    public Pair<Status, ArrayList<String>> getSimpleKeys() {
        if(this.size() > 0) {
            return Pair.of(Status.SUCCESSFUL_GET_KEYS, new ArrayList<>(this.keySet()));
        }else {
            return Pair.of(Status.NO_KEYS_AVAILABLE, null);
        }
    }

    public Pair<Status, HashMap<String, String>> getSimpleData() {
        if(this.size() > 0) {
            return (Pair<Status, HashMap<String, String>>) Pair.of(Status.SUCCESSFUL_GET_DATA, ((HashMap<String,String>)this.clone()));
        }else {
            return Pair.of(Status.NO_KEYS_AVAILABLE, null);
        }
    }
}
