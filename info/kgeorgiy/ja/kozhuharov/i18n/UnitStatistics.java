package info.kgeorgiy.ja.kozhuharov.i18n;

import info.kgeorgiy.ja.kozhuharov.i18n.interfaces.TextUnitStatCounter;

import java.util.Locale;

public class UnitStatistics extends TextStatisticsCounterAbstract<String, Float> implements TextUnitStatCounter {
    private String minLengthUnit;
    private String maxLengthUnit;

    public UnitStatistics(Locale locale) {
        super(locale);
    }

    @Override
    protected void updateMinLength(String s, int length) {
        super.updateMinLength(s, length);
        minLengthUnit = s;
    }

    @Override
    protected void updateMaxLength(String s, int length) {
        super.updateMaxLength(s, length);
        maxLengthUnit = s;
    }

    @Override
    public Float getAverage() {
        return count == 0 ? 0 : (float) sumLength / count;
    }

    @Override
    public String getMinLengthEntry() {
        return minLengthUnit;
    }

    @Override
    public String getMaxLengthEntry() {
        return maxLengthUnit;
    }
}
