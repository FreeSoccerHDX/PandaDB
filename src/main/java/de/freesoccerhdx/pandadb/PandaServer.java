package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.clientutils.DatabaseReader;
import de.freesoccerhdx.pandadb.clientutils.DatabaseWriter;
import de.freesoccerhdx.pandadb.clientutils.PandaClientChannel;
import de.freesoccerhdx.pandadb.clientutils.PandaDataSerializer;
import de.freesoccerhdx.pandadb.serverlisteners.ListListener;
import de.freesoccerhdx.pandadb.serverlisteners.SerializableListener;
import de.freesoccerhdx.pandadb.serverlisteners.TextListener;
import de.freesoccerhdx.pandadb.serverlisteners.ValueListener;
import de.freesoccerhdx.simplesocket.server.ServerClientSocket;
import de.freesoccerhdx.simplesocket.server.ServerListener;
import de.freesoccerhdx.simplesocket.server.SimpleSocketServer;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class PandaServer {

    private SimpleSocketServer simpleSocketServer;
    private ServerDataStorage dataStorage;

    private ListListener listListener;
    private TextListener textListener;
    private ValueListener valueListener;
    private SerializableListener serializableListener;

    private int port;

    public PandaServer(int port) throws IOException {
        dataStorage = new ServerDataStorage();
        this.port = port;
        simpleSocketServer = new SimpleSocketServer(port);

        this.listListener = new ListListener(this);
        this.textListener = new TextListener(this);
        this.valueListener = new ValueListener(this);
        this.serializableListener = new SerializableListener(this);

        simpleSocketServer.setServerListener("datatree", new ServerListener() {
            @Override
            public void recive(SimpleSocketServer simpleSocketServer, ServerClientSocket clientSocket, String channel, String msg) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dataStorage.generateDataTree();
                    }
                });
                thread.setDaemon(true);
                thread.start();
            }
        });

        simpleSocketServer.setServerListener("dbpipeline", new ServerListener() {
            @Override
            public void recive(SimpleSocketServer simpleSocketServer, ServerClientSocket clientSocket, String __channel, String msg) {
                Thread responseThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        //  System.err.println("####################");
                        //  System.err.println("##########===))))  DBPIPE: " +msg+ "");
                        // System.err.println("####################");
                        JSONObject jsonObject = new JSONObject(msg);
                        int size = jsonObject.getInt("size");
                        String uuid = jsonObject.getString("uuid");

                        JSONObject result = new JSONObject();
                        result.put("size", size);
                        result.put("uuid", uuid);
                        for(int i = 0; i < size; i++){
                            int channelid = jsonObject.getInt("c"+i);
                            PandaClientChannel channel = PandaClientChannel.values()[channelid];
                            JSONObject data = jsonObject.getJSONObject("d"+i);

                            JSONObject resultData = null;
                            if(channel.isText()) {
                                resultData = textListener.parseData(channel, data);
                            }else if(channel.isValue()) {
                                resultData = valueListener.parseData(channel, data);
                            }else if(channel.isSerializable()) {
                                resultData = serializableListener.parseData(channel, data);
                            }else if(channel.isListData()) {
                                resultData = listListener.parseData(channel, data);
                            }else {
                                System.out.println("[PandaServer] Pipeline-Channel not found! Name="+channel + " (Data="+data+")");
                            }

                            if(resultData != null){
                                result.put("r"+i, resultData);
                            }

                        }
                        //System.err.println("####################");
                        //System.err.println("##########===))))  RESU: " +result.toString()+ "");
                        //System.err.println("####################");

                        boolean success = clientSocket.sendNewMessage("dbpiperesult",result,null);
                        //System.err.println("##########===)))) SEND: " +success);
                        //System.err.println("####################");
                    }
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

    private class TestSerializable implements PandaDataSerializer {
        private String test = null;

        public TestSerializable() {

        }

        public TestSerializable(String test){
            this.test = test;
        }

        public String getTest(){
            return this.test;
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

        /*
        if(System.console() == null) {
            System.err.println("[PandaServer] No console found! This will cause problems!");
            // read input from command line in intellij
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if (line.equalsIgnoreCase("stop")) {
                    System.out.println("[PandaServer] Stopping Server...");
                    this.simpleSocketServer.stop();
                    System.out.println("[PandaServer] Server stopped!");
                    break;
                }
            }

            while(this.simpleSocketServer.isRunning()){

            }
            return;
        }*/

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

                } else if(name.equalsIgnoreCase("test:listkeys")) {
                    PandaClient client = new PandaClient("database_test_min","localhost",port);
                    client.setOnLogin(loggedIn -> {
                        if(loggedIn){

                            PipelineSupplier pipe = client.createPipelineSupplier();
                            for(int i = 0; i < 3; i++){
                                pipe.addList(ListType.STRING, "key", "string", (status -> {System.out.println("addList(STRING): " + status);}));
                                pipe.addList(ListType.STRING_ARRAY, "key", "string".split(""), (status -> {System.out.println("addList(STRING_ARRAY): " + status);}));
                                pipe.addList(ListType.BOOLEAN, "key", true, (status -> {System.out.println("addList(BOOLEAN): " + status);}));
                                pipe.addList(ListType.BOOLEAN_ARRAY, "key", new Boolean[]{true,false,true}, (status -> {System.out.println("addList(BOOLEAN_ARRAY): " + status);}));
                                pipe.addList(ListType.INTEGER, "key", 123456, (status -> {System.out.println("addList(INTEGER): " + status);}));
                                pipe.addList(ListType.INTEGER_ARRAY, "key", new Integer[]{1,2,3,4,5,6}, (status -> {System.out.println("addList(INTEGER_ARRAY): " + status);}));
                                pipe.addList(ListType.BYTE, "key", (byte)0, (status -> {System.out.println("addList(BYTE): " + status);}));
                                pipe.addList(ListType.BYTE_ARRAY, "key", new Byte[]{(byte)2,(byte)50}, (status -> {System.out.println("addList(BYTE_ARRAY): " + status);}));
                                pipe.addList(ListType.DOUBLE, "key", 123.456, (status -> {System.out.println("addList(DOUBLE): " + status);}));
                                pipe.addList(ListType.DOUBLE_ARRAY, "key", new Double[]{1.2,3.4,5.6}, (status -> {System.out.println("addList(DOUBLE_ARRAY): " + status);}));
                                pipe.addList(ListType.LONG, "key", 123456789L, (status -> {System.out.println("addList(LONG): " + status);}));
                                pipe.addList(ListType.LONG_ARRAY, "key", new Long[]{987654321L,321654987L,789456123L}, (status -> {System.out.println("addList(LONG_ARRAY): " + status);}));
                            }
                            for(ListType listType : ListType.values()) {
                                pipe.getListSize(listType, "key", (status, size) -> {
                                    System.out.println("getListSize(" + listType + "): "+ status + " -> " + size);
                                });
                                pipe.getListIndex(listType, "key", 0, (status, obj) -> {
                                    System.out.println("getListIndex(" + listType + "): " + status + " -> " + obj);
                                });
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
                                                    pipe6.removeListKey(type,key,(status1 -> {System.out.println("removeLListKey: " + status1);}));
                                                    pipe6.removeListtype(type, (status1 -> {System.out.println("removeListType: " + status1);}));
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

                    client.setStatusInfo(clientStatusInfo -> {
                        System.out.println("StatusInfo: " + clientStatusInfo);
                    });

                    client.start();

                    if(client != null){
                        Timer timer = new Timer(true);
                        PandaClient finalClient = client;
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                finalClient.interrupt();
                                System.out.println("Client stopped.");
                            }
                        }, 1000*5);
                    }

                } else if(name.equalsIgnoreCase("test:serializable")) {
                    PandaClient client = new PandaClient("database_test_min","localhost",port);
                    client.setOnLogin(loggedIn -> {
                        if(loggedIn){
                            PipelineSupplier pipe = client.createPipelineSupplier();
                            pipe.setSerializable("key", "member", new TestSerializable("Hello World!"), (status) -> {System.out.println("setSerializable: " + status);});
                            pipe.getSerializableKeys((keys, status) -> {System.out.println("getSerializableKeys: " + keys + " | " + status);});
                            pipe.getSerializableMemberKeys("key", (keys, status) -> {System.out.println("getSerializableMemberKeys: " + keys + " | " + status);});
                            pipe.getSerializableMemberData("key" , "member",()->{return new TestSerializable();}, (data, status) -> {System.out.println("getSerializableMemberData: " + data + " | " + status);});
                            pipe.getSerializableKeyData("key", TestSerializable::new, (data, status) -> {System.out.println("getSerializableKeyData: " + data + " | " + status);});
                            pipe.removeSerializableKey("key", (status) -> {System.out.println("removeSerializableKey: " + status);});
                            pipe.removeSerializableMember("key", "member", (status) -> {System.out.println("removeSerializableMember: " + status);});
                            pipe.sync();
                        }
                    });

                    client.setStatusInfo(clientStatusInfo -> {
                        System.out.println("StatusInfo: " + clientStatusInfo);
                    });

                    client.start();

                    if(client != null){
                        Timer timer = new Timer(true);
                        PandaClient finalClient = client;
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                finalClient.interrupt();
                                System.out.println("Client stopped.");
                            }
                        }, 1000*5);
                    }


                } else if(name.equalsIgnoreCase("test:value")){
                    PandaClient client = new PandaClient("database_test_min","localhost",port);
                    client.setOnLogin(loggedIn -> {
                        if(loggedIn){
                            PipelineSupplier pipe = client.createPipelineSupplier();
                            pipe.setValue("key", "member", 123456.0, (value,status) -> {System.out.println("setValue: " + value + " | Status: " + status);});
                            pipe.addValue("key", "member", -023456.0, (value,status) -> {System.out.println("addValue: " + value + " | Status: " + status);});
                            pipe.getValueKeys((keys,status) -> {System.out.println("getValueKeys: " + keys + " | Status: " + status);});
                            pipe.getValueMemberKeys("key", (keys,status) -> {System.out.println("getValueMemberKeys: " + keys + " | Status: " + status);});
                            pipe.getValueMemberData("key", "member", (data,status) -> {System.out.println("getValueMemberData: " + data + " | Status: " + status);});
                            pipe.getValueKeyData("key", (data,status) -> {System.out.println("getValueKeyData: " + data + " | Status: " + status);});
                            pipe.getValueInfo("key", true, (info,status) -> {System.out.println("getValueInfo: " + info + " | Status: " + status);});
                            pipe.removeValueMember("key", "member", (value,status) -> {System.out.println("removeValueMember: " + value + " | Status: " + status);});
                            pipe.removeValueKey("key", (status) -> {System.out.println("removeValueKey: " + status);});
                            pipe.sync();
                        }
                    });

                    client.setStatusInfo(clientStatusInfo -> {
                        System.out.println("StatusInfo: " + clientStatusInfo);
                    });

                    client.start();

                    if(client != null){
                        Timer timer = new Timer(true);
                        PandaClient finalClient = client;
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                finalClient.interrupt();
                                System.out.println("Client stopped.");
                            }
                        }, 1000*5);
                    }

                } else if(name.equalsIgnoreCase("test:text")){
                    PandaClient client = new PandaClient("database_test_min","localhost",port);
                    client.setOnLogin(loggedIn -> {
                        if(loggedIn){
                            PipelineSupplier pipe = client.createPipelineSupplier();
                            pipe.setText("key", "member", "Hello World!", status -> {System.out.println("setText: " + status);});
                            pipe.getTextKeys((keys,status) -> {System.out.println("getTextKeys: " +status + " -> "+ keys);});
                            pipe.getTextMemberKeys("key", (keys,status) -> {System.out.println("getTextMemberKeys: " +status + " -> "+ keys);});
                            pipe.getTextMemberData("key", "member", (data,status) -> {System.out.println("getTextMemberData: " +status + " -> "+ data);});
                            pipe.getTextKeyData("key", (data,status) -> {System.out.println("getTextKeyData: " +status + " -> size="+ data.size());});
                            pipe.removeTextMember("key", "member", (data,status) -> {System.out.println("removeTextMember: " + status + " -> " + data);});
                            pipe.removeTextKey("key", status -> {System.out.println("removeTextKey: " + status);});
                            pipe.sync();
                        }
                    });

                    client.setStatusInfo(clientStatusInfo -> {
                        System.out.println("StatusInfo: " + clientStatusInfo);
                    });

                    client.start();

                    if(client != null){
                        Timer timer = new Timer(true);
                        PandaClient finalClient = client;
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                finalClient.interrupt();
                                System.out.println("Client stopped.");
                            }
                        }, 1000*5);
                    }


                } else if(name.equalsIgnoreCase("testmin")){
                    PandaClient client = new PandaClient("database_test_min","localhost",port);
                    client.setOnLogin(loggedIn -> {
                        if(loggedIn){
                            System.out.println("ABC");

                            PipelineSupplier pipe = client.createPipelineSupplier();
                            pipe.setText("key_a", "member_a", "value_a", status -> {
                                System.out.println("Key_a = " + status);
                            });
                            System.out.println("Key_A_send = " + pipe.sync());

                            pipe.setText("key_b", "member_b", "value_b", status -> {
                                System.out.println("Key_b = " + status);
                            });
                            System.out.println("Key_B_send = " + pipe.sync());

                            for(int i = 0; i < 50; i++) {
                                int ii = i;
                                PipelineSupplier pipelineSupplier = client.createPipelineSupplier();
                                pipelineSupplier.setText("key"+ii,"mem"+ii, "data means "+ii, status -> {
                                    PipelineSupplier pipelineSupplier1 = client.createPipelineSupplier();
                                    pipelineSupplier1.getTextMemberData("key"+ii, "mem"+ii, (data, status1) -> {
                                        System.out.println("FirstStatus= " + status + "\nAfterStatus= "+status1+ "\nWithData= " + data);
                                    });
                                    pipelineSupplier1.sync();
                                });
                                pipelineSupplier.sync();
                            }

                        }else{
                            System.out.println("Failed connection");
                        }
                    });

                    client.setStatusInfo(clientStatusInfo -> {
                        System.out.println("StatusInfo: " + clientStatusInfo);
                    });

                    client.start();



                    if(client != null){
                        Timer timer = new Timer(true);
                        PandaClient finalClient = client;
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                finalClient.interrupt();
                                System.out.println("Client stopped.");
                            }
                        }, 1000*5);
                    }

                } else if(name.equalsIgnoreCase("testall")){

                    PandaClient client = new PandaClient("database_test","localhost",port);
                    client.start();

                    try{
                        PipelineSupplier pipe = client.createPipelineSupplier();
                        pipe.setText("_key", "_member", "_value", status -> System.out.println("set_status == " + status));
                        pipe.setValue("_key", "_member", -0.00001, (data, status) -> System.out.println("setValue_status == " + status + "("+data+")"));
                        pipe.addValue("_key", "_member", -0.00002, (data, status) -> System.out.println("addValue_status == " + status + "("+data+")"));

                        pipe.addList(ListType.BYTE, "_key", (byte)0, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addList(ListType.BOOLEAN,"_key", false, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addList(ListType.DOUBLE,"_key", 1.045678d, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addList(ListType.INTEGER,"_key", 123, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addList(ListType.LONG,"_key", 123123456789L, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addList(ListType.STRING,"_key", "abc.defg.hijklmnop", status -> System.out.println("addListEntry_status == " + status));
                        pipe.addList(ListType.BOOLEAN_ARRAY,"_key", new Boolean[]{false,true,false}, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addList(ListType.BYTE_ARRAY,"_key", new Byte[]{(byte)0, (byte)1}, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addList(ListType.DOUBLE_ARRAY,"_key", new Double[]{123.000D,654.045D}, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addList(ListType.LONG_ARRAY,"_key", new Long[]{123L,456L,789L}, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addList(ListType.INTEGER_ARRAY,"_key", new Integer[]{987,654,321}, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addList(ListType.STRING_ARRAY,"_key", new String[]{"abc","defg","hijklmnop"}, status -> System.out.println("addListEntry_status == " + status));

                        pipe.setSerializable("_key", "_listkey", new TestSerializableObject(), status -> System.out.println("storeSerializable_status == " + status));

                        pipe.getTextMemberData("_key", "_member", (data, status) -> System.out.println("get_status == " + status + "("+data+")"));
                        pipe.getValueMemberData("_key", "_member", (data, status) -> System.out.println("getValue_status == " + status + "("+data+")"));

                        for(ListType listType : ListType.values()){
                            PipelineSupplier specialPipe = client.createPipelineSupplier();
                            pipe.getListKeys(listType, (data, status) -> {
                                System.out.println("getListKeys(-"+listType+"-)_status == " + status + "("+data+")");

                                for(String key : data){
                                    specialPipe.getListData(listType, key, (data1, status1) -> System.out.println("getListData(-"+listType+"-)_status == " + status1 + "("+data1+")"));
                                }

                                boolean success = specialPipe.sync();
                                System.out.println(" \\\\ get subkeys" +success+ " from " + listType + " with " + data.size());
                            //    System.out.println(" ");
                            });
                        }


                        System.out.println("Pipeline defined.");
                        boolean success = pipe.sync();
                        System.out.println("Pipeline sync called. ["+success+"]");


                    }finally {
                        System.out.println("Test finished");
                        if(client != null){
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

                }else{
                    System.out.println("You should try to use `?` or `help` to get some help!");
                }

            }
        }
    }

}
