package de.freesoccerhdx.pandadb;

import java.io.File;

public class PandaDB {


    private static final File databaseFile = new File("C:\\Users\\timau\\Documents\\intellij-workspace\\trash","panda.db");
    private static final File dataTreeFile = new File("C:\\Users\\timau\\Documents\\intellij-workspace\\trash","datatree.txt");


    public static void main(String[] args) {
        System.out.println("Starting Server with Params: "+String.join("\n   ", args));

        try{
           PandaServer pandaServer = new PandaServer(21496, databaseFile, dataTreeFile);
           pandaServer.listenCommands();

        }catch (Exception exception){
            exception.printStackTrace();
        }

    }

}
