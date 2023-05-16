package lib;

import main.ATOMScope;
import main.ATOMValue;
import main.ATOMValueType;

import java.util.HashMap;

public class RequestObject extends ATOMScope {

    private String httpMethod;
    private String body;
    private HashMap<String, ATOMValue> queryParams = new HashMap<>();

    public RequestObject(String httpMethod, String body) {
        this.httpMethod = httpMethod;
        this.body = body;
    }

    public RequestObject withQueryString(String queryString) {
        for (String param : queryString.split("&")) {
            String[] keyVal = param.split("=");
            queryParams.put(keyVal[0], new ATOMValue(keyVal.length>=2?keyVal[1]:""));
        }
        return this;
    }

    public ATOMValue dereference(ATOMValue key) {
        if (key.getType() == ATOMValueType.STRING) {
            if (key.getStrVal().equals("method")) {
                return new ATOMValue(httpMethod);
            }
            if (key.getStrVal().equals("body")) {
                return new ATOMValue(body);
            }
            if (queryParams.get(key.getStrVal())!=null) {
                return queryParams.get(key.getStrVal());
            }
        }
        return ATOMValue.NULL();
    }

    public ATOMValue eval() {
        return new ATOMValue(this);
    }

}
