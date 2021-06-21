package fr.miage.fsgbd;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


/**
 * @author Galli Gregory, Mopolo Moke Gabriel
 * @param <Type>
 */
public class BTreePlus<Type> implements java.io.Serializable {
    private Noeud<Type> racine;
    private Noeud<Type> precedent = null;
    private boolean refresh = false;
    public BTreePlus(int u, Executable e) {
        racine = new Noeud<Type>(u, e, null);
    }

    public void afficheArbre() {
        racine.afficheNoeud(true, 0);
    }

    /**
     * Méthode récursive permettant de récupérer tous les noeuds
     *
     * @return DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode bArbreToJTree() {
        if(precedent!= null)
            refresh = true;
        return bArbreToJTree(racine);
    }

    private DefaultMutableTreeNode bArbreToJTree(Noeud<Type> root) {
        StringBuilder txt = new StringBuilder();
        if(root.fils.size()==0) {
            if (precedent != null && !refresh)
                precedent.next = root;
            precedent = root;

        }
        for (Type key : root.keys) {

            txt.append(key.toString()).append(" ");

        }
        //decommenter pour voir que la liaison entre les feuilles est bien effective (appuyer sur le boutton refresh
       /*if (refresh)
        {
            if (root.next != null)
                txt.append(" " + root.next.keys.toString()+"");
       }*/
        DefaultMutableTreeNode racine2 = new DefaultMutableTreeNode(txt.toString(), true);
        for (Noeud<Type> fil : root.fils)
            racine2.add(bArbreToJTree(fil));

        return racine2;
    }



    public boolean addValeur(Type valeur) {
        refresh = false;
        this.precedent =null;
        System.out.println("Ajout de la valeur : " + valeur.toString());
        if (racine.contient(valeur) == null) {
            Noeud<Type> newRacine = racine.addValeur(valeur);
            if (racine != newRacine)
                racine = newRacine;
            return true;
        }
        return false;
    }
    public boolean addValeur(Type valeur,int ligne) {
        refresh = false;
        this.precedent =null;
        System.out.println("Ajout de la valeur : " + valeur.toString());
        if (racine.contient(valeur) == null) {
            Noeud<Type> newRacine = racine.addValeur(valeur,ligne);
            if (racine != newRacine)
                racine = newRacine;
            return true;
        }
        return false;
    }


    public void removeValeur(Type valeur) {
        refresh = false;
        this.precedent =null;
        System.out.println("Retrait de la valeur : " + valeur.toString());
        if (racine.contient(valeur) != null) {
            Noeud<Type> newRacine = racine.removeValeur(valeur, false);
            if (racine != newRacine)
                racine = newRacine;
        }
    }
    public void rechercherLigne() throws IOException {

        Long tempsSequentiel = (long)0, tempsIndex= (long)0, sequentielMin= (long)9999, sequentielMax= (long)0, indexMin= (long)9999, indexMax = (long)0;
        ArrayList<Integer> valeurs = new ArrayList<>();
        String id;
        try(BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
            int ligne = 0;
            for(String line; (line = br.readLine()) != null; ) {
                ligne++;
                if (ligne%100 ==0) {
                    id = line.substring(0, line.indexOf(","));
                    int valeur = Integer.parseInt(id);
                   valeurs.add(valeur);
                }
            }
        } catch (IOException ioException) {
            System.out.println("Veuillez d'abbord charger les données du fichier");
        }
        System.out.println("Recherche des lignes de maniere sequentielle: ");
        for( Integer value : valeurs ) {
            Long debut = System.nanoTime();
            try(BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
                for(String line; (line = br.readLine()) != null; ) {
                     id = line.substring( 0, line.indexOf(","));
                    if(Integer.parseInt(id) == (int)value)
                    {
                        Long fin = System.nanoTime();
                        Long total = (fin-debut)/1000;
                        System.out.println(line +" "+total+" microseconds");
                        tempsSequentiel = tempsSequentiel+total;
                        if (total > sequentielMax)
                            sequentielMax = total;
                        else if (total < sequentielMin)
                            sequentielMin = total;
                        break;
                    }
                }
                // line is not visible here.
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        System.out.println("Temps total en sequentiel : "+tempsSequentiel+ " microseconds");

        String line;
        System.out.println("Recherche des lignes de maniere indexee: ");
        for( Integer value : valeurs ) {
            Long debut = System.nanoTime();
            try (Stream<String> lines = Files.lines(Paths.get("data.txt"))) {
                int test = Noeud.pointeurs.get(value);
                line = lines.skip(test).findFirst().get();
            }
                 Long fin = System.nanoTime();
                        Long total = (fin-debut)/1000;
            System.out.println(line +" "+total+" microseconds");
            tempsIndex = tempsIndex+total;
            if (total > indexMax)
                indexMax = total;
            else if (total < indexMin)
                indexMin = total;

                    }
        System.out.println("\nTemps total en sequentiel : "+tempsSequentiel+ " microseconds, en moyenne une recherche prend " +tempsSequentiel/100 +" microseconds la plus courte est de "+ sequentielMin +" microseconds et la plus longue de "+ sequentielMax+" microseconds");
        System.out.println("Temps total en index : "+tempsIndex+ " microseconds, en moyenne une recherche prend " +tempsIndex/100 +" microseconds la plus courte est de "+ indexMin +" microseconds et la plus longue de "+ indexMax+" microseconds");

    }
}
