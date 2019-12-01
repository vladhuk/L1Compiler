package com.vladhuk.l1compiler.syntax;

import com.vladhuk.l1compiler.lexical.Lexem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.vladhuk.l1compiler.lexical.Token.*;


public class Grammar {

    private static final List<String> errorStack = new LinkedList<>();

    private static void showError(String terminalName, String description, List<Lexem> lexems) {
        final String lexemNames = lexems.stream()
                .map(Lexem::getName)
                .collect(Collectors.joining(" "));
        description = description.isBlank() ? "" : " (" + description + ")";
        errorStack.add("Wrong " + terminalName + description + " on line " + lexems.get(0).getRowNumber() + ": " + lexemNames);
    }

    private static int findIndexBeforeFromEnd(List<Lexem> lexems, Predicate<List<Lexem>> predicate) {
        final LinkedList<Lexem> end = new LinkedList<>();
        int indexOfOp = -1;
        for (int i = lexems.size() - 1; i >= 1; i--) {
            end.addFirst(lexems.get(i));

            if (predicate.test(end)) {
                indexOfOp = i - 1;
                break;
            }
        }

        return indexOfOp;
    }

    private static int findLastIndexBeforeFromEnd(List<Lexem> lexems, Predicate<List<Lexem>> predicate) {
        final LinkedList<Lexem> end = new LinkedList<>(lexems);
        int indexOfOp = -1;
        for (int i = 0; i < lexems.size() - 1; i++) {
            end.pollFirst();

            if (predicate.test(end)) {
                indexOfOp = i;
                break;
            }
        }

        return indexOfOp;
    }

    public static boolean Program(List<Lexem> lexems) {
        if (lexems.size() == 0) {
            return false;
        }
        if (StatementList(lexems)) {
            return true;
        } else {
            System.err.println(errorStack.get(0));
            return false;
        }
    }

    public static boolean StatementList(List<Lexem> lexems) {
        final List<List<Lexem>> splitBySameRowLexems = new ArrayList<>();

        splitBySameRowLexems.add(new ArrayList<>(Collections.singletonList(lexems.get(0))));

        Lexem lastAdded = lexems.get(0);

        for (Lexem lexem : lexems.subList(1, lexems.size())) {
            if (lexem.getRowNumber() == lastAdded.getRowNumber()) {
                splitBySameRowLexems.get(splitBySameRowLexems.size() - 1).add(lexem);
            } else {
                splitBySameRowLexems.add(new ArrayList<>(Collections.singletonList(lexem)));
            }
            lastAdded = lexem;
        }

        final List<Lexem> multiplyRowStatement = new ArrayList<>();
        for (int i = 0; i < splitBySameRowLexems.size(); i++) {
            errorStack.clear();

            if (!Statement(splitBySameRowLexems.get(i))) {

                for (; i < splitBySameRowLexems.size(); i++) {
                    multiplyRowStatement.addAll(splitBySameRowLexems.get(i));

                    if (Statement(multiplyRowStatement)) {
                        multiplyRowStatement.clear();
                        break;
                    }
                }
            }
        }

        return multiplyRowStatement.isEmpty();
    }

    public static boolean Statement(List<Lexem> lexems) {
        return Declaration(lexems) || ConstantDefinition(lexems) || Assign(lexems)
                || Loop(lexems) || Condition(lexems) || Goto(lexems) || LabelMark(lexems);
    }

    public static boolean Declaration(List<Lexem> lexems) {
        if (lexems.get(0).getToken() != DECLARATION) {
            return false;
        }

        if (lexems.size() < 4) {
            showError("declaration", "", lexems);
            return false;
        }

        final boolean identifier = lexems.get(1).getToken() == IDENTIFIER;

        if (!identifier) {
            showError("declaration", "expected correct identifier", lexems);
            return false;
        }

        final boolean typeDefinition = lexems.get(2).getName().equals(":")
                && lexems.get(3).getToken() == TYPE;

        final boolean assign = typeDefinition
                ? lexems.size() >= 5 && lexems.get(4).getToken() == ASSIGN
                : lexems.get(2).getToken() == ASSIGN;

        final List<Lexem> afterAssign = typeDefinition
                ? lexems.size() >= 5 ? lexems.subList(5, lexems.size()) : null
                : lexems.subList(3, lexems.size());

        final boolean assigning = assign && (Expression(afterAssign) || Identifier(afterAssign));

        final boolean emptyAssign = typeDefinition
                ? lexems.size() == 4
                : lexems.size() == 2;

        final boolean val = lexems.get(0).getName().equals("val");
        final boolean var = lexems.get(0).getName().equals("var");

        if (val) {
            if (assigning) {
                return true;
            } else {
                showError("declaration", "expected assigning in val declaration", lexems);
                return false;
            }
        }

        if (var) {
            if (assigning) {
                return true;
            } else if (typeDefinition && emptyAssign) {
                return true;
            } else {
                showError("declaration", "expected type when variable is not initialized", lexems);
                return false;
            }
        }

        return false;
    }

