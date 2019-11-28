package com.vladhuk.l1compiler.syntax;

import com.vladhuk.l1compiler.lexical.Lexem;
import com.vladhuk.l1compiler.lexical.Token;
import com.vladhuk.l1compiler.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SyntaxAnalyzer {

    public static void analyze(List<String> tables) {
        final List<Lexem> lexems = getLexemsFromTable(getLexemTableFromTables(tables));

        if (Grammar.Program(lexems)) {
            System.out.println("Syntax is correct");
        } else {
            System.err.println("Syntax is wrong!");
        }
    }

    private static List<Lexem> getLexemsFromTable(List<String> table) {
        final Pattern stringDelimiter = Pattern.compile("('[^']*')|( +)");

        return table.stream()
                .map(row -> {
                    final List<String> elements = Util.splitIncludingDelimiters(row, stringDelimiter).stream()
                            .filter(str -> !str.isBlank())
                            .collect(Collectors.toList());

                    final int rowNumber = Integer.parseInt(elements.get(0));
                    final String lexem = elements.get(1);
                    final Token token = Token.valueOf(elements.get(2));

                    return new Lexem(rowNumber, lexem, token);
                })
                .collect(Collectors.toList());
    }

    private static List<String> getLexemTableFromTables(List<String> tables) {
        final String delimiter = tables.get(0);
        final List<String> tablesWithoutFirstRow = tables.subList(1, tables.size());
        return tablesWithoutFirstRow.subList(0, tablesWithoutFirstRow.indexOf(delimiter));
    }

    public static void analyze(Path tables) throws IOException {
        analyze(Files.readAllLines(tables));
    }

    public static void analyze(File tables) throws IOException {
        analyze(tables.toPath());
    }

}
