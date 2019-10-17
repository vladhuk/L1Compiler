package com.vladhuk.l1compiler.lexical;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vladhuk.l1compiler.lexical.Token.*;

public class LexicalAnalyzer {

    public static String parse(String text) {
        final List<String> rows = Arrays.asList(text.split("\n"));
        final StringBuilder lexems = new StringBuilder();

        for (int i = 0; i < rows.size(); i++) {
            final List<String> elements = Stream.of(rows.get(i).split("\\s|(?===|<=|>=|!=|\\(|\\)|:|\\^)"
                                                                              + "|(?<===|<=|>=|!=|\\(|\\)|:|\\^)"))
                    .filter(str -> !str.isBlank())
                    .collect(Collectors.toList());

            for (String element : elements) {
                Token token = UNKNOWN;

                if (element.matches("var|val") ) {
                    token = DECLARATION;
                } else if (element.matches("for|while|to|do|end")) {
                    token = LOOP;
                } else if (element.matches("true|false|'.*'|\\d+((\\.\\d+)|((\\.\\d+)?e[+-]\\d+))?")) {
                    token = LITERAL;
                } else if (element.matches("if|then")) {
                    token = CONDITION;
                } else if (element.matches("goto")) {
                    token = JUMP;
                } else if (element.matches("number|boolean|string")) {
                    token = TYPE;
                } else if (element.matches("=")) {
                    token = ASSIGN;
                } else if (element.matches("[+-]")) {
                    token = ADD_OP;
                } else if (element.matches("[*/]")) {
                    token = MULT_OP;
                } else if (element.matches("^")) {
                    token = POW_OP;
                } else if (element.matches(">|>=|<|<=|==|!=")) {
                    token = REL_OP;
                } else if (element.matches("[()]")) {
                    token = BRACKET_OP;
                } else if (element.matches("[.:]")) {
                    token = PUNCT;
                } else if (element.matches("[a-zA-Z]+\\w*")) {
                    token = IDENTIFIER;
                }

                lexems.append(String.format("%-7d %-15s %-15s\n", i + 1, element, token.name()));
            }
        }

        return lexems.toString();
    }

    public static void parse(Path source, Path destination) throws IOException {
        Files.writeString(destination, parse(Files.readString(source)));
    }

    public static void parse(File source, File destination) throws IOException {
        parse(source.toPath(), destination.toPath());
    }

}
