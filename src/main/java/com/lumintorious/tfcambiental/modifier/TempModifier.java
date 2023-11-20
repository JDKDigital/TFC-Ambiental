package com.lumintorious.tfcambiental.modifier;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TempModifier implements Comparable<TempModifier>
{
    private String unlocalizedName;
    private float change;
    private float potency;
    private int count = 1;
    private float multiplier = 1f;

    public float getChange() {
        return change * multiplier * 1;
    }

    public void setChange(float change) {
        this.change = change;
    }

    public float getPotency() {
        return potency * multiplier * 1;
    }

    public void setPotency(float potency) {
        this.potency = potency;
    }

    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public TempModifier(String unlocalizedName) {
        this(unlocalizedName, 0f, 0f);
    }

    public TempModifier(String unlocalizedName, float change, float potency) {
        this.unlocalizedName = unlocalizedName;
        this.change = change;
        this.potency = potency;
    }

    public static Optional<TempModifier> defined(String unlocalizedName, float change, float potency) {
        return Optional.of(new TempModifier(unlocalizedName, change, potency));
    }

    public static Optional<TempModifier> none() {
        return Optional.empty();
    }

    @Override
    public int compareTo(@NotNull TempModifier o) {
        return Float.compare(this.change, o.change);
    }
}
