package com.linguachat.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Languages offered in the config screen dropdowns (ISO 639-1 codes accepted
 * by Google/Kagi/DeepL). "auto" is first and is only offered for the source field.
 */
public class LangList {
    private static final String[][] LANGUAGES = {
        {"auto", "Auto-detect"},
        {"en", "English"},
        {"ru", "Russian"},
        {"uk", "Ukrainian"},
        {"de", "German"},
        {"fr", "French"},
        {"es", "Spanish"},
        {"pt", "Portuguese"},
        {"it", "Italian"},
        {"pl", "Polish"},
        {"cs", "Czech"},
        {"sk", "Slovak"},
        {"sl", "Slovenian"},
        {"hr", "Croatian"},
        {"sr", "Serbian"},
        {"bs", "Bosnian"},
        {"bg", "Bulgarian"},
        {"mk", "Macedonian"},
        {"ro", "Romanian"},
        {"hu", "Hungarian"},
        {"el", "Greek"},
        {"tr", "Turkish"},
        {"ar", "Arabic"},
        {"he", "Hebrew"},
        {"fa", "Persian"},
        {"hi", "Hindi"},
        {"bn", "Bengali"},
        {"ta", "Tamil"},
        {"th", "Thai"},
        {"vi", "Vietnamese"},
        {"id", "Indonesian"},
        {"ms", "Malay"},
        {"tl", "Filipino"},
        {"zh", "Chinese (Simplified)"},
        {"zh-TW", "Chinese (Traditional)"},
        {"ja", "Japanese"},
        {"ko", "Korean"},
        {"nl", "Dutch"},
        {"sv", "Swedish"},
        {"no", "Norwegian"},
        {"da", "Danish"},
        {"fi", "Finnish"},
        {"is", "Icelandic"},
        {"et", "Estonian"},
        {"lv", "Latvian"},
        {"lt", "Lithuanian"},
        {"be", "Belarusian"},
        {"kk", "Kazakh"},
        {"uz", "Uzbek"},
        {"ka", "Georgian"},
        {"hy", "Armenian"},
        {"az", "Azerbaijani"},
        {"sw", "Swahili"},
        {"la", "Latin"},
        {"eo", "Esperanto"},
    };

    public static int count() {
        return LANGUAGES.length;
    }

    public static String code(int index) {
        return LANGUAGES[index][0];
    }

    /** Row label as shown in the dropdown, e.g. "EN — English" */
    public static String label(int index) {
        return LANGUAGES[index][0].toUpperCase() + " — " + LANGUAGES[index][1];
    }

    /** Button caption for a stored language code (unknown codes are shown as-is) */
    public static String display(String code) {
        return code + " ▾";
    }

    public static int indexOf(String code) {
        for (int i = 0; i < LANGUAGES.length; i++) {
            if (LANGUAGES[i][0].equalsIgnoreCase(code)) {
                return i;
            }
        }
        return -1;
    }

    /** Indices of languages matching the search text (case-insensitive, code or name) */
    public static List<Integer> search(String query, boolean includeAuto) {
        String q = query == null ? "" : query.trim().toLowerCase();
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < LANGUAGES.length; i++) {
            if (!includeAuto && LANGUAGES[i][0].equals("auto")) {
                continue;
            }
            if (q.isEmpty()
                    || LANGUAGES[i][0].toLowerCase().startsWith(q)
                    || LANGUAGES[i][1].toLowerCase().contains(q)) {
                result.add(i);
            }
        }
        return result;
    }
}
