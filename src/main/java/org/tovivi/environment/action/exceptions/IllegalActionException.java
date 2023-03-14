package org.tovivi.environment.action.exceptions;

public class IllegalActionException extends Exception {

    public IllegalActionException() {
        super("Illegal Move");
    }

    public IllegalActionException(String message) {
        super(message);
    }
}
