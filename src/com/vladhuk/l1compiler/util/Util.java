package com.vladhuk.l1compiler.util;

import com.vladhuk.l1compiler.lexical.Lexem;
import com.vladhuk.l1compiler.lexical.Pair;
import com.vladhuk.l1compiler.lexical.Token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Util {

    private static final Pattern STRING_DELIMITER = Pattern.compile("('[^']*')|( +)");

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
        return table.stream()
                .map(row -> {
                    final List<String> elements = Util.splitIncludingDelimiters(row, STRING_DELIMITER).stream()
                            .filter(str -> !str.isBlank())
                            .collect(Collectors.toList());

                    final int rowNumber = Integer.parseInt(elements.get(0));
                    final String lexem = elements.get(1);
                    final Token token = Token.valueOf(elements.get(2));
                    final int index = elements.size() > 3 ? Integer.valueOf(elements.get(3)) : -1;

                    return new Lexem(rowNumber, lexem, token, index);
                })
                .collect(Collectors.toList());
    }

    public static List<Pair> getPairsFromTable(List<String> table) {
        return table.stream()
                .map(row -> {
                    final List<String> elements = Util.splitIncludingDelimiters(row, STRING_DELIMITER).stream()
                            .filter(str -> !str.isBlank())
                            .collect(Collectors.toList());

                    final String name = elements.get(0);
                    final Pair.Type type = Pair.Type.valueOf(elements.get(1));
                    final String value = elements.get(2).equals(Pair.UNDEF) ? Pair.UNDEF : elements.get(2);
                    final boolean modifiable = Boolean.parseBoolean(elements.get(3));
                    final int index = Integer.parseInt(elements.get(4));

                    return new Pair(name, type, value, modifiable, index);
                })
                .collect(Collectors.toList());
    }

    public static List<List<String>> getSeparatedTables(List<String> tablesRows) {
        final String delimiter = tablesRows.get(tablesRows.size() - 1);
        List<String> tempTableRows = tablesRows;

        final List<List<String>> splitTables = new ArrayList<>();

        int indexOfDelimiter;
        while ((indexOfDelimiter = tempTableRows.indexOf(delimiter)) != -1) {
            splitTables.add(tempTableRows.subList(0, indexOfDelimiter));
            tempTableRows = tempTableRows.subList(indexOfDelimiter + 1, tempTableRows.size());
        }

        return splitTables;
    }

    public static String tableToString(Collection<?> table) {
        return table.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }

}
