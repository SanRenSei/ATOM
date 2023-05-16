package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ATOMExpression extends ATOMElement {

    List<ATOMElement> children;

    public ATOMExpression() {
        children = new ArrayList<>();
    }

    public ATOMExpression(ATOMValue val) {
        this.children = Collections.singletonList(val);
    }

    public ATOMExpression(List<ATOMElement> children) {
        this.children = children;
    }

    private void setImplicitParentheticalScope() {
        List<ATOMOperation> implicitScopeOperations = Arrays.asList(
                ATOMOperation.FOREACH,
                ATOMOperation.IFOREACH,
                ATOMOperation.WHERE,
                ATOMOperation.IWHERE,
                ATOMOperation.MAP,
                ATOMOperation.INTO,
                ATOMOperation.THROUGH
        );
        for (int i=0;i<children.size();i++) {
            if (implicitScopeOperations.contains(children.get(i))) {
                if (i == children.size()-2 && children.get(children.size()-1) instanceof ATOMScope) {
                    return;
                }
                ATOMScope subScope = new ATOMScope();
                subScope.type = ATOMScopeType.PARENTHESIS;
                subScope.parent = parent;
                List<ATOMElement> implicitScope = new ArrayList<>();
                while (children.size()>i+1) {
                    ATOMElement child = children.remove(i+1);
                    child.parent = subScope;
                    implicitScope.add(child);
                }
                ATOMExpression subExpression = new ATOMExpression(implicitScope);
                subExpression.parent = subScope;
                subScope.components.add(subExpression);
                children.add(subScope);
                return;
            }
        }
    }

    private void setImplicitArrayLength() {
        for (int i=2;i<children.size();i++) {
            if (children.get(i) instanceof ATOMValueDynamic) {
                ATOMValueDynamic var = (ATOMValueDynamic) children.get(i);
                if (var.getType() == ATOMValueType.SCOPED) {
                    if (children.get(i-1) instanceof ATOMOperation && children.get(i-1)==ATOMOperation.DEREFERENCE) {
                        children.set(i, new ATOMValue(var.name));
                    }
                }
            }
        }
    }

    public ATOMValue eval() {
        setImplicitParentheticalScope();
        setImplicitArrayLength();
        List<ATOMElement> copyOfChildren = new ArrayList<>(children);
        while (copyOfChildren.size()>1) {
            int nextOperationIndex = -1;
            for (int i=0;i<copyOfChildren.size();i++) {
                if (copyOfChildren.get(i) instanceof ATOMOperation) {
                    if (nextOperationIndex==-1) {
                        nextOperationIndex = i;
                    }
                    if (((ATOMOperation) copyOfChildren.get(i)).order < ((ATOMOperation) copyOfChildren.get(nextOperationIndex)).order) {
                        nextOperationIndex = i;
                    }
                    // 100+ indicates a right to left order of operations
                    if (((ATOMOperation) copyOfChildren.get(i)).order == ((ATOMOperation) copyOfChildren.get(nextOperationIndex)).order
                            && ((ATOMOperation) copyOfChildren.get(i)).order>=100) {
                        nextOperationIndex = i;
                    }
                }
            }

            if (nextOperationIndex==-1) {
                throw new RuntimeException("Probably a syntax error somewhere, not enough operators:\n" + this.children);
            }

            ATOMElement left=null, right=null;
            if (nextOperationIndex<copyOfChildren.size()-1 && !(copyOfChildren.get(nextOperationIndex+1) instanceof ATOMOperation)) {
                right = copyOfChildren.get(nextOperationIndex+1);
                copyOfChildren.remove(nextOperationIndex+1);
            }
            ATOMOperation op = (ATOMOperation) copyOfChildren.remove(nextOperationIndex);
            if (nextOperationIndex>0 && !(copyOfChildren.get(nextOperationIndex-1) instanceof ATOMOperation)) {
                left = copyOfChildren.get(nextOperationIndex-1);
                copyOfChildren.remove(nextOperationIndex-1);
            }
            ATOMValue newVal = op.eval(left, right);
            if (left == null) {
                copyOfChildren.add(nextOperationIndex, newVal);
            } else {
                copyOfChildren.add(nextOperationIndex - 1, newVal);
            }
        }
        if (copyOfChildren.size()==0 || copyOfChildren.get(0)==null) {
            return null;
        }
        return copyOfChildren.get(0).eval();
    }

    public ATOMValue compute() {
        if (children.size()==1) {
            return children.get(0).compute();
        }
        return eval();
    }

//     BUNDLER: BEGIN IGNORE
    public String toString() {
        List<String> childrenStr = children.stream().map(ATOMElement::toString).collect(Collectors.toList());
        return String.join(" ", childrenStr);
    }
    // BUNDLER: END IGNORE
    // BUNDLER: BEGIN INJECT
//    public String toString() {
//        String toReturn = "";
//        for (ATOMElement elem:children) {
//            toReturn += elem.toString() + " ";
//        }
//        return toReturn;
//    }
    // BUNDLER: END INJECT

}
