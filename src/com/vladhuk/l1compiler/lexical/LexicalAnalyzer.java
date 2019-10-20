package com.vladhuk.l1compiler.lexical;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.vladhuk.l1compiler.lexical.Token.LITERAL;
import static com.vladhuk.l1compiler.lexical.Token.UNKNOWN;

public class LexicalAnalyzer {

    private static String getSplitRegexWithoutDelimiterRemoving(String regex) {
        return String.format("(?=%s)|(?<=%s)", regex, regex);
    }

    private static String deleteQuotes(String string) {
        return string.replaceAll("'", "");
    }

    public static String parse(String text) {
        final List<String> rows = Arrays.asList(text.split("\n"));

        List<Lexem> lexemsTable;

        lexemsTable = identifyStringLiterals(rows);
        lexemsTable = firstEntry(lexemsTable);
        lexemsTable = secondEntry(lexemsTable);

        final Set<Identifier> indetifiersTable = getIdentifiersTable(lexemsTable);
        final Set<Constant> constantsTable = getConstantsTable(lexemsTable);

        final StringBuilder finalTables = new StringBuilder();
        //
        finalTables.append("-----");
        //
        finalTables.append("-----");

        return finalTables.toString();
    }

    private static List<Lexem> identifyStringLiterals(List<String> rows) {
        final List<Lexem> lexemsTable = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            final int rowNumber = i;
            Stream.of(rows.get(i).split(getSplitRegexWithoutDelimiterRemoving("'\\.\\*'")))
                    .filter(str -> !str.isBlank())
                    .forEach(element -> {
                        if (element.matches(LITERAL.getRegex())) {
                            lexemsTable.add(new Lexem(rowNumber, element, LITERAL));
                        } else {
                            lexemsTable.add(new Lexem(rowNumber, element, UNKNOWN));
                        }
                    });
        }

        return lexemsTable;
    }

    private static List<Lexem> firstEntry(List<Lexem> lexemsTable) {
        return null;
    }

    private static List<Lexem> secondEntry(List<Lexem> lexemsTable) {
        return null;
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
