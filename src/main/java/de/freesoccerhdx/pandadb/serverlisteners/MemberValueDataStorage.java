package de.freesoccerhdx.pandadb.serverlisteners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class MemberValueDataStorage extends HashMap<String, Double>{

    private double lowestValue = Double.MAX_VALUE;
    private double highestValue = Double.MIN_VALUE;
    private double totalValue = 0.0;
    private double averageValue = 0.0;

    public MemberValueDataStorage() {

    }

    public Double removeMember(String key) {
        Double oldvalue = super.remove(key);
        if(oldvalue != null) {
            int newsize = size();
        }
        return oldvalue;
    }
    public Double setValue(String key, Double value) {
        Double oldvalue = super.put(key, value);
        return oldvalue;
    }

    public Double getValue(String member) {
        Double value = super.get(member);
        return value;
    }

    public ValueMembersInfo getInfo(boolean withMembers) {
        Stream<Double> sortedStream = values().stream();
        lowestValue = sortedStream.min((o1, o2) -> {
            return o1 < o2 ? 0 : 1;
        }).get();
        sortedStream = values().stream();
        highestValue = sortedStream.min((o1, o2) -> {
            return o1 > o2 ? 0 : 1;
        }).get();

        totalValue = values().stream().mapToDouble(Double::doubleValue).sum();
        averageValue = totalValue / (1.0*size());

        return new ValueMembersInfo(lowestValue,highestValue,averageValue,size(),withMembers ? new ArrayList<>(keySet()) : null);
    }

    public static record ValueMembersInfo(double lowestValue, double highestValue, double averageValue, int size, List<String> members){

        @Override
        public String toString() {
            return "ValueMembersInfo{" +
                    "lowestValue=" + lowestValue +
                    ", highestValue=" + highestValue +
                    ", averageValue=" + averageValue +
                    ", size=" + size +
                    ", members=" + members +
                    '}';
        }
    }


}
