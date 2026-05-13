package utils;

public final class ThemeContext {
    private static boolean darkMode;

    private ThemeContext() {}

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean enabled) {
        darkMode = enabled;
    }
}
