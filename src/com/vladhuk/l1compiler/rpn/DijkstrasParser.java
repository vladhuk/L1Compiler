package com.vladhuk.l1compiler.rpn;

import com.vladhuk.l1compiler.lexical.Lexem;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


public class DijkstrasParser {

    private DijkstrasParser() {
    }

    public static List<Lexem> convertInfixToRpn(List<Lexem> symbols) {
        final List<Lexem> symbolsCopy = symbols.stream().map(Lexem::new).collect(Collectors.toList());
        final LinkedList<Lexem> correctSymbols = new LinkedList<>(replaceUnaryMinuses(symbolsCopy));

        final LinkedList<Lexem> rpnStack = new LinkedList<>();
        final LinkedList<Lexem> operatorStack = new LinkedList<>();

        while (!correctSymbols.isEmpty()) {
            final Lexem currentSymbol = correctSymbols.pop();

            if (isOperator(currentSymbol)) {
                if (currentSymbol.getName().equals("(")) {
                    operatorStack.push(currentSymbol);
                } else {
                    while (!operatorStack.isEmpty() && getPriority(operatorStack.peek()) >= getPriority(currentSymbol)) {
                        rpnStack.addLast(operatorStack.pop());
                    }
                    if (currentSymbol.getName().equals(")")) {
                        operatorStack.pop();
                    } else {
                        operatorStack.push(currentSymbol);
                    }
                }
            } else {
                rpnStack.addLast(currentSymbol);
            }
        }

        while (!operatorStack.isEmpty()) {
            rpnStack.addLast(operatorStack.pop());
        }

        return rpnStack;
    }

    private static List<Lexem> replaceUnaryMinuses(List<Lexem> symbols) {
        if (symbols.size() < 2) {
            return symbols;
        }

        final List<Lexem> symbolsCopy = symbols.stream().map(Lexem::new).collect(Collectors.toList());

        for (int i = 0; symbolsCopy.get(i).getName().equals("-"); i++) {
            symbolsCopy.get(i).setName("@");
        }

        for (int i = 0; i < symbolsCopy.size() - 1; i++) {
            if (isOperator(symbolsCopy.get(i)) && symbolsCopy.get(i + 1).getName().equals("-")) {
                symbolsCopy.get(i + 1).setName("@");
            }
        }

        return symbolsCopy;
    }

    private static boolean isArithmOperator(Lexem symbol) {
        return symbol.getName().matches("[()+\\-@*/^]");
    }

    private static boolean isOperator(Lexem symbol) {
        return isArithmOperator(symbol) || symbol.getName().matches("[<>=]|==|!=|<=|>=|val|var");
    }

    private static Short getPriority(Lexem symbol) {
        final String string = symbol.getName();

        if (string.matches("\\(")) return 0;
        if (string.matches("\\)")) return 1;
        if (string.matches("=")) return 2;
        if (string.matches("[<>]|==|!=|<=|>=")) return 3;
        if (string.matches("[+\\-]")) return 4;
        if (string.matches("[*/]")) return 5;
        if (string.matches("\\^")) return 6;
        if (string.matches("@")) return 7;
        if (string.matches("val|var")) return 8;

        throw new RuntimeException("Unknown symbol");
    }

}
