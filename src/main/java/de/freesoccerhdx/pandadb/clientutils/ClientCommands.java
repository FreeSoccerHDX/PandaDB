package de.freesoccerhdx.pandadb.clientutils;

import de.freesoccerhdx.pandadb.DataResult;
import de.freesoccerhdx.pandadb.ListType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.freesoccerhdx.pandadb.DataResult.*;

/**
 * Contains all important Commands for the Client to use
 * @author FreeSoccerHDX
 * @version 1.0
 */
public interface ClientCommands {

    /**
     * interface to create a new serializable object
     */
    interface SerializerFactory<T> {
        /**
         * Method to create a new serializable object
         *
         * @return the new object
         */
        T create();
    }

    /**
     * Sets the value of a member in a specific Key
     *
     * @param key the key
     * @param member the member under the key
     * @param value the value for the member
     * @param result Object representing the result of the operation
     *               The result contains the old value of the member if present and Status.SUCCESSFUL_OVERWRITE_OLD,
     *               or null and Status.SUCCESSFUL_CREATED_NEW if the member was not present
     */
    void setText(@NotNull String key, @NotNull String member, @NotNull String value, @Nullable TextResult result);

    /**
     * Gets a list of all Keys
     *
     * @param result Object representing the result of the operation
     *               The result contains a list of all keys if present and Status.SUCCESSFUL_GET_KEYS,
     *               or null and Status.NO_KEYS_AVAILABLE if no keys are present
     */
    void getTextKeys(@NotNull KeysResult result);

    /**
     * Gets a list of all Members of a specific Key
     *
     * @param key the key
     * @param result Object representing the result of the operation
     *               The result contains a list of all keys if present and Status.SUCCESSFUL_GET_KEYS,
     *               or null and Status.KEY_NOT_FOUND if no keys are present
     */
    void getTextMemberKeys(@NotNull String key, @NotNull KeysResult result);

    /**
     * Gets the stored text for the specific key and member
     *
     * @param key the key
     * @param member the member under the key
     * @param result Object representing the result of the operation
     *               The result contains the stored text if present and Status.SUCCESSFUL_GET_DATA,
     *               or null and Status.KEY_NOT_FOUND if the key is not present,
     *               or null and Status.MEMBER_NOT_FOUND if the member is not present
     */
    void getTextMemberData(@NotNull String key, @NotNull String member, @NotNull TextResult result);

    /**
     * Gets all stored members with text for the specific key
     *
     * @param key the key
     * @param result Object representing the result of the operation
     *               The result contains the stored members with texts if present and Status.SUCCESSFUL_GET_DATA,
     *               or null and Status.KEY_NOT_FOUND if the key is not present
     */
    void getTextKeyData(@NotNull String key, @NotNull MemberDataResult result);

    /**
     * Remove a Key and all its members and texts
     *
     * @param key the key
     * @param result Object representing the result of the operation
     *               The result contains Status.SUCCESSFUL_REMOVED_KEY if the key was present
     *               and removed, or Status.KEY_NOT_FOUND if the key was not present
     */
    void removeTextKey(@NotNull String key, @Nullable StatusResult result);

    /**
     * Remove a member from a key
     *
     * @param key the key
     * @param member the member under the key
     * @param result Object representing the result of the operation
     *               The result contains the stored text if present and Status.SUCCESSFUL_REMOVED_MEMBER,
     *               or null and Status.KEY_NOT_FOUND if the key is not present,
     *               or null and Status.MEMBER_NOT_FOUND if the member is not present
     */
    void removeTextMember(@NotNull String key, @NotNull String member, @Nullable TextResult result);



    /**
     * Sets the PandaDataSerializer-Object of a member in a specific Key
     *
     * @param key the key
     * @param member the member under the key
     * @param serializer the serializable object for the member
     * @param result Object representing the result of the operation
     *               The result contains if present and Status.SUCCESSFUL_OVERWRITE_OLD,
     *               or Status.SUCCESSFUL_CREATED_NEW if the member was not present
     * @param <T> the type of the serializable object
     */
    <T> void setSerializable(@NotNull String key, @NotNull String member, @NotNull PandaDataSerializer<T> serializer, @Nullable StatusResult result);

    /**
     * Gets all stored Keys for the serializable objects
     *
     * @param result Object representing the result of the operation
     *               The result contains all keys if present and Status.SUCCESSFUL_GET_KEYS,
     *               or null and Status.NO_KEYS_AVAILABLE if the member was not present
     */
    void getSerializableKeys(@NotNull KeysResult result);

