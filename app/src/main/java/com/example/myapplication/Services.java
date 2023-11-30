package com.example.myapplication;

import java.util.regex.*;

public class Services {

    public static boolean onlySpecialCharacters(String str) {
        String regex = "[^a-zA-Z0-9]+";
        Pattern p = Pattern.compile(regex);

        if (str == null) {
            return false;
        }

        Matcher m = p.matcher(str);

        if (m.matches())
            return true;

        return false;
    }

}
