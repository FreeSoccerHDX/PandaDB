package de.freesoccerhdx.pandadb.clientutils;

import org.json.JSONObject;

public class DatabaseReader {

    private JSONObject jsonObject;
    public DatabaseReader(JSONObject jsonObject){
        this.jsonObject = jsonObject;
    }

    public String toJSON(){
        return jsonObject.toString();
    }

    public boolean hasKey(String key){
        return this.jsonObject.has(key);
    }

    public String getString(String key){
        return this.jsonObject.getString(key);
    }

    public Double getDouble(String key){
        return this.jsonObject.getDouble(key);
    }

    public Integer getInt(String key){
        return this.jsonObject.getInt(key);
    }

    public Long getLong(String key){
        return this.jsonObject.getLong(key);
    }

    public Boolean getBoolean(String key){
        return this.jsonObject.getBoolean(key);
    }

    public Float getFloat(String key){
        return this.jsonObject.getFloat(key);
    }



}
