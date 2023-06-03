package test;

import main.ATOMRuntime;
import main.ATOMValue;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.testng.Assert.*;

public class OperatorTests {

    @Test
    public void additionTest() {
        assertEquals(ATOMRuntime.processInput("1+2").compute().getIntVal(), 3);
    }

    @Test
    public void arrayAdditionTest() {
        assertTrue(ATOMValue.listEquals(ATOMRuntime.processInput("a = [3,1,4];a+1").getArrVal().toList(), Arrays.asList(
                new ATOMValue(3),
                new ATOMValue(1),
                new ATOMValue(4),
                new ATOMValue(1)
        )));
        assertTrue(ATOMValue.listEquals(ATOMRuntime.processInput("a = [3,1,4];a+=1").getArrVal().toList(), Arrays.asList(
                new ATOMValue(3),
                new ATOMValue(1),
                new ATOMValue(4),
                new ATOMValue(1)
        )));
    }

    @Test
    public void subtractionTest() {
        assertEquals(ATOMRuntime.processInput("5-2").compute().getIntVal(), 3);
    }

    @Test
    public void multiplicationTest() {
        assertEquals(ATOMRuntime.processInput("2*3").compute().getIntVal(), 6);
    }

    @Test
    public void divisionTest() {
        assertEquals(ATOMRuntime.processInput("7/2").compute().getIntVal(), 3);
    }

    @Test
    public void stringSplitTest() {
        assertTrue(ATOMValue.listEquals(ATOMRuntime.processInput("\"a b c\"/\" \"").getArrVal().toList(), Arrays.asList(
                new ATOMValue("a"),
                new ATOMValue("b"),
                new ATOMValue("c")
        )));
    }

    @Test
    public void stringLength() {
        assertEquals(ATOMRuntime.processInput("\uD83D\uDCCF\"foo\"").compute().getIntVal(), 3);
    }

    @Test
    public void arrayLength() {
        assertEquals(ATOMRuntime.processInput("\uD83D\uDCCF[3,1,4]").compute().getIntVal(), 3);
    }

    @Test
    public void stringTrim() {
        assertEquals(ATOMRuntime.processInput("✂\"  foo \"").compute().getStrVal(), "foo");
    }

    @Test
    public void moduloTest() {
        assertEquals(ATOMRuntime.processInput("7%2").compute().getIntVal(), 1);
    }

    @Test
    public void negationTest() {
        assertTrue(ATOMRuntime.processInput("!0").getBoolVal());
        assertFalse(ATOMRuntime.processInput("!1").getBoolVal());
    }

    @Test
    public void dereferenceArrayTest() {
        assertEquals(ATOMRuntime.processInput("[3,1,4,1,5].2").compute().getIntVal(), 4);
        assertEquals(ATOMRuntime.processInput("[3,1,4,1,5].-1").compute().getIntVal(), 5);
    }

    @Test
    public void dereferenceObjectTest() {
        ATOMRuntime.reset();
        assertEquals(ATOMRuntime.processInput("{\"a\":1,\"b\":2}.\"a\"").compute().getIntVal(), 1);
        assertEquals(ATOMRuntime.processInput("{\"a\":1,\"b\":2}.a").compute().getIntVal(), 1);
    }

    @Test
    public void assignmentTest() {
        assertEquals(ATOMRuntime.processInput("a = 1; a").compute().getIntVal(), 1);
    }

    @Test
    public void assignmentToArrayIndexTest() {
        assertTrue(ATOMValue.listEquals(ATOMRuntime.processInput("a = [1,2,3]; a.1 = 5; a").getArrVal().toList(), Arrays.asList(
                new ATOMValue(1),
                new ATOMValue(5),
                new ATOMValue(3)
        )));
    }

    @Test
    public void assignmentToObjectFieldTest() {
        assertEquals(ATOMRuntime.processInput("a = {}; a.\"field\" = 5; a.\"field\"").getIntVal(), 5);
    }

    @Test
    public void assignmentToGlobalVarTest() {
        ATOMRuntime.processInput("$a=5");
        assertEquals(ATOMRuntime.globalVars.get("a").getIntVal(), 5);
    }

    @Test
    public void assignmentToNonExistantArrayTest() {
        ATOMRuntime.reset();
        assertTrue(ATOMValue.listEquals(ATOMRuntime.processInput("a.0=4;a").getArrVal().toList(),
                Collections.singletonList(new ATOMValue(4))));
        ATOMRuntime.processInput("$a.0=4");
        assertTrue(ATOMValue.listEquals(ATOMRuntime.globalVars.get("a").getArrVal().toList(),
                Collections.singletonList(new ATOMValue(4))));
    }

    @Test
    public void assignmentToNonExistantObjectTest() {
        ATOMRuntime.reset();
        assertEquals(ATOMRuntime.processInput("a.b=4;a.b").getIntVal(),4);
        ATOMRuntime.processInput("$a.b=4");
        assertTrue(ATOMRuntime.globalVars.get("a").getObjVal().dereference(new ATOMValue("b")).equals(new ATOMValue(4)));
    }

    @Test
    public void arrayCreationTest() {
        assertTrue(ATOMValue.listEquals(ATOMRuntime.processInput("1~5").compute().getArrVal().toList(), Arrays.asList(
                new ATOMValue(1),
                new ATOMValue(2),
                new ATOMValue(3),
                new ATOMValue(4),
                new ATOMValue(5))));
    }

    @Test
    public void callFunctionTest() {
        assertEquals(ATOMRuntime.processInput("a = {5}; ~a").getIntVal(), 5);
    }

    @Test
    public void opEqualsRightToLeftTest() {
        assertTrue(ATOMValue.listEquals(ATOMRuntime.processInput("a=5;b=7;a+=b+=1;[a,b]").getArrVal().toList(), Arrays.asList(
                new ATOMValue(13),
                new ATOMValue(8)
        )));
    }

    @Test
    public void plusEqualsTest() {
        assertEquals(ATOMRuntime.processInput("a=5;a+=2;a").getIntVal(), 7);
    }

    @Test
    public void minusEqualsTest() {
        assertEquals(ATOMRuntime.processInput("a=5;a-=2;a").getIntVal(), 3);
    }

    @Test
    public void timesEqualsTest() {
        assertEquals(ATOMRuntime.processInput("a=5;a*=2;a").getIntVal(), 10);
    }

    @Test
    public void divideEqualsTest() {
        assertEquals(ATOMRuntime.processInput("a=5;a/=2;a").getIntVal(), 2);
    }

    @Test
    public void unpackTest() {
        assertEquals(ATOMRuntime.processInput(
                "var = {\"a\":5};" +
                "var UNPACK a;" +
                "a").getIntVal(), 5);
    }

    @Test
    public void unpackArrayTest() {
        assertTrue(ATOMValue.listEquals(ATOMRuntime.processInput(
                "var = {\"a\":5, \"b\":7};" +
                        "var UNPACK {a,b};" +
                        "[a,b]").getArrVal().toList(),
                Arrays.asList(new ATOMValue(5), new ATOMValue(7)
        )));
    }

}
