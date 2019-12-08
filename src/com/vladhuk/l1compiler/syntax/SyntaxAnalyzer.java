package com.vladhuk.l1compiler.syntax;

import com.vladhuk.l1compiler.lexical.Lexem;
import com.vladhuk.l1compiler.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class SyntaxAnalyzer {

    public static void analyze(List<String> rows) {
        final List<Lexem> lexems = Util.getLexemsFromLexemTable(Util.getSeparatedTables(rows).get(0));

        final Grammar grammar = new Grammar();

        if (grammar.Program(lexems)) {
            System.out.println("Syntax is correct");
        } else {
            System.err.println("Syntax is wrong!");
            grammar.showError();
        }
    }

    public static void analyze(Path tables) throws IOException {
        analyze(Files.readAllLines(tables));
    }

    public static void analyze(File tables) throws IOException {
        analyze(tables.toPath());
    }

}
