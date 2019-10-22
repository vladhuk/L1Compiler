package com.vladhuk.l1compiler.lexical;

import java.util.Objects;

public class Identifier {

    private String name;
    private int index;
    private Type type;

    public Identifier() {}

    public Identifier(String name) {
        this.name = name;
    }

    public Identifier(String name, int index, Type type) {
        this.name = name;
        this.index = index;
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("%-15s %-7d %s", name, index, "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return index == that.index &&
                Objects.equals(name, that.name) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, index, type);
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
}
