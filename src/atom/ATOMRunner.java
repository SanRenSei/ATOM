package atom;

import lib.FileSystem;
import lib.HttpServer;
import main.ATOMRuntime;
import main.ATOMValue;

import java.io.File;

public class ATOMRunner {

    public static void main(String[] args) throws Exception {
        ATOMRuntime.injectVariable("httpServer", new ATOMValue(new HttpServer()));
        ATOMRuntime.injectVariable("fs", new ATOMValue(new FileSystem()));
        ATOMRuntime.processFile(new File("samples/simpleWebapp/server.atom"));
    }

}
