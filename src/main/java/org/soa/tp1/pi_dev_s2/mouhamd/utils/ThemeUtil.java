package org.soa.tp1.pi_dev_s2.mouhamd.utils;

import javafx.scene.Parent;

public final class ThemeUtil {
    private ThemeUtil() {}

    public static void applyTheme(Parent root) {
        if (root == null) return;
        if (SessionContext.isDarkMode()) {
            if (!root.getStyleClass().contains("dark-mode")) root.getStyleClass().add("dark-mode");
        } else {
            root.getStyleClass().remove("dark-mode");
        }
    }
}
