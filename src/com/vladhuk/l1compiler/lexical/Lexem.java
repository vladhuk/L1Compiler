package com.vladhuk.l1compiler.lexical;

public class Lexem {

    private int rowNumber;
    private String lexem;
    private Token token;
    private int index;

    public Lexem(int rowNumber, String lexem, Token token) {
        this.rowNumber = rowNumber;
        this.lexem = lexem;
        this.token = token;
    }

    public Lexem(int rowNumber, String lexem, Token token, int index) {
        this(rowNumber, lexem, token);
        this.index = index;
    }

    @Override
    public String toString() {
        return String.format("%-7d %-15s %-15s %d", rowNumber, lexem, token.name(), index);
    }

    public int getRowNumber() {
        return rowNumber;
    }
    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }
    public String getLexem() {
        return lexem;
    }
    public void setLexem(String lexem) {
        this.lexem = lexem;
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
