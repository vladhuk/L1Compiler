package com.vladhuk.l1compiler;

import com.vladhuk.l1compiler.interpretation.Interpreter;
import com.vladhuk.l1compiler.lexical.LexicalAnalyzer;
import com.vladhuk.l1compiler.syntax.SyntaxAnalyzer;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        final File source = args.length > 0
            ? new File(args[0])
            : new File(Main.class.getClassLoader().getResource("resources/test-src/L1_source.txt").getPath());

        final File outDir = new File("out");
        outDir.mkdir();

        final File lexems = outDir.toPath().resolve("L1_lexems.txt").toFile();
        final File translation = outDir.toPath().resolve("L1_translation.txt").toFile();
        final File interpretation = outDir.toPath().resolve("L1_interpretation.txt").toFile();

        lexems.createNewFile();
        translation.createNewFile();
        interpretation.createNewFile();

        LexicalAnalyzer.parse(source, lexems);
        SyntaxAnalyzer.analyze(lexems, translation);
        Interpreter.interpret(translation, interpretation);
    }
}
