package com.vladhuk.l1compiler.interpretation;

import com.vladhuk.l1compiler.lexical.Lexem;
import com.vladhuk.l1compiler.lexical.Pair;
import com.vladhuk.l1compiler.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class Interpreter {

    public static String interpret(List<String> rows) {
        final List<List<String>> tables = Util.getSeparatedTables(rows);

        final List<Lexem> lexems = Util.getLexemsFromLexemTable(tables.get(0));
        final List<Pair> identifiers = Util.getPairsFromTable(tables.get(2));

        

        return Util.tableToString(identifiers) +
                "\n-----";
    }

    public static void interpret(Path tables, Path destination) throws IOException {
        Files.writeString(destination, interpret(Files.readAllLines(tables)));
    }

    public static void interpret(File tables, File destination) throws IOException {
        interpret(tables.toPath(), destination.toPath());
    }

}
