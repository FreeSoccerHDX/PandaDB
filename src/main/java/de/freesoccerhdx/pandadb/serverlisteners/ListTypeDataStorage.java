package de.freesoccerhdx.pandadb.serverlisteners;

import de.freesoccerhdx.pandadb.ListType;
import de.freesoccerhdx.pandadb.ServerDataStorage;
import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.simplesocket.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListTypeDataStorage extends HashMap<ListType, HashMap<String, List<Object>>> {

    private final ServerDataStorage serverDataStorage;
    public ListTypeDataStorage(ServerDataStorage serverDataStorage) {
        this.serverDataStorage = serverDataStorage;
    }

    /**
     * Gets a stored List under the specific ListType, Key
     *
     * @return Pair of Status(LISTTYPE_NOT_FOUND/LISTKEY_NOT_FOUND/SUCCESSFUL/KEY_NOT_FOUND) and stored List(or null)
     * */
    public Pair<Status,List<Object>> getList(ListType listType, String key) {
        List<Object> objectList = null;
        Status status = Status.LISTTYPE_NOT_FOUND;

        HashMap<String, List<Object>> typeData = this.get(listType);
        if(typeData != null){
            objectList = typeData.get(key);
            if(objectList == null){
                status = Status.KEY_NOT_FOUND;
            }else{
                status = Status.SUCCESSFUL;
            }
        }


        return Pair.of(status,objectList);
    }

    /**
     * Adds an Object to the specific ListKey under the Key and ListType
     *
     * @return Status(LISTTYPE_NOT_FOUND/LISTKEY_NOT_FOUND/SUCCESSFUL/KEY_NOT_FOUND)
     * */
    public Status addListEntry(String key, ListType listType, Object value) {
        Status status = Status.SUCCESSFUL_ADD_ENTRY;

        HashMap<String, List<Object>> typeData = this.computeIfAbsent(listType, k -> new HashMap<>());
        List<Object> listkeydata = typeData.get(key);
        if(listkeydata == null){
            listkeydata = new ArrayList<>();
            typeData.put(key,listkeydata);
            status = Status.SUCCESSFUL_CREATED_NEW;
        }
        serverDataStorage.needSave();
        listkeydata.add(value);
        return status;
    }

    /**
     * Removes the specific Index in a List by ListType, Key and ListKey
     *
     * @return Status(LISTTYPE_NOT_FOUND/SUCCESSFUL_REMOVED_LISTINDEX/LISTINDEX_NOT_FOUND/LISTKEY_NOT_FOUND/KEY_NOT_FOUND)
     * */
    public Pair<Status, Object> removeListIndex(ListType listType, String key, int index) {
        HashMap<String, List<Object>> typeData = this.get(listType);
        Status status = Status.LISTTYPE_NOT_FOUND;
        Object obj = null;
        if(typeData != null){

            List<Object> objects = typeData.get(key);
            if(objects != null) {
                if(objects.size() > index) {

                    obj = objects.remove(index);
                    boolean erfolg = obj != null;
                    if (objects.size() == 0) {
                        typeData.remove(key);
                    }
                    if (typeData.size() == 0) {
                        this.remove(listType);
                    }
                    serverDataStorage.needSave();

                    if (erfolg) {
                        status = Status.SUCCESSFUL_REMOVED_LISTINDEX;
                    } else {
                        status = Status.LISTINDEX_NOT_FOUND;
                    }
                }else{
                    status = Status.LISTINDEX_NOT_FOUND;
                }
            }else{
                status = Status.KEY_NOT_FOUND;
            }
        }

        return Pair.of(status, obj);
    }


    /**
     * Gets a List of all Keys for a specific ListType
     *
     * @return Pair of Status(KEY_NOT_FOUND/SUCCESSFUL) and the Keys
     * */
    public Pair<Status,List<String>> getListKeys(ListType listType) {
        Status status = Status.LISTTYPE_NOT_FOUND;
        List<String> stringList = null;
        HashMap<String, List<Object>> listtypeMap = this.get(listType);
        if(listtypeMap != null){
            stringList = new ArrayList<>(listtypeMap.keySet());
            status = Status.SUCCESSFUL;
        }

        return Pair.of(status,stringList);
    }


    public Status removeListType(ListType listType) {
        Status status = this.remove(listType) == null ? Status.LISTTYPE_NOT_FOUND : Status.SUCCESSFUL_REMOVED_LISTTYPE;
        if(status == Status.SUCCESSFUL_REMOVED_LISTTYPE) {
            serverDataStorage.needSave();
        }
        return status;
    }

    public Status removeListKey(ListType listType, String key) {
        Status status = Status.LISTTYPE_NOT_FOUND;
        HashMap<String, List<Object>> listtypeMap = this.get(listType);
        if(listtypeMap != null){
            boolean erfolg = listtypeMap.remove(key) != null;
            if(listtypeMap.size() == 0){
                this.remove(listType);
            }
            if(erfolg){
                status = Status.SUCCESSFUL_REMOVED_KEY;
                serverDataStorage.needSave();
            }else{
                status = Status.LISTKEY_NOT_FOUND;
            }
        }
        return status;
    }

    public Pair<Status, List<Integer>> getListTypes() {
        List<Integer> list = new ArrayList<>();
        for(ListType listType : this.keySet()){
            list.add(listType.ordinal());
        }
        return list.size() == 0 ? Pair.of(Status.NO_KEYS_AVAILABLE, null) : Pair.of(Status.SUCCESSFUL, list);
    }

    public Pair<Status, Object> getListIndex(ListType listType, String key, int index) {
        HashMap<String, List<Object>> typeData = this.get(listType);
        Status status = Status.LISTTYPE_NOT_FOUND;
        Object obj = null;
        if(typeData != null){

            List<Object> objects = typeData.get(key);
            if(objects != null) {
                if(objects.size() > index) {

                    obj = objects.get(index);
                    boolean erfolg = obj != null;

                    if (erfolg) {
                        status = Status.SUCCESSFUL_GET_LISTINDEX;
                    } else {
                        status = Status.LISTINDEX_NOT_FOUND;
                    }
                }else{
                    status = Status.LISTINDEX_NOT_FOUND;
                }
            }else{
                status = Status.KEY_NOT_FOUND;
            }
        }

        return Pair.of(status, obj);
    }

    public Pair<Status, Integer> getListSize(ListType listType, String key) {
        HashMap<String, List<Object>> typeData = this.get(listType);
        Status status = Status.LISTTYPE_NOT_FOUND;
        Integer size = null;
        if(typeData != null){
            List<Object> objects = typeData.get(key);
            if(objects != null) {
                size = objects.size();
                status = Status.SUCCESSFUL;
            }else{
                status = Status.KEY_NOT_FOUND;
            }

        }

        return Pair.of(status, size);
    }
}
