package com.vladhuk.l1compiler.util;

import com.vladhuk.l1compiler.lexical.Lexem;
import com.vladhuk.l1compiler.lexical.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Util {

    public static List<String> splitIncludingDelimiters(String text, Pattern regex) {
        final List<String> resultList = new ArrayList<>();
        final Matcher matcher = regex.matcher(text);
        int start = 0;

        while (matcher.find()) {
            if (matcher.start() != 0) {
                resultList.add(text.substring(start, matcher.start()));
            }
            resultList.add(matcher.group());
            start = matcher.end();
        }

        if (start < text.length()) {
            resultList.add(text.substring(start));
        }

        return resultList;
    }

    public static List<Lexem> getLexemsFromLexemTable(List<String> table) {
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


    public static List<List<String>> getSeparatedTables(List<String> tablesRows) {
        final String delimiter = tablesRows.get(0);
        List<String> tablesWithoutFirstRow = tablesRows.subList(1, tablesRows.size());

        final List<List<String>> splitTables = new ArrayList<>();

        int indexOfDelimiter;
        while ((indexOfDelimiter = tablesWithoutFirstRow.indexOf(delimiter)) != -1) {
            splitTables.add(tablesWithoutFirstRow.subList(0, indexOfDelimiter));
            tablesWithoutFirstRow = tablesWithoutFirstRow.subList(indexOfDelimiter + 1, tablesWithoutFirstRow.size());
        }

        return splitTables;
    }

}
