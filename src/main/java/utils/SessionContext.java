package utils;

public final class SessionContext {

    public enum Role {
        ADMIN,
        USER
    }

    private static Role currentRole;

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
}
