package de.freesoccerhdx.pandadb;

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

import java.io.IOException;

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

    public PandaServer(int port) throws IOException {
        simpleSocketServer = new SimpleSocketServer(port);
        dataStorage = new ServerDataStorage();

        this.listListener = new ListListener(this);
        this.removeListener = new RemoveListener(this);
        this.textListener = new TextListener(this);
        this.valueListener = new ValueListener(this);
        this.listKeysListener = new ListKeysListener(this);
        this.serializableListener = new SerializableListener(this);
        this.memberDataListener = new MemberDataListener(this);

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
                //System.err.println(msg);
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

                clientSocket.sendNewMessage("dbpiperesult",result,null);
            }
        });

    }

    public void listenCommands() {
        while(this.simpleSocketServer.isRunning()){
            String name = System.console().readLine();
            if(name != null){
                System.out.println("You wrote: '"+name+"'");
            }
        }
    }

    public ServerDataStorage getDataStorage() {
        return this.dataStorage;
    }

    public SimpleSocketServer getSimpleSocketServer() {
        return this.simpleSocketServer;
    }
}
