package lib;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class HttpServer extends ATOMScope {

    private ATOMValue isGet = new ATOMValue(new ATOMScope(){
        public ATOMValue compute() {
            ATOMScope request = getIndexedVar(0).getObjVal();
            return new ATOMValue(request.dereference(new ATOMValue("method")).getStrVal().equals("GET"));
        }
    });

    private ATOMValue isPost = new ATOMValue(new ATOMScope(){
        public ATOMValue compute() {
            ATOMScope request = getIndexedVar(0).getObjVal();
            return new ATOMValue(request.dereference(new ATOMValue("method")).getStrVal().equals("POST"));
        }
    });

    public ATOMValue compute() {
        try {
            ATOMScope routes = getIndexedVar(0).getObjVal();
            com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(9090), 0);
            server.createContext("/", new HttpHandlerImpl(routes));
            server.setExecutor(null); // creates a default executor
            System.out.println("STARTING SERVER");
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ATOMValue.NULL();
    }

    public ATOMValue dereference(ATOMValue key) {
        if (key.getType() != ATOMValueType.STRING) {
            throw new RuntimeException("Incorrect type: " + key.getType());
        }
        if (key.getStrVal().equals("get")) {
            return isGet;
        }
        if (key.getStrVal().equals("post")) {
            return isPost;
        }
        return ATOMValue.NULL();
    }

    static class HttpHandlerImpl implements HttpHandler {

        private ATOMScope routes;

        public HttpHandlerImpl(ATOMScope routes) {
            this.routes = routes;
        }

        public void handle(HttpExchange t) throws IOException {
            URI reqUrl = t.getRequestURI();
            String urlPath = reqUrl.getPath();
            String queries = reqUrl.getQuery();


            String response = "Error!";
            try {
                ATOMValue val = routes.dereference(new ATOMValue(urlPath));
                while (!val.equals(ATOMValue.NULL())) {
                    if (val.getType() == ATOMValueType.STRING) {
                        response = val.getStrVal();
                        break;
                    } else if (val.getType() == ATOMValueType.OBJECT) {
                        val = ATOMOperation.INTO.operate.execute(null,
                                new RequestObject(t.getRequestMethod(), getRequestBody(t.getRequestBody()))
                                        .withQueryString(queries),
                                val.getObjVal());
                        continue;
                    } else {
                        response = val.toString();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (response.equals("Error!")) {
                System.out.println("CAUGHT ERROR FROM PATH: "+urlPath);
            }
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private String getRequestBody(InputStream in){
            try {
                InputStreamReader isr =  new InputStreamReader(in, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                int b;
                StringBuilder buf = new StringBuilder(512);
                while ((b = br.read()) != -1) {
                    buf.append((char) b);
                }

                br.close();
                isr.close();
                return buf.toString();
            } catch (Exception e) {
                return "";
            }
        }
    }

}