    /**
     * Gets all stored members for the specific key
     *
     * @param key the key
     * @param result Object representing the result of the operation
     *               The result contains all members if present and Status.SUCCESSFUL_GET_KEYS,
     *               or null and Status.KEY_NOT_FOUND if the key is not present
     */
    void getSerializableMemberKeys(@NotNull String key, @NotNull KeysResult result);

    /**
     * Gets the stored PandaDataSerializer-Object for the specific key and member
     *
     * @param key the key
     * @param member the member under the key
     * @param factory a Factory to create a new instance of the object
     * @param result Object representing the result of the operation
     *               The result contains the stored object if present and Status.SUCCESSFUL_GET_DATA,
     *               or null and Status.KEY_NOT_FOUND if the key is not present,
     *               or null and Status.MEMBER_NOT_FOUND if the member is not present
     * @param <T> the type of the serializable object
     */
    <T> void getSerializableMemberData(@NotNull String key, @NotNull String member, @NotNull SerializerFactory<T> factory, @NotNull DataResult.SpecificResult<T> result);

    /**
     * Gets all stored members with PandaDataSerializer-Objects for the specific key
     *
     * @param key the key
     * @param factory a Factory to create a new instance of the object
     * @param result Object representing the result of the operation
     *               The result contains the stored members with objects if present and Status.SUCCESSFUL_GET_DATA,
     *               or null and Status.KEY_NOT_FOUND if the key is not present
     * @param <T> the type of the serializable object
     */
    <T> void getSerializableKeyData(@NotNull String key, @NotNull SerializerFactory<T> factory, @NotNull ListStoredSerializableResult result);

    /**
     * Remove a Key and all its members and objects
     *
     * @param key the key
     * @param result Object representing the result of the operation
     *               The result contains Status.SUCCESSFUL_REMOVED_KEY if the key was present
     *               and removed, or Status.KEY_NOT_FOUND if the key was not present
     */
    void removeSerializableKey(@NotNull String key, @Nullable StatusResult result);

    /**
     * Remove a member from a key
     *
     * @param key the key
     * @param member the member under the key
     * @param result Object representing the result of the operation
     *               The result contains if present Status.SUCCESSFUL_REMOVED_MEMBER,
     *               or Status.KEY_NOT_FOUND if the key is not present,
     *               or Status.MEMBER_NOT_FOUND if the member is not present
     */
    void removeSerializableMember(@NotNull String key, @NotNull String member, @Nullable StatusResult result);



    /**
     * Sets the value of a member in a specific Key
     *
     * @param key the key
     * @param member the member under the key
     * @param value the value for the member
     * @param result Object representing the result of the operation
     *               The result contains the old value of the member if present and Status.SUCCESSFUL_OVERWRITE_OLD,
     *               or null and Status.SUCCESSFUL_CREATED_NEW if the member was not present
     */
    void setValue(@NotNull String key, @NotNull String member, double value, @Nullable ValueResult result);

    /**
     * Adds a value to a member in a specific Key
     *
     * @param key the key
     * @param member the member under the key
     * @param value the value for the member
     * @param result Object representing the result of the operation
     *               The result contains the new value of the member and Status.SUCCESSFUL_OVERWRITE_OLD,
     *               or null and Status.KEY_NOT_FOUND if the key is not present,
     *               or null and Status.MEMBER_NOT_FOUND if the member is not present
     */
    void addValue(@NotNull String key, @NotNull String member, double value, @Nullable ValueResult result);

    /**
     * Gets a list of all Keys
     *
     * @param result Object representing the result of the operation
     *               The result contains a list of all keys if present and Status.SUCCESSFUL_GET_KEYS,
     *               or null and Status.NO_KEYS_AVAILABLE if no keys are present
     */
    void getValueKeys(@NotNull KeysResult result);

    /**
     * Gets a list of all Members of a specific Key
     *
     * @param key the key
     * @param result Object representing the result of the operation
     *               The result contains a list of all members if present and Status.SUCCESSFUL_GET_KEYS,
     *               or null and Status.KEY_NOT_FOUND if the key is not present
     */
    void getValueMemberKeys(@NotNull String key, @NotNull KeysResult result);

