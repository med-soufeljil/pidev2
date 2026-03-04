package utils;

import models.Utilisateur;

/**
 * Holds the currently authenticated user for the lifetime of the session.
 * Call Session.setCurrent(user) on successful login, Session.clear() on logout.
 */
public class Session {

    private static Utilisateur current;

    private Session() {}

    public static Utilisateur getCurrent() { return current; }

    public static void setCurrent(Utilisateur utilisateur) { current = utilisateur; }

    public static void clear() { current = null; }

    public static boolean isLoggedIn() { return current != null; }
}
