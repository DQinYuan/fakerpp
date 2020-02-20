package org.testd.fakerpp.core.util;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedRandom<T> {

    private TreeMap<Double, WeightedItem<T>> weightMap = new TreeMap<>();

    private double count = 0;

    public static class WeightedItem<T>{
        private int weight;
        private T item;

        public WeightedItem(int weight, T item) {
            this.weight = weight;
            this.item = item;
        }
    }

    public WeightedRandom(List<WeightedItem<T>> weightedItems){
        for (WeightedItem<T> weightedItem : weightedItems) {
            weightMap.put(count, weightedItem);
            count += weightedItem.weight;
        }
    }

    public T random(){
        double index = (SeedableThreadLocalRandom.nextDouble() * count);
        return weightMap.floorEntry(index).getValue().item;
    }

}
