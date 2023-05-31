import ATOMElement from './ATOMElement.js';
import ATOMExpression from './ATOMExpression.js';
import ATOMOperation from './ATOMOperation.js';
import ATOMScopeType from './ATOMScopeType.js';
import ATOMValue from './ATOMValue.js';
import ATOMValueDynamic from './ATOMValueDynamic.js';

export default class ATOMScope {

  constructor(arg1, arg2) {
    this.template = null;
    this.type = null;
    this.components = [];
    this.branches = [];
    this.localVars = {};
    if (!arg1 && !arg2) {
      arg1 = 'PARENTHESIS';
    }
    if (typeof arg1 == 'string' && !arg2) {
      this.type = arg1;
      return;
    }
    let children = [];
    this.template = arg1;
    this.type = arg2;
    this.template.currentIndex++;
    while (this.template.currentValidChar()!=ATOMScopeType.closing(this.type)) {
      let elem = ATOMElement.fromTemplate(this.template);
      if (elem!=null) {
        elem.parent = this;
        children.push(elem);
      }
    }
    this.template.currentIndex++;

    if (this.type == 'OBJECT') {
      this.branches.push([]);
      this.branches[0][0] = [];
      this.branches[0][1] = [];
    }

    let latestExpression = new ATOMExpression();
    latestExpression.parent = this;
    let predicateCommandFlag = 0;
    while (children.length>0) {
      let elem = children.splice(0,1)[0];
      if (elem==ATOMOperation.MULTIPLY) {
        let currentCommand = latestExpression.children;
        if (currentCommand.length==0) {
          elem = new ATOMValueDynamic(0);
          elem.parent = this;
        } else if (currentCommand[currentCommand.length-1] instanceof ATOMOperation) {
          elem = new ATOMValueDynamic(0);
          elem.parent = this;
        }
      }
      if (this.type == 'OBJECT') {
        if (elem == ATOMOperation.END_STATEMENT) {
          this.branches[this.branches.length-1][predicateCommandFlag].push(latestExpression);
          latestExpression = new ATOMExpression();
          latestExpression.parent = this;
        } else if (elem == ATOMOperation.P_SEPERATOR) {
          this.branches[this.branches.length-1][predicateCommandFlag].push(latestExpression);
          latestExpression = new ATOMExpression();
          latestExpression.parent = this;
          predicateCommandFlag = 1;
        } else if (elem == ATOMOperation.S_SEPERATOR) {
          this.branches[this.branches.length-1][predicateCommandFlag].push(latestExpression);
          latestExpression = new ATOMExpression();
          latestExpression.parent = this;
          predicateCommandFlag = 0;
          let nextBranch = [[],[]];
          this.branches.push(nextBranch);
        } else {
          latestExpression.children.push(elem);
        }
      } else {
        if (elem == ATOMOperation.END_STATEMENT || elem == ATOMOperation.S_SEPERATOR) {
          this.components.push(latestExpression);
          latestExpression = new ATOMExpression();
          latestExpression.parent = this;
        } else {
          latestExpression.children.push(elem);
        }
      }
    }
    if (latestExpression.children.length>0) {
      if (this.type == 'OBJECT') {
        this.branches[this.branches.length-1][predicateCommandFlag].push(latestExpression);
      } else {
        this.components.push(latestExpression);
      }
    }
  }

  eval() {
    if (this.type=='PARENTHESIS') {
      if (this.components.length==0) {
        return ATOMValue.NULL();
      }
      for (let i=0;i<this.components.length-1;i++) {
        this.components[i].eval();
      }
      return this.components[this.components.length-1].eval();
    }
    if (this.type == 'ARRAY') {
      return this.compute();
    }
    if (this.type == 'OBJECT') {
      let toReturn = new ATOMValue(this);
      toReturn.parent = this.parent;
      return toReturn;
    }
    throw 'Something went wrong with main.ATOMScopeType in Eval';
  }

  compute() {
    if (this.type=='PARENTHESIS') {
      if (this.components.length==0) {
        return ATOMValue.NULL();
      }
      if (this.components.length==1) {
        return this.components[0].eval().compute();
      }
      for (let i=0;i<this.components.length-1;i++) {
        this.components[i].eval();
      }
      return this.components[this.components.length-1].eval();
    }
    if (this.type=='ARRAY') {
      let values = [];
      for (let i=0;i<this.components.length;i++) {
        values.push(this.components[i].eval());
      }
      return new ATOMValue(values);
    }
    if (this.type=='OBJECT') {
      let toReturn = null;
      for (let branch of this.branches) {
        for (let i = 0; i < branch[0].length; i++) {
          toReturn = branch[0][i].eval();
        }
        if (toReturn!=null && toReturn.isTruthy()) {
          if (branch[1].length==0) {
            return toReturn;
          }
          for(let i = 0; i < branch[1].length; i++) {
            toReturn = branch[1][i].eval();
          }
          return toReturn;
        }
      }
      return toReturn;
    }
    throw 'Something went wrong with main.ATOMScopeType in Compute';
  }

  dereference(key) {
    if (this.type=='PARENTHESIS') {
      throw 'Cannot dereference parenthetical scope by key';
    }
    if (this.type == 'ARRAY') {
      throw 'Must eval array before dereferencing';
    }
    if (this.type != 'OBJECT') {
      throw 'Something went wrong with ATOMScopeType in derefernce';
    }

    let predicate = null;
    for (let branch in this.branches) {
      for (let i=0;i<branch[0].length;i++) {
        predicate = branch[0][i].eval();
      }
      if (predicate.equals(key)) {
        if (branch[1].length==0) {
          return ATOMValue.NULL();
        }
        for (let i=0;i<branch[1].length;i++) {
          predicate = branch[1][i].eval();
        }
        return predicate;
      }
    }
    return ATOMValue.NULL();
  }

  getLocalVar(name) {
    let pathToRoot = this;
    while (pathToRoot != null) {
      if (pathToRoot.localVars[name]!=null) {
        return pathToRoot.localVars[name];
      }
      pathToRoot = pathToRoot.parent;
    }
    return ATOMValue.NULL();
  }

  setLocalVar(name, val) {
    let pathToRoot = this;
    while (pathToRoot != null) {
      if (pathToRoot.localVars[name]!=null) {
        pathToRoot.localVars[name] = val;
        return;
      }
      pathToRoot = pathToRoot.parent;
    }
    this.localVars[name] = val;
  }

  toString() {
    let childrenStr = this.components.map(atomEx => atomEx.toString());
    return ATOMScopeType.opening(this.type) + childrenStr + ATOMScopeType.closing(this.type);
  }

}