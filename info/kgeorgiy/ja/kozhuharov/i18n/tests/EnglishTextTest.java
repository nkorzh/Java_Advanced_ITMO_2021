package info.kgeorgiy.ja.kozhuharov.i18n.tests;

import info.kgeorgiy.ja.kozhuharov.i18n.TextStatistics;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

import static info.kgeorgiy.ja.kozhuharov.i18n.tests.TextTestUtils.*;

public class EnglishTextTest {
    protected static String TESTED_TEXT = TEXTS_FOLDER + File.separator + "english.in";
    protected static Locale LOCALE = new Locale("en", "US");

    static TextStatistics stat;

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
    public void testConstructor() {
        checkConstructor("create with Locale", getTestedClass(), Locale.class);
    }

    @Test
    public void testWords() {
        testUnitStatistics(stat.getWordStats(),
                43, 38,
                "year", "Bitcoin",
                "remittances", "a", 4.325);
    }

    @Test
    public void testSentences() {
        String max = "On the other side, Bitcoin will have 10 million potential new users and the fastest growing way to transfer $6,000,000,000 a year in remittances.";
        String min = "Bitcoin has a market cap of $680 billion.";
        String longest = max;
        String shortest = min;
        double avgLen = 87.0;
        testUnitStatistics(stat.getSentenceStats(), 3, 3, max, min, longest, shortest, avgLen);
    }

    @Test
    public void testNumbers() {
        double max = 6_000_000_000.;
        double min = 1;
        double avg = 1.2000001432e9;
        testNumberStatistics(stat.getNumStats(), 5, 5, max, min, avg);
    }

    @Test
    public void testCurrency() {
        double max = 6_000_000_000.;
        double min = 680;
        double avg = 3_000_000_340.;
        testNumberStatistics(stat.getCurrencyStats(), 2, 2, max, min, avg);
    }

    @Test
    public void testDates() {
        testDateStatistics(stat.getDateStats(), 0, 0, null, null, null);
    }
}

