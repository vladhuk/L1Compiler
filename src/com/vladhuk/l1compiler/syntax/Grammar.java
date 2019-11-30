package com.vladhuk.l1compiler.syntax;

import com.vladhuk.l1compiler.lexical.Lexem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static com.vladhuk.l1compiler.lexical.Token.*;

public class Grammar {

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

    public static boolean Program(List<Lexem> lexems) {
        if (lexems.size() == 0) {
            return false;
        }
        return StatementList(lexems);
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
        if (lexems.size() < 4) {
            return false;
        }

        if (lexems.get(0).getToken() != DECLARATION) {
            return false;
        }

        final boolean identifier = lexems.get(1).getToken() == IDENTIFIER;

        final boolean typeDefinition = lexems.get(2).getName().equals(":")
                && lexems.get(3).getToken() == TYPE;

        final boolean assign = typeDefinition
                ? lexems.get(4).getToken() == ASSIGN
                : lexems.get(2).getToken() == ASSIGN;

        final List<Lexem> afterAssign = typeDefinition
                ? lexems.subList(5, lexems.size())
                : lexems.subList(3, lexems.size());

        final boolean assigning = assign && (Expression(afterAssign) || Identifier(afterAssign));

        final boolean emptyAssign = typeDefinition
                ? lexems.size() == 4
                : lexems.size() == 2;

        final boolean valDeclaration = lexems.get(0).getName().equals("val") && identifier
                && assigning;
        final boolean varDeclaration = lexems.get(0).getName().equals("var") && identifier
                && (assigning || (typeDefinition && emptyAssign));

        return valDeclaration || varDeclaration;
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

        final List<Lexem> arithmExpression1 = new ArrayList<>();
        int indexOfRelOp = -1;
        for (int i = 0; i < lexems.size() - 2; i++) {
            arithmExpression1.add(lexems.get(i));

            if (ArithmExpression(arithmExpression1)) {
                indexOfRelOp = i + 1;
                break;
            }
        }

        if (indexOfRelOp == -1) {
            return false;
        }

        if (lexems.get(indexOfRelOp).getToken() != REL_OP) {
            return false;
        }

        return ArithmExpression(lexems.subList(indexOfRelOp + 1, lexems.size()));
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

        final int indexOfAddOp = findIndexBeforeFromEnd(lexems, Grammar::Term);

        if (indexOfAddOp == -1) {
            return false;
        }

        final boolean addOp = lexems.get(indexOfAddOp).getToken() == ADD_OP;

        if (!addOp) {
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

        int indexOfMultOp = findIndexBeforeFromEnd(lexems, Grammar::Factor);

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
        if (Identifier(lexems) || SignedNumber(lexems)) {
            return true;
        }

        if (lexems.size() < 3) {
            return false;
        }

        final boolean brackets = lexems.get(0).getToken() == BRACKET_OP && lexems.get(lexems.size() - 1).getToken() == BRACKET_OP;

        if (!brackets) {
            return false;
        }

        return ArithmExpression(lexems.subList(1, lexems.size() - 1));
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
        final int lastIndexOfLoopStatement = findIndexBeforeFromEnd(lexems, Grammar::LoopStatements);

        if (lastIndexOfLoopStatement == -1) {
            return false;
        }

        final List<Lexem> loopStatement = lexems.subList(0, lastIndexOfLoopStatement + 1);

        return WhileLoop(loopStatement) || ForLoop(loopStatement);
    }

    public static boolean ForLoop(List<Lexem> lexems) {
        if (lexems.size() < 4) {
            return false;
        }

        if (!lexems.get(0).getName().equals("for")) {
            return false;
        }

        final int indexOfTo = findIndexBeforeFromEnd(lexems, Grammar::ArithmExpression);

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
        return lexems.size() >= 2
                && lexems.get(0).getName().equals("while")
                && BoolExpression(lexems.subList(1, lexems.size()));
    }

    public static boolean LoopStatements(List<Lexem> lexems) {
        final boolean wrapWords = lexems.get(0).getName().equals("do")
                && lexems.get(lexems.size() - 1).getName().equals("end");

        return wrapWords && StatementList(lexems.subList(1, lexems.size() - 1));
    }

    public static boolean Condition(List<Lexem> lexems) {
        if (lexems.size() < 4) {
            return false;
        }

        if (!Goto(lexems.subList(lexems.size() - 2, lexems.size()))) {
            return false;
        }

        final boolean wrapKeywords = lexems.get(0).getName().equals("if")
                && lexems.get(lexems.size() - 3).getName().equals("then");

        if (!wrapKeywords) {
            return false;
        }

        return BoolExpression(lexems.subList(1, lexems.size() - 3));
    }

    public static boolean Goto(List<Lexem> lexems) {
        return lexems.get(0).getToken() == JUMP
                && Mark(lexems.subList(1, lexems.size()));
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
