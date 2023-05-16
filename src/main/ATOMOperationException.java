package main;

public class ATOMOperationException extends RuntimeException {

    public ATOMOperationException(String operatorName, ATOMElement left, ATOMElement right) {
        super("Operation " + operatorName + "could not be called on arguments:\n"
        + "Left: "+left + "\n"
        + "Right: "+right);
    }

}
