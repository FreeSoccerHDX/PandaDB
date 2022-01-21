package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.simplesocket.Pair;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ServerDataStorage {

    private HashMap<String, ValueDataStorage> valueData = new HashMap<>();
    private TextsDataStorage textData = new TextsDataStorage(this);
    private TextsDataStorage serializableData = new TextsDataStorage(this);

    private HashMap<ListType, HashMap<String, HashMap<String,List<Object>>>> listData = new HashMap<>();

    private File databaseFile = new File("C:\\Users\\timau\\Documents\\intellj\\trash","panda.db");
    private File dataTreeFile = new File("C:\\Users\\timau\\Documents\\intellj\\trash","datatree.txt");
    private boolean haschanged = true;

    public ServerDataStorage(){

        try {
            System.out.println(" ");
            System.out.println("Loading databse...");
            long start = System.currentTimeMillis();
            loadDatabase();
            long end = System.currentTimeMillis();
            System.out.println("Database was loaded in " + (end-start) + "ms");
            System.out.println(" ");

        } catch (IOException e) {
            e.printStackTrace();
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                try {
                    if(haschanged || !databaseFile.exists()) {
                        databaseFile.mkdirs();
                        System.out.println(" ");
                        System.out.println("Try to save the data...");
                        long start = System.currentTimeMillis();
                        saveDatabase();
                        long end = System.currentTimeMillis();
                        System.out.println("Saving data was successful in "+(end-start)+"ms!");
                        System.out.println(" ");
                        haschanged = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Saving data was not successful!");
                    System.out.println(" ");
                }
            }
        },1000L*30,1000L*60);
    }


    public void generateDataTree() {
        FileOutputStream fos = null;
        try {
            dataTreeFile.mkdirs();
            if(dataTreeFile.exists()){
                dataTreeFile.delete();
            }
            dataTreeFile.createNewFile();
            fos = new FileOutputStream(dataTreeFile);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            bw.write("ValueData(Size="+valueData.size()+"):");
            bw.newLine();

            for(String key : valueData.keySet()){
                ValueDataStorage valueDataStorage = valueData.get(key);
                bw.write("    KEY="+key + " (Size="+valueDataStorage.size()+")");
                bw.newLine();
                int maxlength = -1;
                for(String member : valueDataStorage.keySet()){
                    maxlength = Math.max(maxlength,member.length());
                }
                maxlength += 2;
                for(String member : valueDataStorage.keySet()){
                    int remainlength = maxlength-member.length();
                    String buffer = " ".repeat(remainlength);
                    bw.write("        MEMBER=" +member+buffer+ "Value="+valueDataStorage.get(member));
                    bw.newLine();
                }
            }
            bw.write("TextData(Size="+textData.size()+"):");
            bw.newLine();

            for(String key : textData.keySet()){
                HashMap<String, String> keydata = textData.get(key);
                bw.write("    KEY="+key + " (Size="+keydata.size()+")");
                bw.newLine();
                int maxlength = -1;
                for(String member : keydata.keySet()){
                    maxlength = Math.max(maxlength,member.length());
                }
                maxlength += 2;
                for(String member : keydata.keySet()){
                    int remainlength = maxlength-member.length();
                    String buffer = " ".repeat(remainlength);
                    bw.write("        MEMBER=" +member+buffer+ "Text="+keydata.get(member));
                    bw.newLine();
                }
            }

            bw.write("SerializableData(Size="+serializableData.size()+"):");
            bw.newLine();

            for(String key : serializableData.keySet()){
                HashMap<String, String> keydata = serializableData.get(key);
                bw.write("    KEY="+key + " (Size="+keydata.size()+")");
                bw.newLine();
                int maxlength = -1;
                for(String member : keydata.keySet()){
                    maxlength = Math.max(maxlength,member.length());
                }
                maxlength += 2;
                for(String member : keydata.keySet()){
                    int remainlength = maxlength-member.length();
                    String buffer = " ".repeat(remainlength);
                    bw.write("        MEMBER=" +member+buffer+ "Text="+keydata.get(member));
                    bw.newLine();
                }
            }

            bw.write("ListData:");
            bw.newLine();
            for(ListType listType : ListType.values()){
                if(listData.containsKey(listType)){
                    HashMap<String, HashMap<String, List<Object>>> listkeys = listData.get(listType);
                    bw.write("    "+listType+"(Size="+listkeys.size()+"):");
                    bw.newLine();
                    for(String key : listkeys.keySet()){
                        HashMap<String, List<Object>> keyData = listkeys.get(key);

                        bw.write("        KEY="+key+"(Size="+keyData.size()+"):");
                        bw.newLine();

                        for(String valuekey : keyData.keySet()){
                            List<Object> objects = keyData.get(valuekey);
                            bw.write("            LISTKEY="+valuekey+"(Size="+objects.size()+"):");
                            bw.newLine();
                            bw.write("                ");
                            int c = 0;
                            for(Object obj : objects){
                                bw.write(listType.asString(obj));
                                if(c != objects.size()-1){
                                    bw.write(",");
                                }
                                c++;
                            }
                            bw.newLine();
                        }

                    }


                }else{
                    bw.write("    "+listType+"(Size=0)");
                    bw.newLine();
                }
            }



            bw.close();
        }catch (Exception exception){
            exception.printStackTrace();
        }

    }

    public void loadDatabase() throws  IOException {
        System.out.println("Path to database: "+databaseFile.getPath());
        if (!databaseFile.exists()) {
            System.err.println("No database found!");
            return;
        }
        DataInputStream dis = new DataInputStream(new FileInputStream(databaseFile));
        try {
            int valuedatasize = dis.readInt();
            for(int i = 0; i < valuedatasize; i++){
                String key = dis.readUTF();
                ValueDataStorage memberMap = new ValueDataStorage();

                int membersize = dis.readInt();
                for(int a = 0; a < membersize; a++){
                    String member = dis.readUTF();
                    double value = dis.readDouble();
                    memberMap.put(member,value);
                }
                if(memberMap.size() > 0) {
                    valueData.put(key, memberMap);
                }
            }

            int textdatasize = dis.readInt();
            for(int i = 0; i < textdatasize; i++){
                String key = dis.readUTF();
                HashMap<String, String> memberMap = new HashMap<>();

                int membersize = dis.readInt();
                for(int a = 0; a < membersize; a++){
                    String member = dis.readUTF();
                    String value = dis.readUTF();
                    memberMap.put(member,value);
                }
                if(memberMap.size() > 0) {
                    textData.put(key, memberMap);
                }
            }

            int seridatasize = dis.readInt();
            for(int i = 0; i < seridatasize; i++){
                String key = dis.readUTF();
                HashMap<String, String> memberMap = new HashMap<>();

                int membersize = dis.readInt();
                for(int a = 0; a < membersize; a++){
                    String member = dis.readUTF();
                    String value = dis.readUTF();
                    memberMap.put(member,value);
                }
                if(memberMap.size() > 0) {
                    serializableData.put(key, memberMap);
                }
            }


            for (ListType listType : ListType.values()) {
                int listamount = dis.readInt();

                if(listamount > 0){
                    HashMap<String, HashMap<String,List<Object>>> keydata = new HashMap<>();
                    for(int i = 0; i < listamount; i++){
                        String key = dis.readUTF();
                        int keysize = dis.readInt();
                        HashMap<String,List<Object>> memberdata = new HashMap<>();
                        for(int a = 0; a < keysize; a++){
                            String member = dis.readUTF();
                            int membersize = dis.readInt();
                            List<Object> memberList = new ArrayList<>();

                            for (int c = 0; c < membersize; c++) {
                                Object readyobj = listType.read(dis);
                                memberList.add(readyobj);
                            }

                            if(memberList.size() > 0){
                                memberdata.put(member, memberList);
                            }

                        }

                        if(memberdata.size() > 0){
                            keydata.put(key, memberdata);
                        }
                    }
                    if(keydata.size() > 0) {
                        listData.put(listType, keydata);
                    }

                }

            }

        }finally {
            if(dis != null){
                dis.close();
            }
        }
    }

    public void saveDatabase() throws IOException {
        System.out.println("Path to database: "+databaseFile.getPath());
        if(databaseFile.exists()){
            databaseFile.delete();
        }
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(databaseFile));
        try {
            dos.writeInt(valueData.size()); // size of valueData
            for (String key : valueData.keySet()) {
                dos.writeUTF(key); // write key
                HashMap<String, Double> memberData = valueData.get(key);
                dos.writeInt(memberData.size()); // write how many members are stored
                for (String memberKey : memberData.keySet()) {
                    dos.writeUTF(memberKey); // the member from the key
                    dos.writeDouble(memberData.get(memberKey)); // the value of the member
                }
            }

            dos.writeInt(textData.size()); // size of valueData
            for (String key : textData.keySet()) {
                dos.writeUTF(key); // write key
                HashMap<String, String> memberData = textData.get(key);
                dos.writeInt(memberData.size()); // write how many members are stored
                for (String memberKey : memberData.keySet()) {
                    dos.writeUTF(memberKey); // the member from the key
                    dos.writeUTF(memberData.get(memberKey)); // the text of the member
                }
            }

            dos.writeInt(serializableData.size()); // size of valueData
            for (String key : serializableData.keySet()) {
                dos.writeUTF(key); // write key
                HashMap<String, String> memberData = serializableData.get(key);
                dos.writeInt(memberData.size()); // write how many members are stored
                for (String memberKey : memberData.keySet()) {
                    dos.writeUTF(memberKey); // the member from the key
                    dos.writeUTF(memberData.get(memberKey)); // the text of the member
                }
            }

            //dos.write(listData.size()); // size of listData
            for (ListType listType : ListType.values()) {
                if (listData.containsKey(listType)) {
                    HashMap<String, HashMap<String, List<Object>>> listtypeData = listData.get(listType);
                    dos.writeInt(listtypeData.size());
                    for (String key : listtypeData.keySet()) {
                        HashMap<String, List<Object>> keydata = listtypeData.get(key);
                        dos.writeUTF(key);
                        dos.writeInt(keydata.size());
                        for (String member : keydata.keySet()) {
                            List<Object> memberList = keydata.get(member);
                            dos.writeUTF(member);
                            dos.writeInt(memberList.size());
                            for (Object obj : memberList) {
                                listType.write(dos, obj);
                            }
                        }
                    }

                } else {
                    dos.writeInt(0);
                }
            }
        }finally {
            if(dos != null) {
                dos.close();
            }
        }


    }

    /**
     * Gets the value for the specific key and member
     *
     * @return Pair of Status(KEY_NOT_FOUND/SUCCESSFUL) and value
     * */
    public Pair<Status,Double> getValue(String key, String member) {

        HashMap<String, Double> keymap = valueData.get(key);
        Double val = null;
        Status status = Status.KEY_NOT_FOUND;

        if(keymap != null){

            val = keymap.get(member);
            if(val == null){
                status = Status.MEMBER_NOT_FOUND;
            }else{
                status = Status.SUCCESSFUL;
            }
        }

        return Pair.of(status, val);
    }

    /**
     * Sets the value for the specific key and member
     *
     * @return Pair of Status(SUCCESSFUL_OVERWRITE_OLD/SUCCESSFUL_CREATED_NEW) and new value
     * */
    public Pair<Status,Double> setValue(String key, String member, double value) {

        ValueDataStorage keymap = valueData.get(key);
        Double newvalue = value;
        Status status = Status.SUCCESSFUL_OVERWRITE_OLD;

        if(keymap == null){
            keymap = new ValueDataStorage();
            valueData.put(key, keymap);
        }
        if(!keymap.containsKey(member)){
            status = Status.SUCCESSFUL_CREATED_NEW;
        }

        keymap.put(member, value);
        haschanged = true;

        return Pair.of(status, newvalue);
    }

    /**
     * Adds the value for the specific key and member (and creates the new if not set before)
     *
     * @return Pair of Status(SUCCESSFUL_OVERWRITE_OLD/SUCCESSFUL_CREATED_NEW) and new value
     * */
    public Pair<Status,Double> addValue(String key, String member, double value) {
        ValueDataStorage keymap = valueData.get(key);
        Status status = Status.SUCCESSFUL_OVERWRITE_OLD;
        Double newvalue = null;

        if(keymap == null){
            keymap = new ValueDataStorage();
            valueData.put(key, keymap);
            status = Status.SUCCESSFUL_CREATED_NEW;
        }
        Double prevalue = keymap.get(member);
        if(prevalue == null){
            status = Status.SUCCESSFUL_CREATED_NEW;
        }
        newvalue = value + ((prevalue == null) ? 0 : prevalue);
        keymap.put(member, newvalue);

        haschanged = true;
        return Pair.of(status, newvalue);
    }

    /**
     * Removes the member from the key with his value
     *
     * @return Status(SUCCESSFUL_REMOVED_MEMBER/MEMBER_NOT_FOUND/KEY_NOT_FOUND)
     * */
    public Status removeValue(String key, String member) {
        HashMap<String, Double> keymap = valueData.get(key);
        if(keymap != null){
            boolean erfolg = keymap.remove(member) != null;

            if(keymap.size() == 0){
                valueData.remove(key);
            }

            if(erfolg){
                haschanged = true;
                return Status.SUCCESSFUL_REMOVED_MEMBER;
            }
            return Status.MEMBER_NOT_FOUND;
        }


        return Status.KEY_NOT_FOUND;
    }

    /**
     * Removes the member from the key with his text
     *
     * @return Status(SUCCESSFUL_REMOVED_MEMBER/MEMBER_NOT_FOUND/KEY_NOT_FOUND)
     * */
    public Status remove(String key, String member) {
       return textData.remove(key, member);
    }

    /**
     * Gets the stored text for the specific key and member
     *
     * @return Status(KEY_NOT_FOUND/MEMBER_NOT_FOUND/SUCCESSFUL)
     * */
    public Pair<Status,String> get(String key, String member) {
        return textData.get(key, member);
    }

    /**
     * Sets for the given key and member the specific text
     *
     * @return Status(SUCCESSFUL_OVERWRITE_OLD/SUCCESSFUL_CREATED_NEW)
     * */
    public Status set(String key, String member, String value) {
        return textData.set(key,member,value);
    }

    /**
     * Gets a stored List under the specific ListType, Key and ListKey
     *
     * @return Pair of Status(LISTTYPE_NOT_FOUND/LISTKEY_NOT_FOUND/SUCCESSFUL/KEY_NOT_FOUND) and stored List(or null)
     * */
    public Pair<Status,List<Object>> getList(String key, String listkey, ListType listType) {
        List<Object> objectList = null;
        Status status = Status.LISTTYPE_NOT_FOUND;

        HashMap<String, HashMap<String, List<Object>>> typeData = listData.get(listType);
        if(typeData != null){
            HashMap<String, List<Object>> listkeydata = typeData.get(key);
            if(listkeydata != null){
                objectList = listkeydata.get(listkey);
                if(objectList == null){
                    status = Status.LISTKEY_NOT_FOUND;
                }else{
                    status = Status.SUCCESSFUL;
                }
            }else{
                status = Status.KEY_NOT_FOUND;
            }
        }

        return Pair.of(status,objectList);
    }

    /**
     * Adds an Object to the specific ListKey under the Key and ListType
     *
     * @return Status(LISTTYPE_NOT_FOUND/LISTKEY_NOT_FOUND/SUCCESSFUL/KEY_NOT_FOUND)
     * */
    public Status addListEntry(String key, String listkey, ListType listType, Object value) {
        Status status = Status.SUCCESSFUL_ADD_ENTRY;

        HashMap<String, HashMap<String, List<Object>>> typeData = listData.get(listType);
        if (typeData == null) {
            typeData = new HashMap<>();
            listData.put(listType, typeData);
        }
        HashMap<String, List<Object>> listkeydata = typeData.get(key);
        if (listkeydata == null) {
            listkeydata = new HashMap<>();
            typeData.put(key, listkeydata);
        }
        List<Object> objects = listkeydata.get(listkey);
        if(objects == null){
            objects = new ArrayList<>();
            listkeydata.put(listkey,objects);
            status = Status.SUCCESSFUL_CREATED_NEW;
        }
        haschanged = true;
        objects.add(value);
        return status;
    }

    /**
     * Removes the specific ListKey by ListType and Key
     *
     * @return Status(LISTTYPE_NOT_FOUND/SUCCESSFUL_REMOVED_LISTKEY/LISTKEY_NOT_FOUND/KEY_NOT_FOUND)
     * */
    public Status removeList(String key, String listkey, ListType listType) {
        HashMap<String, HashMap<String, List<Object>>> typeData = listData.get(listType);
        Status status = Status.LISTTYPE_NOT_FOUND;
        if(typeData != null){
            HashMap<String, List<Object>> keydata = typeData.get(key);
            if(keydata != null) {
                boolean erfolg = keydata.remove(listkey) != null;

                if (keydata.size() == 0) {
                    typeData.remove(key);
                }
                if (typeData.size() == 0) {
                    listData.remove(listType);
                }
                haschanged = true;

                if(erfolg){
                    status = Status.SUCCESSFUL_REMOVED_LISTKEY;
                }else{
                    status = Status.LISTKEY_NOT_FOUND;
                }

            }else{
                status = Status.KEY_NOT_FOUND;
            }
        }


        return status;
    }

    /**
     * Removes the specific Index in a List by ListType, Key and ListKey
     *
     * @return Status(LISTTYPE_NOT_FOUND/SUCCESSFUL_REMOVED_LISTINDEX/LISTINDEX_NOT_FOUND/LISTKEY_NOT_FOUND/KEY_NOT_FOUND)
     * */
    public Status removeListIndex(String key, String listkey, ListType listType, int index) {
        HashMap<String, HashMap<String, List<Object>>> typeData = listData.get(listType);
        Status status = Status.LISTTYPE_NOT_FOUND;
        if(typeData != null){
            HashMap<String, List<Object>> keydata = typeData.get(key);
            if(keydata != null) {
                List<Object> objects = keydata.get(listkey);
                if(objects != null) {
                    boolean erfolg = objects.remove(index) != null;

                    if(objects.size() == 0){
                        keydata.remove(listkey);
                    }
                    if (keydata.size() == 0) {
                        typeData.remove(key);
                    }
                    if (typeData.size() == 0) {
                        listData.remove(listType);
                    }
                    haschanged = true;

                    if(erfolg){
                        status = Status.SUCCESSFUL_REMOVED_LISTINDEX;
                    }else{
                        status = Status.LISTINDEX_NOT_FOUND;
                    }
                }else{
                    status = Status.LISTKEY_NOT_FOUND;
                }
            }else{
                status = Status.KEY_NOT_FOUND;
            }
        }

        return status;
    }

    /**
     * Removes the specific Key for an ListKey
     *
     * @return Status(LISTTYPE_NOT_FOUND/SUCCESSFUL_REMOVED_KEY/LISTKEY_NOT_FOUND)
     * */
    public Status removeList(String key, ListType listType) {
        Status status = Status.LISTTYPE_NOT_FOUND;
        HashMap<String, HashMap<String, List<Object>>> typeData = listData.get(listType);

        if(typeData != null){
            boolean erfolg = typeData.remove(key) != null;

            if(typeData.size() == 0){
                listData.remove(listType);
            }

            if(erfolg){
                status = Status.SUCCESSFUL_REMOVED_KEY;
                haschanged = true;
            }else{
                status = Status.LISTKEY_NOT_FOUND;
            }
        }


        return status;
    }

    /**
     * Gets a List of Member-Keys for Values under the specific Key
     *
     * @return Pair of Status(KEY_NOT_FOUND/SUCCESSFUL) and the Keys
     * */
    public Pair<Status,List<String>> getValueKeys(String key) {
        HashMap<String, Double> valueMemberKeys = valueData.get(key);
        Status status = Status.KEY_NOT_FOUND;
        List<String> stringList = null;

        if(valueMemberKeys != null){
            stringList = new ArrayList<>(valueMemberKeys.keySet());
            if(stringList != null){
                status = Status.SUCCESSFUL;
            }
        }

        return Pair.of(status,stringList);
    }

    /**
     * Gets a List of all Keys for Values
     *
     * @return Pair of Status(NO_KEYS_AVAILABLE/SUCCESSFUL) and the Keys
     * */
    public Pair<Status,List<String>> getValueKeys() {
        Status status = Status.NO_KEYS_AVAILABLE;
        List<String> stringList = null;

        if(valueData.size() > 0){
            stringList = new ArrayList<>(valueData.keySet());
            if(stringList != null){
                status = Status.SUCCESSFUL;
            }
        }

        return Pair.of(status,stringList);
    }

    /**
     * Gets a List of all Keys for Texts
     *
     * @return Pair of Status(NO_KEYS_AVAILABLE/SUCCESSFUL) and the Keys
     * */
    public Pair<Status,List<String>> getKeys() {
        return textData.getKeys();
    }

    /**
     * Gets a List of all MemberKeys for Texts with from a specific Key
     *
     * @return Pair of Status(KEY_NOT_FOUND/SUCCESSFUL) and the Keys
     * */
    public Pair<Status,List<String>> getMemberKeys(String key) {
       return textData.getMemberKeys(key);
    }

    /**
     * Gets a List of all Keys for a specific ListType
     *
     * @return Pair of Status(KEY_NOT_FOUND/SUCCESSFUL) and the Keys
     * */
    public Pair<Status,List<String>> getListKeys(ListType listType) {
        Status status = Status.LISTTYPE_NOT_FOUND;
        List<String> stringList = null;
        HashMap<String, HashMap<String, List<Object>>> listtypeMap = listData.get(listType);
        if(listtypeMap != null){
            stringList = new ArrayList<>(listtypeMap.keySet());
            status = Status.SUCCESSFUL;
        }

        return Pair.of(status,stringList);
    }

    /**
     * Gets a List of all Keys for a specific ListType
     *
     * @return Pair of Status(KEY_NOT_FOUND/SUCCESSFUL) and the Keys
     * */
    public Pair<Status,List<String>> getListMemberKeys(String key, ListType listType) {
        Status status = Status.LISTTYPE_NOT_FOUND;
        List<String> stringList = null;
        HashMap<String, HashMap<String, List<Object>>> listtypeMap = listData.get(listType);

        if(listtypeMap != null){
            HashMap<String, List<Object>> keydata = listtypeMap.get(key);
            if(keydata != null) {
                stringList = new ArrayList<>(keydata.keySet());
                status = Status.SUCCESSFUL;
            }else{
                status = Status.KEY_NOT_FOUND;
            }
        }

        return Pair.of(status,stringList);
    }

    /**
     * Gets the ValueMembersInfo for the given Key
     *
     * @return Pair of Status(KEY_NOT_FOUND/SUCCESSFUL) and the ValueMembersInfo
     * */
    public Pair<Status, ValueDataStorage.ValueMembersInfo> getValuesInfo(String key, boolean withMembers) {
        ValueDataStorage valueDataStorage = valueData.get(key);
        ValueDataStorage.ValueMembersInfo valueMembersInfo = null;
        Status status = Status.KEY_NOT_FOUND;

        if(valueDataStorage != null){
            valueMembersInfo = valueDataStorage.getInfo(withMembers);
            status = Status.SUCCESSFUL;
        }


        return Pair.of(status,valueMembersInfo);
    }

    public Pair<Status, String> getStoredSerializable(String key, String member) {
        return serializableData.get(key,member);
    }

    public Status storeSerializable(String key, String member, String value) {
        return serializableData.set(key, member, value);
    }

    public Pair<Status, List<String>> getStoredSerializableKeys() {
        return serializableData.getKeys();
    }

    public Pair<Status, List<String>> getStoredSerializableMemberKeys(String key) {
        return serializableData.getMemberKeys(key);
    }

    public Pair<Status, HashMap<String,String>> getStoredSerializableMemberData(String key){
        return serializableData.getMemberData(key);
    }

    public Pair<Status, HashMap<String,String>> getTextMemberData(String key){
        return textData.getMemberData(key);
    }


    public void needSave() {
        this.haschanged = true;
    }
}
