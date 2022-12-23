package de.freesoccerhdx.pandadb.serverlisteners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class MemberValueDataStorage extends HashMap<String, Double>{

    public MemberValueDataStorage() {

    }

    public Double removeMember(String key) {
        return super.remove(key);
    }
    public Double setValue(String key, Double value) {
        return super.put(key, value);
    }

    public Double getValue(String member) {
        return super.get(member);
    }

    public ValueMembersInfo getInfo(boolean withMembers) {
        Stream<Double> sortedStream = values().stream();
        Double lowestValue = sortedStream.min((o1, o2) -> o1 < o2 ? 0 : 1).get();
        sortedStream = values().stream();
        Double highestValue = sortedStream.min((o1, o2) -> o1 > o2 ? 0 : 1).get();

        double totalValue = values().stream().mapToDouble(Double::doubleValue).sum();
        double averageValue = totalValue / (1.0 * Math.max(1,size()));

        return new ValueMembersInfo(lowestValue, highestValue, averageValue,size(),withMembers ? new ArrayList<>(keySet()) : null);
    }

    public record ValueMembersInfo(double lowestValue, double highestValue, double averageValue, int size, List<String> members){

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
