package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ATOMValueDynamic extends ATOMValue {

    String name;
    int index;
    ATOMValueDynamic rootPath;

    public ATOMValueDynamic(String name) {
        this(name, false);
    }

    public ATOMValueDynamic(String name, boolean isGlobal) {
        this(name, isGlobal?ATOMValueType.GLOBAL:ATOMValueType.SCOPED);
    }

    public ATOMValueDynamic(String name, ATOMValueType type) {
        this.name = name;
        this.setType(type);
    }

    public ATOMValueDynamic(int index) {
        this.index = index;
        this.setType(ATOMValueType.INDEXED);
    }

    public ATOMValueDynamic(ATOMValueDynamic root, String name) {
        rootPath = root;
        this.name = name;
        this.setType(ATOMValueType.SCOPED);
    }

    public ATOMValueDynamic(ATOMValueDynamic root, int index) {
        rootPath = root;
        this.index = index;
        this.setType(ATOMValueType.INDEXED);
    }

    public ATOMValueDynamic generateChild(ATOMValue path) {
        if (path.getType() == ATOMValueType.INT) {
            return new ATOMValueDynamic(this, path.getIntVal());
        }
        if (path.getType() == ATOMValueType.STRING) {
            return new ATOMValueDynamic(this, path.getStrVal());
        }
        throw new RuntimeException("CANNOT GENERATE DYNAMIC PATH FROM " + path);
    }

    public ATOMValue getVal() {
        if (rootPath == null) {
            if (getType() == ATOMValueType.INDEXED) {
                return ATOMRuntime.getIndexedVar(index);
            }
            if (getType() == ATOMValueType.SCOPED) {
                return parent.getScopedVar(name);
            }
            if (getType() == ATOMValueType.LOCAL) {
                return parent.getLocalVar(name);
            }
            if (getType() == ATOMValueType.GLOBAL) {
                return ATOMRuntime.getGlobalVar(name);
            }
            throw new RuntimeException("Something went wrong with type in getVal");
        }
        ATOMValue rootVal = rootPath.getVal();
        if (getType() == ATOMValueType.INDEXED) {
            if (rootVal.getType() == ATOMValueType.NULL) {
                rootVal.setType(ATOMValueType.ARRAY);
            }
            if (rootVal.getType() == ATOMValueType.ARRAY) {
                return rootVal.getArrVal().get(index).eval();
            }
            throw new RuntimeException("Cannot get indexed var from nonarray: " + rootVal);
        }
        if (getType() == ATOMValueType.SCOPED) {
            rootVal = rootVal.eval();
            if (rootVal.getType() == ATOMValueType.OBJECT) {
                return rootVal.getObjVal().dereference(new ATOMValue(name));
            }
            if (name.equals("length") && rootVal.getType() == ATOMValueType.ARRAY) {
                return new ATOMValue(rootVal.getArrVal().size());
            }
            throw new RuntimeException("Cannot get named var from nonscope");
        }
        throw new RuntimeException("Something went wrong with type in getVal");
    }

    public ATOMValue tryGetArr() {
        ATOMValue val = getVal();
        if (val == null || val.getType() == ATOMValueType.NULL) {
            setVal(new ATOMValue(new ArrayList<>()));
            return getVal();
        }
        return getVal();
    }

    public ATOMValue tryGetObj() {
        ATOMValue val = getVal();
        if (val == null || val.getType() == ATOMValueType.NULL) {
            setVal(new ATOMValue(new ATOMScope(ATOMScopeType.OBJECT)));
            return getVal();
        }
        return getVal();
    }

    public void setVal(ATOMValue val) {
        if (rootPath == null) {
            if (getType() == ATOMValueType.INDEXED) {
                throw new RuntimeException("Setting indexed var not yet supported.");
            }
            if (getType() == ATOMValueType.SCOPED) {
                parent.setScopedVar(name, val);
            }
            if (getType() == ATOMValueType.GLOBAL) {
                ATOMRuntime.injectVariable(this.name, val);
            }
            if (getType() == ATOMValueType.LOCAL) {
                parent.setLocalVar(name, val);
            }
            return;
        }
        if (getType() == ATOMValueType.INDEXED) {
            rootPath.tryGetArr().getArrVal().set(index, val);
            return;
        }
        if (getType() == ATOMValueType.SCOPED) {
            //noinspection unchecked
            rootPath.tryGetObj().getObjVal().branches.add(0, new List[]{
                    Collections.singletonList(new ATOMExpression(new ATOMValue(this.name))),
                    Collections.singletonList(new ATOMExpression(val))
            });
            return;
        }
        throw new RuntimeException("Unexpected type in value dynamic in setVal: " + getType());
    }

    public ATOMValue eval() {
        return getVal();
    }

    public ATOMValue compute() {
        return eval().compute();
    }

    public String toString() {
        String prefix = rootPath == null ? "" : rootPath.toString();
        if (this.getType() == ATOMValueType.INDEXED) {
            return prefix + "$" + index;
        }
        if (this.getType() == ATOMValueType.LOCAL) {
            return prefix + "%" + name;
        }
        if (this.getType() == ATOMValueType.GLOBAL) {
            return prefix + "$" + name;
        }
        return prefix + "?" + name;
    }

}
