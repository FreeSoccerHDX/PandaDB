package de.freesoccerhdx.pandadb.serverutils;

import de.freesoccerhdx.pandadb.PandaServer;
import de.freesoccerhdx.pandadb.clientutils.PandaClientChannel;
import de.freesoccerhdx.pandadb.serverutils.datastorage.ByteArrayDataStorage;
import org.json.JSONArray;
import org.json.JSONObject;

public class ByteArrayListener extends DataChannelListener {
    
    private final PandaServer server;
    public ByteArrayListener(PandaServer pandaServer) {
        this.server = pandaServer;
    }


    public JSONObject parseData(PandaClientChannel channel, JSONObject jsonObject) {
        String questid = jsonObject.has("q") ? jsonObject.getString("q") : null;
        String key = jsonObject.has("k") ? jsonObject.getString("k") : null;

        Object info = null;

        ByteArrayDataStorage sds = server.getDataStorage().getByteArrayData();
        if(channel == PandaClientChannel.BYTEARRAY_CREATE_NEW) {
            int size = jsonObject.getInt("s");
            info = sds.createNewArray(key, size);
        }else if(channel == PandaClientChannel.BYTEARRAY_SET_INDEX) {
            int index = jsonObject.getInt("i");
            byte value = (byte) jsonObject.getInt("v");
            info = sds.setIndex(key, index, value);
        }else if(channel == PandaClientChannel.BYTEARRAY_GET_INDEX) {
            int index = jsonObject.getInt("i");
            info = sds.getIndex(key, index);
        }else if(channel == PandaClientChannel.BYTEARRAY_GET_INDEXES) {
            JSONArray array = jsonObject.getJSONArray("i");
            int[] indexes = new int[array.length()];
            for(int i = 0; i < indexes.length; i++){
                indexes[i] = array.getInt(i);
            }
            info = sds.getIndexes(key, indexes);
        }else if(channel == PandaClientChannel.BYTEARRAY_GET_KEY_DATA) {
            info = sds.getKeyData(key);
        }else if(channel == PandaClientChannel.BYTEARRAY_GET_KEY_SIZE) {
            info = sds.getKeySize(key);
        }else if(channel == PandaClientChannel.BYTEARRAY_GET_KEYS) {
            info = sds.getKeys();
        }else if(channel == PandaClientChannel.BYTEARRAY_REMOVE_KEY) {
            info = sds.deleteKey(key);
        }else if(channel == PandaClientChannel.BYTEARRAY_CLEAR_KEY) {
            info = sds.clearKey(key);
        }


        return createTotalObject(questid, info);
    }



}
