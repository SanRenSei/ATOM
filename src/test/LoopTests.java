package test;

import main.ATOMElement;
import main.ATOMRuntime;
import main.ATOMValue;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

public class LoopTests {

    @Test
    public void mapTest() {
        Assert.assertEquals(ATOMRuntime.processInput("1~5 MAP 10").getArrVal()
                .map(ATOMElement::eval)
                .map(ATOMValue::getIntVal).toList(), Arrays.asList(10, 10, 10, 10, 10));
    }

    @Test
    public void throughTest() {
        Assert.assertEquals(ATOMRuntime.processInput(
                "a = 5; sum = 0;" +
                "a THROUGH (" +
                "sum = sum + a;" +
                "a = a-1;" +
                "a" +
                ");" +
                "sum").getIntVal(), 15);
    }

}
