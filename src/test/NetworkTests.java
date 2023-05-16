package test;

import lib.HttpServer;
import main.ATOMRuntime;
import main.ATOMValue;
import org.testng.annotations.Test;

public class NetworkTests {

    public static void main(String[] args) {
        ATOMRuntime.injectVariable("httpServer", new ATOMValue(new HttpServer()));
        ATOMRuntime.processInput("{" +
                "\"/\" : \"Hello World\"," +
                "\"/test\" : 123" +
                "} INTO $httpServer");
    }

    @Test
    public void httpServerTest() {

    }

}