    /**
     * Gets the stored value for the specific key and member
     *
     * @param key the key
     * @param member the member under the key
     * @param result Object representing the result of the operation
     *               The result contains the stored value if present and Status.SUCCESSFUL_GET_DATA,
     *               or null and Status.KEY_NOT_FOUND if the key is not present,
     *               or null and Status.MEMBER_NOT_FOUND if the member is not present
     */
    void getValueMemberData(@NotNull String key, @NotNull String member, @NotNull ValueResult result);

    /**
     * Gets all stored members with values for the specific key
     *
     * @param key the key
     * @param result Object representing the result of the operation
     *               The result contains the stored members with values if present and Status.SUCCESSFUL_GET_DATA,
     *               or null and Status.KEY_NOT_FOUND if the key is not present
     */
    void getValueKeyData(@NotNull String key, @NotNull ValueMemberDataResult result);

    /**
     * Remove a Key and all its members and values
     *
     * @param key the key
     * @param result Object representing the result of the operation
     *               The result contains Status.SUCCESSFUL_REMOVED_KEY if the key was present
     *               and removed, or Status.KEY_NOT_FOUND if the key was not present
     */
    void removeValueKey(@NotNull String key, @Nullable StatusResult result);

    /**
     * Remove a member from a key
     *
     * @param key the key
     * @param member the member under the key
     * @param result Object representing the result of the operation
     *               The result contains the stored value if present and Status.SUCCESSFUL_REMOVED_MEMBER,
     *               or null and Status.KEY_NOT_FOUND if the key is not present,
     *               or null and Status.MEMBER_NOT_FOUND if the member is not present
     */
    void removeValueMember(@NotNull String key, @NotNull String member, @Nullable ValueResult result);

    /**
     * Gets some information about the value-storage of the specific key
     *
     * @param key the key
     * @param withKeys if the result should contain the keys of all members
     * @param result Object representing the result of the operation
     *               The result contains the ValueMembersInfo if present and Status.SUCCESSFUL_GET_DATA,
     *               or null and Status.KEY_NOT_FOUND if the key is not present
     */
    void getValueInfo(@NotNull String key, boolean withKeys, @NotNull ValuesInfoResult result);

    /**
     * Gets a sorted array from the lowest to the highest value of the specific key
     *
     * @param key the key
     * @param maxMembers the maximum amount of members to return
     * @param result Object representing the result of the operation
     *               The result contains the sorted array of member and value if present and Status.SUCCESSFUL_GET_DATA,
     *               or null and Status.KEY_NOT_FOUND if the key is not present
     */
    void getValueLowestTop(@NotNull String key, int maxMembers, @NotNull SortedValueMemberDataResult result);

    /**
     * Gets a sorted array from the highest to the lowest value of the specific key
     *
     * @param key the key
     * @param maxMembers the maximum amount of members to return
     * @param result Object representing the result of the operation
     *               The result contains the sorted array of member and value if present and Status.SUCCESSFUL_GET_DATA,
     *               or null and Status.KEY_NOT_FOUND if the key is not present
     */
    void getValueHighestTop(@NotNull String key, int maxMembers, @NotNull SortedValueMemberDataResult result);


    /**
     * Adds a new value to the list of a specific key with the specific ListType
     *
     * @param listType the type of the list
     * @param key the key
     * @param value the value to add to the list with the same type
     * @param result Object representing the result of the operation
     *               If the list is not present, it will be created and the result contains Status.SUCCESSFUL_CREATED_NEW
     *               If the list is present, the result contains Status.SUCCESSFUL_ADD_ENTRY
     * @param <T> the type of the list
     */
    <T> void addList(@NotNull ListType<T> listType, @NotNull String key, @NotNull T value, @Nullable StatusResult result);

    /**
     * Removes all Keys and all Lists of the specific ListType
     *
     * @param listType the type of the list
     * @param result Object representing the result of the operation
     *               If the list is present, the result contains Status.SUCCESSFUL_REMOVED_LISTTYPE
     *               or else Status.LISTTYPE_NOT_FOUND
     */
    void removeListType(@NotNull ListType listType, @Nullable StatusResult result);

    /**
     * Removes a Key and its List of the specific ListType
     *
     * @param listType the type of the list
     * @param key the key
     * @param result Object representing the result of the operation
     *               If the list is present, the result contains Status.SUCCESSFUL_REMOVED_KEY
     *               or else Status.LISTKEY_NOT_FOUND or Status.LISTTYPE_NOT_FOUND
     */
    void removeListKey(@NotNull ListType listType, @NotNull String key, @Nullable StatusResult result);

