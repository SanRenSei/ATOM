package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ATOMScope extends ATOMElement {

    ATOMTemplate template;
    ATOMScopeType type;

    List<ATOMExpression> components = new ArrayList<>(); // Parenthesis and arrays use components
    List<List<ATOMExpression>[]> branches = new ArrayList<>(); // Objects use branches

    public HashMap<String, ATOMValue> localVars = new HashMap<>();

    public ATOMScope() {
    }

    public ATOMScope(ATOMScopeType type) {
        this.type = type;
    }

    public ATOMScope(ATOMTemplate template, ATOMScopeType type) {
        List<ATOMElement> children = new ArrayList<>();
        this.template = template;
        this.type = type;
        template.currentIndex++;
        while (template.currentValidChar()!=type.closing) {
            ATOMElement elem = ATOMElement.fromTemplate(template);
            if (elem!=null) {
                elem.parent = this;
                children.add(elem);
            }
        }
        template.currentIndex++;

        if (type == ATOMScopeType.OBJECT) {
            branches.add(new List[2]);
            branches.get(0)[0] = new ArrayList<>();
            branches.get(0)[1] = new ArrayList<>();
        }

        ATOMExpression latestExpression = new ATOMExpression();
        latestExpression.parent = this;
        int predicateCommandFlag = 0; // 0 for predicate, 1 for command
        while (children.size()>0) {
            ATOMElement elem = children.remove(0);
            if (elem==ATOMOperation.MULTIPLY) {
                // * can either be multiply or indexed var depending on context of prior element

                List<ATOMElement> currentCommand = latestExpression.children;
                if (currentCommand.size()==0) {
                    // If * comes first then that means its wildcard
                    elem = new ATOMValueDynamic(0);
                    elem.parent = this;
                }
                else if (currentCommand.get(currentCommand.size()-1) instanceof ATOMOperation) {
                    // If * comes right after an operation, then its a wildcard
                    elem = new ATOMValueDynamic(0);
                    elem.parent = this;
                }
            }
            if (elem==ATOMOperation.MODULO) {
                // % can either be modulus or local var depending on context of prior element

                List<ATOMElement> currentCommand = latestExpression.children;
                if (currentCommand.size()==0) {
                    // If % comes first then that means its local var
                    // The next child should be a dynamic, so set its type to local
                    // Avoid adding the modulus operator to the expression
                    ((ATOMValueDynamic)children.get(0)).setType(ATOMValueType.LOCAL);
                    continue;
                }
                else if (currentCommand.get(currentCommand.size()-1) instanceof ATOMOperation) {
                    // If * comes right after an operation, then its a local var
                    ((ATOMValueDynamic)children.get(0)).setType(ATOMValueType.LOCAL);
                    continue;
                }
            }


            if (type == ATOMScopeType.OBJECT) {
                if (elem == ATOMOperation.END_STATEMENT) {
                    branches.get(branches.size()-1)[predicateCommandFlag].add(latestExpression);
                    latestExpression = new ATOMExpression();
                    latestExpression.parent = this;
                } else if (elem == ATOMOperation.P_SEPERATOR) {
                    branches.get(branches.size()-1)[predicateCommandFlag].add(latestExpression);
                    latestExpression = new ATOMExpression();
                    latestExpression.parent = this;
                    predicateCommandFlag = 1;
                } else if (elem == ATOMOperation.S_SEPERATOR) {
                    branches.get(branches.size()-1)[predicateCommandFlag].add(latestExpression);
                    latestExpression = new ATOMExpression();
                    latestExpression.parent = this;
                    predicateCommandFlag = 0;
                    List<ATOMExpression>[] nextBranch = new List[2];
                    nextBranch[0] = new ArrayList<>();
                    nextBranch[1] = new ArrayList<>();
                    branches.add(nextBranch);
                } else {
                    latestExpression.children.add(elem);
                }
            } else {
                if (elem == ATOMOperation.END_STATEMENT || elem == ATOMOperation.S_SEPERATOR) {
                    components.add(latestExpression);
                    latestExpression = new ATOMExpression();
                    latestExpression.parent = this;
                } else {
                    latestExpression.children.add(elem);
                }
            }
        }
        if (latestExpression.children.size()>0) {
            if (type == ATOMScopeType.OBJECT) {
                branches.get(branches.size()-1)[predicateCommandFlag].add(latestExpression);
            } else {
                components.add(latestExpression);
            }
        }
    }

    public ATOMValue eval() {
        if (type == ATOMScopeType.PARENTHESIS) {
            if (components.size()==0) {
                return ATOMValue.NULL();
            }
            for (int i = 0; i< components.size()-1; i++) {
                components.get(i).eval();
            }
            return components.get(components.size()-1).eval();
        }
        if (type == ATOMScopeType.ARRAY) {
            return compute();
        }
        if (type == ATOMScopeType.OBJECT) {
            ATOMValue toReturn = new ATOMValue(this);
            toReturn.parent = this.parent;
            return toReturn;
        }
        throw new RuntimeException("Something went wrong with main.ATOMScopeType in Eval");
    }

    public ATOMValue compute() {
        if (type==ATOMScopeType.PARENTHESIS) {
            if (components.size()==0) {
                return ATOMValue.NULL();
            }
            if (components.size()==1) {
                return components.get(0).eval().compute();
            }
            for (int i = 0; i< components.size()-1; i++) {
                components.get(i).compute();
            }
            return components.get(components.size()-1).eval().compute();
        }
        if (type==ATOMScopeType.ARRAY) {
            List<ATOMValue> values = new ArrayList<>();
            for (int i = 0; i< components.size(); i++) {
                values.add(components.get(i).eval());
            }
            return new ATOMValue(values);
        }
        if (type==ATOMScopeType.OBJECT) {
            ATOMValue toReturn = null;
            for (List<ATOMExpression>[] branch : branches) {
                for (int i = 0; i < branch[0].size(); i++) {
                    toReturn = branch[0].get(i).eval();
                }
                if (toReturn!=null && toReturn.isTruthy()) {
                    if (branch[1].size()==0) {
                        return toReturn;
                    }
                    for (int i = 0; i < branch[1].size(); i++) {
                        toReturn = branch[1].get(i).eval();
                    }
                    return toReturn;
                }
            }
            return toReturn;
        }
        throw new RuntimeException("Something went wrong with main.ATOMScopeType in Compute");
    }

    public ATOMValue dereference(ATOMValue key) {
        if (type == ATOMScopeType.PARENTHESIS) {
            throw new RuntimeException("Cannot dereference parenthetical scope by key");
        }
        if (type == ATOMScopeType.ARRAY) {
            throw new RuntimeException("Must eval array before dereferencing");
        }
        if (type != ATOMScopeType.OBJECT) {
            throw new RuntimeException("Something went wrong with ATOMScopeType in derefernce");
        }

        ATOMRuntime.pushIndexedVar(key);
        ATOMValue predicate = null;
        for (List<ATOMExpression>[] branch : branches) {
            for (int i = 0; i < branch[0].size(); i++) {
                predicate = branch[0].get(i).eval();
            }
            if (predicate.equals(key)) {
                if (branch[1].size()==0) {
                    ATOMRuntime.popIndexedVar();
                    return ATOMValue.NULL();
                }
                for (int i = 0; i < branch[1].size(); i++) {
                    predicate = branch[1].get(i).eval();
                }
                ATOMRuntime.popIndexedVar();
                return predicate;
            }
        }
        ATOMRuntime.popIndexedVar();
        return ATOMValue.NULL();
    }

    public ATOMValue getLocalVar(String name) {
        if (localVars.get(name)!=null) {
            return localVars.get(name);
        }
        return ATOMValue.NULL();
    }

    public ATOMValue getScopedVar(String name) {
        ATOMScope pathToRoot = this;
        while (pathToRoot!=null) {
            if (pathToRoot.localVars.get(name)!=null) {
                return pathToRoot.localVars.get(name);
            }
            pathToRoot = pathToRoot.parent;
        }
        if (ATOMRuntime.globalVars.get(name)!=null) {
            return ATOMRuntime.globalVars.get(name);
        }
        return ATOMValue.NULL();
    }

    public void setLocalVar(String name, ATOMValue val) {
        localVars.put(name, val);
    }

    public void setScopedVar(String name, ATOMValue val) {
        ATOMScope pathToRoot = this;
        while (pathToRoot!=null) {
            if (pathToRoot.localVars.get(name)!=null) {
                pathToRoot.localVars.put(name, val);
                return;
            }
            pathToRoot = pathToRoot.parent;
        }
        if (ATOMRuntime.globalVars.get(name)!=null) {
            ATOMRuntime.globalVars.put(name, val);
            return;
        }
        localVars.put(name, val);
    }

    // BUNDLER: BEGIN IGNORE
    public String toString() {
        if (type == ATOMScopeType.OBJECT) {
            List<String> childrenStr = branches.stream().map(b -> b[0].toString()+":"+b[1].toString()).collect(Collectors.toList());
            return type.opening + String.join(",", childrenStr) + type.closing;
        }
        List<String> childrenStr = components.stream().map(ATOMExpression::toString).collect(Collectors.toList());
        return type.opening + String.join(";", childrenStr) + type.closing;
    }
    // BUNDLER: END IGNORE
    // BUNDLER: BEGIN INJECT
//    public String toString() {
//        StringBuilder toReturn = new StringBuilder(type.opening);
//        if (type == ATOMScopeType.OBJECT) {
//            for (List<ATOMExpression>[] b: branches) {
//                toReturn.append(b[0].toString()).append(":").append(b[1].toString()).append(",");
//            }
//        } else {
//            for (ATOMExpression e : components) {
//                toReturn.append(e.toString()).append(";");
//            }
//        }
//        toReturn.append(type.closing);
//        return toReturn.toString();
//    }
    // BUNDLER: END INJECT

}
