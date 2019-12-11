package com.vladhuk.l1compiler.automat;

import com.vladhuk.l1compiler.lexical.Lexem;
import com.vladhuk.l1compiler.lexical.Token;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class Automatic {

    private static final List<State> statesWhiteList = Arrays.asList(
            State.ERROR, State.EXIT, State.ARITHM_OR_BOOL_SIGN, State.ARITHM_SIGN, State.BOOL_SIGN
    );

    private LinkedList<String> localStorageStack = new LinkedList<>();
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

            if (lexems.size() == 0 && currentState == State.EXIT) {
                return true;
            } else if (currentState == State.ERROR) {
                return false;
            }
        }
    }

    private State getState(Lexem lexem) {
        switch (lexem.getToken()) {
            case DECLARATION: return lexem.getName().equals("val") ? State.START_DECLARATION_VAL : State.START_DECLARATION_VAR;
            default: return State.ERROR;
        }
    }

    private State manageState(State state) {
        if (lexems.size() == 0 && !statesWhiteList.contains(state)) {
            state = State.ERROR;
        }

        switch (state) {
            case EXIT: return performExit();
            case EOL: return performEol();
            case START_DECLARATION_VAL: return performStartDeclarationVal();
            case START_DECLARATION_VAR: return performStartDeclarationVar();
            case VAL_OR_VAR: return performValOrVar();
            case DECLARATION_IDENTIFIER: return performDeclarationIdentifier();
            case DECLARATION_OPTIONAL_PUNCT: return performDeclarationOptionalPunct();
            case TYPE: return performType();
            case ASSIGN: return performAssign();
            case OPTIONAL_ASSIGN: return performOptionalAssign();
            case EXPRESSION_OR_IDENT: return performExpressionOrIdent();
            case EXPRESSION: return performExpression();
            case STRING: return performString();
            case ARITHM_OR_BOOL_SIGN: return performArithmOrBoolSign();
            case BOOL_SIGN: return performBoolSign();
            case ARITHM_SIGN: return performArithmSign();
            case BOOL_EXPRESSION: return performBoolExpression();
            case ARITHM_EXPRESSION: return performArithmExpression();
            case CLOSE_BRACKET: return performCloseBracket();
            case CLOSE_BRACKET_ARITHM: return performCloseBracketArithm();
            case CLOSE_BRACKET_BOOL: return performCloseBracketBool();

            default: return State.ERROR;
        }
    }

    private State performExit() {
        final State state;

        if (stateStack.size() != 0) {
            state = stateStack.poll();
        } else {
            state = lexems.size() == 0
                    ? State.EXIT
                    : getState(lexems.poll());
        }

        if (state == State.EXIT || state == State.ERROR) {
            return state;
        } else {
            return manageState(state);
        }
    }

    private State performEol() {
        final Lexem lexem = lexems.poll();

        if (lexem == null || lexem.getName().equals("\n")) {
            return State.EXIT;
        } else {
            return State.ERROR;
        }
    }

    private State performStartDeclarationVal() {
        stateStack.push(State.EOL);
        localStorageStack.push("val");
        return State.VAL_OR_VAR;
    }

    private State performStartDeclarationVar() {
        stateStack.push(State.EOL);
        localStorageStack.push("var");
        return State.VAL_OR_VAR;
    }

    private State performValOrVar() {
        return State.DECLARATION_IDENTIFIER;
    }

    private State performDeclarationIdentifier() {
        if (lexems.poll().getToken() == Token.IDENTIFIER) {
            return State.DECLARATION_OPTIONAL_PUNCT;
        } else {
            return State.ERROR;
        }
    }

    private State performDeclarationOptionalPunct() {
        final Lexem lexem = lexems.poll();

        if (lexem.getToken() == Token.PUNCT) {
            return State.TYPE;
        } else if (lexem.getToken() == Token.ASSIGN) {
            return State.EXPRESSION_OR_IDENT;
        } else {
            return State.ERROR;
        }
    }

    private State performType() {
        if (lexems.poll().getToken() == Token.TYPE) {
            if (localStorageStack.poll().equals("val")) {
                return State.ASSIGN;
            } else {
                return State.OPTIONAL_ASSIGN;
            }
        } else {
            return State.ERROR;
        }
    }

    private State performAssign() {
        if (lexems.poll().getToken() == Token.ASSIGN) {
            return State.EXPRESSION_OR_IDENT;
        } else {
            return State.ERROR;
        }
    }

    private State performOptionalAssign() {
        if (lexems.peek().getToken() == Token.ASSIGN) {
            return State.ASSIGN;
        } else {
            return State.EXIT;
        }
    }

    private State performExpressionOrIdent() {
        stateStack.push(State.EXIT);
        return State.EXPRESSION;
    }

    private State performExpression() {
        final Lexem lexem = lexems.peek();

        if (lexem.getToken() == Token.IDENTIFIER) {
            lexems.poll();
            return State.ARITHM_OR_BOOL_SIGN;
        }
        if (lexem.getToken() != Token.CONSTANT && lexem.getToken() != Token.BRACKET_OP) {
            return State.ERROR;
        }
        if (lexem.getName().equals("(")) {
            stateStack.push(State.CLOSE_BRACKET);
            lexems.poll();
            return State.EXPRESSION;
        }
        if (lexem.getName().matches("true|false")) {
            return State.BOOL_EXPRESSION;
        }
        if (lexem.getName().matches("'[^']*'")) {
            return State.STRING;
        }

        return State.ARITHM_EXPRESSION;
    }

    private State performString() {
        return State.EXIT;
    }

    private State performArithmOrBoolSign() {
        final Lexem lexem = lexems.poll();

        if (lexem == null) {
            return State.EXIT;
        }
        if (lexem.getToken() == Token.ADD_OP || lexem.getToken() == Token.MULT_OP || lexem.getToken() == Token.POW_OP) {
            return State.ARITHM_EXPRESSION;
        }
        if (lexem.getToken() == Token.REL_OP) {
            return State.BOOL_EXPRESSION;
        }

        lexems.push(lexem);
        return State.EXIT;
    }

    private State performArithmSign() {
        final Lexem lexem = lexems.peek();

        if (lexem == null) {
            return State.EXIT;
        }
        if (lexem.getToken() == Token.ADD_OP || lexem.getToken() == Token.MULT_OP || lexem.getToken() == Token.POW_OP) {
            lexems.poll();
            return State.ARITHM_EXPRESSION;
        }

        return State.EXIT;
    }

    private State performBoolSign() {
        if (lexems.size() == 0) {
            return State.EXIT;
        }
        if (lexems.peek().getToken() == Token.REL_OP) {
            lexems.poll();
            return State.BOOL_EXPRESSION;
        }

        return State.EXIT;
    }

    private State performArithmExpression() {
        final Lexem lexem = lexems.poll();

        if (lexem.getName().equals("(")) {
            stateStack.push(State.CLOSE_BRACKET_ARITHM);
            return State.ARITHM_EXPRESSION;
        }
        if (lexem.getToken() == Token.IDENTIFIER || lexem.getName().matches("\\d+(((\\.\\d+)?e[+-]\\d+)|(\\.\\d+))?")) {
            return State.ARITHM_SIGN;
        }

        return State.ERROR;
    }

    private State performBoolExpression() {
        final Lexem lexem = lexems.poll();

        if (lexem.getName().equals("(")) {
            stateStack.push(State.CLOSE_BRACKET_BOOL);
            return State.ARITHM_EXPRESSION;
        }
        if (lexem.getToken() == Token.IDENTIFIER || lexem.getName().matches("true|false")) {
            return State.BOOL_SIGN;
        }

        return State.ERROR;
    }

    private State performCloseBracket() {
        return lexems.poll().getName().equals(")") ? State.ARITHM_OR_BOOL_SIGN : State.ERROR;
    }

    private State performCloseBracketArithm() {
        return lexems.poll().getName().equals(")") ? State.ARITHM_SIGN : State.ERROR;
    }

    private State performCloseBracketBool() {
        return lexems.poll().getName().equals(")") ? State.BOOL_SIGN : State.ERROR;
    }

}
