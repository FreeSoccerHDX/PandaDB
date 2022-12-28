package de.freesoccerhdx.pandadb;

import org.json.JSONArray;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public abstract class ListType<T> {

    public static final ArrayList<ListType> VALUES = new ArrayList<>();
    public static ListType[] values(){
        return VALUES.toArray(new ListType[0]);
    }

    public static final ListType<String> STRING = new ListType(String.class){
        @Override
        public void write(DataOutputStream dos, Object obj) throws IOException {
            dos.writeUTF((String) obj);
        }

        @Override
        public String read(DataInputStream dis) throws IOException {
            return dis.readUTF();
        }

        @Override
        public String parse(String obj) {
            return obj;
        }
    };

    public static final ListType<String[]> STRING_ARRAY = new ListType(String[].class){
        @Override
        public void write(DataOutputStream dos, Object obj) throws IOException {
            if(obj instanceof JSONArray jsonArray){
                dos.writeInt(jsonArray.length());
                for(Object listobj : jsonArray.toList()){
                    dos.writeUTF((String) listobj);
                }
            }else {
                String[] array = (String[]) obj;
                dos.writeInt(array.length);
                for (String l : array) {
                    dos.writeUTF(l);
                }
            }
        }

        @Override
        public String[] read(DataInputStream dis) throws IOException {
            String[] array = new String[dis.readInt()];
            for(int i = 0; i < array.length; i++){
                array[i] = (String) STRING.read(dis);
            }
            return array;
        }

        @Override
        public String[] parse(String obj) {
            if(obj.length() >= 2) {
                return obj.substring(1, obj.length() - 1).split(",");
            }

            return null;
        }
    };

    public static final ListType<Boolean> BOOLEAN = new ListType(Boolean.class){
        @Override
        public void write(DataOutputStream dos, Object obj) throws IOException {
            dos.writeBoolean((Boolean) obj);
        }

        @Override
        public Boolean read(DataInputStream dis) throws IOException {
            return dis.readBoolean();
        }

        @Override
        public Boolean parse(String obj) {
            if(obj.equals("0")){
                return false;
            }else if(obj.equals("1")){
                return true;
            }

            return Boolean.valueOf(obj);
        }
    };

    public static final ListType<Boolean[]> BOOLEAN_ARRAY = new ListType(Boolean[].class){
        @Override
        public void write(DataOutputStream dos, Object obj) throws IOException {
            if(obj instanceof JSONArray jsonArray){
                dos.writeInt(jsonArray.length());
                for(Object listobj : jsonArray.toList()){
                    dos.writeBoolean((Boolean) listobj);
                }
            }else {
                Boolean[] array = (Boolean[]) obj;
                dos.writeInt(array.length);
                for (Boolean l : array) {
                    dos.writeBoolean(l);
                }
            }
        }

        @Override
        public Boolean[] read(DataInputStream dis) throws IOException {
            Boolean[] array = new Boolean[dis.readInt()];
            for(int i = 0; i < array.length; i++){
                array[i] = (Boolean) BOOLEAN.read(dis);
            }
            return array;
        }

        @Override
        public Boolean[] parse(String obj) {
            if(obj.length() >= 2) {
                String[] dat = obj.substring(1, obj.length() - 1).split(",");
                Boolean[] data = new Boolean[dat.length];
                for(int i = 0; i < dat.length; i++){
                    data[i] = (Boolean) ListType.BOOLEAN.parse(dat[i]);
                }
                return data;
            }

            return null;
        }
    };

    public static final ListType<Integer> INTEGER = new ListType(Integer.class){
        @Override
        public void write(DataOutputStream dos, Object obj) throws IOException {
            dos.writeInt((Integer) obj);
        }

        @Override
        public Object read(DataInputStream dis) throws IOException {
            return dis.readInt();
        }

        @Override
        public Integer parse(String obj) {
            return Integer.parseInt(obj);
        }
    };

    public static final ListType<Integer[]> INTEGER_ARRAY = new ListType(Integer[].class){
        @Override
        public void write(DataOutputStream dos, Object obj) throws IOException {
            if(obj instanceof JSONArray jsonArray){
                dos.writeInt(jsonArray.length());
                for(Object listobj : jsonArray.toList()){
                    dos.writeInt((Integer) listobj);
                }
            }else {
                Integer[] array = (Integer[]) obj;
                dos.writeInt(array.length);
                for (Integer l : array) {
                    dos.writeInt(l);
                }
            }

        }

        @Override
        public Integer[] read(DataInputStream dis) throws IOException {
            Integer[] array = new Integer[dis.readInt()];
            for(int i = 0; i < array.length; i++){
                array[i] = (Integer) INTEGER.read(dis);
            }
            return array;
        }

        @Override
        public Integer[] parse(String obj) {
            if(obj.length() >= 2) {
                String[] dat = obj.substring(1, obj.length() - 1).split(",");
                Integer[] data = new Integer[dat.length];
                for(int i = 0; i < dat.length; i++){
                    data[i] = (Integer) ListType.INTEGER.parse(dat[i]);
                }
                return data;
            }

            return null;
        }
    };

    public static final ListType<Byte> BYTE = new ListType(Byte.class){
        @Override
        public void write(DataOutputStream dos, Object obj) throws IOException {
            if(obj instanceof Number number){
                dos.writeByte(number.byteValue());
            }else {
                dos.writeByte((Byte) obj);
            }
        }

        @Override
        public Byte read(DataInputStream dis) throws IOException {
            return dis.readByte();
        }

        @Override
        public Byte parse(String obj) {
            return Byte.parseByte(obj);
        }
    };

    public static final ListType<Byte[]> BYTE_ARRAY = new ListType(Byte[].class){
        @Override
        public void write(DataOutputStream dos, Object obj) throws IOException {
            if(obj instanceof JSONArray jsonArray){
                dos.writeInt(jsonArray.length());
                for(Object listobj : jsonArray.toList()){
                    BYTE.write(dos, listobj);
                }
            }else {
                Byte[] array = (Byte[]) obj;
                dos.writeInt(array.length);
                for (Byte l : array) {
                    dos.writeByte(l);
                }
            }
        }

        @Override
        public Byte[] read(DataInputStream dis) throws IOException {
            Byte[] array = new Byte[dis.readInt()];
            for(int i = 0; i < array.length; i++){
                array[i] = (Byte) BYTE.read(dis);
            }
            return array;
        }

        @Override
        public Byte[] parse(String obj) {
            if(obj.length() >= 2) {
                String[] dat = obj.substring(1, obj.length() - 1).split(",");
                Byte[] data = new Byte[dat.length];
                for(int i = 0; i < dat.length; i++){
                    data[i] = (Byte) ListType.BYTE.parse(dat[i]);
                }
                return data;
            }

            return null;
        }
    };
    public static final ListType<Double> DOUBLE = new ListType(Double.class){
        @Override
        public void write(DataOutputStream dos, Object obj) throws IOException {
            if(obj instanceof Number number){
                dos.writeDouble(number.doubleValue());
            }else {
                dos.writeDouble((Double) obj);
            }
        }

        @Override
        public Double read(DataInputStream dis) throws IOException {
            return dis.readDouble();
        }

        @Override
        public Double parse(String obj) {
            return Double.parseDouble(obj);
        }
    };

    public static final ListType<Double[]> DOUBLE_ARRAY = new ListType(Double[].class){
        @Override
        public void write(DataOutputStream dos, Object obj) throws IOException {
            if(obj instanceof JSONArray jsonArray){
                dos.writeInt(jsonArray.length());
                for(Object listobj : jsonArray.toList()){
                    DOUBLE.write(dos, listobj);
                }
            }else {
                Double[] array = (Double[]) obj;
                dos.writeInt(array.length);
                for (Double l : array) {
                    dos.writeDouble(l);
                }
            }
        }

        @Override
        public Double[] read(DataInputStream dis) throws IOException {
            Double[] array = new Double[dis.readInt()];
            for(int i = 0; i < array.length; i++){
                array[i] = (Double) DOUBLE.read(dis);
            }
            return array;
        }

        @Override
        public Double[] parse(String obj) {
            if(obj.length() >= 2) {
                String[] dat = obj.substring(1, obj.length() - 1).split(",");
                Double[] data = new Double[dat.length];
                for(int i = 0; i < dat.length; i++){
                    data[i] = (Double) ListType.DOUBLE.parse(dat[i]);
                }
                return data;
            }

            return null;
        }
    };

    public static final ListType<Long> LONG = new ListType(Long.class){
        @Override
        public void write(DataOutputStream dos, Object obj) throws IOException {
            if(obj instanceof Number number){
                dos.writeLong(number.longValue());
            }else {
                dos.writeLong((Long) obj);
            }
        }

        @Override
        public Long read(DataInputStream dis) throws IOException {
            return dis.readLong();
        }

        @Override
        public Long parse(String obj) {
            return  Long.parseLong(obj);
        }
    };

    public static final ListType<Long[]> LONG_ARRAY = new ListType(Long[].class){
        @Override
        public void write(DataOutputStream dos, Object obj) throws IOException {
            if(obj instanceof JSONArray jsonArray){
                dos.writeInt(jsonArray.length());
                for(Object listobj : jsonArray.toList()){
                    LONG.write(dos, listobj);
                }
            }else {
                Long[] array = (Long[]) obj;
                dos.writeInt(array.length);
                for (Long l : array) {
                    dos.writeLong(l);
                }
            }
        }

        @Override
        public Long[] read(DataInputStream dis) throws IOException {
            Long[] array = new Long[dis.readInt()];
            for(int i = 0; i < array.length; i++){
                array[i] = (Long) LONG.read(dis);
            }
            return array;
        }
        @Override
        public Long[] parse(String obj) {
            if(obj.length() >= 2) {
                String[] dat = obj.substring(1, obj.length() - 1).split(",");
                Long[] data = new Long[dat.length];
                for(int i = 0; i < dat.length; i++){
                    data[i] = (Long) ListType.LONG.parse(dat[i]);
                }
                return data;
            }

            return null;
        }
    };



    private final Class<T> type;
    private final int ordinal;
    private ListType(Class<T> typeclass) {
        this.type = typeclass;
        VALUES.add(this);
        ordinal = VALUES.size() - 1;
    }

    public int ordinal(){
        return ordinal;
    }

    public boolean check(T tocheck){
        return tocheck != null && tocheck.getClass().equals(type);
    }
    public abstract T parse(String obj);
    public abstract void write(DataOutputStream dos, Object obj) throws IOException;
    public abstract Object read(DataInputStream dis) throws IOException;

    public String name(){
        return type.getSimpleName().toUpperCase();
    }

    @Override
    public String toString() {
        return "ListType{" +
                "type=" + type.getSimpleName() +
                ", ordinal=" + ordinal +
                '}';
    }

    public String asString(Object obj) {
        StringBuilder result;
        if(obj instanceof JSONArray jsonArray){
            int c = 0;
            result = new StringBuilder("[");
            for(Object listobj : jsonArray.toList()){
                result.append(listobj);
                if(c != jsonArray.length()-1){
                    result.append(",");
                }
                c++;
            }
            result.append("]");
        }else if(obj.getClass().isArray()){
            Object[] array = (Object[]) obj;
            int c = 0;
            result = new StringBuilder("[");
            for (Object l : array) {
                result.append(l);
                if(c != array.length-1){
                    result.append(",");
                }
                c++;
            }
            result.append("]");
        }else{
            result = new StringBuilder(String.valueOf(obj));
        }
        return result.toString();
    }
}
