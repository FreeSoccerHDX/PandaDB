package de.freesoccerhdx.pandadb.serverutils;

import de.freesoccerhdx.pandadb.Status;
import de.freesoccerhdx.pandadb.serverutils.datastorage.MemberValueDataStorage;
import de.freesoccerhdx.simplesocket.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class DataChannelListener {


    public JSONObject createTotalObject(String questid, Object info) {
        if(questid != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", questid);
            if (info != null) {
                if(info instanceof Status s) {
                    jsonObject.put("s", s.ordinal());
                }else if(info instanceof Pair pair) {
                    jsonObject.put("s", ((Status)pair.getFirst()).ordinal());
                    if(pair.getSecond() != null) {
                        Object value = pair.getSecond();
                        if(value instanceof Pair[]) {
                            Pair<String, Double>[] pairs = (Pair<String, Double>[]) value;
                            JSONArray array = new JSONArray();
                            for (Pair<String, Double> pair2 : pairs) {
                                array.put(pair2.getFirst());
                                array.put(pair2.getSecond());
                            }
                            jsonObject.put("i", array);
                        }else if(value instanceof Byte[] bytes) {
                            JSONArray array = new JSONArray();
                            for(int i = 0; i < bytes.length; i++) {
                                array.put(i, bytes[i] == null ? null : bytes[i].intValue());
                            }
                            jsonObject.put("i", array);
                        }else if(value instanceof MemberValueDataStorage) {
                            jsonObject.put("i", ((HashMap<String,Double>) value));
                        }else if(value instanceof HashMap map) {
                            jsonObject.put("i", ((HashMap<String, String>)map));
                        }else {
                            jsonObject.put("i", pair.getSecond());
                        }
                    }
                }
            }
            return jsonObject;
        }
        return null;
    }


}
