package de.freesoccerhdx.pandadb.clientutils;

import org.json.JSONObject;

public class DatabaseReader {

    private final JSONObject jsonObject;
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

    public String getStringOrDefault(String key, String def){
        if(hasKey(key)){
            return getString(key);
        }else{
            return def;
        }
    }

    public Double getDouble(String key){
        return this.jsonObject.getDouble(key);
    }

    public Double getDoubleOrDefault(String key, Double def){
        if(hasKey(key)){
            return getDouble(key);
        }else{
            return def;
        }
    }

    public Integer getInt(String key){
        return this.jsonObject.getInt(key);
    }

    public Integer getIntOrDefault(String key, Integer def){
        if(hasKey(key)){
            return getInt(key);
        }else{
            return def;
        }
    }

    public Long getLong(String key){
        return this.jsonObject.getLong(key);
    }

    public Long getLongOrDefault(String key, Long def){
        if(hasKey(key)){
            return getLong(key);
        }else{
            return def;
        }
    }

    public Boolean getBoolean(String key){
        return this.jsonObject.getBoolean(key);
    }

    public Boolean getBooleanOrDefault(String key, Boolean def){
        if(hasKey(key)){
            return getBoolean(key);
        }else{
            return def;
        }
    }

    public Float getFloat(String key){
        return this.jsonObject.getFloat(key);
    }

    public Float getFloatOrDefault(String key, Float def){
        if(hasKey(key)){
            return getFloat(key);
        }else{
            return def;
        }
    }


}
