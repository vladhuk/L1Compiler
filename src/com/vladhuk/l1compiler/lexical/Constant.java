package com.vladhuk.l1compiler.lexical;

import java.util.Objects;

public class Constant {

    private String name;
    private int index;

    public Constant() {}

    public Constant(String name) {
        this.name = name;
    }

    public Constant(String name, int index) {
        this.name = name;
        this.index = index;
    }

    @Override
    public String toString() {
        return String.format("%-15s %d", name, index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constant constant1 = (Constant) o;
        return index == constant1.index &&
                Objects.equals(name, constant1.name);
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
}
