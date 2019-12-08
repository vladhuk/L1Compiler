package com.vladhuk.l1compiler.automat;

import com.vladhuk.l1compiler.lexical.Lexem;
import com.vladhuk.l1compiler.lexical.Token;
import com.vladhuk.l1compiler.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class AutomaticStarter {

    public static void start(List<String> rows) {
        final List<Lexem> lexems = Util.getLexemsFromLexemTable(Util.getSeparatedTables(rows).get(0));
        addEolsLexems(lexems);

        final boolean isSyntaxCorrect = Automatic.build(lexems).run();

        if (isSyntaxCorrect) {
            System.out.println("Syntax is correct");
        } else {
            System.err.println("Syntax is wrong!");
        }
    }

    public static void addEolsLexems(List<Lexem> lexems) {
        for (int i = 0; i < lexems.size() - 1; i++) {
            if (lexems.get(i).getRowNumber() != lexems.get(i + 1).getRowNumber()) {
                lexems.add(i + 1, new Lexem(lexems.get(i).getRowNumber(), "\n", Token.UNKNOWN));
                i++;
            }
        }
    }

    public static void start(Path tables) throws IOException {
        start(Files.readAllLines(tables));
    }

    public static void start(File tables) throws IOException {
        start(tables.toPath());
    }

}
