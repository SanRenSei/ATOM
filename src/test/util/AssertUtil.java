package test.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.testng.Assert.assertEquals;

public class AssertUtil {

    public static void assertWillPrint(ThrowingRunnable runnable, String toPrint) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);

        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.flush();
        System.setOut(old);

        assertEquals(baos.toString(), toPrint);
    }

}
