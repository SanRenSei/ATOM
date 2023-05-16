package test;

import lib.JavaAdd;
import main.ATOMRuntime;
import main.ATOMValue;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class VariableTests {

    @Test
    public void globalVarInjectTest() {
        ATOMRuntime.injectVariable("a", new ATOMValue(5));
        assertEquals(ATOMRuntime.processInput("$a").compute().getIntVal(), 5);
    }

    @Test
    public void nativeFunctionInjectTest() {
        ATOMRuntime.injectVariable("add", new ATOMValue(new JavaAdd()));
        assertEquals(ATOMRuntime.processInput("[2,3] INTO $add").compute().getIntVal(), 5);
    }

}