    /**
     * Removes a specific index from the list of a specific key with the specific ListType
     *
     * @param listType the type of the list
     * @param key the key
     * @param index the index to remove from the list with the same type
     * @param result Object representing the result of the operation
     *               If the index was removed successfully, the result contains
     *               the Object and Status.SUCCESSFUL_REMOVED_LISTINDEX
     *               or else Status.LISTKEY_NOT_FOUND or Status.LISTTYPE_NOT_FOUND or Status.INDEX_NOT_FOUND
     * @param <T> the type of the list
     */
    <T> void removeListIndex(@NotNull ListType<T> listType, @NotNull String key, int index, @Nullable DataResult.ListTypeValueResult<T> result);

    /**
     * Gets the list of all keys from the specific ListType
     *
     * @param listType the type of the list
     * @param result Object representing the result of the operation
     *               If the list is present, the result contains the keys and Status.SUCCESSFUL_GET_KEYS
     *               or else Status.LISTTYPE_NOT_FOUND
     */
    void getListKeys(@NotNull ListType listType, @NotNull KeysResult result);

    /**
     * Gets the list of all ListTypes that have keys
     *
     * @param result Object representing the result of the operation
     *               If the list is present, the result contains the ListTypes and Status.SUCCESSFUL_GET_KEYS
     *               or else Status.NO_KEYS_AVAILABLE
     */
    void getListTypes(@NotNull ListTypeResult result);

    /**
     * Gets the complete list of a specific key with the specific ListType
     *
     * @param listType the type of the list
     * @param key the key
     * @param result Object representing the result of the operation
     *               If the list is present, the result contains the Objects and Status.SUCCESSFUL_GET_DATA
     *               or else null and Status.LISTTYPE_NOT_FOUND or Status.KEY_NOT_FOUND
     * @param <T> the type of the list
     */
    <T> void getListData(@NotNull ListType<T> listType, @NotNull String key, @NotNull DataResult.ListResult<T> result);

    /**
     * Gets the list of a specific key with the specific ListType from the specific index
     *
     * @param listType the type of the list
     * @param key the key
     * @param index the index to get from the list
     * @param result Object representing the result of the operation
     *               If present, the result contains the Object and Status.SUCCESSFUL_GET_LISTINDEX
     *               or else null and Status.LISTTYPE_NOT_FOUND or Status.KEY_NOT_FOUND or Status.INDEX_NOT_FOUND
     * @param <T> the type of the list
     */
    <T> void getListIndex(@NotNull ListType<T> listType, @NotNull String key, int index, @NotNull DataResult.ListTypeValueResult<T> result);

    /**
     * Gets the size of the specific ListType and Key
     *
     * @param listType the type of the list
     * @param key the key
     * @param result Object representing the result of the operation
     *               If present, the result contains the size and Status.SUCCESSFUL_GET_DATA
     *               or else null and Status.LISTTYPE_NOT_FOUND or Status.KEY_NOT_FOUND
     */
    void getListSize(@NotNull ListType listType, @NotNull String key, @NotNull ListSizeResult result);



    /**
     * Sets a text for a specific key
     *
     * @param key the key
     * @param value the text for the key
     * @param result Object representing the result of the operation
     *               The result contains the stored text if present and Status.SUCCESSFUL_OVERWRITE_OLD,
     *               or null and Status.SUCCESSFUL_CREATED_NEW if the key was not present
     */
    void setSimple(@NotNull String key, @NotNull String value, @Nullable TextResult result);

    /**
     * Gets the stored text for the specific key
     *
     * @param key the key
     * @param result Object representing the result of the operation
     *               The result contains the stored text if present and Status.SUCCESSFUL_GET_DATA,
     *               or null and Status.KEY_NOT_FOUND if the key is not present
     */
    void getSimple(@NotNull String key, @NotNull TextResult result);

    /**
     * Remove a Key and its stored text
     *
     * @param key the key
     * @param result Object representing the result of the operation
     *               The result contains the text and Status.SUCCESSFUL_REMOVED_KEY if the key was present
     *               and removed, or null and Status.KEY_NOT_FOUND if the key was not present
     */
    void removeSimple(@NotNull String key, @Nullable TextResult result);

