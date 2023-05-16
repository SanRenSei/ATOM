package lib;

import main.ATOMRuntime;
import main.ATOMScope;
import main.ATOMValue;
import main.SparseArrayList;

public class JavaAdd extends ATOMScope {

    public ATOMValue compute() {
        SparseArrayList<ATOMValue> array = ATOMRuntime.getIndexedVar(0).getArrVal();
        return new ATOMValue(array.get(0).getIntVal() + array.get(1).getIntVal());
    }

}
