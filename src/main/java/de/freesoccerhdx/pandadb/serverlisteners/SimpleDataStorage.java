package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class SimpleDataStorage extends HashMap<String,String> {

    public Pair<Status,String> setSimple(String key, String value) {
        String oldvalue = this.put(key, value);
        if(oldvalue != null) {
            return new Pair<>(Status.SUCCESSFUL_OVERWRITE_OLD, oldvalue);
        }
        return Pair.of(Status.SUCCESSFUL_CREATED_NEW, null);
    }

    public Pair<Status,String> getSimple(String key) {
        return this.containsKey(key) ? Pair.of(Status.SUCCESSFUL, this.get(key)) : Pair.of(Status.KEY_NOT_FOUND, null);
    }

    public Pair<Status,String> removeSimple(String key) {
        String removed = this.remove(key);
        return removed != null ? Pair.of(Status.SUCCESSFUL, removed) : Pair.of(Status.KEY_NOT_FOUND, null);
    }

    public Pair<Status, ArrayList<String>> getSimpleKeys() {
        if(this.size() > 0) {
            return Pair.of(Status.SUCCESSFUL, new ArrayList<>(this.keySet()));
        }else {
            return Pair.of(Status.NO_KEYS_AVAILABLE, null);
        }
    }

    public Pair<Status, HashMap<String, String>> getSimpleData() {
        if(this.size() > 0) {
            return (Pair<Status, HashMap<String, String>>) Pair.of(Status.SUCCESSFUL, ((HashMap<String,String>)this.clone()));
        }else {
            return Pair.of(Status.NO_KEYS_AVAILABLE, null);
        }
    }
}
