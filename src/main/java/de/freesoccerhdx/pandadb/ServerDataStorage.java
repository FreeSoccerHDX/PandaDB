package de.freesoccerhdx.pandadb;

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

    private HashMap<String, HashMap<String, Double>> valueData = new HashMap<>();
    private HashMap<String, HashMap<String, String>> textData = new HashMap<>();

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
            fos = new FileOutputStream(dataTreeFile);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            bw.write("ValueData(Size="+valueData.size()+"):");
            bw.newLine();

            for(String key : valueData.keySet()){
                HashMap<String, Double> keydata = valueData.get(key);
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
                    bw.write("        MEMBER=" +member+buffer+ "Value="+keydata.get(member));
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
            bw.write("ListData:");
            bw.newLine();
            for(ListType listType : ListType.values()){
                if(listData.containsKey(listType)){
                    bw.write("    "+listType+"(Size="+textData.size()+"):");
                    bw.newLine();
                    HashMap<String, HashMap<String, List<Object>>> listkeys = listData.get(listType);
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
                HashMap<String, Double> memberMap = new HashMap<>();

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

    public Double getValue(String key, String member) {

        HashMap<String, Double> keymap = valueData.get(key);
        if(keymap != null){
            return keymap.get(member);
        }

        return null;
    }

    public Double setValue(String key, String member, double value) {

        HashMap<String, Double> keymap = valueData.get(key);
        if(keymap == null){
            keymap = new HashMap<>();
            valueData.put(key, keymap);
        }
        keymap.put(member, value);
        haschanged = true;
        return value;
    }

    public Double addValue(String key, String member, double value) {
        HashMap<String, Double> keymap = valueData.get(key);
        if(keymap == null){
            keymap = new HashMap<>();
            valueData.put(key, keymap);
        }
        Double prevalue = keymap.get(member);
        double newvalue = value + ((prevalue == null) ? 0 : prevalue);
        keymap.put(member, newvalue);
        haschanged = true;
        return newvalue;
    }

    public boolean removeValue(String key, String member) {
        HashMap<String, Double> keymap = valueData.get(key);
        if(keymap != null){
            boolean erfolg = keymap.remove(member) != null;

            if(keymap.size() == 0){
                valueData.remove(key);
            }
            haschanged = true;
            return erfolg;
        }


        return false;
    }

    public boolean remove(String key, String member) {
        HashMap<String, String> keymap = textData.get(key);
        if(keymap != null){
            boolean erfolg = keymap.remove(member) != null;

            if(keymap.size() == 0){
                textData.remove(key);
            }
            haschanged = true;
            return erfolg;
        }

        return false;
    }

    public String get(String key, String member) {
        try {
            HashMap<String, String> keymap = textData.get(key);
            if (keymap != null) {
                return keymap.get(member);
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }
        return null;
    }

    public boolean set(String key, String member, String value) {
        try {
            HashMap<String, String> keymap = textData.get(key);
            if (keymap == null) {
                keymap = new HashMap<>();
                textData.put(key, keymap);
            }
            keymap.put(member, value);
            haschanged = true;
            return true;
        }catch (Exception exception){
            exception.printStackTrace();
        }
        return false;
    }

    public List<Object> getList(String key, String listkey, ListType listType) {

        HashMap<String, HashMap<String, List<Object>>> typeData = listData.get(listType);
        if(typeData != null){
            HashMap<String, List<Object>> listkeydata = typeData.get(key);
            if(listkeydata != null){
                return listkeydata.get(listkey);
            }
        }

        return null;
    }

    public boolean addListEntry(String key, String listkey, ListType listType, Object value) {
        try {
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
            }
            haschanged = true;
            objects.add(value);
            return true;

        }catch (Exception exception){
            exception.printStackTrace();
        }
        return false;
    }

    public boolean removeList(String key, String listkey, ListType listType) {
        HashMap<String, HashMap<String, List<Object>>> typeData = listData.get(listType);
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
                return erfolg;
            }
        }


        return false;
    }

    public boolean removeListIndex(String key, String listkey, ListType listType, int index) {
        HashMap<String, HashMap<String, List<Object>>> typeData = listData.get(listType);
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
                    return erfolg;
                }
            }
        }

        return false;
    }

    public boolean removeList(String key, ListType listType) {
        HashMap<String, HashMap<String, List<Object>>> typeData = listData.get(listType);
        if(typeData != null){
            boolean erfolg = typeData.remove(key) != null;

            if(typeData.size() == 0){
                listData.remove(listType);
            }
            haschanged = true;
            return erfolg;
        }


        return false;
    }

    public List<String> getValueKeys(String key) {
        HashMap<String, Double> valueMemberKeys = valueData.get(key);

        if(valueMemberKeys != null){
            return new ArrayList<>(valueMemberKeys.keySet());
        }

        return null;
    }

    public List<String> getValueKeys() {

        if(valueData.size() > 0){
            return new ArrayList<>(valueData.keySet());
        }

        return null;
    }

    public List<String> getKeys() {
        if(textData.size() > 0){
            return new ArrayList<>(textData.keySet());
        }

        return null;
    }

    public List<String> getKeys(String key) {
        HashMap<String, String> textMemberKeys = textData.get(key);

        if(textMemberKeys != null){
            return new ArrayList<>(textMemberKeys.keySet());
        }

        return null;
    }

    public List<String> getListKeys(ListType listType) {

        HashMap<String, HashMap<String, List<Object>>> listtypeMap = listData.get(listType);
        if(listtypeMap != null){
            return new ArrayList<>(listtypeMap.keySet());
        }

        return null;
    }

    public List<String> getListKeys(String key, ListType listType) {

        HashMap<String, HashMap<String, List<Object>>> listtypeMap = listData.get(listType);
        if(listtypeMap != null){
            HashMap<String, List<Object>> keydata = listtypeMap.get(key);
            if(keydata != null) {
                return new ArrayList<>(keydata.keySet());
            }
        }

        return null;
    }


}
