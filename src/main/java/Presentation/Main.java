package Presentation;

import Dao.DaoImp;
import Metier.MetierImp;

public class Main {
    public static void main(String[] args) {

        DaoImp dao = new DaoImp();
        MetierImp metier = new MetierImp(dao);

        String days = metier.calcul() + " jours restants";
        System.out.println("resultat avec injection statique = "
                + days);
    }
}
