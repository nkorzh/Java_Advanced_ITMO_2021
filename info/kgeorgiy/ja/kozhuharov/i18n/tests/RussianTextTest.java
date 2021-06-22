package info.kgeorgiy.ja.kozhuharov.i18n.tests;

import info.kgeorgiy.ja.kozhuharov.i18n.TextStatistics;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Locale;

import static info.kgeorgiy.ja.kozhuharov.i18n.tests.TextTestUtils.*;

public class RussianTextTest {
    protected static String TESTED_TEXT = TEXTS_FOLDER + File.separator + "russian.in";
    protected static Locale LOCALE = new Locale("ru", "RU");

    static TextStatistics stat;

    @Test
    public void testConstructor() {
        checkConstructor("create with Locale", getTestedClass(), Locale.class);
    }

    @BeforeClass
    public static void initStatistics() {
        try {
            stat = getTextStatistics(TESTED_TEXT, LOCALE, Charset.forName("UTF-8"));
        } catch (final IOException e) {
            Assert.fail("Could not get text stat: " + e.getMessage());
        }
        System.out.println("Testing: " + TESTED_TEXT);
    }

    @Test
    public void testWords() {
        testUnitStatistics(stat.getWordStats(),
                22, 22,
                "хлеба", "Василий",
                "картофелин", "на", 5.272);
    }

    @Test
    public void testSentences() {
        String max = "Сегодня праздник: папа Сереже на день рождения гуся за 2 560,00 ₽.";
        String min = "Василий съел 28 картофелин 20.10.2002.";
        String longest = max;
        String shortest = "Мама мыла раму.";
        double avgLen = 35.0;
        testUnitStatistics(stat.getSentenceStats(), 5, 5, max, min, longest, shortest, avgLen);
    }

    @Test
    public void testNumbers() {
        double max = 2560;
        double min = 28;
        double avg = 1294;
        testNumberStatistics(stat.getNumStats(), 2, 2, max, min, avg);
    }

    @Test
    public void testCurrency() {
        double max = 2560;
        double min = 2560;
        double avg = 2560;
        testNumberStatistics(stat.getCurrencyStats(), 1, 1, max, min, avg);
    }

    @Test
    public void testDates() {
        DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, LOCALE);
        try {
            testDateStatistics(stat.getDateStats(), 2, 2,
                    format.parse("20.10.2002"),
                    format.parse("11.09.2001"),
                    format.parse("01.04.2002 ")
            );
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
