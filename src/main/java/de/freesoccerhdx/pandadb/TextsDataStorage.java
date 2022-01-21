package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.simplesocket.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TextsDataStorage extends HashMap<String, HashMap<String, String>>{

    private ServerDataStorage serverDataStorage;
    public TextsDataStorage(ServerDataStorage serverDataStorage){
        this.serverDataStorage = serverDataStorage;
    }


    public Pair<Status,HashMap<String,String>> getMemberData(String key){
        Status status = Status.KEY_NOT_FOUND;
        HashMap<String, String> memberData = null;

        HashMap<String, String> storedData = this.get(key);
        if(storedData != null){
            memberData = storedData;
            status = Status.SUCCESSFUL;
        }

        return Pair.of(status,memberData);
    }

    public Status remove(String key, String member) {
        HashMap<String, String> keymap = this.get(key);
        if(keymap != null){
            boolean erfolg = keymap.remove(member) != null;

            if(keymap.size() == 0){
                this.remove(key);
            }
            if(erfolg){
                serverDataStorage.needSave();
                return Status.SUCCESSFUL_REMOVED_MEMBER;
            }

            return Status.MEMBER_NOT_FOUND;
        }

        return Status.KEY_NOT_FOUND;
    }

    public Pair<Status,String> get(String key, String member) {
        String text = null;
        Status status = Status.KEY_NOT_FOUND;
        try {
            HashMap<String, String> keymap = this.get(key);
            if (keymap != null) {
                text = keymap.get(member);
                if(text == null){
                    status = Status.MEMBER_NOT_FOUND;
                }else{
                    status = Status.SUCCESSFUL;
                }
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }
        return Pair.of(status,text);
    }

    public Status set(String key, String member, String value) {
        HashMap<String, String> keymap = this.get(key);
        Status status = Status.SUCCESSFUL_OVERWRITE_OLD;
        if (keymap == null) {
            keymap = new HashMap<>();
            this.put(key, keymap);
        }
        if(!keymap.containsKey(member)){
            status = Status.SUCCESSFUL_CREATED_NEW;
        }
        keymap.put(member, value);
        serverDataStorage.needSave();
        return status;
    }

    public Pair<Status, List<String>> getKeys() {
        Status status = Status.NO_KEYS_AVAILABLE;
        List<String> stringList = null;

        if(this.size() > 0){
            stringList = new ArrayList<>(this.keySet());
            status = Status.SUCCESSFUL;
        }

        return Pair.of(status,stringList);
    }

    public Pair<Status,List<String>> getMemberKeys(String key) {
        Status status = Status.KEY_NOT_FOUND;
        List<String> stringList = null;
        HashMap<String, String> textMemberKeys = this.get(key);

        if(textMemberKeys != null){
            stringList = new ArrayList<>(textMemberKeys.keySet());
            status = Status.SUCCESSFUL;
        }

        return Pair.of(status,stringList);
    }

}
