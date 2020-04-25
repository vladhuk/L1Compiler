package com.vladhuk.l1compiler.syntax;

import com.vladhuk.l1compiler.lexical.Lexem;
import com.vladhuk.l1compiler.lexical.Pair;
import com.vladhuk.l1compiler.lexical.Token;
import com.vladhuk.l1compiler.rpn.DijkstrasParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.vladhuk.l1compiler.lexical.Token.*;


public class Grammar {

    private final List<String> errorStack = new LinkedList<>();

    private List<Pair> identifiers;
    private LinkedList<Lexem> rpn = new LinkedList<>();

    public void showError() {
        System.err.println(errorStack.get(0));
    }

    public void setIdentifiers(List<Pair> identifiers) {
        this.identifiers = identifiers;
    }

    public List<Lexem> getRpn() {
        return rpn;
    }

    private void pushError(String terminalName, String description, List<Lexem> lexems) {
        final String lexemNames = lexems.stream()
                .map(Lexem::getName)
                .collect(Collectors.joining(" "));
        description = description.isBlank() ? "" : " (" + description + ")";
        errorStack.add("Wrong " + terminalName + description + " on line " + lexems.get(0).getRowNumber() + ": " + lexemNames);
    }

    private int findLastIndexBeforeFromEnd(List<Lexem> lexems, Predicate<List<Lexem>> predicate) {
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

    public boolean Program(List<Lexem> lexems) {
        errorStack.clear();
        if (lexems.size() == 0) {
            return false;
        }
        return StatementList(lexems);
    }

    public boolean StatementList(List<Lexem> lexems) {
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

    public boolean Statement(List<Lexem> lexems) {
        return Declaration(lexems) || ConstantDefinition(lexems) || Assign(lexems)
                || Loop(lexems) || Condition(lexems) || Goto(lexems) || LabelMark(lexems);
    }

    public boolean Declaration(List<Lexem> lexems) {
        if (lexems.get(0).getToken() != DECLARATION) {
            return false;
        }

        if (lexems.size() < 4) {
            pushError("declaration", "", lexems);
            return false;
        }

        final Lexem identifier = lexems.get(1);
        final boolean existsIdentifier = identifier.getToken() == IDENTIFIER;

        if (!existsIdentifier) {
            pushError("declaration", "expected correct identifier", lexems);
            return false;
        }

        final boolean typeDefinition = lexems.get(2).getName().equals(":")
                && lexems.get(3).getToken() == TYPE;

        final Lexem assignSymbol = typeDefinition && lexems.size() >= 5
                ? lexems.get(4)
                : lexems.get(2);

        final boolean assign = typeDefinition
                ? assignSymbol.getToken() == ASSIGN
                : assignSymbol.getToken() == ASSIGN;

        final List<Lexem> afterAssign = typeDefinition
                ? lexems.size() >= 5 ? lexems.subList(5, lexems.size()) : null
                : lexems.subList(3, lexems.size());

        final boolean assigning = assign && (Expression(afterAssign) || Identifier(afterAssign));

        final List<Lexem> infixExp = new ArrayList<>();
        infixExp.add(lexems.get(0));
        infixExp.add(identifier);

        if (assigning) {
            infixExp.add(assignSymbol);
            infixExp.addAll(afterAssign);
        }

        rpn.addAll(DijkstrasParser.convertInfixToRpn(infixExp));

        final Pair identifierPair = identifiers.get(identifier.getIndex());

        if (assigning && !typeDefinition) {
            if (String(afterAssign)) {
                identifierPair.setType(Pair.Type.STRING);
            } else if (BoolExpression(afterAssign)) {
                identifierPair.setType(Pair.Type.BOOLEAN);
            } else {
                identifierPair.setType(Pair.Type.NUMBER);
            }
        } else if (typeDefinition) {
            final Lexem typeLexem = lexems.get(3);
            final Pair.Type type = typeLexem.getName().equals("string")
                    ? Pair.Type.STRING
                    : typeLexem.getName().equals("boolean")
                        ? Pair.Type.BOOLEAN : Pair.Type.NUMBER;
            identifierPair.setType(type);
        }

        final boolean val = lexems.get(0).getName().equals("val");
        final boolean var = lexems.get(0).getName().equals("var");

        if (val) {
            if (assigning) {
                return true;
            } else {
                pushError("declaration", "expected assigning in val declaration", lexems);
                return false;
            }
        }

        if (var) {
            if (assigning) {
                return true;
            } else if (typeDefinition) {
                return true;
            } else {
                pushError("declaration", "expected type when variable is not initialized", lexems);
                return false;
            }
        }

        return false;
    }

    public boolean Identifier(List<Lexem> lexems) {
        return lexems.size() == 1 && lexems.get(0).getToken() == IDENTIFIER;
    }

    public boolean Expression(List<Lexem> lexems) {
        return ArithmExpression(lexems) || BoolExpression(lexems) || String(lexems);
    }

    public boolean BoolExpression(List<Lexem> lexems) {
        if (Boolean(lexems) || Identifier(lexems)) {
            return true;
        }

        if (lexems.size() < 3) {
            return false;
        }

        final int indexOfRelOp = findLastIndexBeforeFromEnd(lexems, this::ArithmExpression);

        if (indexOfRelOp == -1) {
            return false;
        }

        if (lexems.get(indexOfRelOp).getToken() != REL_OP) {
            return false;
        }

        return ArithmExpression(lexems.subList(0, indexOfRelOp));
    }

    public boolean ArithmExpression(List<Lexem> lexems) {
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

        final int indexOfAddOp = findLastIndexBeforeFromEnd(lexems, this::Term);

        if (indexOfAddOp == -1) {
            return false;
        }

        final boolean addOp = lexems.get(indexOfAddOp).getToken() == ADD_OP;

        if (!addOp) {
            pushError("arithmetical expression", "", lexems);
            return false;
        }

        return ArithmExpression(lexems.subList(0, indexOfAddOp));
    }

    public boolean Term(List<Lexem> lexems) {
        if (Factor(lexems)) {
            return true;
        }

        if (lexems.size() < 3) {
            return false;
        }

        int indexOfMultOp = findLastIndexBeforeFromEnd(lexems, this::Factor);

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

    public boolean Factor(List<Lexem> lexems) {
        final boolean brackets = lexems.size() >= 3
                && lexems.get(0).getToken() == BRACKET_OP
                && lexems.get(lexems.size() - 1).getToken() == BRACKET_OP;

        if (brackets && ArithmExpression(lexems.subList(1, lexems.size() - 1))) {
            return true;
        }

        return Identifier(lexems) || UnsignedNumber(lexems);
    }

    public boolean SignedNumber(List<Lexem> lexems) {
        return SignedInteger(lexems) || SignedReal(lexems);
    }

    public boolean SignedReal(List<Lexem> lexems) {
        final boolean sign = lexems.size() > 1 && lexems.get(0).getToken() == ADD_OP;

        return sign
                ? UnsignedReal(lexems.subList(1, lexems.size()))
                : UnsignedReal(lexems.subList(0, lexems.size()));
    }

    public boolean SignedInteger(List<Lexem> lexems) {
        final boolean sign = lexems.size() > 1 && lexems.get(0).getToken() == ADD_OP;

        return sign
                ? UnsignedInteger(lexems.subList(1, lexems.size()))
                : UnsignedInteger(lexems.subList(0, lexems.size()));
    }

    public boolean UnsignedNumber(List<Lexem> lexems) {
        return UnsignedInteger(lexems) || UnsignedReal(lexems);
    }

    public boolean UnsignedReal(List<Lexem> lexems) {
        return lexems.size() == 1
                && lexems.get(0).getToken() == CONSTANT
                && lexems.get(0).getName().matches("\\d+(((\\.\\d+)?e[+-]\\d+)|(\\.\\d+))");
    }

    public boolean UnsignedInteger(List<Lexem> lexems) {
        return lexems.size() == 1
                && lexems.get(0).getToken() == CONSTANT
                && lexems.get(0).getName().matches("\\d+");
    }

    public boolean Boolean(List<Lexem> lexems) {
        return lexems.size() == 1
                && lexems.get(0).getToken() == CONSTANT
                && lexems.get(0).getName().matches("true|false");
    }

    public boolean String(List<Lexem> lexems) {
        return lexems.size() == 1
                && lexems.get(0).getToken() == CONSTANT
                && lexems.get(0).getName().matches("'[^']*'");
    }

    public boolean Assign(List<Lexem> lexems) {
        final boolean assign =  lexems.size() >= 3
                && lexems.get(0).getToken() == IDENTIFIER
                && lexems.get(1).getToken() == ASSIGN
                && Expression(lexems.subList(2, lexems.size()));

        if (assign) {
            rpn.addAll(DijkstrasParser.convertInfixToRpn(lexems));
        }

        return assign;
    }

    public boolean ConstantDefinition(List<Lexem> lexems) {
        final boolean cd = lexems.size() == 3
                && lexems.get(0).getToken() == IDENTIFIER
                && lexems.get(1).getToken() == ASSIGN
                && lexems.get(2).getToken() == CONSTANT;

        if (cd) {
            rpn.addAll(DijkstrasParser.convertInfixToRpn(lexems));
        }

        return cd;
    }

    public boolean Loop(List<Lexem> lexems) {
        if (lexems.get(0).getToken() != LOOP) {
            return false;
        }

        final int currentRpnSize = rpn.size();

        final int lastIndexOfLoopStatement = findLastIndexBeforeFromEnd(lexems, this::LoopStatements);

        if (lastIndexOfLoopStatement == -1) {
            rpn = new LinkedList<>(rpn.subList(0, currentRpnSize));
            return false;
        }

        final List<Lexem> loopStatement = lexems.subList(0, lastIndexOfLoopStatement + 1);

        final int beforeForRpnSize = rpn.size();

        if (WhileLoop(loopStatement) || ForLoop(loopStatement)) {

            final LinkedList<Lexem> tempRpn = new LinkedList<>();
            final List<String> lexemNames = lexems.stream().map(Lexem::getName).collect(Collectors.toList());
            final int doIndex = lexemNames.indexOf("do");
            final Lexem mark1 = new Lexem(-1, "$" + identifiers.size(), Token.IDENTIFIER, identifiers.size());
            final Lexem mark2 = new Lexem(-1, "$" + (identifiers.size() + 1), Token.IDENTIFIER, identifiers.size());
            identifiers.add(new Pair(mark1.getName(), Pair.Type.MARK, Pair.UNDEF, false, mark1.getIndex()));
            identifiers.add(new Pair(mark2.getName(), Pair.Type.MARK, Pair.UNDEF, false, mark2.getIndex()));

            if (lexems.get(0).getName().equals("for")) {
                rpn = new LinkedList<>(rpn.subList(0, beforeForRpnSize));
                final int toIndex = lexemNames.indexOf("to");
                tempRpn.addAll(DijkstrasParser.convertInfixToRpn(lexems.subList(1, toIndex)));
                tempRpn.add(mark1);
                tempRpn.add(new Lexem(-1, ":", Token.PUNCT));
                tempRpn.add(lexems.get(1).getToken() == Token.IDENTIFIER ? lexems.get(1) : lexems.get(2));
                tempRpn.addAll(DijkstrasParser.convertInfixToRpn(lexems.subList(toIndex + 1, doIndex)));
                tempRpn.add(new Lexem(-1, "<", Token.REL_OP));
            } else {
                tempRpn.add(mark1);
                tempRpn.add(new Lexem(-1, ":", Token.PUNCT));
                tempRpn.addAll(DijkstrasParser.convertInfixToRpn(lexems.subList(1, doIndex)));
            }
            tempRpn.add(mark2);
            tempRpn.add(new Lexem(-1, "if", Token.CONDITION));
            rpn.addAll(currentRpnSize, tempRpn);
            rpn.add(mark1);
            rpn.add(new Lexem(-1, "goto", Token.JUMP));
            rpn.add(mark2);
            rpn.add(new Lexem(-1, ":", Token.PUNCT));

            return true;
        } else {
            rpn = new LinkedList<>(rpn.subList(0, currentRpnSize));
            pushError("loop", "", lexems);
            return false;
        }
    }

    public boolean ForLoop(List<Lexem> lexems) {
        if (lexems.size() < 4) {
            return false;
        }

        if (!lexems.get(0).getName().equals("for")) {
            return false;
        }

        final int indexOfTo = findLastIndexBeforeFromEnd(lexems, this::ArithmExpression);

        if (indexOfTo == -1) {
            return false;
        }

        if (!lexems.get(indexOfTo).getName().equals("to")) {
            return false;
        }

        final List<Lexem> assigningLexems = lexems.subList(1, indexOfTo);

        return Declaration(assigningLexems) || Assign(assigningLexems);
    }

    public boolean WhileLoop(List<Lexem> lexems) {
        if (lexems.size() < 2 || !lexems.get(0).getName().equals("while")) {
            return false;
        }

        if (BoolExpression(lexems.subList(1, lexems.size()))) {
            return true;
        } else {
            pushError("while loop", "wrong bool expression", lexems);
            return false;
        }
    }

    public boolean LoopStatements(List<Lexem> lexems) {
        if (!lexems.get(0).getName().equals("do")) {
            pushError("loop statements", "expected keyword 'do'", lexems);
            return false;
        }

        if (!lexems.get(lexems.size() - 1).getName().equals("end")) {
            pushError("loop statements", "expected keyword 'end'", lexems);
            return false;
        }

        return StatementList(lexems.subList(1, lexems.size() - 1));
    }

    public boolean Condition(List<Lexem> lexems) {
        if (lexems.size() < 4) {
            return false;
        }

        if (!lexems.get(0).getName().equals("if")) {
            return false;
        }

        if (!lexems.get(lexems.size() - 3).getName().equals("then")) {
            pushError("condition", "expected 'then'", lexems);
            return false;
        }

        final List<Lexem> boolExpression = lexems.subList(1, lexems.size() - 3);

        final boolean condition = BoolExpression(boolExpression);

        if (condition) {
            rpn.addAll(DijkstrasParser.convertInfixToRpn(boolExpression));

            final Lexem mark = new Lexem(-1, "$" + identifiers.size(), Token.IDENTIFIER, identifiers.size());
            identifiers.add(new Pair(mark.getName(), Pair.Type.MARK, Pair.UNDEF, false, mark.getIndex()));

            rpn.add(mark);
            rpn.add(lexems.get(0));

            if (!Goto(lexems.subList(lexems.size() - 2, lexems.size()))) {
                pushError("goto", "", lexems);
                return false;
            }

            rpn.add(mark);
            rpn.add(new Lexem(-1, ":", Token.PUNCT));
        }

        return condition;
    }

    public boolean Goto(List<Lexem> lexems) {
        if (lexems.get(0).getToken() != JUMP) {
            return false;
        }

        if (Mark(lexems.subList(1, lexems.size()))) {
            Collections.reverse(lexems);
            rpn.addAll(lexems);
            return true;
        } else {
            pushError("goto", "wrong mark", lexems);
            return false;
        }
    }

    public boolean Mark(List<Lexem> lexems) {
        return Identifier(lexems);
    }

    public boolean LabelMark(List<Lexem> lexems) {
        final boolean isLabelMark = lexems.size() == 2
                && Mark(lexems.subList(0, lexems.size() - 1))
                && lexems.get(lexems.size() - 1).getName().equals(":");

        if (isLabelMark) {
            identifiers.get(lexems.get(0).getIndex()).setType(Pair.Type.MARK);
            rpn.addAll(lexems);
        }

        return isLabelMark;
    }

}
