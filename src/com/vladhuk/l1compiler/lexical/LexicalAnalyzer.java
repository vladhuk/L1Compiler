package com.vladhuk.l1compiler.lexical;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.vladhuk.l1compiler.lexical.Token.*;
import static com.vladhuk.l1compiler.util.Util.splitIncludingDelimiters;

public class LexicalAnalyzer {

    public static String parse(String text) {
        final List<String> rows = Arrays.asList(text.split("\n"));

        final List<Lexem> lexemsTable = createLexemsTable(rows);
        final Set<Pair> constantsTable = addLexemIndexesAndGetPairTable(lexemsTable, CONSTANT);
        final Set<Pair> identifiersTable = addLexemIndexesAndGetPairTable(lexemsTable, IDENTIFIER);

        return "-----\n" +
                tableToString(lexemsTable) +
                "\n-----\n" +
                tableToString(constantsTable) +
                "\n-----\n" +
                tableToString(identifiersTable);
    }

    private static List<Lexem> createLexemsTable(List<String> rows) {
        final Pattern delimiter = Pattern.compile(
                "'[^']*'"                                    // String
                + "|\\d+(((\\.\\d+)?e[+-]\\d+)|(\\.\\d+))"   // Number
                + "|\\s"                                     // Space
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
                    .map(lexemName -> {
                        final Token token = Token.getToken(lexemName);
                        if (token == UNKNOWN) {
                            System.err.format("Unidentified lexem \"%s\" on the row %d.\n", lexemName, rowNumber + 1);
                            return null;
                        } else {
                            return new Lexem(rowNumber + 1, lexemName, token);
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            lexemsTable.addAll(rowList);
        }

        return lexemsTable;
    }

    private static Set<Pair> addLexemIndexesAndGetPairTable(List<Lexem> lexemsTable, Token pairType) {
        final Set<Pair> pairTable = new LinkedHashSet<>();
        for (Lexem lexem : lexemsTable) {
            if (lexem.getToken() == pairType) {
                pairTable.add(new Pair(lexem.getName()));
            }
        }

        int pairIndex = 0;
        for (Pair pair : pairTable) {
            pair.setIndex(pairIndex++);
        }

        lexemsTable.forEach(lexem -> {
            if (lexem.getToken() == pairType) {
                for (Pair element : pairTable) {
                    if (Objects.equals(element.getName(), lexem.getName())) {
                        lexem.setIndex(element.getIndex());
                    }
                }
            }
        });

        return pairTable;
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
