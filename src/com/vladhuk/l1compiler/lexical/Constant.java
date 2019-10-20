package com.vladhuk.l1compiler.lexical;

import java.util.Objects;

public class Constant {

    private String constant;
    private int index;

    public Constant(String constant, int index) {
        this.constant = constant;
        this.index = index;
    }

    @Override
    public String toString() {
        return String.format("%-15s %d", constant, index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constant constant1 = (Constant) o;
        return index == constant1.index &&
                Objects.equals(constant, constant1.constant);
    }
    @Override
    public int hashCode() {
        return Objects.hash(constant, index);
    }

    public String getConstant() {
        return constant;
    }
    public void setConstant(String constant) {
        this.constant = constant;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
}
