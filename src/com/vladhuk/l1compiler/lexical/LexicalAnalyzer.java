package com.vladhuk.l1compiler.lexical;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.vladhuk.l1compiler.lexical.Token.*;

public class LexicalAnalyzer {

    public static String parse(String text) {
        final List<String> rows = Arrays.asList(text.split("\n"));

        final List<Lexem> lexemsTable = createLexemsTable(rows);
        final List<Constant> constantsTable = addConstantsIndexesAndGetTable(lexemsTable);
        final List<Identifier> identifiersTable = addIdentifiersIndexesAndGetTable(lexemsTable, constantsTable);

        return tableToString(lexemsTable) +
                "\n-----\n" +
                tableToString(constantsTable) +
                "\n-----\n" +
                tableToString(identifiersTable);
    }

    private static List<Lexem> createLexemsTable(List<String> rows) {
        final Pattern delimiter = Pattern.compile("'[^']*'|\\d+(((\\.\\d+)?e[+-]\\d+))|\\s"
                + "|" + REL_OP.getRegex()
                + "|" + ADD_OP.getRegex()
                + "|" + MULT_OP.getRegex()
                + "|" + POW_OP.getRegex()
                + "|" + PUNCT.getRegex()
                + "|" + ASSIGN.getRegex()
                + "|" + BRACKET_OP.getRegex());
        final List<Lexem> lexemsTable = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            final int rowNumber = i;

            final List<Lexem> rowList = splitIncludingDelimiters(rows.get(i), delimiter).stream()
                    .filter(str -> !str.isBlank())
                    .map(lexemName -> new Lexem(rowNumber + 1, lexemName, Token.getToken(lexemName)))
                    .collect(Collectors.toList());

            lexemsTable.addAll(rowList);
        }

        return lexemsTable;
    }

    private static List<String> splitIncludingDelimiters(String text, Pattern regex) {
        final List<String> resultList = new ArrayList<>();
        final Matcher matcher = regex.matcher(text);
        int start = 0;

        while (matcher.find()) {
            if (matcher.start() != 0) {
                resultList.add(text.substring(start, matcher.start()));
            }
            resultList.add(matcher.group());
            start = matcher.end();
        }

        if (start < text.length()) {
            resultList.add(text.substring(start));
        }

        return resultList;
    }

    private static List<String> splitIncludingDelimiters(String text, String regex) {
        return splitIncludingDelimiters(text, Pattern.compile(regex));
    }

    private static List<Constant> addConstantsIndexesAndGetTable(List<Lexem> lexemsTable) {
        final Set<Constant> constantsTableSet = new LinkedHashSet<>();
        for (Lexem lexem : lexemsTable) {
            if (lexem.getToken() == CONSTANT) {
                constantsTableSet.add(new Constant(lexem.getName()));
            }
        }

        final List<Constant> constantsTable = new ArrayList<>(constantsTableSet);
        for (int i = 0; i < constantsTable.size(); i++) {
            constantsTable.get(i).setIndex(i);
        }

        lexemsTable.stream().forEach(lexem -> {
            if (lexem.getToken() == CONSTANT) {
                for (Constant constant : constantsTable) {
                    if (Objects.equals(constant.getName(), lexem.getName())) {
                        lexem.setIndex(constant.getIndex());
                    }
                }
            }
        });

        return constantsTable;
    }

    private static List<Identifier> addIdentifiersIndexesAndGetTable(List<Lexem> lexemsTable, List<Constant> constantTable) {
        final Set<Identifier> identifiersTableSet = new LinkedHashSet<>();
        for (Lexem lexem : lexemsTable) {
            if (lexem.getToken() == IDENTIFIER) {
                identifiersTableSet.add(new Identifier(lexem.getName()));
            }
        }

        final List<Identifier> identifiersTable = new ArrayList<>(identifiersTableSet);
        for (int i = 0; i < identifiersTable.size(); i++) {
            identifiersTable.get(i).setIndex(i);
        }

        lexemsTable.stream().forEach(lexem -> {
            if (lexem.getToken() == IDENTIFIER) {
                for (Identifier identifier : identifiersTable) {
                    if (Objects.equals(identifier.getName(), lexem.getName())) {
                        lexem.setIndex(identifier.getIndex());
                    }
                }
            }
        });

        return identifiersTable;
    }

    private static String tableToString(Collection<?> table) {
        return table.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }

    public static void parse(Path source, Path destination) throws IOException {
        Files.writeString(destination, parse(Files.readString(source)));
    }

    public static void parse(File source, File destination) throws IOException {
        parse(source.toPath(), destination.toPath());
    }

}
