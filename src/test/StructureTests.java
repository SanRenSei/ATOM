package test;

import main.ATOMRuntime;
import main.ATOMValue;
import main.ATOMValueType;
import org.testng.annotations.Test;
import test.util.AssertUtil;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class StructureTests {

    @Test
    public void declareEmptyParanetheticalTest() {
        assertEquals(ATOMRuntime.processInput("()").getType(), ATOMValueType.NULL);
    }

    @Test
    public void declareEmptyArrayTest() {
        ATOMValue val = ATOMRuntime.processInput("[]").compute();
        assertEquals(val.getType(), ATOMValueType.ARRAY);
        assertEquals(val.getArrVal().size(), 0);
    }

    @Test
    public void declareEmptyControllerTest() {
        assertNull(ATOMRuntime.processInput("{}").compute());
    }

    @Test
    public void getLengthOfArrayTest() {
        assertEquals(ATOMRuntime.processInput("[].\"length\"").compute().getIntVal(), 0);
        assertEquals(ATOMRuntime.processInput("[3,1,4,1].\"length\"").compute().getIntVal(), 4);
    }

    @Test
    public void getLengthOfArrayWithoutQuotesTest() {
        assertEquals(ATOMRuntime.processInput("[].length").compute().getIntVal(), 0);
        assertEquals(ATOMRuntime.processInput("[3,1,4,1].length").compute().getIntVal(), 4);
    }

    @Test
    public void controllerReturnsLastCommandOfFirstPredicateMatchTest() {
        assertEquals(ATOMRuntime.processInput("6 INTO {" +
                "*%2==0 : 5;7," +
                "*%3==0 : 4;8," +
                "1" +
                "}").compute().getIntVal(), 7);
    }

    @Test
    public void controllerReturnsLastPredicateIfNoMatchTest() {
        assertEquals(ATOMRuntime.processInput("3 INTO {" +
                "*%2==0 : 7," +
                "1" +
                "}").compute().getIntVal(), 1);
        assertEquals(ATOMRuntime.processInput("3 INTO {" +
                "*%2==0 : 7," +
                "0" +
                "}").compute().getIntVal(), 0);
    }

    @Test
    public void implicitScopingTest() {
        assertEquals(ATOMRuntime.processInput("sum = 0;" +
                "[1,2,3] FOREACH (sum = sum+*);" +
                "sum").compute().getIntVal(), 6);
        assertEquals(ATOMRuntime.processInput("sum = 0;" +
                "[1,2,3] FOREACH sum = sum+*;" +
                "sum").compute().getIntVal(), 6);
    }

    @Test
    public void runtimeVarEvalTest() {
        AssertUtil.assertWillPrint(() -> ATOMRuntime.processInput("a=5;" +
                "b={PRINT a};" +
                "a=10;" +
                "~b"), "10\r\n");
    }

    @Test
    public void declarationVarEvalTest() {
        AssertUtil.assertWillPrint(() -> ATOMRuntime.processInput("a=5;" +
                "b=a=>{PRINT a};" +
                "a=10;" +
                "~b"), "5\r\n");
    }

    @Test()
    public void varInjectionTest() {
        AssertUtil.assertWillPrint(() -> ATOMRuntime.processInput(
                "b={\"a\":5}=>{PRINT a};" +
                "a=10;" +
                "~b"), "5\r\n");
    }

}
