package com.vladhuk.l1compiler;

import com.vladhuk.l1compiler.lexical.LexicalAnalyzer;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        final File source = new File("D:/L1_source.txt");
        final File lexems = new File("D:/L1_lexems.txt");

        if (!lexems.exists()) {
            lexems.createNewFile();
        }

        LexicalAnalyzer.parse(source, lexems);
    }
}
