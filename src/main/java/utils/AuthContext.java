package utils;

import models.Utilisateur;

public final class AuthContext {

    public enum Role {
        ADMIN,
        EMPLOYE,
        CANDIDAT
    }

    private static Role currentRole = null;
    private static Utilisateur currentUser = null;

    private AuthContext() {
    }

    public static Role getRole() {
        return currentRole;
    }

    public static void setRole(Role role) {
        currentRole = role;
    }

    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
        if (user != null && user.getRole() != null) {
            currentRole = Role.valueOf(user.getRole());
        }
    }

    public static int getCurrentUserId() {
        return currentUser == null ? 0 : currentUser.getId();
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentRole == Role.ADMIN;
    }

    public static boolean isEmploye() {
        return currentRole == Role.EMPLOYE;
    }

    public static boolean isCandidat() {
        return currentRole == Role.CANDIDAT;
    }

    public static boolean isEmployeeOrCandidate() {
        return isEmploye() || isCandidat();
    }
}
