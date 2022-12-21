package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.ServerDataStorage;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TextsDataStorage extends HashMap<String, HashMap<String, String>>{

    private final ServerDataStorage serverDataStorage;
    public TextsDataStorage(ServerDataStorage serverDataStorage){
        this.serverDataStorage = serverDataStorage;
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
        return size() == 0 ? new Pair<>(Status.NO_KEYS_AVAILABLE, null) : new Pair<>(Status.SUCCESSFUL, new ArrayList<>(keySet()));
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

    /**
     * Gets the stored text for the specific key and member
     *
     * @return Pair<Status(KEY_NOT_FOUND/MEMBER_NOT_FOUND/SUCCESSFUL), StoredString>
     * */
    public Pair<Status,String> getMemberData(String key, String member) {
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



    public Pair<Status,HashMap<String,String>> getKeyData(String key){
        Status status = Status.KEY_NOT_FOUND;
        HashMap<String, String> memberData = get(key);

        if(memberData != null){
            status = Status.SUCCESSFUL;
        }

        return Pair.of(status,memberData);
    }

    /**
     * Removes the key with all members
     *
     * @return Status(KEY_NOT_FOUND / SUCCESSFUL_REMOVED_KEY)
     */
    public Status removeKey(String key) {
        Status status = this.remove(key) == null ? Status.KEY_NOT_FOUND : Status.SUCCESSFUL_REMOVED_KEY;
        if(status == Status.SUCCESSFUL_REMOVED_KEY){
            serverDataStorage.needSave();
        }
        return status;
    }

    /**
     * Removes the member from the key with its value
     *
     * @return Pair<StoredString, Status(SUCCESSFUL_REMOVED_MEMBER / MEMBER_NOT_FOUND / KEY_NOT_FOUND)>
     */
    public Pair<Status,String> removeMember(String key, String member) {
        HashMap<String, String> keymap = this.get(key);
        if(keymap != null){
            String oldvalue = keymap.remove(member);
            boolean erfolg = oldvalue != null;

            if(keymap.size() == 0){
                this.remove(key);
            }
            if(erfolg){
                serverDataStorage.needSave();
                return Pair.of(Status.SUCCESSFUL_REMOVED_MEMBER,oldvalue);
            }

            return Pair.of(Status.MEMBER_NOT_FOUND, null);
        }

        return Pair.of(Status.KEY_NOT_FOUND, null);
    }






}
