package info.kgeorgiy.ja.kozhuharov.bank;

import java.util.Objects;
import java.util.stream.Stream;

public class ConsoleUtils {
    static boolean anyNull(Object... args) {
        return Stream.of(args).anyMatch(Objects::isNull);
    }
}
