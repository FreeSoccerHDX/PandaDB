package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.serverutils.datastorage.ByteArrayDataStorage;
import de.freesoccerhdx.pandadb.serverutils.datastorage.ListTypeDataStorage;
import de.freesoccerhdx.pandadb.serverutils.datastorage.MemberValueDataStorage;
import de.freesoccerhdx.pandadb.serverutils.datastorage.SimpleDataStorage;
import de.freesoccerhdx.pandadb.serverutils.datastorage.TextsDataStorage;
import de.freesoccerhdx.pandadb.serverutils.datastorage.ValueDataStorage;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ServerDataStorage {

    private final ValueDataStorage valueData;
    private final TextsDataStorage textData;
    private final TextsDataStorage serializableData;
    private final SimpleDataStorage simpleData;

    private final ListTypeDataStorage listData;

    private final ByteArrayDataStorage byteArrayData;

    private final File databaseFile;
    private final File dataTreeFile;
    private long haschanged = -1;

    public ServerDataStorage(@NotNull File databaseFile, @NotNull File dataTreeFile) {
        this.databaseFile = databaseFile;
        this.dataTreeFile = dataTreeFile;
        haschanged = System.currentTimeMillis();
        valueData = new ValueDataStorage(this);
        textData = new TextsDataStorage(this);
        serializableData = new TextsDataStorage(this);
        simpleData = new SimpleDataStorage(this);
        listData = new ListTypeDataStorage(this);
        byteArrayData = new ByteArrayDataStorage(this);

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
                    long dif = System.currentTimeMillis() - haschanged;
                    if((dif > 1000 && haschanged != -1) || !databaseFile.exists()) {
                        databaseFile.mkdirs();
                        System.out.println(" ");
                        System.out.println("Try to save the data...");
                        long start = System.currentTimeMillis();
                        saveDatabase();
                        long end = System.currentTimeMillis();
                        System.out.println("Saving data was successful in "+(end-start)+"ms!");
                        System.out.println(" ");
                        haschanged = -1;
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
        try {
            dataTreeFile.mkdirs();
            if(dataTreeFile.exists()){
                dataTreeFile.delete();
            }
            dataTreeFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(dataTreeFile);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            bw.write("ByteArrayData(Size="+byteArrayData.size()+"):");
            bw.newLine();
            for(String s : byteArrayData.keySet()){
                bw.write("    KEY="+s + " VALUE="+ Arrays.toString(byteArrayData.get(s)).replaceAll(" null",""));
                bw.newLine();
            }

            bw.write("SimpleData(Size="+simpleData.size()+"):");
            bw.newLine();
            for(String s : simpleData.keySet()){
                bw.write("    KEY="+s + " VALUE="+simpleData.get(s));
                bw.newLine();
            }

            bw.write("ValueData(Size="+valueData.size()+"):");
            bw.newLine();

            for(String key : valueData.keySet()){
                MemberValueDataStorage valueDataStorage = valueData.get(key);
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
                    HashMap<String, List<Object>> listkeys = listData.get(listType);
                    bw.write("    "+listType+"(Size="+listkeys.size()+"):");
                    bw.newLine();
                    for(String key : listkeys.keySet()){
                        List<Object> keyData = listkeys.get(key);

                        bw.write("        LISTKEY="+key+"(Size="+keyData.size()+"):");
                        int c = 0;
                        for(Object obj : keyData){
                            bw.write(listType.asString(obj));
                            if(c != keyData.size()-1){
                                bw.write(", ");
                            }
                            c++;
                        }
                        bw.newLine();

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
        try (DataInputStream dis = new DataInputStream(new FileInputStream(databaseFile))) {
            int valuedatasize = dis.readInt();
            for (int i = 0; i < valuedatasize; i++) {
                String key = dis.readUTF();
                MemberValueDataStorage memberMap = new MemberValueDataStorage();

                int membersize = dis.readInt();
                for (int a = 0; a < membersize; a++) {
                    String member = dis.readUTF();
                    double value = dis.readDouble();
                    memberMap.put(member, value);
                }
                if (memberMap.size() > 0) {
                    valueData.put(key, memberMap);
                }
            }

            int textdatasize = dis.readInt();
            for (int i = 0; i < textdatasize; i++) {
                String key = dis.readUTF();
                HashMap<String, String> memberMap = new HashMap<>();

                int membersize = dis.readInt();
                for (int a = 0; a < membersize; a++) {
                    String member = dis.readUTF();
                    String value = dis.readUTF();
                    memberMap.put(member, value);
                }
                if (memberMap.size() > 0) {
                    textData.put(key, memberMap);
                }
            }

            int seridatasize = dis.readInt();
            for (int i = 0; i < seridatasize; i++) {
                String key = dis.readUTF();
                HashMap<String, String> memberMap = new HashMap<>();

                int membersize = dis.readInt();
                for (int a = 0; a < membersize; a++) {
                    String member = dis.readUTF();
                    String value = dis.readUTF();
                    memberMap.put(member, value);
                }
                if (memberMap.size() > 0) {
                    serializableData.put(key, memberMap);
                }
            }


            for (ListType listType : ListType.values()) {
                int listamount = dis.readInt();

                if (listamount > 0) {
                    HashMap<String, List<Object>> keydata = new HashMap<>();
                    for (int i = 0; i < listamount; i++) {
                        String key = dis.readUTF();
                        int keysize = dis.readInt();
                        List<Object> memberdata = new ArrayList<>();
                        for (int a = 0; a < keysize; a++) {
                            Object readyobj = listType.read(dis);
                            memberdata.add(readyobj);
                        }

                        if (memberdata.size() > 0) {
                            keydata.put(key, memberdata);
                        }
                    }
                    if (keydata.size() > 0) {
                        listData.put(listType, keydata);
                    }

                }
            }

            int simpleSize = dis.readInt();
            for(int i = 0; i < simpleSize; i++){
                String key = dis.readUTF();
                String value = dis.readUTF();
                simpleData.put(key,value);
            }

            int byteArraySize = dis.readInt();
            for(int i = 0; i < byteArraySize; i++){
                String key = dis.readUTF();
                int arraySize = dis.readInt();
                Byte[] array = new Byte[arraySize];
                for(int a = 0; a < arraySize; a++){
                    int value = dis.readInt();
                    if(value != -4000) {
                        array[a] = (byte) value;
                    }

                }
                byteArrayData.put(key, array);
            }


        }
    }

    public void saveDatabase() throws IOException {
        System.out.println("Path to database: "+databaseFile.getPath());
        if(databaseFile.exists()){
            databaseFile.delete();
        }

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(databaseFile))) {
            HashMap<String, MemberValueDataStorage> copyvaluedata = (HashMap<String, MemberValueDataStorage>) valueData.clone();
            HashMap<String, HashMap<String, String>> copytextdata = (HashMap<String, HashMap<String, String>>) textData.clone();
            HashMap<String, HashMap<String, String>> seritextdata = (HashMap<String, HashMap<String, String>>) serializableData.clone();
            HashMap<ListType, HashMap<String, List<Object>>> listclonedata = (HashMap<ListType, HashMap<String, List<Object>>>) listData.clone();

            dos.writeInt(copyvaluedata.size()); // size of valueData
            for (String key : copyvaluedata.keySet()) {
                dos.writeUTF(key); // write key
                HashMap<String, Double> memberData = copyvaluedata.get(key);
                dos.writeInt(memberData.size()); // write how many members are stored
                for (String memberKey : memberData.keySet()) {
                    dos.writeUTF(memberKey); // the member from the key
                    dos.writeDouble(memberData.get(memberKey)); // the value of the member
                }
            }

            dos.writeInt(copytextdata.size()); // size of valueData
            for (String key : copytextdata.keySet()) {
                dos.writeUTF(key); // write key
                HashMap<String, String> memberData = copytextdata.get(key);
                dos.writeInt(memberData.size()); // write how many members are stored
                for (String memberKey : memberData.keySet()) {
                    dos.writeUTF(memberKey); // the member from the key
                    dos.writeUTF(memberData.get(memberKey)); // the text of the member
                }
            }

            dos.writeInt(seritextdata.size()); // size of valueData
            for (String key : seritextdata.keySet()) {
                dos.writeUTF(key); // write key
                HashMap<String, String> memberData = seritextdata.get(key);
                dos.writeInt(memberData.size()); // write how many members are stored
                for (String memberKey : memberData.keySet()) {
                    dos.writeUTF(memberKey); // the member from the key
                    dos.writeUTF(memberData.get(memberKey)); // the text of the member
                }
            }

            //dos.write(listData.size()); // size of listData
            for (ListType listType : ListType.values()) {
                if (listclonedata.containsKey(listType)) {
                    HashMap<String, List<Object>> listtypeData = listclonedata.get(listType);
                    dos.writeInt(listtypeData.size());
                    for (String key : listtypeData.keySet()) {
                        List<Object> keydata = listtypeData.get(key);
                        dos.writeUTF(key);
                        dos.writeInt(keydata.size());
                        for (Object obj : keydata) {
                            listType.write(dos, obj);
                        }
                    }

                } else {
                    dos.writeInt(0);
                }
            }

            dos.writeInt(simpleData.size());
            for(String key : simpleData.keySet()){
                dos.writeUTF(key);
                dos.writeUTF(simpleData.get(key));
            }

            dos.writeInt(byteArrayData.size());
            for(String key : byteArrayData.keySet()){
                dos.writeUTF(key);
                dos.writeInt(byteArrayData.get(key).length);
                for(Byte b : byteArrayData.get(key)){
                    dos.writeInt(b == null ? -4000 : b);

                }
            }
        }


    }

    public TextsDataStorage getTextData() {
        return textData;
    }

    public TextsDataStorage getSerializableData() {
        return serializableData;
    }

    public ValueDataStorage getValueData() {
        return valueData;
    }

    public ListTypeDataStorage getListData() {
        return listData;
    }

    public SimpleDataStorage getSimpleData() {
        return simpleData;
    }

    public ByteArrayDataStorage getByteArrayData() {
        return byteArrayData;
    }

    public void needSave() {
        this.haschanged = System.currentTimeMillis();
    }


}
