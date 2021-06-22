package info.kgeorgiy.ja.kozhuharov.i18n;

import java.util.Date;
import java.util.Locale;

public class DateStatistics extends TextStatisticsCounterAbstract<Date, Date> {

    public DateStatistics(Locale locale) {
        super(locale);
    }

    @Override
    public Date getAverage() {
        if (count == 0) {
            return null;
        }
        return new Date(
                (long) (units.entrySet()
                        .stream()
                        .mapToLong(e -> e.getValue() * e.getKey().getTime() / (1000L))
                        .average()
                        .getAsDouble() * 1000L)
        );
    }
}
