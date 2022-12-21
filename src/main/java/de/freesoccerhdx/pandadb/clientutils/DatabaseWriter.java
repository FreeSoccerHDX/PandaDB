package de.freesoccerhdx.pandadb.clientutils;

import org.json.JSONObject;

public class DatabaseWriter {

    private final JSONObject jsonObject;

    public DatabaseWriter(){
        this.jsonObject = new JSONObject();
    }

    public String toJSON(){
        return this.jsonObject.toString();
    }

    public void writeString(String key, String info){
        jsonObject.put(key, info);
    }

    public void writeInt(String key, int info){
        jsonObject.put(key, info);
    }

    public void writeDouble(String key, double info){
        jsonObject.put(key, info);
    }

    public void writeFloat(String key, float info){
        jsonObject.put(key, info);
    }

    public void writeBoolean(String key, boolean info){
        jsonObject.put(key, info);
    }

    public void writeLong(String key, long info){
        jsonObject.put(key, info);
    }

}
