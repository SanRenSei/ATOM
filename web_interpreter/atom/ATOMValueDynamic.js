import ATOMExpression from './ATOMExpression.js';
import ATOMRuntime from './ATOMRuntime.js';
import ATOMValue from './ATOMValue.js';

export default class ATOMValueDynamic extends ATOMValue {

  constructor(arg1, arg2) {
    super();
    this.name = null;
    this.index = null;
    this.rootPath = null;
    if (typeof arg1 == 'string' && !arg2) {
      arg2 = false;
    }
    if (typeof arg1 =='string' && typeof arg2 == 'boolean') {
      this.name = arg1;
      if (arg2) {
        this.setType('GLOBAL');
      } else {
        this.setType('SCOPED');
      }
    }
    if (typeof arg1 == 'number') {
      this.index = arg1;
      this.setType('INDEXED');
    }
    if (typeof arg1 == 'object' && typeof arg2 == 'string') {
      this.rootPath = arg1;
      this.name = arg2;
      this.setType('SCOPED');
    }
    if (typeof arg1 == 'object' && typeof arg2 == 'number') {
      this.rootPath = arg1;
      this.index = arg2;
      this.setType('INDEXED');
    }
  }

  generateChild(path) {
    if (path.getType()=='INT') {
      return new ATOMValueDynamic(this, path.intVal);
    }
    if (path.getType()=='STRING') {
      return new ATOMValueDynamic(this, path.strVal);
    }
    throw 'CANNOT GENERATE DYNAMIC PATH FROM ' + path;
  }

  getVal() {
    if (this.rootPath == null) {
      if (this.getType()=='INDEXED') {
        return ATOMRuntime.getIndexedVar(this.index);
      }
      if (this.getType()=='SCOPED') {
        return this.parent.getLocalVar(this.name);
      }
      if (this.getType() == 'GLOBAL') {
        return ATOMRuntime.globalVars[name];
      }
      throw 'Something went wrong with type in getVal';
    }
    let rootVal = this.rootPath.getVal();
    if (this.getType() == 'INDEXED') {
      if (rootVal.getType() == 'NULL') {
        rootVal.setType('ARRAY');
      }
      if (rootVal.getType() == 'ARRAY') {
        return rootVal.arrVal.get(this.index).eval();
      }
      throw 'Cannot get indexed var from nonarray: ' + rootVal;
    }
    if (this.getType() == 'SCOPED') {
      rootVal = rootVal.eval();
      if (rootVal.getType() == 'OBJECT') {
        return rootVal.objVal.dereference(new ATOMValue(this.name));
      }
      if (name=='length' && rootVal.getType() == 'ARRAY') {
        return new ATOMValue(rootVal.arrVal.length);
      }
      throw 'Cannot get named var from nonscope';
    }
    throw 'Something went wrong with type in getVal';
  }

  tryGetArr() {
    let val = this.getVal();
    if (val == null || val.getType() == 'NULL') {
      this.setVal(new ATOMValue([]));
      return this.getVal();
    }
    return this.getVal();
  }

  tryGetObj() {
    let val = this.getVal();
    if (val == null || val.getType() == 'NULL') {
      this.setVal(new ATOMValue(new ATOMScope('OBJECT')));
      return this.getVal();
    }
    return this.getVal();
  }

  setVal(val) {
    if (this.rootPath == null) {
      if (this.getType()=='INDEXED') {
        throw 'Setting indexed var not yet supported.';
      }
      if (this.getType()=='SCOPED') {
        this.parent.setLocalVar(this.name, val);
      }
      if (this.getType()=='GLOBAL') {
        ATOMRuntime.injectVariable(this.name, val);
      }
      return;
    }
    if (this.getType() == 'INDEXED') {
      this.rootPath.tryGetArr().arrVal.set(this.index, val);
      return;
    }
    if (this.getType() == 'SCOPED') {
      this.rootPath.tryGetObj.branches.unshift([
        [new ATOMExpression(new ATOMValue(this.name))],
        [new ATOMExpression(val)]
      ]);
      return;
    }
    throw 'Unexpected type in value dynamic in setVal: ' + this.getType();
  }

  eval() {
    return this.getVal();
  }

  compute() {
    return this.eval().compute();
  }

  toString() {
    let prefix = this.rootPath == null ? "" : this.rootPath.toString();
    if (this.getType() == 'INDEXED') {
      return prefix + '$' + this.index;
    }
    return prefix + '$' + this.name;
  }

}