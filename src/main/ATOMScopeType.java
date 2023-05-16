package main;

public enum ATOMScopeType {

    PARENTHESIS('(',')'),
    ARRAY('[',']'),
    OBJECT('{','}');

    public char opening;
    public char closing;

    ATOMScopeType(char opening, char closing) {
        this.opening = opening;
        this.closing = closing;
    }

}
