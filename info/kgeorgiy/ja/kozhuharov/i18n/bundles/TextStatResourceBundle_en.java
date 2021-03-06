package info.kgeorgiy.ja.kozhuharov.i18n.bundles;

import java.text.ChoiceFormat;

public class TextStatResourceBundle_en extends TextStatResourceBundleAbstract {
    private final double[] limits = {0d, 1d, 2d};
    private final String[] uniques = {"unique values", "unique value", "unique values"};
    private final ChoiceFormat choiceFormat = new ChoiceFormat(limits, uniques);

    private static final Object[][] CONTENTS = {
            {"Analyzed file", "Analyzed file"},
            {"General stats", "General stats"},
            {"Stats", "Stats"},
            {"by words", "by words"},
            {"by sentences", "by sentences"},
            {"by lines", "by lines"},
            {"by currency", "by currency"},
            {"by numbers", "by numbers"},
            {"by dates", "by dates"},

            {"Sentences count", "Sentences count"},
            {"Lines count", "Lines count"},
            {"Nums count", "Nums count"},
            {"Dates count", "Dates count"},
            {"Currencies count", "Currencies count"},
            {"Words count", "Words count"},

            {"Count", "Count"},
            {"Min", "Minimum"},
            {"Max", "Maximum"},
            {"Min length", "Minimum length"},
            {"Max length", "Maximum length"},
            {"Avg length", "Average length"},
            {"Avg value", "Average value"},
            {"Avg date", "Average date"},
    };

    @Override
    protected Object[][] getContents() {
        return CONTENTS;
    }

    @Override
    public String uniqueElements(int n) {
        return choiceFormat.format(n);
    }
}
