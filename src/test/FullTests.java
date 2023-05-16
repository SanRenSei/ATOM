package test;

import main.*;
import org.testng.annotations.Test;
import test.util.AssertUtil;

import java.io.File;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class FullTests {

    @Test
    public void test1() throws Exception {
        ATOMRuntime.injectVariable("arr", new ATOMValue(Arrays.asList(
                new ATOMValue(10),
                new ATOMValue(15),
                new ATOMValue(3),
                new ATOMValue(7)
        )));
        ATOMRuntime.injectVariable("k", new ATOMValue(17));
        assertTrue(ATOMRuntime.processFile(new File("samples/assorted/sample1.atom")).getBoolVal());
    }

    @Test
    public void test2() throws Exception {
        ATOMTemplate atom = new ATOMTemplate();
        atom.importFile(new File("samples/assorted/sample2.atom"));
        ATOMScope program = (ATOMScope) ATOMElement.fromTemplate(atom);
        assertTrue(ATOMValue.listEquals(program.compute().getArrVal().toList(), Arrays.asList(
                new ATOMValue(120),
                new ATOMValue(60),
                new ATOMValue(40),
                new ATOMValue(30),
                new ATOMValue(24)
        )));
    }

    @Test
    public void test3() throws Exception {
        ATOMTemplate atom = new ATOMTemplate();
        atom.importFile(new File("samples/assorted/sample3.atom"));
        ATOMScope program = (ATOMScope) ATOMElement.fromTemplate(atom);
        assertEquals(program.compute().getIntVal(), -3);
    }

    @Test
    public void test4() throws Exception {
        ATOMTemplate atom = new ATOMTemplate();
        atom.importFile(new File("samples/assorted/sample4.atom"));
        ATOMScope program = (ATOMScope) ATOMElement.fromTemplate(atom);
        System.out.println(program.compute());
    }

    @Test
    public void test5() throws Exception {
        ATOMTemplate atom = new ATOMTemplate();
        atom.importFile(new File("samples/assorted/sample5.atom"));
        ATOMScope program = (ATOMScope) ATOMElement.fromTemplate(atom);
        System.out.println(program.compute());
    }

    @Test
    public void test6() throws Exception {
        ATOMTemplate atom = new ATOMTemplate();
        atom.importFile(new File("samples/assorted/sample6.atom"));
        ATOMScope program = (ATOMScope) ATOMElement.fromTemplate(atom);
        assertEquals(program.compute().getStrVal(), "abc");
    }

    @Test
    public void test7() {
        AssertUtil.assertWillPrint(() -> {
            ATOMTemplate atom = new ATOMTemplate();
            atom.importFile(new File("samples/assorted/sample7.atom"));
            ATOMScope program = (ATOMScope) ATOMElement.fromTemplate(atom);
            program.compute();
        }, "*****\r\n****\r\n***\r\n**\r\n*\r\n");
    }

    @Test
    public void test8() throws Exception {
        ATOMTemplate atom = new ATOMTemplate();
        atom.importFile(new File("samples/assorted/sample8.atom"));
        ATOMScope program = (ATOMScope) ATOMElement.fromTemplate(atom);
        program.compute();
        assertTrue(ATOMValue.listEquals(ATOMRuntime.processFile(new File("samples/assorted/sample8.atom")).getArrVal().toList(),
                Arrays.asList(
                        new ATOMValue(1),
                        new ATOMValue(1),
                        new ATOMValue(2),
                        new ATOMValue(3),
                        new ATOMValue(3),
                        new ATOMValue(3),
                        new ATOMValue(4),
                        new ATOMValue(5),
                        new ATOMValue(6),
                        new ATOMValue(9)
                )));
    }

    @Test
    public void test9() throws Exception {
        assertEquals(ATOMRuntime.processFile(new File("samples/assorted/sample9.atom")).getStrVal(), "ABC");
    }


}
