package de.freesoccerhdx.pandadb.serverutils.datastorage;

import de.freesoccerhdx.pandadb.ServerDataStorage;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValueDataStorage extends HashMap<String, MemberValueDataStorage> {
    
    private final ServerDataStorage serverDataStorage;
    public ValueDataStorage(ServerDataStorage serverDataStorage){
        this.serverDataStorage = serverDataStorage;
    }

    /**
     * Sets the value for the specific key and member
     *
     * @return Pair of Status(SUCCESSFUL_OVERWRITE_OLD/SUCCESSFUL_CREATED_NEW) and old value
     * */
    public Pair<Status,Double> setValue(String key, String member, double value) {

        MemberValueDataStorage keymap = this.get(key);
        Status status = Status.SUCCESSFUL_OVERWRITE_OLD;

        if(keymap == null){
            keymap = new MemberValueDataStorage();
            this.put(key, keymap);
        }
        if(!keymap.containsKey(member)){
            status = Status.SUCCESSFUL_CREATED_NEW;
        }

        Double oldvalue = keymap.put(member, value);
        this.serverDataStorage.needSave();

        return Pair.of(status, oldvalue);
    }

    /**
     * Adds the value for the specific key and member (and creates the new if not set before)
     *
     * @return Pair of Status(SUCCESSFUL_OVERWRITE_OLD/SUCCESSFUL_CREATED_NEW) and new value
     * */
    public Pair<Status,Double> addValue(String key, String member, double value) {
        MemberValueDataStorage keymap = this.get(key);

        if(keymap == null) {
            return Pair.of(Status.KEY_NOT_FOUND, null);
        }
        if(!keymap.containsKey(member)) {
            return Pair.of(Status.MEMBER_NOT_FOUND, null);
        }

        Double newvalue = value + keymap.get(member);
        keymap.put(member, newvalue);

        this.serverDataStorage.needSave();
        return Pair.of(Status.SUCCESSFUL_OVERWRITE_OLD, newvalue);
    }

    public Pair<Status, List<String>> getKeys() {
        Status status = Status.NO_KEYS_AVAILABLE;
        List<String> stringList = null;

        if(this.size() > 0) {
            stringList = new ArrayList<>(this.keySet());
            status = Status.SUCCESSFUL_GET_KEYS;
        }

        return Pair.of(status,stringList);
    }

    /**
     * Gets a List of Member-Keys for Values under the specific Key
     *
     * @return Pair of Status(KEY_NOT_FOUND/SUCCESSFUL) and the Keys
     * */
    public Pair<Status, List<String>> getMemberKeys(String key) {
        HashMap<String, Double> valueMemberKeys = this.get(key);
        Status status = Status.KEY_NOT_FOUND;
        List<String> stringList = null;

        if(valueMemberKeys != null){
            stringList = new ArrayList<>(valueMemberKeys.keySet());
            status = Status.SUCCESSFUL_GET_KEYS;
        }

        return Pair.of(status,stringList);
    }

    /**
     * Gets the value for the specific key and member
     *
     * @return Pair of Status(KEY_NOT_FOUND/SUCCESSFUL) and value
     * */
    public Pair<Status,Double> getMemberData(String key, String member) {

        HashMap<String, Double> keymap = this.get(key);
        Double val = null;
        Status status = Status.KEY_NOT_FOUND;

        if(keymap != null){

            val = keymap.get(member);
            if(val == null){
                status = Status.MEMBER_NOT_FOUND;
            }else{
                status = Status.SUCCESSFUL_GET_DATA;
            }
        }

        return Pair.of(status, val);
    }

    public Pair<Status,HashMap<String,Double>> getKeyData(String key){
        Status status = Status.KEY_NOT_FOUND;
        HashMap<String, Double> memberData =  this.get(key);

        if(memberData != null) {
            status = Status.SUCCESSFUL_GET_DATA;
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
     * Removes the member from the key with his value
     *
     * @return Status(SUCCESSFUL_REMOVED_MEMBER/MEMBER_NOT_FOUND/KEY_NOT_FOUND)
     * */
    public Pair<Status,Double> removeMember(String key, String member) {
        HashMap<String, Double> keymap = this.get(key);
        if(keymap != null) {
            Double oldvalue = keymap.remove(member);
            boolean erfolg = oldvalue != null;

            if(keymap.size() == 0){
                this.remove(key);
            }

            if(erfolg){
               this.serverDataStorage.needSave();
                return Pair.of(Status.SUCCESSFUL_REMOVED_MEMBER,oldvalue);
            }
            return Pair.of(Status.MEMBER_NOT_FOUND, null);
        }


        return Pair.of(Status.KEY_NOT_FOUND, null);
    }




    /**
     * Gets the ValueMembersInfo for the given Key
     *
     * @return Pair of Status(KEY_NOT_FOUND/SUCCESSFUL) and the ValueMembersInfo
     * */
    public Pair<Status, MemberValueDataStorage.ValueMembersInfo> getValuesInfo(String key, boolean withMembers) {
        MemberValueDataStorage valueDataStorage = this.get(key);
        MemberValueDataStorage.ValueMembersInfo valueMembersInfo = null;
        Status status = Status.KEY_NOT_FOUND;

        if(valueDataStorage != null){
            valueMembersInfo = valueDataStorage.getInfo(withMembers);
            status = Status.SUCCESSFUL_GET_DATA;
        }

        return Pair.of(status,valueMembersInfo);
    }

    public Pair<Status, Pair<String, Double>[]> getLowestTop(String key, int maxMember) {
        HashMap<String, Double> memberMap = this.get(key);
        if(memberMap == null) {
            return Pair.of(Status.KEY_NOT_FOUND, null);
        }
        memberMap = (HashMap<String, Double>) memberMap.clone();
        Pair<String,Double>[] members = new Pair[maxMember < 0 ? memberMap.size() : maxMember];
        // Sort the memberMap by value and start with the lowest
        ArrayList<String> usedMembers = new ArrayList<>();

        for(int i = 0; i < members.length; i++) {
            double lowest = Double.MAX_VALUE;
            String lowestMember = null;
            for(Map.Entry<String, Double> entry : memberMap.entrySet()) {
                if(entry.getValue() < lowest && !usedMembers.contains(entry.getKey())) {
                    lowest = entry.getValue();
                    lowestMember = entry.getKey();
                }
            }
            if(lowestMember == null) {
                break;
            }
            members[i] = Pair.of(lowestMember, lowest);
            usedMembers.add(lowestMember);
        }


        return Pair.of(Status.SUCCESSFUL_GET_DATA, members);
    }

    public Pair<Status, Pair<String, Double>[]> getHighestTop(String key, int maxMember) {
        HashMap<String, Double> memberMap = this.get(key);
        if(memberMap == null) {
            return Pair.of(Status.KEY_NOT_FOUND, null);
        }
        memberMap = (HashMap<String, Double>) memberMap.clone();
        Pair<String,Double>[] members = new Pair[maxMember < 0 ? memberMap.size() : maxMember];
        // Sort the memberMap by value and start with the lowest
        ArrayList<String> usedMembers = new ArrayList<>();

        for(int i = 0; i < members.length; i++) {
            double lowest = Double.MIN_VALUE;
            String lowestMember = null;
            for(Map.Entry<String, Double> entry : memberMap.entrySet()) {
                if(entry.getValue() > lowest && !usedMembers.contains(entry.getKey())) {
                    lowest = entry.getValue();
                    lowestMember = entry.getKey();
                }
            }
            if(lowestMember == null) {
                break;
            }
            members[i] = Pair.of(lowestMember, lowest);
            usedMembers.add(lowestMember);
        }


        return Pair.of(Status.SUCCESSFUL_GET_DATA, members);
    }
}
