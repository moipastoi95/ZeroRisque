package org.tovivi.environment.action;

import java.util.ArrayList;

public class Actions {
    private Deployment deployment;
    private Offensive firstOffensive;

    /**
     * A following of depoy, attack and fortify moves. It represents the turn of a player
     * @param deploys the Deploy move
     * @param firstAttack the first attack to do
     * @param fortify the Fortify move
     */
    public Actions(Deployment deployment, Offensive firstOffensive) {
        this.deployment = deployment;
        this.firstOffensive = firstOffensive;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public Offensive getFirstOffensive() {
        return firstOffensive;
    }
}
