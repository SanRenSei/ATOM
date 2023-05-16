package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class ATOMOperation extends ATOMElement {

    public static int ORDER_DEREF = 1;
    public static int ORDER_ARRGEN = 2;
    public static int ORDER_MULT = 3;
    public static int ORDER_ADD = 4;
    public static int ORDER_MINMAX = 5;
    public static int ORDER_COMPARE = 6;
    public static int ORDER_AND = 7;
    public static int ORDER_INJECT = 8;
    public static int ORDER_ASSIGNMENT = 9;
    public static int ORDER_SEP = 10;
    public static int ORDER_PRINT = 11;
    public static int ORDER_ARRIN = 12;
    public static int ORDER_FUNC = 13;
    public static int ORDER_OPASSIGN = 100;

    public static ATOMOperation DEREFERENCE = new ATOMOperation(Collections.singletonList("."), ORDER_DEREF, (left, right) -> {
        ATOMValue rightVal = right.eval();
        if (rightVal.getType() == ATOMValueType.STRING && left.parent!=null) {
            ATOMValue potentialVar = left.parent.getLocalVar(rightVal.getStrVal());
            if (!potentialVar.equals(ATOMValue.NULL())) {
                rightVal = potentialVar;
            }
        }

        if (left instanceof ATOMValueDynamic) {
            return ((ATOMValueDynamic)left).generateChild(rightVal);
        }

        ATOMValue leftVal = left.eval();

        if (leftVal.getType() == ATOMValueType.ARRAY && rightVal.getType()==ATOMValueType.STRING
                && rightVal.getStrVal().equals("length")) {
            return new ATOMValue(leftVal.getArrVal().size());
        }
        if (leftVal.getType()==ATOMValueType.ARRAY && rightVal.getType()==ATOMValueType.INT) {
            return leftVal.getArrVal().get(rightVal.getIntVal()).eval();
        }
        if (leftVal.getType() == ATOMValueType.OBJECT && leftVal.getObjVal().type==ATOMScopeType.OBJECT) {
            return leftVal.getObjVal().dereference(rightVal);
        }
        throw new ATOMOperationException(".", left, right);
    });

    public static ATOMOperation ARR_GEN = new ATOMOperation(Collections.singletonList("~"), ORDER_ARRGEN, (left, right) -> {
        ATOMValue rightVal = right.eval();
        if (left == null && rightVal.getType() == ATOMValueType.OBJECT) {
            return rightVal.getObjVal().compute();
        }
        ATOMValue leftVal = left.eval();
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            int startVal = leftVal.getIntVal();
            int endVal = rightVal.getIntVal();
            List<ATOMValue> arrGen = new ArrayList<>();
            for (int i=startVal;i<=endVal;i++) {
                arrGen.add(new ATOMValue(i));
            }
            return new ATOMValue(arrGen);
        }
        throw new ATOMOperationException("~", left, right);
    });

    public static ATOMOperation NEGATE = new ATOMOperation(Collections.singletonList("!"), ORDER_MULT,
            (left, right) -> new ATOMValue(!right.eval().isTruthy()));

    public static ATOMOperation MULTIPLY = new ATOMOperation(Collections.singletonList("*"), ORDER_MULT, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() * rightVal.getIntVal());
        }
        throw new ATOMOperationException("*", left, right);
    });
    public static ATOMOperation DIVISION = new ATOMOperation(Collections.singletonList("/"), ORDER_MULT, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() / rightVal.getIntVal());
        }
        throw new ATOMOperationException("/", left, right);
    });
    public static ATOMOperation MODULO = new ATOMOperation(Collections.singletonList("%"), ORDER_MULT, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() % rightVal.getIntVal());
        }
        throw new ATOMOperationException("%", left, right);
    });

    public static ATOMOperation ADD = new ATOMOperation(Collections.singletonList("+"), ORDER_ADD, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.NULL && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(rightVal.getIntVal());
        }
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() + rightVal.getIntVal());
        }
        if (leftVal.getType() == ATOMValueType.STRING && rightVal.getType() == ATOMValueType.STRING) {
            return new ATOMValue(leftVal.getStrVal() + rightVal.getStrVal());
        }
        throw new ATOMOperationException("+", left, right);
    });
    public static ATOMOperation SUBTRACT = new ATOMOperation(Collections.singletonList("-"), ORDER_ADD, (left, right) -> {
        ATOMValue rightVal = right.eval();
        if (left == null) {
            if (rightVal.getType() == ATOMValueType.INT) {
                return new ATOMValue(-rightVal.getIntVal());
            }
            throw new ATOMOperationException("-", left, right);
        }
        ATOMValue leftVal = left.eval();
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() - rightVal.getIntVal());
        }
        throw new ATOMOperationException("-", left, right);
    });

    public static ATOMOperation MAXIMUM = new ATOMOperation(Collections.singletonList("><"), ORDER_MINMAX, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.NULL && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(rightVal.getIntVal());
        }
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(Math.max(leftVal.getIntVal(), rightVal.getIntVal()));
        }
        if (leftVal.getType() == ATOMValueType.STRING && rightVal.getType() == ATOMValueType.STRING) {
            return new ATOMValue(leftVal.getStrVal().compareTo(rightVal.getStrVal())>0?leftVal.getStrVal():rightVal.getStrVal());
        }
        throw new ATOMOperationException("><", left, right);
    });

    public static ATOMOperation MINIMUM = new ATOMOperation(Collections.singletonList("<>"), ORDER_MINMAX, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.NULL && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(rightVal.getIntVal());
        }
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(Math.min(leftVal.getIntVal(), rightVal.getIntVal()));
        }
        if (leftVal.getType() == ATOMValueType.STRING && rightVal.getType() == ATOMValueType.STRING) {
            return new ATOMValue(leftVal.getStrVal().compareTo(rightVal.getStrVal())>0?rightVal.getStrVal():leftVal.getStrVal());
        }
        throw new ATOMOperationException("><", left, right);
    });

    public static ATOMOperation EQUALITY = new ATOMOperation(Collections.singletonList("=="), ORDER_COMPARE, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() == rightVal.getIntVal());
        }
        throw new ATOMOperationException("==", left, right);
    });
    public static ATOMOperation NOTEQUAL = new ATOMOperation(Collections.singletonList("!="), ORDER_COMPARE, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() != rightVal.getIntVal());
        }
        throw new ATOMOperationException("!=", left, right);
    });
    public static ATOMOperation LESSTHAN = new ATOMOperation(Collections.singletonList("<"), ORDER_COMPARE, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() < rightVal.getIntVal());
        }
        throw new ATOMOperationException("<", left, right);
    });
    public static ATOMOperation LESSTHANOREQUAL = new ATOMOperation(Collections.singletonList("<="), ORDER_COMPARE, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() <= rightVal.getIntVal());
        }
        throw new ATOMOperationException("<=", left, right);
    });
    public static ATOMOperation GREATER = new ATOMOperation(Collections.singletonList(">"), ORDER_COMPARE, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() > rightVal.getIntVal());
        }
        throw new ATOMOperationException(">", left, right);
    });
    public static ATOMOperation GREATEROREQUAL = new ATOMOperation(Collections.singletonList(">="), ORDER_COMPARE, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() >= rightVal.getIntVal());
        }
        throw new ATOMOperationException(">=", left, right);
    });

    public static ATOMOperation AND = new ATOMOperation(Collections.singletonList("&"), ORDER_AND, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.BOOLEAN && rightVal.getType() == ATOMValueType.BOOLEAN) {
            return new ATOMValue(leftVal.getBoolVal() && rightVal.getBoolVal());
        }
        throw new ATOMOperationException("&", left, right);
    });

    public static ATOMOperation INJECT = new ATOMOperation(Arrays.asList("INJECT", "->", "=>"), ORDER_INJECT, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (rightVal.getType() == ATOMValueType.OBJECT) {
            ATOMScope rightValObj = rightVal.getObjVal();
            if (left instanceof ATOMValueDynamic) {
                rightValObj.localVars.put(((ATOMValueDynamic) left).name, rightValObj.getLocalVar(((ATOMValueDynamic) left).name).compute());
                return rightVal;
            }
            if (leftVal.getType() == ATOMValueType.OBJECT) {
                ATOMScope leftValScope = leftVal.getObjVal();
                if (leftValScope.type == ATOMScopeType.OBJECT) {
                    for (List<ATOMExpression>[] branch : leftValScope.branches) {
                        ATOMValue predicate = null, result = null;
                        for (int i = 0; i < branch[0].size(); i++) {
                            predicate = branch[0].get(i).eval();
                        }
                        if (predicate!=null && predicate.getType() == ATOMValueType.STRING) {
                            for (int i = 0; i < branch[1].size(); i++) {
                                result = branch[1].get(i).eval();
                            }
                            rightValObj.localVars.put(predicate.getStrVal(), result);
                        }
                    }
                    return rightVal;
                }
            }
            return ATOMValue.NULL();
//            ATOMValue rightVal = right.eval();
//            if (rightVal.getType() == ATOMValueType.OBJECT) {
//                ATOMScope vars = rightVal.getObjVal();
//                vars.branches.forEach(b -> {
//                    ATOMElement var = b[0].get(0).children.get(0);
//                    if (var instanceof ATOMValueDynamic) {
//                        String name = ((ATOMValueDynamic) var).name;
//                        rightVal.parent.setLocalVar(name, leftValObj.dereference(new ATOMValue(name)));
//                    } else {
//                        throw new ATOMOperationException("UNPACK", left, right);
//                    }
//                });
//                return ATOMValue.NULL();
//            }
        }
        throw new ATOMOperationException("UNPACK", left, right);
    });

    public static ATOMOperation ASSIGNMENT = new ATOMOperation(Collections.singletonList("="), ORDER_ASSIGNMENT, (left, right) -> {
        if (left instanceof ATOMValueDynamic) {
            ((ATOMValueDynamic) left).setVal(right.eval());
            return left.eval();
        }
        throw new ATOMOperationException("ASSIGNMENT", left, right);
    });

    public static ATOMOperation S_SEPERATOR = new ATOMOperation(Collections.singletonList(","), ORDER_SEP, null);
    public static ATOMOperation P_SEPERATOR = new ATOMOperation(Collections.singletonList(":"), ORDER_SEP, null);

    public static ATOMOperation END_STATEMENT = new ATOMOperation(Collections.singletonList(";"), ORDER_SEP, null);

    public static ATOMOperation PRINT = new ATOMOperation(Arrays.asList("PRINT", "🖨"), ORDER_PRINT, (left, right) -> {
        ATOMValue rightVal = right.eval();
        System.out.println(rightVal.getType() == ATOMValueType.STRING ? rightVal.getStrVal() : rightVal);
        return rightVal;
    });

    public static ATOMOperation IN = new ATOMOperation(Collections.singletonList("IN"), ORDER_ARRIN, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (rightVal.getType() == ATOMValueType.ARRAY) {
            for (int i=0;i<rightVal.getArrVal().size();i++) {
                if (leftVal.equals(rightVal.getArrVal().get(i))) {
                    return new ATOMValue(true);
                }
            }
            return new ATOMValue(false);
        }
        throw new ATOMOperationException("IN", left, right);
    });

    public static ATOMOperation INTO = new ATOMOperation(Collections.singletonList("INTO"), ORDER_FUNC, (left, right) -> {
        if (right instanceof ATOMScope) {
            ATOMRuntime.pushIndexedVar(left.eval());
            ATOMValue toReturn = right.compute();
            ATOMRuntime.popIndexedVar();
            return toReturn;
        }
        throw new RuntimeException("BAD TYPING INSIDE INTO");
    });
    public static ATOMOperation FOREACH = new ATOMOperation(Arrays.asList("FOREACH", "∀"), ORDER_FUNC, (left, right) -> {
        ATOMValue leftVal = left.eval();
        if (leftVal.getType() == ATOMValueType.ARRAY && right instanceof ATOMScope) {
            for (int i=0;i<leftVal.getArrVal().size();i++) {
                ATOMElement iterator = leftVal.getArrVal().get(i);
                ATOMRuntime.pushIndexedVar(iterator.eval());
                right.compute();
                ATOMRuntime.popIndexedVar();
            }
            return null;
        }
        throw new ATOMOperationException("FOREACH", left, right);
    });
    public static ATOMOperation IFOREACH = new ATOMOperation(Arrays.asList("iFOREACH", "i∀"), ORDER_FUNC, (left, right) -> {
        if (left == null) {
            throw new RuntimeException("No left operand for operator iFOREACH");
        }
        if (right == null) {
            throw new RuntimeException("No right operand for operator iFOREACH");
        }
        ATOMValue leftVal = left.eval();
        if (leftVal.getType() == ATOMValueType.ARRAY && right instanceof ATOMScope) {
            for (int i=0;i<leftVal.getArrVal().size();i++) {
                ATOMRuntime.pushIndexedVar(new ATOMValue(i));
                right.compute();
                ATOMRuntime.popIndexedVar();
            }
            return ATOMValue.NULL();
        }
        throw new ATOMOperationException("IFOREACH", left, right);
    });
    public static ATOMOperation MAP = new ATOMOperation(Arrays.asList("MAP", "\uD83D\uDDFA"), ORDER_FUNC, (left, right) -> {
        ATOMValue leftVal = left.eval();
        if (leftVal.getType() == ATOMValueType.ARRAY && right instanceof ATOMScope) {
            List<ATOMValue> mappedVals = new ArrayList<>();
            for (int i=0;i< leftVal.getArrVal().size();i++) {
                ATOMElement iterator = leftVal.getArrVal().get(i);
                ATOMRuntime.pushIndexedVar(iterator.eval());
                mappedVals.add(right.compute());
                ATOMRuntime.popIndexedVar();
            }
            return new ATOMValue(mappedVals);
        }
        throw new ATOMOperationException("MAP", left, right);
    });
    public static ATOMOperation WHERE = new ATOMOperation(Arrays.asList("WHERE","🔍"), ORDER_FUNC, (left, right) -> {
        ATOMValue leftVal = left.eval();
        if (leftVal.getType() == ATOMValueType.ARRAY && (right instanceof ATOMScope)) {
            List<ATOMValue> filteredVals = new ArrayList<>();
            for (int i=0;i< leftVal.getArrVal().size();i++) {
                ATOMValue iterator = leftVal.getArrVal().get(i);
                ATOMRuntime.pushIndexedVar(iterator.eval());
                ATOMValue filterVal = right.compute();
                if (filterVal.isTruthy()) {
                    filteredVals.add(iterator);
                }
                ATOMRuntime.popIndexedVar();
            }
            return new ATOMValue(filteredVals);
        }
        throw new ATOMOperationException("WHERE", left, right);
    });
    public static ATOMOperation IWHERE = new ATOMOperation(Arrays.asList("iWHERE","i🔍"), ORDER_FUNC, (left, right) -> {
        ATOMValue leftVal = left.eval();
        if (leftVal.getType() == ATOMValueType.ARRAY && (right instanceof ATOMScope)) {
            List<ATOMValue> filteredVals = new ArrayList<>();
            for (int i=0;i< leftVal.getArrVal().size();i++) {
                ATOMValue iterator = new ATOMValue(i);
                ATOMRuntime.pushIndexedVar(iterator.eval());
                ATOMValue filterVal = right.compute();
                if (filterVal.isTruthy()) {
                    filteredVals.add(iterator);
                }
                ATOMRuntime.popIndexedVar();
            }
            return new ATOMValue(filteredVals);
        }
        throw new ATOMOperationException("IWHERE", left, right);
    });
    public static ATOMOperation THROUGH = new ATOMOperation(Arrays.asList("THROUGH", "\uD83D\uDD73"), ORDER_FUNC, (left, right) -> {
        ATOMValue leftVal = left.eval();
        if (right instanceof ATOMScope) {
            while (leftVal.isTruthy()) {
                ATOMRuntime.pushIndexedVar(leftVal);
                leftVal = right.compute();
                ATOMRuntime.popIndexedVar();
            }
            return leftVal;
        }
        throw new ATOMOperationException("THROUGH", left, right);
    });
    public static ATOMOperation UNPACK = new ATOMOperation(Arrays.asList("UNPACK", "\uD83C\uDF92"), ORDER_FUNC, (left, right) -> {
        ATOMValue leftVal = left.eval();
        if (leftVal.getType() == ATOMValueType.OBJECT) {
            ATOMScope leftValObj = leftVal.getObjVal();
            if (right instanceof ATOMValueDynamic) {
                ((ATOMValueDynamic) right).setVal(leftValObj.dereference(new ATOMValue(((ATOMValueDynamic) right).name)));
                return ATOMValue.NULL();
            }
            ATOMValue rightVal = right.eval();
            if (rightVal.getType() == ATOMValueType.OBJECT) {
                ATOMScope vars = rightVal.getObjVal();
                vars.branches.forEach(b -> {
                    ATOMElement var = b[0].get(0).children.get(0);
                    if (var instanceof ATOMValueDynamic) {
                        String name = ((ATOMValueDynamic) var).name;
                        rightVal.parent.setLocalVar(name, leftValObj.dereference(new ATOMValue(name)));
                    } else {
                        throw new ATOMOperationException("UNPACK", left, right);
                    }
                });
                return ATOMValue.NULL();
            }
        }
        throw new ATOMOperationException("UNPACK", left, right);
    });

    public static ATOMOperation PLUS_EQUALS = new ATOMOperation(Collections.singletonList("+="), ORDER_OPASSIGN,
            (left, right) -> ASSIGNMENT.operate.apply(left, ADD.operate.apply(left, right)));

    public static ATOMOperation MINUS_EQUALS = new ATOMOperation(Collections.singletonList("-="), ORDER_OPASSIGN,
            (left, right) -> ASSIGNMENT.operate.apply(left, SUBTRACT.operate.apply(left, right)));

    public static ATOMOperation TIMES_EQUALS = new ATOMOperation(Collections.singletonList("*="), ORDER_OPASSIGN,
            (left, right) -> ASSIGNMENT.operate.apply(left, MULTIPLY.operate.apply(left, right)));

    public static ATOMOperation DIVIDE_EQUALS = new ATOMOperation(Collections.singletonList("/="), ORDER_OPASSIGN,
            (left, right) -> ASSIGNMENT.operate.apply(left, DIVISION.operate.apply(left, right)));

    public static ATOMOperation MAX_EQUALS = new ATOMOperation(Collections.singletonList("><="), ORDER_OPASSIGN,
            (left, right) -> ASSIGNMENT.operate.apply(left, MAXIMUM.operate.apply(left, right)));

    public static ATOMOperation MIN_EQUALS = new ATOMOperation(Collections.singletonList("<>="), ORDER_OPASSIGN,
            (left, right) -> ASSIGNMENT.operate.apply(left, MINIMUM.operate.apply(left, right)));

    public static ATOMOperation[] operations = {
            EQUALITY, // ==
            NOTEQUAL, // !=
            PLUS_EQUALS, // +=
            MINUS_EQUALS, // -=
            TIMES_EQUALS, // *=
            DIVIDE_EQUALS, // /=
            MAX_EQUALS, // ><=
            MIN_EQUALS, // <>=
            INJECT, // INJECT -> =>
            ASSIGNMENT,  // =
            AND, // &
            ADD, // +
            SUBTRACT, // -
            MULTIPLY, // *
            DIVISION, // /
            MODULO, // %
            MAXIMUM, // ><
            MINIMUM, // <>
            ARR_GEN, // ~
            DEREFERENCE, // .
            LESSTHANOREQUAL, // <=
            LESSTHAN, // <
            GREATEROREQUAL, // >=
            GREATER, // >
            NEGATE, // !
            END_STATEMENT, // ;
            P_SEPERATOR, // :
            S_SEPERATOR, // ,
            PRINT, // PRINT 🖨️
            INTO, // INTO
            FOREACH, // FOREACH ∀
            IFOREACH, // iFOREACH i∀
            MAP, // MAP 🗺️
            WHERE, // WHERE 🔍
            IWHERE, // iWHERE i🔍
            IN, // IN 🏠
            THROUGH, // THROUGH 🕳️
            UNPACK // UNPACK 🎒
    };

    List<String> commands;
    int order;
    public final BiFunction<ATOMElement, ATOMElement, ATOMValue> operate;

    ATOMOperation(List<String> commands, int order, BiFunction<ATOMElement, ATOMElement, ATOMValue> operate) {
        this.commands = commands;
        this.order = order;
        this.operate = operate;
    }

    public ATOMValue eval() {
        throw new RuntimeException("Operations cannot be evaluated to a value");
    }

    public ATOMValue compute() {
        throw new RuntimeException("Operations cannot be computed to a value");
    }

    public ATOMValue eval(ATOMElement left, ATOMElement right) {
        if (operate==null) {
            throw new RuntimeException("Must implement operate for "+this);
        }
        return operate.apply(left, right);
    }

    public String toString() {
        return commands.get(0);
    }

}
