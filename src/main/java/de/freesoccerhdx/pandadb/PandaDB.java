package de.freesoccerhdx.pandadb;

public class PandaDB {



    public static void main(String[] args) {
        System.out.println("Starting Server with Params: "+String.join("\n   ", args));

        try{
           PandaServer pandaServer = new PandaServer(21496);
           pandaServer.listenCommands();

        }catch (Exception exception){
            exception.printStackTrace();
        }

    }

}
