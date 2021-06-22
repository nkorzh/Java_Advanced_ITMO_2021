package info.kgeorgiy.ja.kozhuharov.i18n;

import java.util.Locale;

public class NumberStatistics extends TextStatisticsCounterAbstract<Double, Double> {

    public NumberStatistics(final Locale locale) {
        super(locale);
    }

    @Override
    protected int getLength(final String s) {
        double x = Double.parseDouble(s);
        if (x == (int) x) {
            return super.getLength(String.valueOf((int) x));
        }
        return super.getLength(s);
    }

    @Override
    public Double getAverage() {
        if (count == 0) {
            return 0.;
        }

        double sum = units.entrySet()
                .stream()
                .mapToDouble(e -> e.getKey() * e.getValue())
                .reduce(0, Double::sum);
        return sum / count;
    }
}
