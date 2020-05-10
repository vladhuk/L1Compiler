package com.vladhuk.l1compiler.syntax;

import com.vladhuk.l1compiler.lexical.Lexem;
import com.vladhuk.l1compiler.lexical.Pair;
import com.vladhuk.l1compiler.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class SyntaxAnalyzer {

    public static String analyze(List<String> rows) {
        final List<List<String>> tables = Util.getSeparatedTables(rows);

        final List<Lexem> lexems = Util.getLexemsFromLexemTable(tables.get(0));
        final List<Pair> constants = Util.getPairsFromTable(tables.get(1));
        final List<Pair> identifiers = Util.getPairsFromTable(tables.get(2));

        final Grammar grammar = new Grammar();
        grammar.setIdentifiers(identifiers);

        final boolean isSyntaxCorrect = grammar.Program(lexems);
        if (!isSyntaxCorrect) {
            System.err.println("Syntax is wrong!");
            grammar.showError();
        }

        return Util.tableToString(grammar.getRpn()) +
                "\n-----\n" +
                Util.tableToString(constants) +
                "\n-----\n" +
                Util.tableToString(identifiers) +
                "\n-----";
    }

    public static void analyze(Path tables, Path destination) throws IOException {
        Files.writeString(destination, analyze(Files.readAllLines(tables)));
    }

    public static void analyze(File tables, File destination) throws IOException {
        analyze(tables.toPath(), destination.toPath());
    }

}
