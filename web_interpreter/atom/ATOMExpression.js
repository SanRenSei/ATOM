import ATOMElement from './ATOMElement.js';
import ATOMOperation from './ATOMOperation.js';
import ATOMScope from './ATOMScope.js';
import ATOMValue from './ATOMValue.js';
import ATOMValueDynamic from './ATOMValueDynamic.js';

export default class ATOMExpression {

  constructor(arg) {
    this.children = null;
    if (arg instanceof Array) {
      this.children = arg;
      return;
    }
    if (arg) {
      this.children = [arg];
      return;
    }
    this.children = [];
  }

  setImplicitParentheticalScope() {
    let implicitScopeOperations = [
      ATOMOperation.FOREACH,
      ATOMOperation.IFOREACH,
      ATOMOperation.WHERE,
      ATOMOperation.IWHERE,
      ATOMOperation.MAP,
      ATOMOperation.INTO,
      ATOMOperation.THROUGH
    ];
    for (let i=0;i<this.children.length;i++) {
      if (implicitScopeOperations.indexOf(this.children[i])>=0) {
        if (i == this.children.length-2 && this.children[this.children.length-1] instanceof ATOMScope) {
          return;
        }
        let subScope = new ATOMScope();
        subScope.type = 'PARENTHESIS';
        subScope.parent = this.parent;
        let implicitScope = [];
        while (this.children.length>i+1) {
          let child = this.children.splice(i+1, 1)[0];
          child.parent = subScope;
          implicitScope.push(child);
        }
        let subExpression = new ATOMExpression(implicitScope);
        subExpression.parent = subScope;
        subScope.components.push(subExpression);
        this.children.add(subScope);
        return;
      }
    }
  }

  setImplicitArrayLength() {
    for (let i=2;i<this.children.length;i++) {
      if (this.children[i] instanceof ATOMValueDynamic) {
        let val = this.children[i];
        if (val.getType()=='SCOPED') {
          if (this.children[i-1] instanceof ATOMOperation && children[i-1]==ATOMOperation.DEREFERENCE) {
            children[i] = new ATOMValue(val.name);
          }
        }
      }
    }
  }

  eval() {
    this.setImplicitParentheticalScope();
    this.setImplicitArrayLength();
    let copyOfChildren = [...this.children];
    while (copyOfChildren.length>1) {
      let nextOperationIndex = -1;
      for (let i=0;i<copyOfChildren.length;i++) {
        if (copyOfChildren[i] instanceof ATOMOperation) {
          if (nextOperationIndex==-1) {
            nextOperationIndex = i;
          }
          if (copyOfChildren[i].order < copyOfChildren[nextOperationIndex].order
            || copyOfChildren[i].order == copyOfChildren[nextOperationIndex].order
            && copyOfChildren[i].order == ATOMOperation.ORDER_UNARY) {
            nextOperationIndex = i;
          }
          if (copyOfChildren[i].order == copyOfChildren[nextOperationIndex].order && copyOfChildren[i].order>=100) {
            nextOperationIndex = i;
          }
        }
      }
      if (nextOperationIndex==-1) {
        throw 'Probably a syntax error somewhere, not enough operators:\n' + this.children.toString();
      }
      let left = null, right = null;
      if (nextOperationIndex<copyOfChildren.length-1 && !(copyOfChildren[nextOperationIndex+1] instanceof ATOMOperation)) {
        right = copyOfChildren[nextOperationIndex+1];
        copyOfChildren.splice(nextOperationIndex+1, 1);
      }
      let op = copyOfChildren.splice(nextOperationIndex, 1)[0];
      if (nextOperationIndex>0 && !(copyOfChildren[nextOperationIndex-1] instanceof ATOMOperation)) {
        left = copyOfChildren[nextOperationIndex-1];
        copyOfChildren.splice(nextOperationIndex-1, 1);
      }
      let newVal = op.eval(left, right);
      if (left == null) {
        copyOfChildren.splice(nextOperationIndex, 0, newVal);
      } else {
        copyOfChildren.splice(nextOperationIndex - 1, 0, newVal);
      }
    }
    if (copyOfChildren.length==0 || copyOfChildren[0]==null) {
      return null;
    }
    return copyOfChildren[0].eval();
  }

  compute() {
    if (this.children.length==1) {
      return this.children[0].compute();
    }
    return this.eval();
  }

  toString() {
    return this.children.map(c => c.toString()).join(' ');
  }

}