    public static boolean Identifier(List<Lexem> lexems) {
        return lexems.size() == 1 && lexems.get(0).getToken() == IDENTIFIER;
    }

    public static boolean Expression(List<Lexem> lexems) {
        return ArithmExpression(lexems) || BoolExpression(lexems) || String(lexems);
    }

    public static boolean BoolExpression(List<Lexem> lexems) {
        if (Boolean(lexems) || Identifier(lexems)) {
            return true;
        }

        if (lexems.size() < 3) {
            return false;
        }

        final int indexOfRelOp = findLastIndexBeforeFromEnd(lexems, Grammar::ArithmExpression);

        if (indexOfRelOp == -1) {
            return false;
        }

        if (lexems.get(indexOfRelOp).getToken() != REL_OP) {
            return false;
        }

        return ArithmExpression(lexems.subList(0, indexOfRelOp));
    }

    public static boolean ArithmExpression(List<Lexem> lexems) {
        final boolean sign = lexems.size() > 1 && lexems.get(0).getToken() == ADD_OP;

        final boolean term = sign
                ? Term(lexems.subList(1, lexems.size()))
                : Term(lexems.subList(0, lexems.size()));

        if (term) {
            return true;
        }

        if (lexems.size() < 3) {
            return false;
        }

        final int indexOfAddOp = findLastIndexBeforeFromEnd(lexems, Grammar::Term);

        if (indexOfAddOp == -1) {
            return false;
        }

        final boolean addOp = lexems.get(indexOfAddOp).getToken() == ADD_OP;

        if (!addOp) {
            showError("arithmetical expression", "", lexems);
            return false;
        }

        return ArithmExpression(lexems.subList(0, indexOfAddOp));
    }

    public static boolean Term(List<Lexem> lexems) {
        if (Factor(lexems)) {
            return true;
        }

        if (lexems.size() < 3) {
            return false;
        }

        int indexOfMultOp = findLastIndexBeforeFromEnd(lexems, Grammar::Factor);

        if (indexOfMultOp == -1) {
            return false;
        }

        final boolean multOp = lexems.get(indexOfMultOp).getToken() == MULT_OP
                || lexems.get(indexOfMultOp).getToken() == POW_OP;

        if (!multOp) {
            return false;
        }

        return Term(lexems.subList(0, indexOfMultOp));
    }

    public static boolean Factor(List<Lexem> lexems) {
        final boolean brackets = lexems.size() >= 3
                && lexems.get(0).getToken() == BRACKET_OP
                && lexems.get(lexems.size() - 1).getToken() == BRACKET_OP;

        if (brackets && ArithmExpression(lexems.subList(1, lexems.size() - 1))) {
            return true;
        }

        return Identifier(lexems) || UnsignedNumber(lexems);
    }

    public static boolean SignedNumber(List<Lexem> lexems) {
        return SignedInteger(lexems) || SignedReal(lexems);
    }

    public static boolean SignedReal(List<Lexem> lexems) {
        final boolean sign = lexems.size() > 1 && lexems.get(0).getToken() == ADD_OP;

        return sign
                ? UnsignedReal(lexems.subList(1, lexems.size()))
                : UnsignedReal(lexems.subList(0, lexems.size()));
    }

    public static boolean SignedInteger(List<Lexem> lexems) {
        final boolean sign = lexems.size() > 1 && lexems.get(0).getToken() == ADD_OP;

        return sign
                ? UnsignedInteger(lexems.subList(1, lexems.size()))
                : UnsignedInteger(lexems.subList(0, lexems.size()));
    }

    public static boolean UnsignedNumber(List<Lexem> lexems) {
        return UnsignedInteger(lexems) || UnsignedReal(lexems);
    }

    public static boolean UnsignedReal(List<Lexem> lexems) {
        return lexems.size() == 1
                && lexems.get(0).getToken() == CONSTANT
                && lexems.get(0).getName().matches("\\d+(((\\.\\d+)?e[+-]\\d+)|(\\.\\d+))");
    }

