package com.vladhuk.l1compiler;

import com.vladhuk.l1compiler.automat.AutomaticStarter;
import com.vladhuk.l1compiler.lexical.LexicalAnalyzer;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        final File source = new File(Main.class.getClassLoader().getResource("resources/test-src/L1_source.txt").getPath());
        final File lexems = new File("L1_lexems.txt");

        if (!lexems.exists()) {
            lexems.createNewFile();
        }

        LexicalAnalyzer.parse(source, lexems);
        AutomaticStarter.start(lexems);
    }
}
