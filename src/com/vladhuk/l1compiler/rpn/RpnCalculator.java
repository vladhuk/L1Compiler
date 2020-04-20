package com.vladhuk.l1compiler.rpn;

import java.util.LinkedList;
import java.util.List;


public class RpnCalculator {

    private RpnCalculator() {
    }

    public static String calculate(List<String> rpn) {
        final LinkedList<String> symbols = new LinkedList<>(rpn);

        final LinkedList<String> lastValues = new LinkedList<>();

        while (!symbols.isEmpty()) {
            final String symbol = symbols.pop();

            if (isUnaryOperator(symbol)) {
                switch (symbol) {
                    case "-":
                        lastValues.push(String.valueOf(-Double.valueOf(lastValues.pop())));
                        break;
                }
            } else if (isBinaryOperator(symbol)) {
                final String last = lastValues.pop();
                final String preLast = lastValues.pop();

                switch (symbol) {
                    case "==":
                        lastValues.push(String.valueOf(preLast.equals(last)));
                        break;
                    case "!=":
                        lastValues.push(String.valueOf(!preLast.equals(last)));
                        break;
                    case ">=":
                        lastValues.push(String.valueOf(Double.valueOf(preLast) >= Double.valueOf(last)));
                        break;
                    case "<=":
                        lastValues.push(String.valueOf(Double.valueOf(preLast) <= Double.valueOf(last)));
                        break;
                    case ">":
                        lastValues.push(String.valueOf(Double.valueOf(preLast) > Double.valueOf(last)));
                        break;
                    case "<":
                        lastValues.push(String.valueOf(Double.valueOf(preLast) < Double.valueOf(last)));
                        break;
                    case "+":
                        lastValues.push(String.valueOf(Double.valueOf(preLast) + Double.valueOf(last)));
                        break;
                    case "-":
                        lastValues.push(String.valueOf(Double.valueOf(preLast) - Double.valueOf(last)));
                        break;
                    case "*":
                        lastValues.push(String.valueOf(Double.valueOf(preLast) * Double.valueOf(last)));
                        break;
                    case "/":
                        lastValues.push(String.valueOf(Double.valueOf(preLast) / Double.valueOf(last)));
                        break;
                    case "^":
                        lastValues.push(String.valueOf(Math.pow(Double.valueOf(preLast), Double.valueOf(last))));
                        break;
                }
            } else {
                lastValues.push(symbol);
            }
        }

        return String.join(" ", lastValues);
    }

    private static boolean isBinaryOperator(String symbol) {
        return symbol.matches("[+\\-*/^<>]|==|!=|<=|>=");
    }

    private static boolean isUnaryOperator(String symbol) {
        return symbol.matches("@|var|val");
    }

}
