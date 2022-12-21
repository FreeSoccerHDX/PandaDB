package de.freesoccerhdx.pandadb;

public enum Status {

    SUCCESSFUL,
    SUCCESSFUL_CREATED_NEW,
    SUCCESSFUL_REMOVED_KEY,
    SUCCESSFUL_REMOVED_MEMBER,
    SUCCESSFUL_OVERWRITE_OLD,
    SUCCESSFUL_ADD_ENTRY,
    SUCCESSFUL_REMOVED_LISTTYPE,
    SUCCESSFUL_GET_LISTINDEX,


    KEY_NOT_FOUND,
    LISTTYPE_NOT_FOUND,
    LISTKEY_NOT_FOUND,
    SUCCESSFUL_REMOVED_LISTKEY,
    SUCCESSFUL_REMOVED_LISTINDEX,
    LISTINDEX_NOT_FOUND,
    NO_KEYS_AVAILABLE,
    MEMBER_NOT_FOUND;


    public boolean isSuccessful(){
        return this == SUCCESSFUL
                || this == SUCCESSFUL_ADD_ENTRY
                || this == SUCCESSFUL_REMOVED_MEMBER
                || this == SUCCESSFUL_CREATED_NEW
                || this == SUCCESSFUL_OVERWRITE_OLD
                || this == SUCCESSFUL_REMOVED_LISTTYPE
                || this == SUCCESSFUL_GET_LISTINDEX
                || this == SUCCESSFUL_REMOVED_KEY;

    }

}
