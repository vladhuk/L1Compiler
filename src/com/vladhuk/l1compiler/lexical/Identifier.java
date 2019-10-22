package com.vladhuk.l1compiler.lexical;

import java.util.Objects;

public class Identifier {

    private String name;
    private int index;

    public Identifier() {}

    public Identifier(String name) {
        this.name = name;
    }

    public Identifier(String name, int index) {
        this.name = name;
        this.index = index;
    }

    @Override
    public String toString() {
        return String.format("%-15s %-7d", name, index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
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
}
