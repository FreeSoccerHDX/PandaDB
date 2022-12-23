package de.freesoccerhdx.pandadb.clientutils;

public enum PandaClientChannel {

    TEXT_SET, // -> set Text for key and Member
    TEXT_GET_KEYS, // -> return List<String> of Keys
    TEXT_GET_MEMBER_KEYS, // -> return List<String> of Members for key
    TEXT_GET_MEMBER_DATA, // -> return Text for key and Member
    TEXT_GET_KEY_DATA, // -> return HashMap<String,String> for key
    TEXT_REMOVE_KEY, // -> remove key with all members
    TEXT_REMOVE_MEMBER, // -> remove member from key with its Text

    SERIALIZABLE_SET, // -> set Text for key and Member
    SERIALIZABLE_GET_KEYS, // -> return List<String> of Keys
    SERIALIZABLE_GET_MEMBER_KEYS, // -> return List<String> of Members for key
    SERIALIZABLE_GET_MEMBER_DATA, // -> return Text for key and Member
    SERIALIZABLE_GET_KEY_DATA, // -> return HashMap<String,String> for key
    SERIALIZABLE_REMOVE_KEY, // -> remove key with all members
    SERIALIZABLE_REMOVE_MEMBER, // -> remove member from key with its Text

    VALUE_SET, // -> set Value for key and Member
    VALUE_ADD, // -> add Value to key and Member
    VALUE_GET_KEYS, // -> return List<String> of Keys
    VALUE_GET_MEMBER_KEYS, // -> return List<String> of Members for key
    VALUE_GET_MEMBER_DATA, // -> return Value for key and Member
    VALUE_GET_KEY_DATA, // -> return HashMap<String,Value> for key
    VALUE_REMOVE_KEY, // -> remove key with all members
    VALUE_REMOVE_MEMBER, // -> remove member from key with its Value
    VALUE_GET_VALUE_INFO, // -> return ValueInfo for key
    VALUE_GET_LOWEST_TOP, //
    VALUE_GET_HIGHEST_TOP, //

    LISTDATA_ADD_LIST_ENTRY, // -> add Data for key in ListType
    LISTDATA_REMOVE_LISTTYPE, // -> remove specific ListType
    LISTDATA_REMOVE_LISTKEY, // -> remove Key from specific ListType
    LISTDATA_REMOVE_INDEX, // -> remove Index from specific ListType and Key
    LISTDATA_GET_LISTTYPE_KEYS, // -> return Keys as List<String> of specific ListType
    LISTDATA_GET_LISTTYPES, // -> return ListTypes that are stored
    LISTDATA_GET_LISTKEY_DATA, // -> return List<Object> of specific ListType and Key
    LISTDATA_GET_LISTINDEX, // -> return Object of specific ListType, Key and Index
    LISTDATA_GET_LISTSIZE, // -> return size of specific ListType and Key


    SIMPLE_SET,
    SIMPLE_GET,
    SIMPLE_REMOVE,
    SIMPLE_GET_KEYS,
    SIMPLE_GET_DATA,

    ;

    public boolean isText(){
        return this.name().startsWith("TEXT_");
    }

    public boolean isValue(){
        return this.name().startsWith("VALUE_");
    }

    public boolean isSerializable(){
        return this.name().startsWith("SERIALIZABLE_");
    }

    public boolean isListData(){
        return this.name().startsWith("LISTDATA_");
    }

    public boolean isSimple(){
        return this.name().startsWith("SIMPLE_");
    }


/*
    ADDLIST,
    GETLIST,
    GET,
    SET,
    GETVALUE,
    SETVALUE,
    ADDVALUE,
    REMOVE,
    LISTKEYS,
    INFOVALUES,
    STORE_SERIALIZABLE,
    GET_MEMBER_DATA,
    GET_SERIALIZABLE_MEMBER_DATA,
    GET_STORED_SERIALIZABLE;
*/

}
