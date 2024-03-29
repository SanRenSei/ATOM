package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ATOMOperation extends ATOMElement {

    public static int ORDER_DEREF = 1;
    public static int ORDER_UNARY = ORDER_DEREF+1;
    public static int ORDER_ARRGEN = ORDER_UNARY+1;
    public static int ORDER_EXPONENT = ORDER_ARRGEN+1;
    public static int ORDER_MULT = ORDER_EXPONENT+1;
    public static int ORDER_ADD = ORDER_MULT+1;
    public static int ORDER_MINMAX = ORDER_ADD+1;
    public static int ORDER_COMPARE = ORDER_MINMAX+1;
    public static int ORDER_AND = ORDER_COMPARE+1;
    public static int ORDER_INJECT = ORDER_AND+1;
    public static int ORDER_ASSIGNMENT = ORDER_INJECT+1;
    public static int ORDER_SEP = ORDER_ASSIGNMENT+1;
    public static int ORDER_PRINT = ORDER_SEP+1;
    public static int ORDER_ARRIN = ORDER_PRINT+1;
    public static int ORDER_FUNC = ORDER_ARRIN+1;
    public static int ORDER_OPASSIGN = 100;

    public static ATOMOperation DEREFERENCE = new ATOMOperation(Collections.singletonList("."), ORDER_DEREF, (left, right) -> {
        ATOMValue rightVal = right.eval();
        if (rightVal.getType() == ATOMValueType.STRING && left.parent!=null) {
            ATOMValue potentialVar = left.parent.getScopedVar(rightVal.getStrVal());
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
        if (leftVal.getType() == ATOMValueType.NULL) {
            return ATOMValue.NULL();
        }
        throw new ATOMOperationException(".", left, right);
    });

    public static ATOMOperation DEREFERENCE_NEGATIVE = new ATOMOperation(Collections.singletonList(".-"), ORDER_DEREF, (left, right) -> {
        ATOMValue rightVal = right.eval();
        if (rightVal.getType() != ATOMValueType.INT) {
            throw new ATOMOperationException(".-", left, right);
        }

        if (left instanceof ATOMValueDynamic) {
            return ((ATOMValueDynamic)left).generateChild(new ATOMValue(-rightVal.getIntVal()));
        }

        ATOMValue leftVal = left.eval();

        if (leftVal.getType()==ATOMValueType.ARRAY && rightVal.getType()==ATOMValueType.INT) {
            int index = leftVal.getArrVal().size()-rightVal.getIntVal();
            return leftVal.getArrVal().get(index).eval();
        }
        if (leftVal.getType() == ATOMValueType.NULL) {
            return ATOMValue.NULL();
        }
        throw new ATOMOperationException(".-", left, right);
    });

    public static ATOMOperation GET_LENGTH = new ATOMOperation(Collections.singletonList("\uD83D\uDCCF"), ORDER_UNARY, (left, right) -> {
        ATOMValue rightVal = right.eval();
        if (left == null && rightVal.getType() == ATOMValueType.NULL) {
            return ATOMValue.NULL();
        }
        if (left == null && rightVal.getType() == ATOMValueType.STRING) {
            return new ATOMValue(rightVal.getStrVal().length());
        }
        if (left == null && rightVal.getType() == ATOMValueType.ARRAY) {
            return new ATOMValue(rightVal.getArrVal().size());
        }
        throw new ATOMOperationException("\uD83D\uDCCF", left, right);
    });

    public static ATOMOperation STR_TRIM = new ATOMOperation(Collections.singletonList("✂"), ORDER_UNARY, (left, right) -> {
        ATOMValue rightVal = right.eval();
        if (left == null && rightVal.getType() == ATOMValueType.STRING) {
            return new ATOMValue(rightVal.getStrVal().trim());
        }
        throw new ATOMOperationException("✂", left, right);
    });

    public static ATOMOperation ARR_FLATTEN = new ATOMOperation(Collections.singletonList("\uD83E\uDDB6"), ORDER_UNARY, (left, right) -> {
        ATOMValue rightVal = right.eval();
        if (left == null && rightVal.getType() == ATOMValueType.ARRAY) {
            List<ATOMValue> flattened = new ArrayList<>();
            rightVal.getArrVal().forEach(val -> {
                if (val.getType() == ATOMValueType.ARRAY) {
                    val.getArrVal().forEach((Consumer<? super ATOMValue>) flattened::add);
                } else {
                    flattened.add(val);
                }
            });
            return new ATOMValue(flattened);
        }
        throw new ATOMOperationException("\uD83E\uDDB6", left, right);
    });

    public static ATOMOperation TO_STRING = new ATOMOperation(Collections.singletonList("\uD83E\uDDF6"), ORDER_UNARY, (left, right) -> {
        ATOMValue rightVal = right.eval();
        if (left == null) {
            return new ATOMValue(rightVal.toString());
        }
        throw new ATOMOperationException("\uD83E\uDDF6", left, right);
    });

    public static ATOMOperation ATOM_EXECUTE = new ATOMOperation(Collections.singletonList("⚛"), ORDER_UNARY, (left, right) -> {
        ATOMValue rightVal = right.eval();
        if (left == null) {
            String program = rightVal.getStrVal();
            return ATOMRuntime.processInput(program);
        }
        throw new ATOMOperationException("⚛", left, right);
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

    public static ATOMOperation EXPONENT = new ATOMOperation(Collections.singletonList("^"), ORDER_EXPONENT, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.NULL && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(0);
        }
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.NULL) {
            return new ATOMValue(1);
        }
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue((int) Math.pow(leftVal.getIntVal(), rightVal.getIntVal()));
        }
        throw new ATOMOperationException("^", left, right);
    });

    public static ATOMOperation MULTIPLY = new ATOMOperation(Collections.singletonList("*"), ORDER_MULT, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.NULL && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(0);
        }
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.NULL) {
            return new ATOMValue(0);
        }
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() * rightVal.getIntVal());
        }
        throw new ATOMOperationException("*", left, right);
    });

    public static ATOMOperation DIVISION = new ATOMOperation(Collections.singletonList("/"), ORDER_MULT, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.NULL && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(0);
        }
        if (leftVal.getType() == ATOMValueType.NULL) {
            return ATOMValue.NULL();
        }
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.NULL) {
            return new ATOMValue(1/0);
        }
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() / rightVal.getIntVal());
        }
        if (leftVal.getType() == ATOMValueType.STRING && rightVal.getType() == ATOMValueType.STRING) {
            List<ATOMValue> splitStr = Arrays.stream(leftVal.getStrVal().split(Pattern.quote(rightVal.getStrVal()))).map(ATOMValue::new).collect(Collectors.toList());
            return new ATOMValue(splitStr);
        }
        throw new ATOMOperationException("/", left.eval(), right.eval());
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
        if (leftVal.getType() == ATOMValueType.NULL && rightVal.getType() == ATOMValueType.NULL) {
            return ATOMValue.NULL();
        }
        if (leftVal.getType() == ATOMValueType.NULL && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(rightVal.getIntVal());
        }
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.NULL) {
            return new ATOMValue(leftVal.getIntVal());
        }
        if (leftVal.getType() == ATOMValueType.NULL && rightVal.getType() == ATOMValueType.STRING) {
            return new ATOMValue(rightVal.getStrVal());
        }
        if (leftVal.getType() == ATOMValueType.STRING && rightVal.getType() == ATOMValueType.NULL) {
            return new ATOMValue(leftVal.getStrVal());
        }
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() + rightVal.getIntVal());
        }
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.STRING) {
            return new ATOMValue(leftVal.getIntVal() + rightVal.getStrVal());
        }
        if (leftVal.getType() == ATOMValueType.STRING && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getStrVal() + rightVal.getIntVal());
        }
        if (leftVal.getType() == ATOMValueType.STRING && rightVal.getType() == ATOMValueType.STRING) {
            return new ATOMValue(leftVal.getStrVal() + rightVal.getStrVal());
        }
        if (leftVal.getType() == ATOMValueType.STRING) {
            return new ATOMValue(leftVal.getStrVal() + rightVal.toString());
        }
        if (leftVal.getType() == ATOMValueType.ARRAY && rightVal.getType() == ATOMValueType.ARRAY) {
            rightVal.getArrVal().forEach(rVal -> leftVal.getArrVal().add(rVal));
            return leftVal;
        }
        if (leftVal.getType() == ATOMValueType.ARRAY) {
            leftVal.getArrVal().add(rightVal);
            return leftVal;
        }
        throw new ATOMOperationException("+", left, right);
    });
    public static ATOMOperation SUBTRACT = new ATOMOperation(Collections.singletonList("-"), ORDER_ADD, (left, right) -> {
        ATOMValue rightVal = right.eval();
        if (left == null) {
            if (rightVal.getType() == ATOMValueType.INT) {
                return new ATOMValue(-rightVal.getIntVal());
            }
            if (rightVal.getType() == ATOMValueType.STRING) {
                return new ATOMValue(new StringBuilder(rightVal.getStrVal()).reverse().toString());
            }
            throw new ATOMOperationException("-", left, right);
        }
        ATOMValue leftVal = left.eval();
        if (leftVal.getType() == ATOMValueType.NULL && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(-rightVal.getIntVal());
        }
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.NULL) {
            return new ATOMValue(leftVal.getIntVal());
        }
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() - rightVal.getIntVal());
        }
        if (leftVal.getType() == ATOMValueType.ARRAY && rightVal.getType() == ATOMValueType.INT) {
            leftVal.getArrVal().remove(rightVal.getIntVal());
            return leftVal;
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
        if (leftVal.getType() == ATOMValueType.NULL && rightVal.getType() == ATOMValueType.NULL) {
            return new ATOMValue(true);
        }
        if (leftVal.getType() == ATOMValueType.INT && rightVal.getType() == ATOMValueType.INT) {
            return new ATOMValue(leftVal.getIntVal() == rightVal.getIntVal());
        }
        if (leftVal.getType() == ATOMValueType.STRING && rightVal.getType() == ATOMValueType.STRING) {
            return new ATOMValue(leftVal.getStrVal().equals(rightVal.getStrVal()));
        }
        throw new ATOMOperationException("==", left, right);
    });
    public static ATOMOperation NOTEQUAL = new ATOMOperation(Collections.singletonList("!="), ORDER_COMPARE, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (leftVal.getType() == ATOMValueType.NULL && rightVal.getType() == ATOMValueType.NULL) {
            return new ATOMValue(false);
        }
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
                rightValObj.localVars.put(((ATOMValueDynamic) left).name, rightValObj.getScopedVar(((ATOMValueDynamic) left).name).compute());
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

    public static ATOMOperation S_SEPERATOR = new ATOMOperation(Collections.singletonList(","), ORDER_SEP, (TriFunction) null);
    public static ATOMOperation P_SEPERATOR = new ATOMOperation(Collections.singletonList(":"), ORDER_SEP, (TriFunction) null);

    public static ATOMOperation END_STATEMENT = new ATOMOperation(Collections.singletonList(";"), ORDER_SEP, (TriFunction) null);

    public static ATOMOperation PRINT = new ATOMOperation(Arrays.asList("PRINT", "🖨"), ORDER_PRINT, (left, right) -> {
        ATOMValue rightVal = right.eval();
        System.out.println(rightVal.getType() == ATOMValueType.STRING ? rightVal.getStrVal() : rightVal);
        return rightVal;
    });

    public static ATOMOperation IN = new ATOMOperation(Arrays.asList("IN", "\uD83C\uDFE0"), ORDER_ARRIN, (left, right) -> {
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

    public static ATOMOperation NOTIN = new ATOMOperation(Arrays.asList("NOTIN", "NIN", "\uD83C\uDFD5"), ORDER_ARRIN, (left, right) -> {
        ATOMValue leftVal = left.eval();
        ATOMValue rightVal = right.eval();
        if (rightVal.getType() == ATOMValueType.ARRAY) {
            for (int i=0;i<rightVal.getArrVal().size();i++) {
                if (leftVal.equals(rightVal.getArrVal().get(i))) {
                    return new ATOMValue(false);
                }
            }
            return new ATOMValue(true);
        }
        throw new ATOMOperationException("NOTIN", left, right);
    });

    public static ATOMOperation INTO = new ATOMOperation(Arrays.asList("INTO", "\uD83D\uDEAA"), ORDER_FUNC, (scope, left, right) -> {
        if (right.eval().getType()==ATOMValueType.OBJECT) {
            ATOMScope rightScope = right.eval().getObjVal();
            if (rightScope.parent==null) {
                rightScope.parent = scope;
            }
            rightScope.indexedVar = left.eval();
            ATOMValue toReturn = rightScope.compute();
            return toReturn;
        }
        throw new ATOMOperationException("INTO", left, right);
    });
    public static ATOMOperation FOREACH = new ATOMOperation(Arrays.asList("FOREACH", "∀"), ORDER_FUNC, (left, right) -> {
        ATOMValue leftVal = left.eval();
        if (leftVal.getType() == ATOMValueType.ARRAY && right instanceof ATOMScope) {
            for (int i=0;i<leftVal.getArrVal().size();i++) {
                ATOMElement iterator = leftVal.getArrVal().get(i);
                ((ATOMScope) right).indexedVar = iterator.eval();
                right.compute();
            }
            return null;
        }
        if (leftVal.getType() == ATOMValueType.STRING && right instanceof ATOMScope) {
            for (int i=0;i<leftVal.getStrVal().length();i++) {
                ATOMElement iterator = new ATOMValue(""+leftVal.getStrVal().charAt(i));
                ((ATOMScope) right).indexedVar = iterator.eval();
                right.compute();
            }
            return null;
        }
        if (leftVal.getType() == ATOMValueType.INT && right instanceof ATOMScope) {
            for (int i=0;i<leftVal.getIntVal();i++) {
                ((ATOMScope) right).indexedVar = new ATOMValue(i);
                right.compute();
            }
            return null;
        }
        throw new ATOMOperationException("FOREACH", left, right);
    });
    public static ATOMOperation IFOREACH = new ATOMOperation(Arrays.asList("iFOREACH", "i∀", "\uD83D\uDD22"), ORDER_FUNC, (left, right) -> {
        if (left == null) {
            throw new RuntimeException("No left operand for operator iFOREACH");
        }
        if (right == null) {
            throw new RuntimeException("No right operand for operator iFOREACH");
        }
        ATOMValue leftVal = left.eval();
        if (leftVal.getType() == ATOMValueType.ARRAY && right instanceof ATOMScope) {
            for (int i=0;i<leftVal.getArrVal().size();i++) {
                ((ATOMScope) right).indexedVar = new ATOMValue(i);
                right.compute();
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
                ((ATOMScope) right).indexedVar = iterator.eval();
                mappedVals.add(right.compute());
            }
            return new ATOMValue(mappedVals);
        }
        throw new ATOMOperationException("MAP", left.eval(), right);
    });

    public static ATOMOperation IMAP = new ATOMOperation(Arrays.asList("iMAP", "i\uD83D\uDDFA"), ORDER_FUNC, (left, right) -> {
        ATOMValue leftVal = left.eval();
        if (leftVal.getType() == ATOMValueType.ARRAY && right instanceof ATOMScope) {
            List<ATOMValue> mappedVals = new ArrayList<>();
            for (int i=0;i< leftVal.getArrVal().size();i++) {
                ATOMValue iterator = new ATOMValue(i);
                ((ATOMScope) right).indexedVar = iterator.eval();
                mappedVals.add(right.compute());
            }
            return new ATOMValue(mappedVals);
        }
        throw new ATOMOperationException("MAP", left.eval(), right);
    });

    public static ATOMOperation WHERE = new ATOMOperation(Arrays.asList("WHERE","🔍"), ORDER_FUNC, (left, right) -> {
        ATOMValue leftVal = left.eval();
        if (leftVal.getType() == ATOMValueType.ARRAY && (right instanceof ATOMScope)) {
            List<ATOMValue> filteredVals = new ArrayList<>();
            for (int i=0;i< leftVal.getArrVal().size();i++) {
                ATOMValue iterator = leftVal.getArrVal().get(i);
                ((ATOMScope) right).indexedVar = iterator.eval();
                ATOMValue filterVal = right.compute();
                if (filterVal.isTruthy()) {
                    filteredVals.add(iterator);
                }
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
                ((ATOMScope) right).indexedVar = iterator.eval();
                ATOMValue filterVal = right.compute();
                if (filterVal.isTruthy()) {
                    filteredVals.add(iterator);
                }
            }
            return new ATOMValue(filteredVals);
        }
        throw new ATOMOperationException("IWHERE", left, right);
    });
    public static ATOMOperation THROUGH = new ATOMOperation(Arrays.asList("THROUGH", "\uD83D\uDD73"), ORDER_FUNC, (left, right) -> {
        ATOMValue leftVal = left.eval();
        if (right instanceof ATOMScope) {
            while (leftVal.isTruthy()) {
                ((ATOMScope) right).indexedVar = leftVal;
                leftVal = right.compute();
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
            (scope, left, right) -> ASSIGNMENT.operate.execute(scope, left, ADD.operate.execute(scope, left, right)));

    public static ATOMOperation MINUS_EQUALS = new ATOMOperation(Collections.singletonList("-="), ORDER_OPASSIGN,
            (scope, left, right) -> ASSIGNMENT.operate.execute(scope, left, SUBTRACT.operate.execute(scope, left, right)));

    public static ATOMOperation TIMES_EQUALS = new ATOMOperation(Collections.singletonList("*="), ORDER_OPASSIGN,
            (scope, left, right) -> ASSIGNMENT.operate.execute(scope, left, MULTIPLY.operate.execute(scope, left, right)));

    public static ATOMOperation DIVIDE_EQUALS = new ATOMOperation(Collections.singletonList("/="), ORDER_OPASSIGN,
            (scope, left, right) -> ASSIGNMENT.operate.execute(scope, left, DIVISION.operate.execute(scope, left, right)));

    public static ATOMOperation MAX_EQUALS = new ATOMOperation(Collections.singletonList("><="), ORDER_OPASSIGN,
            (scope, left, right) -> ASSIGNMENT.operate.execute(scope, left, MAXIMUM.operate.execute(scope, left, right)));

    public static ATOMOperation MIN_EQUALS = new ATOMOperation(Collections.singletonList("<>="), ORDER_OPASSIGN,
            (scope, left, right) -> ASSIGNMENT.operate.execute(scope, left, MINIMUM.operate.execute(scope, left, right)));

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
            EXPONENT, // ^
            MODULO, // %
            MAXIMUM, // ><
            MINIMUM, // <>
            ARR_GEN, // ~
            DEREFERENCE_NEGATIVE, // .-
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
            INTO, // INTO 🚪
            FOREACH, // FOREACH ∀
            IFOREACH, // iFOREACH i∀
            MAP, // MAP 🗺️
            IMAP, // iMAP i🗺️
            WHERE, // WHERE 🔍
            IWHERE, // iWHERE i🔍
            IN, // IN 🏠
            NOTIN, // NOTIN NIN 🏕
            THROUGH, // THROUGH 🕳️
            UNPACK, // UNPACK 🎒
            GET_LENGTH, // 📏
            STR_TRIM, // ✂
            ARR_FLATTEN, // 🦶
            TO_STRING, // 🧶
            ATOM_EXECUTE, // ⚛
    };

    List<String> commands;
    int order;
    public final TriFunction operate;

    ATOMOperation(List<String> commands, int order, BiFunction<ATOMElement, ATOMElement, ATOMValue> operate) {
        this.commands = commands;
        this.order = order;
        this.operate = (scope, left, right) -> operate.apply(left,right);
    }

    ATOMOperation(List<String> commands, int order, TriFunction operate2) {
        this.commands = commands;
        this.order = order;
        this.operate = operate2;
    }

    public ATOMValue eval() {
        throw new RuntimeException("Operations cannot be evaluated to a value");
    }

    public ATOMValue compute() {
        throw new RuntimeException("Operations cannot be computed to a value");
    }

    public ATOMValue eval(ATOMScope scope, ATOMElement left, ATOMElement right) {
        if (operate ==null) {
            throw new RuntimeException("Must implement operate for "+this);
        }
        return operate.execute(scope, left, right);
    }

    public String toString() {
        return commands.get(0);
    }

}
