package info.kgeorgiy.ja.kozhuharov.i18n.interfaces;

public interface TextUnitStatCounter extends TextStatisticsCounter<String, Float> {
    String getMinLengthEntry();

    String getMaxLengthEntry();
}
