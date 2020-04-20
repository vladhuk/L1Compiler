package com.vladhuk.l1compiler.lexical;

import java.util.Objects;

public class Pair {

    public final static String UNDEF = "UNDEF";
    public final static String DEF = "DEF";

    private String name;
    private Type type = Type.UNDEF;
    private String value = UNDEF;
    private boolean modifiable = false;
    private int index;

    public Pair() {}

    public Pair(String name, Type type, String value, boolean modifiable, int index) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.index = index;
        this.modifiable = modifiable;
    }

    @Override
    public String toString() {
        return String.format("%-15s %-10s %-15s %-7s %-7d", name, type.name(), value, modifiable, index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair that = (Pair) o;
        return index == that.index &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, index);
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public boolean isModifiable() {
        return modifiable;
    }
    public void setModifiable(boolean modifiable) {
        this.modifiable = modifiable;
    }

    public static enum Type {
        UNDEF, NUMBER, STRING, BOOLEAN, MARK
    }

}
