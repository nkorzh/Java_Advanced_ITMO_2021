package info.kgeorgiy.ja.kozhuharov.i18n;

import info.kgeorgiy.ja.kozhuharov.i18n.bundles.TextStatResourceBundleAbstract;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TextStatisticsComposer {
    private static final String BUNDLES_PACKAGE = String.join(".",
            "info", "kgeorgiy", "ja", "kozhuharov", "i18n", "bundles");
    private static final String NEWLINE = System.lineSeparator();
    private static TextStatResourceBundleAbstract bundle;

    public static String getFullStatText(final TextStatistics stats, final Locale locale) throws IOException {
        bundle = (TextStatResourceBundleAbstract) ResourceBundle
                .getBundle(BUNDLES_PACKAGE + ".TextStatResourceBundle", locale);

        if (bundle == null) {
            throw new IllegalArgumentException("Requested bundle not found: only RU and EN locales supported.");
        }

        String title = MessageFormat.format("{0}: {1}", bundle.getString("Analyzed file"), stats.getFilename());
        String total = getSummaryStat(stats, bundle);

        String statsPrefix = bundle.getString("Stats") + " ";
        List<String> statsByCategories = List.of(
                unitStatToText(stats.getWordStats(), statsPrefix + bundle.getString("by words")),
                unitStatToText(stats.getSentenceStats(), statsPrefix + bundle.getString("by sentences")),
                dateStatToText(stats.getDateStats(), statsPrefix + bundle.getString("by dates")),
                numberStatToText(stats.getNumStats(), statsPrefix + bundle.getString("by numbers")),
                numberStatToText(stats.getCurrencyStats(), statsPrefix + bundle.getString("by currency"))
        );

        final StringBuilder finalReport = new StringBuilder(title);
        finalReport.append(NEWLINE).append(total).append(NEWLINE);
        statsByCategories.forEach(s -> finalReport.append(s).append(NEWLINE));

        return finalReport.toString();
    }

    private static String getSummaryStat(final TextStatistics stat, final TextStatResourceBundleAbstract bundle) {
        List<String> summaryLines = List.of(
                bundle.getString("General stats"),
                formatStatParameter(stat.getSentenceStats().getCount(), "Sentences count"),
                formatStatParameter(stat.getWordStats().getCount(), "Words count"),
                formatStatParameter(stat.getNumStats().getCount(), "Nums count"),
                formatStatParameter(stat.getCurrencyStats().getCount(), "Currencies count"),
                formatStatParameter(stat.getDateStats().getCount(), "Dates count")
        );
        return summaryLines.stream().collect(Collectors.joining(System.lineSeparator()));
    }

    private static <T extends Comparable<? super T>, E> List<String> statToText(TextStatisticsCounterAbstract<T, E> stats,
                                                                                String title) {

        int distinctCount = stats.getCountDistinct() > Integer.MAX_VALUE ?
                Integer.MAX_VALUE :
                (int) stats.getCountDistinct();

        return new ArrayList<>(List.of(
                title,
                tab(MessageFormat.format("{0}: {1} ({2} {3})",
                        bundle.getString("Count"),
                        stats.getCount(),
                        stats.getCountDistinct(),
                        bundle.uniqueElements(distinctCount))),
                formatStatParameter(stats.getMaxEntry(), "Max"),
                formatStatParameter(stats.getMinEntry(), "Min")
        ));
    }

    private static String unitStatToText(final UnitStatistics stat,
                                         final String title) {
        List<String> lines = statToText(stat, title);
        lines.add(formatStatParameter(stat.getMaxLengthEntry(), "Max length"));
        lines.add(formatStatParameter(stat.getMinLengthEntry(), "Min length"));
        lines.add(formatStatParameter(stat.getAverage(), "Avg length"));
        return String.join(NEWLINE, lines);
    }

    private static String numberStatToText(final NumberStatistics stat,
                                           final String title) {
        List<String> lines = statToText(stat, title);
        lines.add(formatStatParameter(stat.getAverage(), "Avg value"));
        return String.join(NEWLINE, lines);
    }

    private static String dateStatToText(final DateStatistics stat,
                                         final String title) {
        List<String> lines = statToText(stat, title);
        lines.add(formatStatParameter(stat.getAverage(), "Avg date"));
        return String.join(NEWLINE, lines);
    }

    private static <T> String formatStatParameter(final T value, final String key) {
        return tab(MessageFormat.format("{0}: {1}", bundle.getString(key), safeToString(value)));
    }

    private static String tab(String s) {
        return "\t" + s;
    }

    private static <T> String safeToString(T o) {
        if (o == null) {
            return "N/A";
        } else if (o instanceof String) {
            return "\"" + o + "\"";
        }
        return o.toString();
    }
}
