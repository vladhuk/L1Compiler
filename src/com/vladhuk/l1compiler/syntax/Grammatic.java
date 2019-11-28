package com.vladhuk.l1compiler.syntax;

import com.vladhuk.l1compiler.lexical.Lexem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.vladhuk.l1compiler.lexical.Token.*;

public class Grammatic {

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

        final List<Lexem> multiplyRowStatement = new LinkedList<>();
        for (int i = 0; i < splitBySameRowLexems.size(); i++){
            final List<Lexem> row = splitBySameRowLexems.get(i);

            if (!Statement(row)) {
                multiplyRowStatement.addAll(row);

                if (Statement(multiplyRowStatement)) {
                    multiplyRowStatement.clear();
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
                && typeDefinition && assigning;
        final boolean varDeclaration = lexems.get(0).getName().equals("var") && identifier
                && (assigning || emptyAssign);

        return valDeclaration || varDeclaration;
    }
    public static boolean Identifier(List<Lexem> lexems) {
        return true;
    }
    public static boolean Type(List<Lexem> lexems) {
        return true;
    }
    public static boolean Expression(List<Lexem> lexems) {
        return true;
    }
    public static boolean BoolExpression(List<Lexem> lexems) {
        return true;
    }
    public static boolean ArithmExpression(List<Lexem> lexems) {
        return true;
    }
    public static boolean Term(List<Lexem> lexems) {
        return true;
    }
    public static boolean Factor(List<Lexem> lexems) {
        return true;
    }
    public static boolean SignedNumber(List<Lexem> lexems) {
        return true;
    }
    public static boolean SignedReal(List<Lexem> lexems) {
        return true;
    }
    public static boolean SignedInteger(List<Lexem> lexems) {
        return true;
    }
    public static boolean UnsignedNumber(List<Lexem> lexems) {
        return true;
    }
    public static boolean Sign(List<Lexem> lexems) {
        return true;
    }
    public static boolean UnsignedReal(List<Lexem> lexems) {
        return true;
    }
    public static boolean UnsignedInteger(List<Lexem> lexems) {
        return true;
    }
    public static boolean FractionalPart(List<Lexem> lexems) {
        return true;
    }
    public static boolean ScaleFactor(List<Lexem> lexems) {
        return true;
    }
    public static boolean DigitSequence(List<Lexem> lexems) {
        return true;
    }
    public static boolean Boolean(List<Lexem> lexems) {
        return true;
    }
    public static boolean String(List<Lexem> lexems) {
        return true;
    }
    public static boolean Assign(List<Lexem> lexems) {
        return true;
    }
    public static boolean ConstantDefinition(List<Lexem> lexems) {
        return true;
    }
    public static boolean Constant(List<Lexem> lexems) {
        return true;
    }
    public static boolean Loop(List<Lexem> lexems) {
        return true;
    }
    public static boolean ForLoop(List<Lexem> lexems) {
        return true;
    }
    public static boolean WhileLoop(List<Lexem> lexems) {
        return true;
    }
    public static boolean LoopStatements(List<Lexem> lexems) {
        return true;
    }
    public static boolean Condition(List<Lexem> lexems) {
        return true;
    }
    public static boolean Goto(List<Lexem> lexems) {
        return true;
    }
    public static boolean Mark(List<Lexem> lexems) {
        return true;
    }
    public static boolean LabelMark(List<Lexem> lexems) {
        return true;
    }
    public static boolean Letter(List<Lexem> lexems) {
        return true;
    }
    public static boolean Digit(List<Lexem> lexems) {
        return true;
    }

}
