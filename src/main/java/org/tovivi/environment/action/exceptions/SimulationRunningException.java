package org.tovivi.environment.action.exceptions;

public class SimulationRunningException extends Exception{

    public SimulationRunningException() {
        super("Cannot perform the actuator. A simulation is in processing");
    }

    public SimulationRunningException(String message) {
        super(message);
    }
}