    /**
     * Gets all stored keys with texts
     *
     * @param result Object representing the result of the operation
     *               The result contains the stored keys if present and Status.SUCCESSFUL_GET_KEYS,
     *               or null and Status.NO_KEYS_AVAILABLE if there are no keys
     */
    void getSimpleKeys(@NotNull KeysResult result);

    /**
     * Gets all stored keys with texts
     *
     * @param result Object representing the result of the operation
     *               The result contains the stored keys and texts if present and Status.SUCCESSFUL_GET_DATA,
     *               or null and Status.NO_KEYS_AVAILABLE if there are no keys
     */
    void getSimpleData(@NotNull MemberDataResult result);



    /**
     * Creates a new Byte-array for the specific key and size
     *
     * @param key the key
     * @param size the size of the array
     * @param result Object representing the result of the operation
     *               The result contains Status.SUCCESSFUL_CREATED_NEW if the key was not present,
     *               or Status.KEY_ALREADY_EXISTS if the key is already present
     */
    void createNewByteArray(@NotNull String key, int size, @Nullable StatusResult result);

    /**
     * Sets a byte at the specific index of the byte-array for the specific key
     *
     * @param key the key
     * @param index the index of the byte
     * @param value the byte to set
     * @param result Object representing the result of the operation
     *               The result contains Status.SUCCESSFUL_SET,
     *               or Status.KEY_NOT_FOUND if the key is not present,
     *               or Status.INDEX_NOT_FOUND if the index is out of bounds
     */
    void setByteArrayIndex(@NotNull String key, int index, @Nullable Byte value, StatusResult result);

    /**
     * Gets a byte at the specific index of the byte-array for the specific key
     *
     * @param key the key
     * @param index the index of the byte
     * @param result Object representing the result of the operation
     *               The result contains the byte if present and Status.SUCCESSFUL_GET_DATA,
     *               or null and Status.KEY_NOT_FOUND if the key is not present,
     *               or null and Status.INDEX_NOT_FOUND if the index is out of bounds
     */
    void getByteArrayIndex(@NotNull String key, int index, @NotNull ByteResult result);

    /**
     * Gets the size of the byte-array for the specific key
     *
     * @param key the key
     * @param indexes array of indexes to get
     * @param result Object representing the result of the operation
     *               The result contains an array of bytes if present and Status.SUCCESSFUL_GET_DATA,
     *               or null and Status.KEY_NOT_FOUND if the key is not present,
     *               or null and Status.INDEX_NOT_FOUND if one of the indexes is out of bounds
     */
    void getByteArrayIndexes(@NotNull String key, int[] indexes, @NotNull KeyByteDataResult result);

    /**
     * Gets the byte-array for the specific key
     *
     * @param key the key
     * @param result Object representing the result of the operation
     *               The result contains the array of bytes if present and Status.SUCCESSFUL_GET_DATA,
     *               or null and Status.KEY_NOT_FOUND if the key is not present
     */
    void getByteArrayKeyData(@NotNull String key, @NotNull KeyByteDataResult result);

    /**
     * Gets the size of the Byte-array for the specific key
     *
     * @param key the key
     * @param result Object representing the result of the operation
     *               The result contains the size of the array if present and Status.SUCCESSFUL_GET_DATA,
     *               or null and Status.KEY_NOT_FOUND if the key is not present
     */
    void getByteArrayKeySize(@NotNull String key, @NotNull ListSizeResult result);

    /**
     * Gets all stored keys with Byte-arrays
     *
     * @param result Object representing the result of the operation
     *               The result contains the stored keys if present and Status.SUCCESSFUL_GET_KEYS,
     *               or null and Status.NO_KEYS_AVAILABLE if there are no keys
     */
    void getByteArrayKeys(@NotNull KeysResult result);

    /**
     * Removes a Byte-array for the specific key
     *
     * @param key the key
     * @param result Object representing the result of the operation
     *               The result contains Status.SUCCESSFUL_REMOVED_KEY if the key was present,
     *               or Status.KEY_NOT_FOUND if the key was not present
     */
    void removeByteArrayKey(@NotNull String key, @Nullable StatusResult result);

    /**
     * Clears the Byte-array for the specific key
     *
     * @param key the key
     * @param result Object representing the result of the operation
     *               The result contains Status.SUCCESSFUL_CLEARED if the key was present,
     *               or Status.KEY_NOT_FOUND if the key was not present
     */
    void clearByteArrayKey(@NotNull String key, @Nullable StatusResult result);


}
