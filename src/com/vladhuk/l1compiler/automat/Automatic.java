package com.vladhuk.l1compiler.automat;

import com.vladhuk.l1compiler.lexical.Lexem;
import com.vladhuk.l1compiler.lexical.Token;

import java.util.LinkedList;
import java.util.List;


public class Automatic {

    private LinkedList<State> stateStack = new LinkedList<>();
    private LinkedList<Lexem> lexems;

    private Automatic() {}

    public static Automatic build(List<Lexem> lexems) {
        final Automatic automatic = new Automatic();
        automatic.lexems = new LinkedList<>(lexems);
        return automatic;
    }

    public boolean run() {
        State currentState = manageState(State.EXIT);

        while (true) {
            currentState = manageState(currentState);

            if (currentState == State.EXIT) {
                return true;
            } else if (currentState == State.ERROR) {
                return false;
            }
        }
    }

    private State getState(Lexem lexem) {
        switch (lexem.getToken()) {
            case DECLARATION: return lexem.getName().equals("val") ? State.VAL : State.VAR;
            default: return State.ERROR;
        }
    }

    private State manageState(State state) {
        switch (state) {
            case EXIT: return performExit();
            case EOL: return performEol();
            case VAL: return performVal();
            case DECLARATION_IDENTIFIER: return performDeclarationIdentifier();
            case DECLARATION_OPTIONAL_PUNCT: return performDeclarationOptionalPunct();
            case TYPE: return performType();
            case ASSIGN: return performAssign();
            case EXPRESSION_OR_IDENT: return performExpressionOrIdent();

            default: return State.ERROR;
        }
    }

    private State performExit() {
        final State state;

        if (stateStack.size() != 0) {
            state = stateStack.pollFirst();
        } else {
            state = lexems.size() == 0
                    ? State.EXIT
                    : getState(lexems.pollFirst());
        }

        if (state == State.EXIT || state == State.ERROR) {
            return state;
        } else {
            return manageState(state);
        }
    }

    private State performEol() {
        final Lexem lexem = lexems.pollFirst();

        if (lexem == null || lexem.getName().equals("\n")) {
            return State.EXIT;
        } else {
            return State.ERROR;
        }
    }

    private State performVal() {
        return State.DECLARATION_IDENTIFIER;
    }

    private State performDeclarationIdentifier() {
        if (lexems.pollFirst().getToken() == Token.IDENTIFIER) {
            return State.DECLARATION_OPTIONAL_PUNCT;
        } else {
            return State.ERROR;
        }
    }

    private State performDeclarationOptionalPunct() {
        final Lexem lexem = lexems.pollFirst();

        if (lexem.getToken() == Token.PUNCT) {
            return State.TYPE;
        } else if (lexem.getToken() == Token.ASSIGN) {
            return State.EXPRESSION_OR_IDENT;
        } else {
            return State.ERROR;
        }
    }

    private State performType() {
        if (lexems.pollFirst().getToken() == Token.TYPE) {
            return State.ASSIGN;
        } else {
            return State.ERROR;
        }
    }

    private State performAssign() {
        if (lexems.pollFirst().getToken() == Token.ASSIGN) {
            return State.EXPRESSION_OR_IDENT;
        } else {
            return State.ERROR;
        }
    }

    private State performExpressionOrIdent() {
        final Lexem lexem = lexems.pollFirst();

        if (lexem.getToken() == Token.IDENTIFIER) {
            return State.EOL;
        } else {
            return State.EXPRESSION;
        }
    }



}
