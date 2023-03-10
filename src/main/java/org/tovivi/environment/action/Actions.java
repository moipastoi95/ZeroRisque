package org.tovivi.environment.action;

import java.util.ArrayList;

public class Actions {
    private ArrayList<Deployment> deploys;
    private Attack firstAttack;
    private Fortification fortify;

    /**
     * A following of depoy, attack and fortify moves. It represents the turn of a player
     * @param deploys the Deploy move
     * @param firstAttack the first attack to do
     * @param fortify the Fortify move
     */
    public Actions(ArrayList<Deployment> deploys, Attack firstAttack, Fortification fortify) {
        this.deploys = deploys;
        this.firstAttack = firstAttack;
        this.fortify = fortify;
    }

    public ArrayList<Deployment> getDeploys() {
        return deploys;
    }

    public Attack getFirstAttack() {
        return firstAttack;
    }

    public Fortification getFortify() {
        return fortify;
    }
}
