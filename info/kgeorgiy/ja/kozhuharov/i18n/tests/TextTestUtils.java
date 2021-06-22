package info.kgeorgiy.ja.kozhuharov.i18n.tests;

import info.kgeorgiy.ja.kozhuharov.i18n.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public abstract class TextTestUtils {
    static final double DOUBLE_EPS = 1e-3;
    static final String TEXTS_FOLDER = String.join(File.separator,
//            "info", "kgeorgiy", "ja", "kozhuharov", "i18n",
            "resources");

    static TextStatistics getTextStatistics(final String filename,
                                            final Locale locale,
                                            final Charset charset) throws IOException {
        final TextStatistics stat = new TextStatistics(locale);
        stat.collectStatistics(filename, charset);
        return stat;
    }

    static Class<?> getTestedClass() {
        return TextStatistics.class;
    }

    static void checkConstructor(final String description, final Class<?> token, final Class<?>... params) {
        try {
            token.getConstructor(params);
        } catch (final NoSuchMethodException e) {
            fail(token.getName() + " should have " + description);
        }
    }

    private static void testStatistics(TextStatisticsCounterAbstract stat,
                                       long count,
                                       long countDistinct) {
        assertEquals(errorText("Count", count, stat.getCount()), count, stat.getCount());
        assertEquals(errorText("Distinct", countDistinct, stat.getCountDistinct()), countDistinct, stat.getCountDistinct());
    }

    public static void testNumberStatistics(NumberStatistics stat,
                                            long count,
                                            long countDistinct,
                                            double max,
                                            double min,
                                            double avg) {
        testStatistics(stat, count, countDistinct);
        assertEquals(errorText("Minimum number", min, stat.getMinEntry()), min, stat.getMinEntry(), DOUBLE_EPS);
        assertEquals(errorText("Maximum number", max, stat.getMaxEntry()), max, stat.getMaxEntry(), DOUBLE_EPS);
        assertEquals(errorText("Average number", avg, stat.getAverage()), avg, stat.getAverage(), DOUBLE_EPS);
    }

    public static void testUnitStatistics(UnitStatistics stat,
                                          long count,
                                          long countDistinct,
                                          String maxUnit,
                                          String minUnit,
                                          String longest,
                                          String shortest,
                                          double avgLen) {

        testStatistics(stat, count, countDistinct);
        assertEquals(errorText("Max unit", maxUnit, stat.getMaxEntry()), maxUnit, stat.getMaxEntry());
        assertEquals(errorText("Min unit", minUnit, stat.getMinEntry()), minUnit, stat.getMinEntry());
        assertEquals(errorText("Longest unit", longest, stat.getMaxLengthEntry()), longest, stat.getMaxLengthEntry());
        assertEquals(errorText("Shortest unit", shortest, stat.getMinLengthEntry()), shortest, stat.getMinLengthEntry());
        assertEquals(errorText("Average length", avgLen, stat.getAverage()), avgLen, stat.getAverage(), DOUBLE_EPS);
    }

    public static void testDateStatistics(DateStatistics stat,
                                          long count,
                                          long countDistinct,
                                          Date maxDate,
                                          Date minDate,
                                          Date averageDate) {
        testStatistics(stat, count, countDistinct);
        assertEquals(errorText("Max date", maxDate, stat.getMaxEntry()), maxDate, stat.getMaxEntry());
        assertEquals(errorText("Min date", minDate, stat.getMinEntry()), minDate, stat.getMinEntry());
        assertEquals(errorText("Average date", averageDate, stat.getAverage()), averageDate, stat.getAverage());
    }

    private static <T> String formatValue(T t) {
        if (t == null) {
            return "N/A (null)";
        } else if (t instanceof String) {
            return "\"" + t + "\"";
        }
        return t.toString();
    }

    private static <T> String errorText(String fieldName, T expected, T was) {
        return fieldName + " expected: " + formatValue(expected) + ", was " + formatValue(was);
    }
}
