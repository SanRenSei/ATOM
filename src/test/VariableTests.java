package test;

import lib.JavaAdd;
import main.ATOMRuntime;
import main.ATOMValue;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.testng.Assert.*;

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

    @Test
    public void nullArithmeticTypeInferenceTest() {
        assertEquals(ATOMRuntime.processInput("a + 5").compute().getIntVal(), 5);
        assertEquals(ATOMRuntime.processInput("5 + a").compute().getIntVal(), 5);
        assertEquals(ATOMRuntime.processInput("a - 5").compute().getIntVal(), -5);
        assertEquals(ATOMRuntime.processInput("5 - a").compute().getIntVal(), 5);
        assertEquals(ATOMRuntime.processInput("a * 5").compute().getIntVal(), 0);
        assertEquals(ATOMRuntime.processInput("5 * a").compute().getIntVal(), 0);
        assertEquals(ATOMRuntime.processInput("a / 5").compute().getIntVal(), 0);
        assertThrows(ArithmeticException.class, () -> ATOMRuntime.processInput("5 / a").compute());
        assertEquals(ATOMRuntime.processInput("a + \"foo\"").compute().getStrVal(), "foo");
        assertEquals(ATOMRuntime.processInput("\"foo\" + a").compute().getStrVal(), "foo");
    }

    @Test
    public void nullArrayTypeInferenceTest() {
        assertTrue(ATOMValue.listEquals(ATOMRuntime.processInput("a.0=1;a").getArrVal().toList(), Collections.singletonList(new ATOMValue(1))));
    }

    @Test
    public void nullControllerTypeInferenceTest() {
        assertEquals(ATOMRuntime.processInput("{\"foo\":\"bar\"}").getObjVal().dereference(new ATOMValue("foo")).getStrVal(), "bar");
    }

}
