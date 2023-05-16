package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ATOMRuntime {

    public static HashMap<String, ATOMValue> globalVars = new HashMap<>();
    private static ArrayList<ATOMValue> stack = new ArrayList<>();

    public static void reset() {
        globalVars = new HashMap<>();
    }

    public static ATOMValue getIndexedVar(int index) {
        return stack.get(stack.size()-(index+1));
    }

    static void pushIndexedVar(ATOMValue var) {
        stack.add(var);
    }

    static ATOMValue popIndexedVar() {
        return stack.remove(stack.size()-1);
    }

    public static ATOMValue processInput(String input) {
        return ATOMElement.fromTemplate(new ATOMTemplate(input)).compute();
    }

    // BUNDLER: BEGIN IGNORE
    public static ATOMValue processFile(File f) throws Exception {
        ATOMTemplate atom = new ATOMTemplate();
        atom.importFile(f);
        ATOMScope program = (ATOMScope) ATOMElement.fromTemplate(atom);
        return program.eval();
    }
    // BUNDLER: END IGNORE

    public static void injectVariable(String name, ATOMValue value) {
        globalVars.put(name, value);
    }

}
