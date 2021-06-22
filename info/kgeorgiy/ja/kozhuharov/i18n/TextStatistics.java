package info.kgeorgiy.ja.kozhuharov.i18n;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.text.*;
import java.util.*;
import java.util.stream.Collectors;

public class TextStatistics {
    private static final int SUBSTR_SIZE = 80;
    private final Locale locale;
    private final NumberFormat numberFormat;
    private final NumberFormat currencyFormat;
    private final List<DateFormat> dateFormats;

    private String filename;

    private NumberStatistics numStats;
    private NumberStatistics currencyStats;
    private UnitStatistics sentenceStats;
    private UnitStatistics wordStats;
    private DateStatistics dateStats;


    public TextStatistics(final Locale locale) {
        this.locale = locale;
        this.numberFormat = NumberFormat.getNumberInstance(locale);
        this.currencyFormat = NumberFormat.getCurrencyInstance(locale);
        this.dateFormats = List.of(
                DateFormat.getDateInstance(DateFormat.SHORT, locale),
                DateFormat.getDateInstance(DateFormat.DEFAULT, locale),
                DateFormat.getDateInstance(DateFormat.FULL, locale),
                DateFormat.getDateInstance(DateFormat.LONG, locale),
                DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
        );
        numStats = new NumberStatistics(locale);
        currencyStats = new NumberStatistics(locale);
        sentenceStats = new UnitStatistics(locale);
        wordStats = new UnitStatistics(locale);
        dateStats = new DateStatistics(locale);
    }

    public void collectStatistics(final String textPath) throws IOException {
        collectStatistics(textPath, Charset.forName("UTF-8"));
    }

    public void collectStatistics(final String textPath, final Charset charset) throws IOException {
        filename = textPath;
        final String text;
        try {
            final Path path = Path.of(textPath);
            try (final BufferedReader in = Files.newBufferedReader(path, charset)) {
                text = in.lines().collect(Collectors.joining());
            } catch (final NoSuchFileException e) {
                throw new IOException("Could not find file: " + path.toAbsolutePath(), e);
            } catch (final IOException e) {
                throw new IOException("Error reading file: " + textPath, e);
            }
        } catch (final InvalidPathException e) {
            throw new IOException("Invalid path to text: " + textPath, e);
        }
        countSmallUnits(text);
        countSentences(text);
    }

    private void countSmallUnits(final String text) {
        final BreakIterator iterator = BreakIterator.getWordInstance(locale);
        iterator.setText(text);
        int lastStop = -1;

        for (int index = iterator.first(), prevIndex = 0;
             index != BreakIterator.DONE;
             prevIndex = index, index = iterator.next()) {

            while (prevIndex < lastStop) {
                prevIndex = index;
                index = iterator.next();
            }
            final String textFromPrev = text.substring(prevIndex, Math.min(text.length(), prevIndex + SUBSTR_SIZE));
            int next = parseDate(textFromPrev, dateStats);
            if (next == 0) {
                next = parseNumber(textFromPrev);
            }
            if (next == 0) {
                parseWord(textFromPrev.substring(0, index - prevIndex).trim(), wordStats);
            }
            lastStop = Math.max(prevIndex + next, index);
        }
    }

    private void countSentences(final String text) {
        final BreakIterator iterator = BreakIterator.getSentenceInstance(locale);
        iterator.setText(text);

        for (int index = iterator.first(), prevIndex = 0;
             index != BreakIterator.DONE;
             prevIndex = index, index = iterator.next()) {
            final String sentence = text.substring(prevIndex, index).trim();
            if (!sentence.isEmpty()) {
                sentenceStats.accept(sentence);
            }
        }
    }

    private int parseNumber(final String text) {
        final ParsePosition pos = new ParsePosition(0);
        Number value = currencyFormat.parse(text, pos);
        if (value != null) {
            currencyStats.accept(value.doubleValue());
            numStats.accept(value.doubleValue());
            return pos.getIndex();
        }
        value = numberFormat.parse(text, pos);
        if (value != null) {
            numStats.accept(value.doubleValue());
            return pos.getIndex();
        }
        return 0;
    }

    private int parseDate(String word, DateStatistics dateStats) {
        final ParsePosition pos = new ParsePosition(0);
        Date date;
        for (final DateFormat dateFormat : dateFormats) {
            date = dateFormat.parse(word, pos);
            if (date != null) {
                dateStats.accept(date);
                return pos.getIndex();
            }
        }
        return 0;
    }

    private void parseWord(String word, UnitStatistics wordStats) {
        if (word == null || word.isEmpty()) {
            return;
        }
        if (word.length() > 1 || Character.isLetter(word.charAt(0))) {
            wordStats.accept(word);
        }
    }

    public String getFilename() {
        return filename;
    }

    public NumberStatistics getNumStats() {
        return numStats;
    }

    public NumberStatistics getCurrencyStats() {
        return currencyStats;
    }

    public UnitStatistics getSentenceStats() {
        return sentenceStats;
    }

    public UnitStatistics getWordStats() {
        return wordStats;
    }

    public DateStatistics getDateStats() {
        return dateStats;
    }

    private static void printUsage() {
        System.err.println("Usage: <file locale language> <output file locale> <input file> <output file>");
        System.err.println("or <file locale language> <file locale country> <output file locale lang> " +
                "<output file locale country> <input file> <output file>");
    }

    public static void main(String[] args) {
        if (args.length != 4 && args.length != 6 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Invalid arguments.");
            printUsage();
            return;
        }
        final Locale textLocale, outputLocale;
        final String inputFilePath, outputFilePath;
        if (args.length == 4) {
            textLocale = new Locale(args[0]);
            outputLocale = new Locale(args[1]);
            inputFilePath = args[2];
            outputFilePath = args[3];
        } else {
            textLocale = new Locale(args[0], args[1]);
            outputLocale = new Locale(args[2], args[3]);
            inputFilePath = args[4];
            outputFilePath = args[5];
        }
        final TextStatistics stat = new TextStatistics(textLocale);
        try {
            stat.collectStatistics(inputFilePath);
        } catch (final IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("Could not build statistics for this file: " + inputFilePath);
            return;
        }
        try (final BufferedWriter writer = Files.newBufferedWriter(Path.of(outputFilePath), Charset.forName("UTF-8"))) {
            writer.write(TextStatisticsComposer.getFullStatText(stat, outputLocale));
        } catch (final MissingResourceException e) {
            System.err.println("Can't find bundle. Current directory: " + Path.of(".").toAbsolutePath());
        } catch (final InvalidPathException e) {
            System.err.println("Invalid path: " + outputFilePath);
        } catch (final IllegalArgumentException e) {
            System.err.println("Invalid arguments: " + e.getMessage());
        } catch (final IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("Could not print stat to file: " + outputFilePath);
        }
    }
}
