package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.serverlisteners.ListKeysListener;
import de.freesoccerhdx.pandadb.serverlisteners.ListListener;
import de.freesoccerhdx.pandadb.serverlisteners.RemoveListener;
import de.freesoccerhdx.pandadb.serverlisteners.TextListener;
import de.freesoccerhdx.pandadb.serverlisteners.ValueListener;
import de.freesoccerhdx.simplesocket.server.SimpleSocketServer;

import java.io.IOException;

public class PandaServer {

    private SimpleSocketServer simpleSocketServer;
    private ServerDataStorage dataStorage;

    public PandaServer(int port) throws IOException {
        simpleSocketServer = new SimpleSocketServer(port);
        dataStorage = new ServerDataStorage();

        new ListListener(this);
        new RemoveListener(this);
        new TextListener(this);
        new ValueListener(this);
        new ListKeysListener(this);
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
