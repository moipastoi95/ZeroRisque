package org.tovivi.agent;

import org.tovivi.environment.Game;
import org.tovivi.environment.action.Actions;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;

public class AgentMonteCarlo extends Agent {

    //TODO: Ajouter les variables nécessaire à l'algo mdrrr
    // Surtout genre comment stocker l'arbre des coups à jouer et leur valeur....

    public AgentMonteCarlo(String color, Game game) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(color, game);

    }

    @Override
    //TODO: while(ressource_left):
    // 0 - Choisir le nouveau noeud à explorer si il y en a un (fonction traverse)
    // 1 - A partir de ce noeud parcourir l'arbre jusqu'à un état final (fonction rollout)
    // 2 - Calculer la valeur de l'état final (en fonction de qui à gagné)
    // 3 - Propager la valeur de manière récursive sur les noeuds parents (fonction backpropagate)
    // 4 - Renvoyer le meilleur child
    public Actions action() {
        return null;
    }

    //TODO: Traverse est censé parcourir les meilleurs noeuds à partir du root qui sont déjà dans l'abre
    // et renvoyer le prochain noeud à explorer.
    public LinkedList<Actions> traverse(){
        return null;
    }

    //TODO: Doit renvoyer la valeur du leaf node atteint à partir du noeud n
    public int rollout(Node n){
        return 0;
    }

    //TODO: Doit renvoyer le prochain noeud à parcourir (Au départ un noeud random et après on pourra rajouter une genre de
    // heuristic ou le NN pour qu'il explore pas random mais dans un ordre logique)
    public Node rolloutPolicy(Node n){
        return null;
    }

    //TODO: Doit propager la valeur du noeud final à tous les noeuds précedents déjà exploré dans l'arbre
    public void backPropagate(Node n, int result){

    }

    //TODO: Doit renvoyer le meilleur noeuds, genre l'action que l'algo doit renvoyer en gros
    public Actions best_child(){
        return null;
    }
}