    public static boolean UnsignedInteger(List<Lexem> lexems) {
        return lexems.size() == 1
                && lexems.get(0).getToken() == CONSTANT
                && lexems.get(0).getName().matches("\\d+");
    }

    public static boolean Boolean(List<Lexem> lexems) {
        return lexems.size() == 1
                && lexems.get(0).getToken() == CONSTANT
                && lexems.get(0).getName().matches("true|false");
    }

    public static boolean String(List<Lexem> lexems) {
        return lexems.size() == 1
                && lexems.get(0).getToken() == CONSTANT
                && lexems.get(0).getName().matches("'[^']*'");
    }

    public static boolean Assign(List<Lexem> lexems) {
        return lexems.size() >= 3
                && lexems.get(0).getToken() == IDENTIFIER
                && lexems.get(1).getToken() == ASSIGN
                && Expression(lexems.subList(2, lexems.size()));
    }

    public static boolean ConstantDefinition(List<Lexem> lexems) {
        return lexems.size() == 3
                && lexems.get(0).getToken() == IDENTIFIER
                && lexems.get(1).getToken() == ASSIGN
                && lexems.get(2).getToken() == CONSTANT;
    }

    public static boolean Loop(List<Lexem> lexems) {
        if (lexems.get(0).getToken() != LOOP) {
            return false;
        }

        final int lastIndexOfLoopStatement = findLastIndexBeforeFromEnd(lexems, Grammar::LoopStatements);

        if (lastIndexOfLoopStatement == -1) {
            return false;
        }

        final List<Lexem> loopStatement = lexems.subList(0, lastIndexOfLoopStatement + 1);

        if (WhileLoop(loopStatement) || ForLoop(loopStatement)) {
            return true;
        } else {
            showError("loop", "", lexems);
            return false;
        }
    }

    public static boolean ForLoop(List<Lexem> lexems) {
        if (lexems.size() < 4) {
            return false;
        }

        if (!lexems.get(0).getName().equals("for")) {
            return false;
        }

        final int indexOfTo = findLastIndexBeforeFromEnd(lexems, Grammar::ArithmExpression);

        if (indexOfTo == -1) {
            return false;
        }

        if (!lexems.get(indexOfTo).getName().equals("to")) {
            return false;
        }

        final List<Lexem> assigningLexems = lexems.subList(1, indexOfTo);

        return Declaration(assigningLexems) || Assign(assigningLexems);
    }

    public static boolean WhileLoop(List<Lexem> lexems) {
        if (lexems.size() < 2 || !lexems.get(0).getName().equals("while")) {
            return false;
        }

        if (BoolExpression(lexems.subList(1, lexems.size()))) {
            return true;
        } else {
            showError("while loop", "wrong bool expression", lexems);
            return false;
        }
    }

    public static boolean LoopStatements(List<Lexem> lexems) {
        if (!lexems.get(0).getName().equals("do")) {
            showError("loop statements", "expected keyword 'do'", lexems);
            return false;
        }

        if (!lexems.get(lexems.size() - 1).getName().equals("end")) {
            showError("loop statements", "expected keyword 'end'", lexems);
            return false;
        }

        return StatementList(lexems.subList(1, lexems.size() - 1));
    }

    public static boolean Condition(List<Lexem> lexems) {
        if (lexems.size() < 4) {
            return false;
        }

        if (!lexems.get(0).getName().equals("if")) {
            return false;
        }

        if (!Goto(lexems.subList(lexems.size() - 2, lexems.size()))) {
            showError("goto", "", lexems);
            return false;
        }

        if (!lexems.get(lexems.size() - 3).getName().equals("then")) {
            showError("condition", "expected 'then'", lexems);
            return false;
        }

        return BoolExpression(lexems.subList(1, lexems.size() - 3));
    }

    public static boolean Goto(List<Lexem> lexems) {
        if (lexems.get(0).getToken() != JUMP) {
            return false;
        }

        if (Mark(lexems.subList(1, lexems.size()))) {
            return true;
        } else {
            showError("goto", "wrong mark", lexems);
            return false;
        }
    }

    public static boolean Mark(List<Lexem> lexems) {
        return Identifier(lexems);
    }

    public static boolean LabelMark(List<Lexem> lexems) {
        return lexems.size() == 2
                && Mark(lexems.subList(0, lexems.size() - 1))
                && lexems.get(lexems.size() - 1).getName().equals(":");
    }

}
