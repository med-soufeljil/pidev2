package utils;

import models.Candidat;
import models.Offre;
import models.Recrutement;
import models.Reunion;

public class NavigationState {
    public static Candidat selectedCandidat;
    public static Offre selectedOffre;
    public static Reunion selectedReunion;
    public static Recrutement selectedRecrutement;
    public static boolean readOnly;
    public static String congesTtView;

    public static void clearAll() {
        selectedCandidat = null;
        selectedOffre = null;
        selectedReunion = null;
        selectedRecrutement = null;
        readOnly = false;
    }
}
