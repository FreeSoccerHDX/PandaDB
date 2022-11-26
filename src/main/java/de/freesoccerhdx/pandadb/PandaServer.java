package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.clientutils.ClientCommands;
import de.freesoccerhdx.pandadb.serverlisteners.ListKeysListener;
import de.freesoccerhdx.pandadb.serverlisteners.ListListener;
import de.freesoccerhdx.pandadb.serverlisteners.MemberDataListener;
import de.freesoccerhdx.pandadb.serverlisteners.RemoveListener;
import de.freesoccerhdx.pandadb.serverlisteners.SerializableListener;
import de.freesoccerhdx.pandadb.serverlisteners.TextListener;
import de.freesoccerhdx.pandadb.serverlisteners.ValueListener;
import de.freesoccerhdx.simplesocket.server.ClientSocket;
import de.freesoccerhdx.simplesocket.server.ServerListener;
import de.freesoccerhdx.simplesocket.server.SimpleSocketServer;
import org.json.JSONObject;

import javax.security.auth.login.LoginContext;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PandaServer {

    private SimpleSocketServer simpleSocketServer;
    private ServerDataStorage dataStorage;

    private ListListener listListener;
    private RemoveListener removeListener;
    private TextListener textListener;
    private ValueListener valueListener;
    private ListKeysListener listKeysListener;
    private SerializableListener serializableListener;
    private MemberDataListener memberDataListener;

    private int port;

    public PandaServer(int port) throws IOException {
        dataStorage = new ServerDataStorage();
        this.port = port;
        simpleSocketServer = new SimpleSocketServer(port);


        this.listListener = new ListListener(this);
        this.removeListener = new RemoveListener(this);
        this.textListener = new TextListener(this);
        this.valueListener = new ValueListener(this);
        this.listKeysListener = new ListKeysListener(this);
        this.serializableListener = new SerializableListener(this);
        this.memberDataListener = new MemberDataListener(this);

        simpleSocketServer.setServerListener("testping", new ServerListener() {
            @Override
            public void recive(SimpleSocketServer simpleSocketServer, ClientSocket clientSocket, String channel, String msg) {
                clientSocket.sendNewMessage("testping", msg, null);
            }
        });


        simpleSocketServer.setServerListener("datatree", new ServerListener() {
            @Override
            public void recive(SimpleSocketServer simpleSocketServer, ClientSocket clientSocket, String channel, String msg) {
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
            public void recive(SimpleSocketServer simpleSocketServer, ClientSocket clientSocket, String __channel, String msg) {
                Thread responseThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        /*
                        System.err.println("####################");
                        System.err.println("##########===))))  DBPIPE: " +msg+ "");
                        System.err.println("####################");*/
                        JSONObject jsonObject = new JSONObject(msg);
                        int size = jsonObject.getInt("size");
                        String uuid = jsonObject.getString("uuid");

                        JSONObject result = new JSONObject();
                        result.put("size", size);
                        result.put("uuid", uuid);
                        for(int i = 0; i < size; i++){
                            int channelid = jsonObject.getInt("c"+i);
                            PandaClientChannel channel = PandaClientChannel.values()[channelid];
                            String data = jsonObject.getJSONObject("d"+i).toString();

                            if(channel == PandaClientChannel.LISTKEYS){
                                JSONObject resultJSON = listKeysListener.parseData(channel, data);
                                if(resultJSON != null) {
                                    result.put("r"+i, resultJSON);
                                }
                            }else if(channel == PandaClientChannel.GET_MEMBER_DATA || channel == PandaClientChannel.GET_SERIALIZABLE_MEMBER_DATA){
                                JSONObject resultJSON = memberDataListener.parseData(channel, data);
                                if(resultJSON != null) {
                                    result.put("r"+i, resultJSON);
                                }
                            }else if(channel == PandaClientChannel.GETVALUE
                                    || channel == PandaClientChannel.SETVALUE
                                    || channel == PandaClientChannel.ADDVALUE
                                    || channel == PandaClientChannel.INFOVALUES){
                                JSONObject resultJSON = valueListener.parseData(channel, data);
                                if(resultJSON != null) {
                                    result.put("r"+i, resultJSON);
                                }
                            }else if(channel == PandaClientChannel.REMOVE){
                                JSONObject resultJSON = removeListener.parseData(channel, data);
                                if(resultJSON != null) {
                                    result.put("r"+i, resultJSON);
                                }
                            }else if(channel == PandaClientChannel.ADDLIST || channel == PandaClientChannel.GETLIST){
                                JSONObject resultJSON = listListener.parseData(channel, data);
                                if(resultJSON != null) {
                                    result.put("r"+i, resultJSON);
                                }
                            }else if(channel == PandaClientChannel.SET || channel == PandaClientChannel.GET){
                                JSONObject resultJSON = textListener.parseData(channel, data);
                                if(resultJSON != null) {
                                    result.put("r"+i, resultJSON);
                                }
                            }else if(channel == PandaClientChannel.GET_STORED_SERIALIZABLE || channel == PandaClientChannel.STORE_SERIALIZABLE){
                                JSONObject resultJSON = serializableListener.parseData(channel, data);
                                if(resultJSON != null) {
                                    result.put("r"+i, resultJSON);
                                }
                            }else{
                                System.out.println("[PandaServer] Pipeline-Channel not found! Name="+channel + " (Data="+data+")");
                            }

                        }
/*
                        System.out.println("--123456789");
                        System.out.println("--");
                        System.out.println("Send back: " + clientSocket.sendNewMessage("dbpiperesult",result,null));
                        System.out.println("--");
                        System.out.println("--123456789");
 */
                        clientSocket.sendNewMessage("dbpiperesult",result,null);
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

    // This will block the Thread and allows console input to handle some stuff
    public void listenCommands() {
        while(this.simpleSocketServer.isRunning()){
            String name = System.console().readLine();
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
                } else if(name.equalsIgnoreCase("datatree")){
                    System.out.println("Start writing Data-Tree");
                    getDataStorage().generateDataTree();
                    System.out.println("Data-Tree created");

                } else if(name.equalsIgnoreCase("testmin")){
                    PandaClient client = new PandaClient("database_test_min","localhost",port);
                    client.setOnLogin(loggedIn -> {
                        if(loggedIn){
                            System.out.println("ABC");

                            PipelineSupplier pipe = client.createPipelineSupplier();
                            pipe.set("key_a", "member_a", "value_a", status -> {
                                System.out.println("Key_a = " + status);
                            });
                            System.out.println("Key_A_send = " + pipe.sync());

                            pipe.set("key_b", "member_b", "value_b", status -> {
                                System.out.println("Key_b = " + status);
                            });
                            System.out.println("Key_B_send = " + pipe.sync());

                            for(int i = 0; i < 50; i++){
                                int ii = i;
                                client.set("key"+ii,"mem"+ii, "data means "+ii, status -> {
                                    client.get("key"+ii, "mem"+ii, (data, status1) -> {
                                        System.out.println("FirstStatus= " + status + "\nAfterStatus= "+status1+ "\nWithData= " + data);
                                    });
                                });
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
                        pipe.set("_key", "_member", "_value", status -> System.out.println("set_status == " + status));
                        pipe.setValue("_key", "_member", -0.00001, (data, status) -> System.out.println("setValue_status == " + status + "("+data+")"));
                        pipe.addValue("_key", "_member", -0.00002, (data, status) -> System.out.println("addValue_status == " + status + "("+data+")"));

                        pipe.addListEntry("_key", "_listkey", ListType.BYTE, (byte)0, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addListEntry("_key", "_listkey", ListType.BOOLEAN, false, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addListEntry("_key", "_listkey", ListType.DOUBLE, 1.045678d, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addListEntry("_key", "_listkey", ListType.INTEGER, 123, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addListEntry("_key", "_listkey", ListType.LONG, 123123456789L, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addListEntry("_key", "_listkey", ListType.STRING, "abc.defg.hijklmnop", status -> System.out.println("addListEntry_status == " + status));
                        pipe.addListEntry("_key", "_listkey", ListType.BOOLEAN_ARRAY, new Boolean[]{false,true,false}, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addListEntry("_key", "_listkey", ListType.BYTE_ARRAY, new Byte[]{(byte)0, (byte)1}, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addListEntry("_key", "_listkey", ListType.DOUBLE_ARRAY, new Double[]{123.000D,654.045D}, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addListEntry("_key", "_listkey", ListType.LONG_ARRAY, new Long[]{123L,456L,789L}, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addListEntry("_key", "_listkey", ListType.INTEGER_ARRAY, new Integer[]{987,654,321}, status -> System.out.println("addListEntry_status == " + status));
                        pipe.addListEntry("_key", "_listkey", ListType.STRING_ARRAY, new String[]{"abc","defg","hijklmnop"}, status -> System.out.println("addListEntry_status == " + status));

                        pipe.storeSerializable("_key", "_listkey", new TestSerializableObject(), status -> System.out.println("storeSerializable_status == " + status));

                        pipe.get("_key", "_member", (data, status) -> System.out.println("get_status == " + status + "("+data+")"));
                        pipe.getValue("_key", "_member", (data, status) -> System.out.println("getValue_status == " + status + "("+data+")"));

                        for(ListType listType : ListType.values()){
                            PipelineSupplier specialPipe = client.createPipelineSupplier();
                            pipe.getListKeys(listType, (data, status) -> {
                                System.out.println("getListKeys(-"+listType+"-)_status == " + status + "("+data+")");

                                for(String key : data){
                                    specialPipe.getListKeys(key, listType, (data1, status1) -> {
                            //            System.out.println("_    <<<<getListKeys["+key+" of "+listType+"]_status == " + status1 + "("+data1+")");

                                        PipelineSupplier innerPipe = client.createPipelineSupplier();

                                        for(String listkey : data1){
                                            innerPipe.getList(key,listkey,listType,(data2, status2) -> {
                                                System.out.println("_        >>>>getList["+listkey+" of "+key+" of "+ listType +"]_status == " + status2 + "("+data2+")");
                                            });
                                        }

                                        boolean success = innerPipe.sync();

                                        System.out.println(" >>> " + listType + ">"+key+" = " + success);
                                        System.out.println(" ");
                                    });
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
