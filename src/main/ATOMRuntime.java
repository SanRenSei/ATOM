package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ATOMRuntime {

    public static HashMap<String, ATOMValue> globalVars = new HashMap<>();

    public static void reset() {
        globalVars = new HashMap<>();
    }

    public static ATOMValue processInput(String input) {
        return ATOMElement.fromTemplate(new ATOMTemplate(input)).eval();
    }

    // BUNDLER: BEGIN IGNORE
    public static ATOMValue processFile(File f) throws Exception {
        ATOMTemplate atom = new ATOMTemplate();
        atom.importFile(f);
        ATOMScope program = (ATOMScope) ATOMElement.fromTemplate(atom);
        return program.eval();
    }
    // BUNDLER: END IGNORE

    public static ATOMValue getGlobalVar(String name) {
        if (globalVars.containsKey(name)) {
            return globalVars.get(name);
        }
        return ATOMValue.NULL();
    }

    public static void injectVariable(String name, ATOMValue value) {
        globalVars.put(name, value);
    }

}
