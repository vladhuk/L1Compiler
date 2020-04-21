package com.vladhuk.l1compiler.interpretation;

import com.vladhuk.l1compiler.lexical.Lexem;
import com.vladhuk.l1compiler.lexical.Pair;
import com.vladhuk.l1compiler.lexical.Token;
import com.vladhuk.l1compiler.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;


public class Interpreter {

    private List<Pair> constants;
    private List<Pair> identifiers;

    public static String interpret(List<String> rows) {
        final List<List<String>> tables = Util.getSeparatedTables(rows);

        final List<Lexem> rpn = Util.getLexemsFromLexemTable(tables.get(0));

        final Interpreter interpreter = new Interpreter();
        interpreter.constants = Util.getPairsFromTable(tables.get(1));
        interpreter.identifiers = Util.getPairsFromTable(tables.get(2));

        final List<Pair> newIdentifiers = interpreter.calculate(rpn);

        return Util.tableToString(newIdentifiers);
    }

    public static void interpret(Path tables, Path destination) throws IOException {
        Files.writeString(destination, interpret(Files.readAllLines(tables)));
    }

    public static void interpret(File tables, File destination) throws IOException {
        interpret(tables.toPath(), destination.toPath());
    }

    public List<Pair> calculate(List<Lexem> rpn) {
        final LinkedList<Lexem> symbols = new LinkedList<>(rpn);

        final LinkedList<Lexem> lastValues = new LinkedList<>();

        while (!symbols.isEmpty()) {
            final Lexem symbol = symbols.pop();

            if (isUnaryOperator(symbol.getName())) {
                switch (symbol.getName()) {
                    case "@":
                        lastValues.push(handleUnaryMinus(lastValues.pop()));
                        break;
                    case "var":
                    case "val":
                        lastValues.push(handleDef(symbol.getName(), lastValues.pop()));
                        break;
                }
            } else if (isBinaryOperator(symbol.getName())) {
                final Lexem last = lastValues.pop();
                final Lexem preLast = lastValues.pop();

                Lexem tempValue;
                Pair.Type tempType;

                switch (symbol.getName()) {
                    case "==":
                        tempValue = getConstant(preLast);
                        tempType = constants.get(tempValue.getIndex()).getType();
                        switch (tempType) {
                            case NUMBER:
                                lastValues.push(handleNumberBiFunc(preLast, last, Double::equals, Pair.Type.BOOLEAN));
                                break;
                            case STRING:
                                lastValues.push(handleStringBiFunc(preLast, last, String::equals));
                                break;
                            case BOOLEAN:
                                lastValues.push(handleBooleanBiFunc(preLast, last, Boolean::equals));
                                break;
                        }
                        break;
                    case "!=":
                        tempValue = getConstant(preLast);
                        tempType = constants.get(tempValue.getIndex()).getType();
                        switch (tempType) {
                            case NUMBER:
                                lastValues.push(handleNumberBiFunc(preLast, last, (a, b) -> !a.equals(b), Pair.Type.BOOLEAN));
                                break;
                            case STRING:
                                lastValues.push(handleStringBiFunc(preLast, last, (a, b) -> !a.equals(b)));
                                break;
                            case BOOLEAN:
                                lastValues.push(handleBooleanBiFunc(preLast, last, (a, b) -> !a.equals(b)));
                                break;
                        }
                        break;
                    case ">=":
                        lastValues.push(handleNumberBiFunc(preLast, last, (a, b) -> a >= b, Pair.Type.BOOLEAN));
                        break;
                    case "<=":
                        lastValues.push(handleNumberBiFunc(preLast, last, (a, b) -> a <= b, Pair.Type.BOOLEAN));
                        break;
                    case ">":
                        lastValues.push(handleNumberBiFunc(preLast, last, (a, b) -> a > b, Pair.Type.BOOLEAN));
                        break;
                    case "<":
                        lastValues.push(handleNumberBiFunc(preLast, last, (a, b) -> a < b, Pair.Type.BOOLEAN));
                        break;
                    case "+":
                        lastValues.push(handleNumberBiFunc(preLast, last, Double::sum, Pair.Type.NUMBER));
                        break;
                    case "-":
                        lastValues.push(handleNumberBiFunc(preLast, last, (a, b) -> a - b, Pair.Type.NUMBER));
                        break;
                    case "*":
                        lastValues.push(handleNumberBiFunc(preLast, last, (a, b) -> a * b, Pair.Type.NUMBER));
                        break;
                    case "/":
                        lastValues.push(handleNumberBiFunc(preLast, last, (a, b) -> a / b, Pair.Type.NUMBER));
                        break;
                    case "^":
                        lastValues.push(handleNumberBiFunc(preLast, last, Math::pow, Pair.Type.NUMBER));
                        break;
                    case "=":
                        lastValues.push(handleAssign(preLast, last));
                        break;
                }
            } else {
                lastValues.push(symbol);
            }
        }

        return identifiers;
    }

