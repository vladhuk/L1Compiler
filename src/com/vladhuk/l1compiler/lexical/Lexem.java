package com.vladhuk.l1compiler.lexical;

import java.util.Objects;


public class Lexem {

    private int rowNumber;
    private String name;
    private Token token;
    private int index = -1;

    public Lexem() {}

    public Lexem(int rowNumber, String name, Token token) {
        this.rowNumber = rowNumber;
        this.name = name;
        this.token = token;
    }

    public Lexem(int rowNumber, String name, Token token, int index) {
        this(rowNumber, name, token);
        this.index = index;
    }

    public Lexem(Lexem lexem) {
        this(lexem.getRowNumber(), lexem.getName(), lexem.getToken(), lexem.getIndex());
    }

    @Override
    public String toString() {
        return String.format("%-7d %-15s %-15s %s", rowNumber, name, token.name(), index == -1 ? "" : index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lexem lexem = (Lexem) o;
        return index == lexem.index &&
                Objects.equals(name, lexem.name) &&
                token == lexem.token;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, token, index);
    }

    public int getRowNumber() {
        return rowNumber;
    }
    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Token getToken() {
        return token;
    }
    public void setToken(Token token) {
        this.token = token;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
}
