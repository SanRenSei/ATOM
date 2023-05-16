package main;

import java.util.List;

public class ATOMValue extends ATOMElement {

    private ATOMValueType type;

    private boolean boolVal;
    private int intVal;
    private String strVal;
    private SparseArrayList<ATOMValue> arrVal;
    private ATOMScope objVal;

    public ATOMValue() {
    }

    public ATOMValue(boolean val) {
        type = ATOMValueType.BOOLEAN;
        boolVal = val;
    }

    public ATOMValue(int val) {
        type = ATOMValueType.INT;
        intVal = val;
    }

    public ATOMValue(String val) {
        type = ATOMValueType.STRING;
        strVal = val;
    }

    public ATOMValue(List<ATOMValue> val) {
        type = ATOMValueType.ARRAY;
        arrVal = new SparseArrayList<>(val);
    }

    public ATOMValue(ATOMScope val) {
        type = ATOMValueType.OBJECT;
        objVal = val;
    }

    public static ATOMValue NULL() {
        ATOMValue newVal = new ATOMValue();
        newVal.type = ATOMValueType.NULL;
        return newVal;
    }

    public ATOMValue eval() {
        return this;
    }

    public ATOMValue compute() {
        if (type == ATOMValueType.OBJECT) {
            return objVal.compute();
        }
        return this;
    }

    public String toString() {
        switch (type) {
            case OBJECT:
                return objVal.toString();
            case ARRAY:
                return arrVal.toString();
            case INT:
                return "" + intVal;
            case STRING:
                return '"' + strVal + '"';
            case BOOLEAN:
                return "" + boolVal;
            case NULL:
                return "NULL";
            default:
                return null;
        }
    }

    public boolean isTruthy() {
        switch (type) {
            case NULL:
                return false;
            case BOOLEAN:
                return boolVal;
            case INT:
                return intVal != 0;
            case ARRAY:
                return true;
            case OBJECT:
                return true;
        }
        return true;
    }

    public ATOMValueType getType() {
        return type;
    }

    protected void setType(ATOMValueType newType) {
        type = newType;
        if (newType == ATOMValueType.ARRAY) {
            arrVal = new SparseArrayList<>();
        }
    }

    public boolean getBoolVal() {
        return boolVal;
    }

    public int getIntVal() {
        return intVal;
    }

    public String getStrVal() {
        return strVal;
    }

    public SparseArrayList<ATOMValue> getArrVal() {
        return arrVal;
    }

    public ATOMScope getObjVal() {
        return objVal;
    }

    public boolean equals(ATOMValue other) {
        switch (this.type) {
            case NULL:
                return true;
            case BOOLEAN:
                return this.boolVal == other.boolVal;
            case INT:
                return this.intVal == other.intVal;
            case STRING:
                return this.strVal.equals(other.strVal);
        }
        return this == other;
    }

    public static boolean listEquals(List<ATOMValue> first, List<ATOMValue> second) {
        if (first.size()!=second.size()) {
            return false;
        }
        for (int i=0;i<first.size();i++) {
            if (!first.get(i).equals(second.get(i))) {
                return false;
            }
        }
        return true;
    }

}
