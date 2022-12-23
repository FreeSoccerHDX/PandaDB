package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.clientutils.BlockingPipelineSupplier;
import de.freesoccerhdx.pandadb.clientutils.DatabaseReader;
import de.freesoccerhdx.pandadb.clientutils.DatabaseWriter;
import de.freesoccerhdx.pandadb.clientutils.PandaClientChannel;
import de.freesoccerhdx.pandadb.clientutils.PandaDataSerializer;
import de.freesoccerhdx.pandadb.clientutils.changelistener.ChangeReason;
import de.freesoccerhdx.pandadb.clientutils.changelistener.TextChangeListener;
import de.freesoccerhdx.pandadb.clientutils.changelistener.ValueChangeListener;
import de.freesoccerhdx.pandadb.serverlisteners.ListListener;
import de.freesoccerhdx.pandadb.serverlisteners.SerializableListener;
import de.freesoccerhdx.pandadb.serverlisteners.SimpleListener;
import de.freesoccerhdx.pandadb.serverlisteners.TextListener;
import de.freesoccerhdx.pandadb.serverlisteners.ValueListener;
import de.freesoccerhdx.simplesocket.server.ServerClientSocket;
import de.freesoccerhdx.simplesocket.server.ServerListener;
import de.freesoccerhdx.simplesocket.server.SimpleSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class PandaServer {

    private final SimpleSocketServer simpleSocketServer;
    private final ServerDataStorage dataStorage;

    private final ListListener listListener;
    private final TextListener textListener;
    private final ValueListener valueListener;
    private final SerializableListener serializableListener;
    private final SimpleListener simpleListener;

    private final int port;

    public PandaServer(int port) throws IOException {
        dataStorage = new ServerDataStorage();
        this.port = port;
        simpleSocketServer = new SimpleSocketServer(port);


        this.listListener = new ListListener(this);
        this.textListener = new TextListener(this);
        this.valueListener = new ValueListener(this);
        this.serializableListener = new SerializableListener(this);
        this.simpleListener = new SimpleListener(this);

        simpleSocketServer.setServerListener("datatree", new ServerListener() {
            @Override
            public void recive(SimpleSocketServer simpleSocketServer, ServerClientSocket clientSocket, String channel, String msg) {
                Thread thread = new Thread(dataStorage::generateDataTree);
                thread.setDaemon(true);
                thread.start();
            }
        });

        simpleSocketServer.setServerListener("changelistener", new ServerListener() {
            private HashMap<UUID, String> listeners = new HashMap<>();
            @Override
            public void recive(SimpleSocketServer simpleSocketServer, ServerClientSocket clientSocket, String channel, String msg) {
                JSONObject json = new JSONObject(msg);
                UUID uuid = UUID.fromString(json.getString("uuid"));
                String type = json.getString("type");

                if(type.equals("text")) {
                    listeners.put(uuid, type);
                    textListener.addChangeListener(uuid, clientSocket.getClientName(), json.getString("key"), json.getString("member"));
                }else if(type.equals("remove")) {
                    String toRem = listeners.get(uuid);
                    if(toRem.equals("text")) {
                        textListener.removeChangeListener(uuid);
                    }else if(toRem.equals("value")) {
                        valueListener.removeChangeListener(uuid);
                    }
                }else if(type.equals("value")) {
                    listeners.put(uuid,type);
                    valueListener.addChangeListener(uuid, clientSocket.getClientName(), json.getString("key"), json.getString("member"));
                }else {
                    System.err.println("Unknown 'changelistener'-type: " + type);
                }

            }
        });

        simpleSocketServer.setServerListener("dbpipeline", new ServerListener() {
            @Override
            public void recive(SimpleSocketServer simpleSocketServer, ServerClientSocket clientSocket, String __channel, String msg) {
                Thread responseThread = new Thread(() -> {
                    JSONObject jsonObject = new JSONObject(msg);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    String uuid = jsonObject.getString("uuid");

                    JSONObject result = new JSONObject();
                    result.put("uuid", uuid);
                    JSONArray resultArray = new JSONArray();
                    jsonArray.forEach(o -> {
                        JSONObject object = (JSONObject) o;

                        int channelid = object.getInt("c");
                        PandaClientChannel channel = PandaClientChannel.values()[channelid];
                        JSONObject data = object.getJSONObject("d");

                        JSONObject resultData = null;
                        if(channel.isText()) {
                            resultData = textListener.parseData(channel, data);
                        }else if(channel.isValue()) {
                            resultData = valueListener.parseData(channel, data);
                        }else if(channel.isSerializable()) {
                            resultData = serializableListener.parseData(channel, data);
                        }else if(channel.isListData()) {
                            resultData = listListener.parseData(channel, data);
                        }else if(channel.isSimple()) {
                            resultData = simpleListener.parseData(channel, data);
                        }else {
                            System.out.println("[PandaServer] Pipeline-Channel not found! Name="+channel + " (Data="+data+")");
                        }

                        if(resultData != null){
                            resultArray.put(resultData);
                        }else{
                            resultArray.put("");
                        }
                    });
                    result.put("data", resultArray);

                    clientSocket.sendNewMessage("dbpiperesult",result,null);
                });
                responseThread.setDaemon(true);
                responseThread.start();
            }
        });

    }

    public ServerDataStorage getDataStorage() {
        return this.dataStorage;
    }

    public SimpleSocketServer getSimpleSocketServer() {
        return this.simpleSocketServer;
    }

    private static class TestSerializable implements PandaDataSerializer {
        private String test = null;

        public TestSerializable() {

        }

        public TestSerializable(String test){
            this.test = test;
        }

        @Override
        public void serialize(DatabaseWriter writer) {
            writer.writeString("value", this.test);
        }

        @Override
        public void deserialize(DatabaseReader reader) {
            this.test = reader.getString("value");
        }

        @Override
        public String toString() {
            return "debug:toString() -> TestSerializable{test='" + test + "'}";
        }
    }

    // This will block the Thread and allows console input to handle some stuff
    public void listenCommands() {

        while(this.simpleSocketServer.isRunning()) {
            Scanner scanner = new Scanner(System.in);
            String name = scanner.nextLine();
            if(name != null){

                if(name.equalsIgnoreCase("help") || name.equalsIgnoreCase("?")) {
                    System.out.println(" Help: ");
                    System.out.println(" -help/? -> shows this help list");
                    System.out.println(" -stop -> saves the Database and stops the Server");
                    System.out.println(" -datatree -> creates the DataTree file");
                    System.out.println(" -testall -> tests all Database functions by creating a client & connect it");

                } else if(name.equalsIgnoreCase("stop")){
                    try {
                        dataStorage.saveDatabase();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    this.simpleSocketServer.stop("Server was stopped by command.");
                    break;
                } else if(name.equalsIgnoreCase("datatree")) {
                    System.out.println("Start writing Data-Tree");
                    getDataStorage().generateDataTree();
                    System.out.println("Data-Tree created");

                } else if(name.equalsIgnoreCase("test:change")) {
                    PandaClient client = new PandaClient("database_test_min","localhost",port);
                    client.setOnLogin(loggedIn -> {
                        if(loggedIn){

                            client.getChangeListenerHandler().addTextListener("key", "member", new TextChangeListener() {
                                @Override
                                public void onChange(ChangeReason changeReason, String oldvalue, String newvalue) {
                                //    System.out.println("Text-Change: " + changeReason + " | old=" + oldvalue + " -> new=" + newvalue);
                                }
                            });
                            client.getChangeListenerHandler().addValueListener("key", "member", new ValueChangeListener() {
                                @Override
                                public void onChange(ChangeReason changeReason, Double oldvalue, Double newvalue) {
                                    System.out.println("Value-Change: " + changeReason + " | old=" + oldvalue + " -> new=" + newvalue);
                                }
                            });

                            PipelineSupplier pipe = client.createPipelineSupplier();
/*
                            pipe.setText("key", "member", "firstVal", (old,status) -> System.out.println("setText0: " + status + " | old=" + old));
                            pipe.setText("key", "member", "secondVal", (old,status) -> System.out.println("setText1: " + status + " | old=" + old));
                            pipe.removeTextMember("key", "member", (oldvalue,status) -> System.out.println("removeTextMember: " + status + " -> " + oldvalue));
                            pipe.setText("key", "member", "firstVal", (old,status) -> System.out.println("setText0: " + status + " | old=" + old));
                            pipe.setText("key", "member", "secondVal", (old,status) -> System.out.println("setText1: " + status + " | old=" + old));
                            pipe.removeTextKey("key", status -> System.out.println("removeTextKey: " + status));
  */
                            pipe.setValue("key", "member", 123.0, (newv,status) -> System.out.println("setValue0: " + status + " | old=" + newv));
                            pipe.setValue("key", "member", 321.0, (newv,status) -> System.out.println("setValue1: " + status + " | old=" + newv));
                            pipe.addValue("key", "member", -200.0, (newv,status) -> System.out.println("addValue: " + status + " | old=" + newv));
                            pipe.removeValueMember("key", "member", (old,status) -> System.out.println("removeValueMember: " + status + " | old=" + old));
                            pipe.setValue("key", "member", 123.0, (newv,status) -> System.out.println("setValue0: " + status + " | old=" + newv));
                            pipe.removeValueKey("key", status -> System.out.println("removeValueKey: " + status));

                            pipe.sync();

                        }
                    });

                    killTestClient(client);
                } else if(name.equalsIgnoreCase("test:sort")) {
                    PandaClient client = new PandaClient("database_test_min","localhost",port);
                    client.setOnLogin(loggedIn -> {
                        if(loggedIn){
                            long nanos = System.nanoTime();
                            PipelineSupplier pipe = client.createPipelineSupplier();
                            for(int i = 0; i < 1000; i++) {
                                pipe.addValue("key", "mem0"+i, 111+i, (oldvalue, status) -> System.out.println("mem0: " + oldvalue + " -> " + status));
                                pipe.addValue("key", "mem1"+i, 222+i, (oldvalue, status) -> System.out.println("mem1: " + oldvalue + " -> " + status));
                                pipe.addValue("key", "mem2"+i, 333+i, (oldvalue, status) -> System.out.println("mem2: " + oldvalue + " -> " + status));
                                pipe.addValue("key", "mem3"+i, 444+i, (oldvalue, status) -> System.out.println("mem3: " + oldvalue + " -> " + status));
                                pipe.addValue("key", "mem4"+i, -555+i, (oldvalue, status) -> System.out.println("mem4: " + oldvalue + " -> " + status));
                            }

                            pipe.getValueHighestTop("key", 5, (pair, status) -> System.out.println("HighestTop: " + Arrays.toString(pair) + " -> " + status));
                            pipe.getValueLowestTop("key", 5, (pair, status) -> System.out.println("LowestTop: " + Arrays.toString(pair) + " -> " + status));
                            pipe.getValueInfo("key", false, (info, status) -> System.out.println("Info: " + info + " -> " + status));
                            pipe.removeValueKey("key", status -> System.out.println("Remove: " + status + " \nTotalTime: " + ((System.nanoTime()-nanos)*1.0)/1000000.0 + "ms"));

                            pipe.sync();

                        }
                    });

                    killTestClient(client);
                } else if(name.equalsIgnoreCase("test:simple")) {
                    PandaClient client = new PandaClient("database_test_min","localhost",port);
                    client.setOnLogin(loggedIn -> {
                        if(loggedIn){

                            PipelineSupplier pipe = client.createPipelineSupplier();

                            pipe.setSimple("key", "value", (old,status) -> {
                                System.out.println("SetSimple: "+status+" (old="+old+")");
                            });
                            pipe.getSimple("key", (value,status) -> {
                                System.out.println("GetSimple: "+status+" (value="+value+")");
                            });
                            pipe.getSimpleKeys((keys,status) -> {
                                System.out.println("GetSimpleKeys: "+status+" (keys="+keys+")");
                            });
                            pipe.getSimpleData((data,status) -> {
                                System.out.println("GetSimpleData: "+status+" (data="+data+")");
                            });
                            pipe.removeSimple("key", (old,status) -> {
                                System.out.println("RemoveSimple: "+status+" (old="+old+")");
                            });

                            pipe.sync();

                        }
                    });

                    killTestClient(client);
                } else if(name.equalsIgnoreCase("test:listkeys")) {
                    PandaClient client = new PandaClient("database_test_min","localhost",port);
                    client.setOnLogin(loggedIn -> {
                        if(loggedIn){

                            //BlockingPipelineSupplier block = client.getBlockingPipelineSupplier();
                            //block.addList(ListType.BYTE, "key", 0, 10);

                            PipelineSupplier pipe = client.createPipelineSupplier();
                            for(int i = 0; i < 3; i++){
                                pipe.addList(ListType.STRING, "key", "string", (status -> System.out.println("addList(STRING): " + status)));
                                pipe.addList(ListType.STRING_ARRAY, "key", "string".split(""), (status -> System.out.println("addList(STRING_ARRAY): " + status)));
                                pipe.addList(ListType.BOOLEAN, "key", true, (status -> System.out.println("addList(BOOLEAN): " + status)));
                                pipe.addList(ListType.BOOLEAN_ARRAY, "key", new Boolean[]{true,false,true}, (status -> System.out.println("addList(BOOLEAN_ARRAY): " + status)));
                                pipe.addList(ListType.INTEGER, "key", 123456, (status -> System.out.println("addList(INTEGER): " + status)));
                                pipe.addList(ListType.INTEGER_ARRAY, "key", new Integer[]{1,2,3,4,5,6}, (status -> System.out.println("addList(INTEGER_ARRAY): " + status)));
                                pipe.addList(ListType.BYTE, "key", (byte)0, (status -> System.out.println("addList(BYTE): " + status)));
                                pipe.addList(ListType.BYTE_ARRAY, "key", new Byte[]{(byte)2,(byte)50}, (status -> System.out.println("addList(BYTE_ARRAY): " + status)));
                                pipe.addList(ListType.DOUBLE, "key", 123.456, (status -> System.out.println("addList(DOUBLE): " + status)));
                                pipe.addList(ListType.DOUBLE_ARRAY, "key", new Double[]{1.2,3.4,5.6}, (status -> System.out.println("addList(DOUBLE_ARRAY): " + status)));
                                pipe.addList(ListType.LONG, "key", 123456789L, (status -> System.out.println("addList(LONG): " + status)));
                                pipe.addList(ListType.LONG_ARRAY, "key", new Long[]{987654321L,321654987L,789456123L}, (status -> System.out.println("addList(LONG_ARRAY): " + status)));
                            }
                            for(ListType listType : ListType.values()) {
                                pipe.getListSize(listType, "key", (status, size) -> System.out.println("getListSize(" + listType + "): "+ status + " -> " + size));
                                pipe.getListIndex(listType, "key", 0, (status, obj) -> System.out.println("getListIndex(" + listType + "): " + status + " -> " + obj));
                                pipe.getListData(listType, "key", (data, status) -> {
                                    System.out.println("::::getListData(" + listType + "): " + status + " ("+data.get(0).getClass().getSimpleName()+")-> " + data);
                                    if(data.get(0).getClass().getSimpleName().equalsIgnoreCase("ArrayList")) {
                                        ArrayList list = (ArrayList) data.get(0);
                                        System.out.println(":::::-----ArrayList: " + list.get(0).getClass().getSimpleName());
                                    }
                                });
                            }

                            pipe.getListTypes((list,status) -> {
                                System.out.println("getListTypes: " + status);
                                PipelineSupplier pipe2 = client.createPipelineSupplier();
                                for(ListType type : list){
                                    System.out.println(" - " + type);
                                    pipe2.getListKeys(type, (keys,status2) -> {
                                        System.out.println("getListKeys(" + type + "): " + status2);
                                        PipelineSupplier pipe3 = client.createPipelineSupplier();
                                        for(String key : keys){
                                            System.out.println(" - " + key);
                                            pipe3.getListData(type, key, (list2,status3) -> {
                                                System.out.println("getList(" + type + "," + key + "): " + status3);
                                                System.out.println(" - " + list2);
                                                PipelineSupplier pipe5 = client.createPipelineSupplier();
                                                pipe5.removeListIndex(type, key, 0, (specific, status4)->{
                                                    System.out.println("removeListIndex(" + type + "," + key + ",0): " + status4 + " -> " + specific);
                                                    PipelineSupplier pipe6 = client.createPipelineSupplier();
                                                    pipe6.removeListKey(type,key,(status1 -> System.out.println("removeLListKey: " + status1)));
                                                    pipe6.removeListtype(type, (status1 -> System.out.println("removeListType: " + status1)));
                                                    pipe6.sync();
                                                });
                                                pipe5.sync();
                                            });
                                        }
                                        pipe3.sync();
                                    });
                                }
                                pipe2.sync();
                            });

                            pipe.sync();

                        }
                    });

                    killTestClient(client);

                } else if(name.equalsIgnoreCase("test:serializable")) {
                    PandaClient client = new PandaClient("database_test_min","localhost",port);
                    client.setOnLogin(loggedIn -> {
                        if(loggedIn){
                            PipelineSupplier pipe = client.createPipelineSupplier();
                            pipe.setSerializable("key", "member", new TestSerializable("Hello World!"), (status) -> System.out.println("setSerializable: " + status));
                            pipe.getSerializableKeys((keys, status) -> System.out.println("getSerializableKeys: " + keys + " | " + status));
                            pipe.getSerializableMemberKeys("key", (keys, status) -> System.out.println("getSerializableMemberKeys: " + keys + " | " + status));
                            pipe.getSerializableMemberData("key" , "member", TestSerializable::new, (data, status) -> System.out.println("getSerializableMemberData: " + data + " | " + status));
                            pipe.getSerializableKeyData("key", TestSerializable::new, (data, status) -> System.out.println("getSerializableKeyData: " + data + " | " + status));
                            pipe.removeSerializableKey("key", (status) -> System.out.println("removeSerializableKey: " + status));
                            pipe.removeSerializableMember("key", "member", (status) -> System.out.println("removeSerializableMember: " + status));
                            pipe.sync();
                        }
                    });

                    killTestClient(client);


                } else if(name.equalsIgnoreCase("test:value")){
                    PandaClient client = new PandaClient("database_test_min","localhost",port);
                    client.setOnLogin(loggedIn -> {
                        if(loggedIn){
                            PipelineSupplier pipe = client.createPipelineSupplier();
                            pipe.setValue("key", "member", 123456.0, (value,status) -> System.out.println("setValue: " + value + " | Status: " + status));
                            pipe.addValue("key", "member", -023456.0, (value,status) -> System.out.println("addValue: " + value + " | Status: " + status));
                            pipe.getValueKeys((keys,status) -> System.out.println("getValueKeys: " + keys + " | Status: " + status));
                            pipe.getValueMemberKeys("key", (keys,status) -> System.out.println("getValueMemberKeys: " + keys + " | Status: " + status));
                            pipe.getValueMemberData("key", "member", (data,status) -> System.out.println("getValueMemberData: " + data + " | Status: " + status));
                            pipe.getValueKeyData("key", (data,status) -> System.out.println("getValueKeyData: " + data + " | Status: " + status));
                            pipe.getValueInfo("key", true, (info,status) -> System.out.println("getValueInfo: " + info + " | Status: " + status));
                            pipe.removeValueMember("key", "member", (value,status) -> System.out.println("removeValueMember: " + value + " | Status: " + status));
                            pipe.removeValueKey("key", (status) -> System.out.println("removeValueKey: " + status));
                            pipe.sync();
                        }
                    });

                    killTestClient(client);

                } else if(name.equalsIgnoreCase("test:text")){
                    PandaClient client = new PandaClient("database_test_min","localhost",port);
                    client.setOnLogin(loggedIn -> {
                        if(loggedIn){
                            PipelineSupplier pipe = client.createPipelineSupplier();
                            pipe.setText("key", "member", "Hello World!", (old,status) -> System.out.println("setText: " + status));
                            pipe.getTextKeys((keys,status) -> System.out.println("getTextKeys: " +status + " -> "+ keys));
                            pipe.getTextMemberKeys("key", (keys,status) -> System.out.println("getTextMemberKeys: " +status + " -> "+ keys));
                            pipe.getTextMemberData("key", "member", (data,status) -> System.out.println("getTextMemberData: " +status + " -> "+ data));
                            pipe.getTextKeyData("key", (data,status) -> System.out.println("getTextKeyData: " +status + " -> size="+ data.size()));
                            pipe.removeTextMember("key", "member", (data,status) -> System.out.println("removeTextMember: " + status + " -> " + data));
                            pipe.removeTextKey("key", status -> System.out.println("removeTextKey: " + status));
                            pipe.sync();
                        }
                    });

                    killTestClient(client);

                }else{
                    System.out.println("You should try to use `?` or `help` to get some help!");
                }

            }
        }
    }

    private void killTestClient(PandaClient client) {
        client.setStatusInfo(clientStatusInfo -> System.out.println("StatusInfo: " + clientStatusInfo));

        client.start();

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                client.interrupt();
                System.out.println("Client stopped.");
            }
        }, 1000*5);
    }

}
