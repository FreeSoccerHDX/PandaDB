package de.freesoccerhdx.pandadb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ValueDataStorage extends HashMap<String, Double>{

    private double lowestValue = Double.MAX_VALUE;
    private double highestValue = Double.MIN_VALUE;
    private double totalValue = 0.0;
    private double averageValue = 0.0;

    public ValueDataStorage() {

    }

    public Double getLowestValue(){
        return lowestValue == Double.MAX_VALUE ? null : lowestValue;
    }
    public Double getHighestValue(){
        return highestValue == Double.MIN_VALUE ? null : highestValue;
    }
    public double getTotalValue(){
        return totalValue;
    }
    public double getAverageValue(){
        return averageValue;
    }


    @Override
    public Double remove(Object key) {
        Double oldvalue = super.remove(key);
        if(oldvalue != null) {
            int newsize = size();
            if (newsize > 0) {
                totalValue -= oldvalue;
                averageValue = totalValue / (1.0*size());
            } else {
                lowestValue = Double.MAX_VALUE;
                highestValue = Double.MIN_VALUE;
                averageValue = 0.0;
                totalValue = 0.0;
            }
        }
        return oldvalue;
    }

    @Override
    public Double put(String key, Double value) {
        Double oldvalue = super.put(key, value);

        totalValue += value;
        if(oldvalue != null){
            totalValue -= oldvalue;
        }
        averageValue = totalValue / (1.0*size());


        return oldvalue;
    }

    @Override
    public Double get(Object key) {
        Double value = super.get(key);
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
        return new ValueMembersInfo(lowestValue,highestValue,averageValue,size(),withMembers ? new ArrayList<>(keySet()) : null);
    }

    public static class ValueMembersInfo{

        private double lowestValue;
        private double highestValue;
        private double averageValue;
        private int size;
        private List<String> members;

        public ValueMembersInfo(double lowestValue, double highestValue, double averageValue, int size, List<String> members) {
            this.lowestValue = lowestValue;
            this.highestValue = highestValue;
            this.averageValue = averageValue;
            this.size = size;
            this.members = members;
        }
        public double getLowestValue() {
            return lowestValue;
        }

        public double getHighestValue() {
            return highestValue;
        }

        public double getAverageValue() {
            return averageValue;
        }

        public int getSize() {
            return size;
        }

        public List<String> getMembers() {
            return members;
        }

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
