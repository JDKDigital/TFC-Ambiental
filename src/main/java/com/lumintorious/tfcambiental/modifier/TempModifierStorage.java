package com.lumintorious.tfcambiental.modifier;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TempModifierStorage implements Iterable<TempModifier>
{
    private List<TempModifier> list = new LinkedList<>();

//    private TempModifier put(String key, TempModifier value) {
//        if((value.getChange() == 0f && value.getPotency() == 0f)) {
//            return null;
//        }
//        TempModifier modifier = map.get(key);
//        if(modifier != null) {
//            modifier.absorb(value);
//            return modifier;
//        }else {
//            return map.put(key, value);
//        }
//    }

    public TempModifierStorage keepOnlyNEach(int n) {
        var grouped = list.stream().collect(Collectors.groupingBy(TempModifier::getUnlocalizedName));
        this.list = grouped
                .entrySet()
                .stream()
                .flatMap(entry ->
                        entry.getValue()
                                .stream()
                                .sorted(Comparator.reverseOrder())
                                .limit(n)
                ).toList();
        return this;
    }

    public void add(TempModifier value) {
        if (value == null) {
            return;
        }
        list.add(value);
    }

    public void add(Optional<TempModifier> tempModifier) {
        tempModifier.ifPresent(mod -> list.add(mod));
    }

    public boolean contains(TempModifier value) {
        return list.contains(value);
    }

    public boolean contains(String name) {
        return list.stream().anyMatch(mod -> mod.getUnlocalizedName().equals(name));
    }

    public float getTotalPotency() {
        float potency = 1f;
        for (var mod : list) {
            potency += mod.getPotency();
        }
        return potency;
    }

    public float getTargetTemperature() {
        float change = 1f;
        for (var mod : list) {
            change += mod.getChange();
        }
        return change;
    }

    public void forEach(Consumer<? super TempModifier> func) {
        list.forEach(func);
    }

    @Override
    public Iterator<TempModifier> iterator() {
        return list.iterator();
    }
}
