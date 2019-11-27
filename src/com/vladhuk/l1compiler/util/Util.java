package com.vladhuk.l1compiler.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

}
