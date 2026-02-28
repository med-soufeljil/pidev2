package utils;

public final class AuthContext {

    public enum Role {
        ADMIN,
        USER
    }

    private static Role currentRole = null;

    private AuthContext() {
    }

    public static Role getRole() {
        return currentRole;
    }

    public static void setRole(Role role) {
        currentRole = role;
    }

    public static boolean isAdmin() {
        return currentRole == Role.ADMIN;
    }
}
