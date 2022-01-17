package de.freesoccerhdx.pandadb;

public class PandaDB {



    public static void main(String[] args){


        try{
           PandaServer pandaServer = new PandaServer(21496);
           pandaServer.listenCommands();

        }catch (Exception exception){
            exception.printStackTrace();
        }

    }

}
