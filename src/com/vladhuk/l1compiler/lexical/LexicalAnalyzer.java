package com.vladhuk.l1compiler.lexical;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vladhuk.l1compiler.lexical.Token.*;

public class LexicalAnalyzer {

    private static String getSplitRegexIncludingDelimiters(String regex) {
        return String.format("(?=%s)|(?<=%s)", regex, regex);
    }

    public static String parse(String text) {
        final List<String> rows = Arrays.asList(text.split("\n"));

        List<Lexem> lexemsTable;

        lexemsTable = firstEntry(rows);
        lexemsTable = secondEntry(lexemsTable);

        final Set<Identifier> identifiersTable = getIdentifiersTable(lexemsTable);
        final Set<Constant> constantsTable = getConstantsTable(lexemsTable);

        final StringBuilder finalTables = new StringBuilder();
        finalTables.append(lexemsTable.stream().map(Lexem::toString).collect(Collectors.joining("\n")));
        finalTables.append("\n-----");
        //
        finalTables.append("\n-----");

        return finalTables.toString();
    }

    private static List<Lexem> firstEntry(List<String> rows) {
        final String stringRegex = "'[^']*'|\\d+(((\\.\\d+)?e[+-]\\d+))|\\s"
                + "|" + REL_OP.getRegex()
                + "|" + ADD_OP.getRegex()
                + "|" + MULT_OP.getRegex()
                + "|" + POW_OP.getRegex()
                + "|" + PUNCT.getRegex()
                + "|" + ASSIGN.getRegex()
                + "|" + BRACKET_OP.getRegex();
        final List<Lexem> lexemsTable = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            final int rowNumber = i;

            final List<Lexem> rowList = splitIncludingDelimiters(rows.get(i), stringRegex).stream()
                    .filter(str -> !str.isBlank())
                    .map(element -> {
                        if (element.matches(LITERAL.getRegex())) {
                            return new Lexem(rowNumber + 1, element, LITERAL);
                        }
                        return new Lexem(rowNumber + 1, element, UNKNOWN);
                    })
                    .collect(Collectors.toList());

            lexemsTable.addAll(rowList);
        }

        return lexemsTable;
    }

    private static List<String> splitIncludingDelimiters(String text, String regex) {
        final List<String> resultList = new ArrayList<>();
        final Matcher matcher = Pattern.compile(regex).matcher(text);
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

    private static List<Lexem> secondEntry(List<Lexem> lexemsTable) {
        return lexemsTable.stream()
                .map(lexem -> {
                    if (lexem.getToken() == LITERAL) {
                        return Collections.singletonList(lexem);
                    }
                    return Stream.of(lexem.getLexem().split("\\s"))
                            .filter(str -> !str.isBlank())
                            .map(str -> new Lexem(lexem.getRowNumber(), str, getSecondEntryToken(str)))
                            .collect(Collectors.toList());
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private static Token getSecondEntryToken(String lexem) {
        return UNKNOWN;
    }

    private static Set<Identifier> getIdentifiersTable(List<Lexem> lexemTable) {
        return null;
    }

    private static Set<Constant> getConstantsTable(List<Lexem> lexemTable) {
        return null;
    }

    public static void parse(Path source, Path destination) throws IOException {
        Files.writeString(destination, parse(Files.readString(source)));
    }

    public static void parse(File source, File destination) throws IOException {
        parse(source.toPath(), destination.toPath());
    }

}
