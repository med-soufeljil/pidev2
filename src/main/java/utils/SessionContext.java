package utils;

public final class SessionContext {

    public enum Role {
        ADMIN,
        USER
    }

    private static Role currentRole;
    private static Integer pendingFormationId;
    private static String pendingFormationTitle;
    private static boolean darkMode;

    private SessionContext() {
    }

    public static Role getCurrentRole() {
        return currentRole;
    }

    public static void setCurrentRole(Role role) {
        currentRole = role;
    }

    public static boolean isAdmin() {
        return currentRole == Role.ADMIN;
    }

    public static boolean isUser() {
        return currentRole == Role.USER;
    }

    public static void setPendingFormation(Integer formationId, String formationTitle) {
        pendingFormationId = formationId;
        pendingFormationTitle = formationTitle;
    }

    public static Integer getPendingFormationId() {
        return pendingFormationId;
    }

    public static String getPendingFormationTitle() {
        return pendingFormationTitle;
    }

    public static boolean hasPendingFormation() {
        return pendingFormationId != null;
    }

    public static void clearPendingFormation() {
        pendingFormationId = null;
        pendingFormationTitle = null;
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean enabled) {
        darkMode = enabled;
    }
}