    private boolean isBinaryOperator(String symbol) {
        return symbol.matches("[=+\\-*/^<>]|==|!=|<=|>=");
    }

    private boolean isUnaryOperator(String symbol) {
        return symbol.matches("@|var|val");
    }

    private Lexem getConstant(Lexem lexem) {
        if (lexem.getToken() == Token.CONSTANT) {
            return lexem;
        }

        final Pair identifier = identifiers.get(lexem.getIndex());

        if (identifier.getValue().equals(Pair.UNDEF)) {
            throw new RuntimeException();
        }

        return createConstantIfNeeds(identifier.getValue(), identifier.getType());
    }

    private Lexem createConstantIfNeeds(String value, Pair.Type type) {
        final Lexem constant = new Lexem();
        constant.setToken(Token.CONSTANT);
        constant.setName(value);

        final int index = checkConstants(value);
        if (index > -1) {
            constant.setIndex(index);
        } else {
            constants.add(new Pair(value, type, value, false, constants.size()));
            constant.setIndex(constants.size() - 1);
        }

        return constant;
    }

    private int checkConstants(String value) {
        return constants.stream().map(Pair::getValue).collect(Collectors.toList()).indexOf(value);
    }

    private Lexem handleUnaryMinus(Lexem lastValue) {
        final Lexem constant = getConstant(lastValue);
        constant.setName(String.valueOf(Double.valueOf("-" + constant.getName())));
        return constant;
    }

    private Lexem handleDef(String defType, Lexem lexem) {
        final Pair identifier = identifiers.get(lexem.getIndex());

        if (!identifier.getValue().equals(Pair.UNDEF)) {
            throw new RuntimeException();
        }

        identifier.setValue(Pair.DEF);

        if (defType.equals("var")) {
            identifier.setModifiable(true);
        }

        return lexem;
    }

    private Lexem handleAssign(Lexem preLast, Lexem last) {
        final Pair identifier = identifiers.get(preLast.getIndex());

        if (identifier.getValue().equals(Pair.UNDEF)) {
            throw new RuntimeException();
        }
        if (!identifier.getValue().equals(Pair.DEF) && !identifier.isModifiable()) {
            throw new RuntimeException();
        }

        final Lexem lastConstant = getConstant(last);

        if (identifier.getType() != constants.get(lastConstant.getIndex()).getType()) {
            throw new RuntimeException();
        }

        identifier.setValue(lastConstant.getName());

        return preLast;
    }

    private Lexem handleNumberBiFunc(Lexem preLast, Lexem last, BiFunction<Double, Double, Object> function, Pair.Type returnValueType) {
        final Lexem preLastConstant = getConstant(preLast);
        final Lexem lastConstant = getConstant(last);

        final Pair.Type type1 = constants.get(preLastConstant.getIndex()).getType();
        final Pair.Type type2 = constants.get(lastConstant.getIndex()).getType();

        if (type1 != type2 || !type1.equals(Pair.Type.NUMBER)) {
            throw new RuntimeException();
        }

        final Object newValue = function.apply(Double.valueOf(preLastConstant.getName()), Double.valueOf(lastConstant.getName()));

        return createConstantIfNeeds(String.valueOf(newValue), returnValueType);
    }

    private Lexem handleBooleanBiFunc(Lexem preLast, Lexem last, BiFunction<Boolean, Boolean, Boolean> function) {
        final Lexem preLastConstant = getConstant(preLast);
        final Lexem lastConstant = getConstant(last);

        final Pair.Type type1 = constants.get(preLastConstant.getIndex()).getType();
        final Pair.Type type2 = constants.get(lastConstant.getIndex()).getType();

        if (type1 != type2 || !type1.equals(Pair.Type.BOOLEAN)) {
            throw new RuntimeException();
        }

        final Boolean newValue = function.apply(Boolean.valueOf(preLastConstant.getName()), Boolean.valueOf(lastConstant.getName()));

        return createConstantIfNeeds(String.valueOf(newValue), Pair.Type.BOOLEAN);
    }

    private Lexem handleStringBiFunc(Lexem preLast, Lexem last, BiFunction<String, String, Boolean> function) {
        final Lexem preLastConstant = getConstant(preLast);
        final Lexem lastConstant = getConstant(last);

        final Pair.Type type1 = constants.get(preLastConstant.getIndex()).getType();
        final Pair.Type type2 = constants.get(lastConstant.getIndex()).getType();

        if (type1 != type2 || !type1.equals(Pair.Type.STRING)) {
            throw new RuntimeException();
        }

        if (constants.get(preLastConstant.getIndex()).getType() != constants.get(lastConstant.getIndex()).getType()) {
            throw new RuntimeException();
        }

        final Boolean newValue = function.apply(preLastConstant.getName(), lastConstant.getName());

        return createConstantIfNeeds(String.valueOf(newValue), Pair.Type.BOOLEAN);
    }

}
