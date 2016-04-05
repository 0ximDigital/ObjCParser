package hr.mihael;

public final class ArgsUtils {

    public static final String DEFAULT_THROW_MESSAGE = "Unsatisfied condition!";

    public static void throwIf(boolean condition) {
        throwIf(condition, DEFAULT_THROW_MESSAGE);
    }

    public static void throwIfNot(boolean condition) {
        throwIfNot(condition, DEFAULT_THROW_MESSAGE);
    }

    public static void throwIf(boolean condition, String throwMessage) {
        if (condition) {
            throw new UnsupportedOperationException(throwMessage);
        }
    }

    public static void throwIfNot(boolean condition, String throwMessage) {
        if (!condition) {
            throw new UnsupportedOperationException(throwMessage);
        }
    }
}
