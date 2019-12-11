package com.vladhuk.l1compiler.automat;

import com.vladhuk.l1compiler.lexical.Lexem;
import com.vladhuk.l1compiler.lexical.Token;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class Automatic {

    private static final List<State> statesWhiteList = Arrays.asList(
            State.ERROR, State.EXIT, State.EOL, State.ARITHM_OR_BOOL_SIGN, State.ARITHM_SIGN, State.BOOL_SIGN,
            State.END
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

    private void showExpectedError(String name, int rowNumber) {
        System.err.println("Expected '" + name + "' on line " + rowNumber + '.');
    }

    private void showUnexpectedError(Lexem lexem) {
        final String lexemName = lexem.getName().equals("\n") ? "new line" : lexem.getName();
        System.err.println("Unexpected '" + lexemName + "' on line " + lexem.getRowNumber() + '.');
    }

    public boolean run() {
        State currentState = State.EXIT;

        while (true) {
            currentState = manageState(currentState);

            if (lexems.size() == 0 && stateStack.size() == 0 && currentState == State.EXIT) {
                return true;
            } else if (currentState == State.ERROR) {
                return false;
            }
        }
    }

    private State getState(Lexem lexem) {
        switch (lexem.getToken()) {
            case DECLARATION: return processDeclaration();
            case IDENTIFIER: return processIdentifier();
            case JUMP: return State.GOTO;
            case CONDITION: return State.IF;
            case LOOP: return State.LOOP;

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
            case DECLARATION: return performDeclaration();
            case DECLARATION_OPTIONAL_PUNCT: return performDeclarationOptionalPunct();
            case TYPE: return performType();
            case ASSIGN: return performAssign();
            case OPTIONAL_ASSIGN: return performOptionalAssign();
            case EXPRESSION: return performExpression();
            case EXPRESSION_OR_STRING: return performExpressionOrString();
            case STRING: return performString();
            case ARITHM_OR_BOOL_SIGN: return performArithmOrBoolSign();
            case BOOL_SIGN: return performBoolSign();
            case ARITHM_SIGN: return performArithmSign();
            case BOOL_EXPRESSION: return performBoolExpression();
            case ARITHM_EXPRESSION: return performArithmExpression();
            case CLOSE_BRACKET: return performCloseBracket();
            case CLOSE_BRACKET_ARITHM: return performCloseBracketArithm();
            case CLOSE_BRACKET_BOOL: return performCloseBracketBool();
            case LABEL_MARK_OR_ASSIGNING: return performLabelMarkOrAssigning();
            case IF: return performIf();
            case THEN: return performThen();
            case IDENTIFIER: return performIdentifier();
            case GOTO: return performGoto();
            case LOOP: return performLoop();
            case TO: return performTo();
            case DO: return performDo();
            case DECLARATION_OR_ASSIGNING: return performDeclarationOrAssigning();
            case END: return performEnd();

            case ERROR:
            default:
                return State.ERROR;
        }
    }

    private State performExit() {
        final State state;

        if (stateStack.size() != 0 && stateStack.peek() != State.END) {
            state = stateStack.poll();
        } else if (lexems.size() == 0 && stateStack.size() > 0) {
            state = State.ERROR;
        } else if (lexems.size() == 0) {
            state = State.EXIT;
        } else {
            state = getState(lexems.peek());
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
        }

        showExpectedError("new line", lexem.getRowNumber());
        return State.ERROR;
    }

    private State processDeclaration() {
        stateStack.push(State.EOL);
        return State.DECLARATION;
    }

    private State performDeclaration() {
        final Lexem lexem = lexems.poll();

        if (lexem.getName().equals("val")) {
            localStorageStack.push("val");
            stateStack.push(State.DECLARATION_OPTIONAL_PUNCT);
            return State.IDENTIFIER;
        }
        if (lexem.getName().equals("var")) {
            localStorageStack.push("var");
            stateStack.push(State.DECLARATION_OPTIONAL_PUNCT);
            return State.IDENTIFIER;
        }

        return State.ERROR;
    }

    private State performDeclarationOptionalPunct() {
        final Lexem lexem = lexems.poll();

        if (lexem.getToken() == Token.PUNCT) {
            return State.TYPE;
        } else if (lexem.getToken() == Token.ASSIGN) {
            stateStack.push(State.EXIT);
            return State.EXPRESSION_OR_STRING;
        }

        showUnexpectedError(lexem);
        return State.ERROR;
    }

    private State performType() {
        final Lexem lexem = lexems.poll();

        if (lexem.getToken() == Token.TYPE) {
            if (localStorageStack.poll().equals("val")) {
                return State.ASSIGN;
            } else {
                return State.OPTIONAL_ASSIGN;
            }
        }

        showExpectedError("type", lexem.getRowNumber());
        return State.ERROR;
    }

    private State performAssign() {
        final Lexem lexem = lexems.poll();

        if (lexem.getToken() == Token.ASSIGN) {
            stateStack.push(State.EXIT);
            return State.EXPRESSION_OR_STRING;
        }

        showExpectedError("=", lexem.getRowNumber());
        return State.ERROR;
    }

    private State performOptionalAssign() {
        if (lexems.peek().getToken() == Token.ASSIGN) {
            return State.ASSIGN;
        } else {
            return State.EXIT;
        }
    }

    private State performExpressionOrString() {
        if (lexems.peek().getName().matches("'[^']*'")) {
            return State.STRING;
        }

        return State.EXPRESSION;
    }

    private State performExpression() {
        final Lexem lexem = lexems.poll();

        if (lexem.getToken() == Token.ADD_OP) {
            stateStack.push(State.ARITHM_OR_BOOL_SIGN);
            return State.EXPRESSION;
        }
        if (lexem.getToken() == Token.IDENTIFIER) {
            return State.ARITHM_OR_BOOL_SIGN;
        }
        if (lexem.getToken() != Token.CONSTANT && lexem.getToken() != Token.BRACKET_OP) {
            return State.ERROR;
        }
        if (lexem.getName().equals("(")) {
            stateStack.push(State.CLOSE_BRACKET);
            return State.EXPRESSION;
        }

        return State.ARITHM_OR_BOOL_SIGN;
    }

    private State performString() {
        lexems.poll();
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

        final Lexem lexem = lexems.poll();

        if (lexem.getToken() == Token.REL_OP) {
            return State.BOOL_EXPRESSION;
        }

        return State.EXIT;
    }

    private State performArithmExpression() {
        final Lexem lexem = lexems.poll();

        if (lexem.getToken() == Token.ADD_OP) {
            stateStack.push(State.ARITHM_SIGN);
            return State.ARITHM_EXPRESSION;
        }
        if (lexem.getName().equals("(")) {
            stateStack.push(State.CLOSE_BRACKET_ARITHM);
            return State.ARITHM_EXPRESSION;
        }
        if (lexem.getToken() == Token.IDENTIFIER || lexem.getName().matches("\\d+(((\\.\\d+)?e[+-]\\d+)|(\\.\\d+))?")) {
            return State.ARITHM_SIGN;
        }

        showUnexpectedError(lexem);
        return State.ERROR;
    }

    private State performBoolExpression() {
        final Lexem lexem = lexems.poll();

        if (lexem.getToken() == Token.ADD_OP) {
            stateStack.push(State.ARITHM_SIGN);
            return State.ARITHM_EXPRESSION;
        }
        if (lexem.getName().equals("(")) {
            stateStack.push(State.CLOSE_BRACKET_BOOL);
            return State.ARITHM_EXPRESSION;
        }
        if (lexem.getToken() == Token.IDENTIFIER || lexem.getToken() == Token.CONSTANT) {
            return State.BOOL_SIGN;
        }

        showUnexpectedError(lexem);
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

    private State performLabelMarkOrAssigning() {
        final Lexem lexem = lexems.poll();

        if (lexem.getToken() == Token.PUNCT) {
            return State.EOL;
        } else if (lexem.getToken() == Token.ASSIGN) {
            stateStack.push(State.EOL);
            return State.EXPRESSION_OR_STRING;
        }

        showUnexpectedError(lexem);
        return State.ERROR;
    }

    private State performGoto() {
        final Lexem lexem = lexems.poll();

        if (lexem.getToken() == Token.JUMP) {
            stateStack.push(State.EOL);
            return State.IDENTIFIER;
        }

        showExpectedError("goto", lexem.getRowNumber() - 1);
        return State.ERROR;
    }

    private State performIf() {
        final Lexem lexem = lexems.poll();

        if (lexem.getName().equals("if")) {
            stateStack.push(State.THEN);
            return State.BOOL_EXPRESSION;
        }

        showExpectedError("if", lexem.getRowNumber());
        return State.ERROR;
    }

    private State performThen() {
        while (lexems.peek().getName().equals("\n")) {
            lexems.poll();
        }

        final Lexem lexem = lexems.poll();

        if (lexem.getName().equals("then")) {
            stateStack.push(State.GOTO);
            return State.EOL;
        }

        showExpectedError("then", lexem.getRowNumber());
        return State.ERROR;
    }

    private State processIdentifier() {
        stateStack.push(State.LABEL_MARK_OR_ASSIGNING);
        return State.IDENTIFIER;
    }

    private State performIdentifier() {
        final Lexem lexem = lexems.poll();

        if (lexem.getToken() == Token.IDENTIFIER) {
            return State.EXIT;
        }

        showExpectedError("identifier", lexem.getRowNumber());
        return State.ERROR;
    }

    private State performLoop() {
        final Lexem lexem = lexems.poll();

        if (lexem.getName().equals("while")) {
            stateStack.push(State.DO);
            return State.BOOL_EXPRESSION;
        }
        if (lexem.getName().equals("for")) {
            return State.DECLARATION_OR_ASSIGNING;
        }
        if (lexem.getName().equals("end")) {
            return State.END;
        }

        showUnexpectedError(lexem);
        return State.ERROR;
    }

    private State performDeclarationOrAssigning() {
        final Lexem lexem = lexems.peek();

        if (lexem.getToken() == Token.IDENTIFIER) {
            stateStack.push(State.TO);
            lexems.poll();
            return State.ASSIGN;
        }
        if (lexem.getToken() == Token.DECLARATION) {
            stateStack.push(State.TO);
            return State.DECLARATION;
        }

        showExpectedError("declaration or assigning", lexem.getRowNumber());
        return State.ERROR;
    }

    private State performTo() {
        final Lexem lexem = lexems.poll();

        if (lexem.getName().equals("to")) {
            stateStack.push(State.DO);
            return State.ARITHM_EXPRESSION;
        }

        showExpectedError("to", lexem.getRowNumber());
        return State.ERROR;
    }

    private State performDo() {
        while (lexems.peek().getName().equals("\n")) {
            lexems.poll();
        }

        final Lexem lexem = lexems.poll();

        if (lexem.getName().equals("do")) {
            stateStack.push(State.END);
            return State.EOL;
        }

        showExpectedError("do", lexem.getRowNumber());
        return State.ERROR;
    }


    private State performEnd() {
        if (stateStack.peek() == State.END) {
            stateStack.poll();
            return State.EOL;
        }

        return State.ERROR;
    }

}
