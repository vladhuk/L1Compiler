package com.vladhuk.l1compiler;

import com.vladhuk.l1compiler.interpretation.Interpreter;
import com.vladhuk.l1compiler.lexical.LexicalAnalyzer;
import com.vladhuk.l1compiler.syntax.SyntaxAnalyzer;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        final File source = new File(Main.class.getClassLoader().getResource("resources/test-src/L1_source.txt").getPath());
        final File lexems = new File("L1_lexems.txt");
        final File translation = new File("L1_translation.txt");
        final File interpretation = new File("L1_interpretation.txt");

        if (!lexems.exists()) {
            lexems.createNewFile();
        }

        if (!translation.exists()) {
            translation.createNewFile();
        }

        if (!interpretation.exists()) {
            interpretation.createNewFile();
        }

        LexicalAnalyzer.parse(source, lexems);
        SyntaxAnalyzer.analyze(lexems, translation);
        Interpreter.interpret(translation, interpretation);
    }
}
