package com.chyzman.reboundless.util;

import java.util.Locale;

public class StringUtil {

    public static String stripString(String string) {
        return string.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    public static boolean isValidForSearch(String searchTerm, String thisTerm) {
        var strippedSearchTerm = stripString(searchTerm);
        var strippedThisTerm = stripString(thisTerm);
        return strippedThisTerm.contains(strippedSearchTerm) || strippedSearchTerm.contains(strippedThisTerm);
    }
}
