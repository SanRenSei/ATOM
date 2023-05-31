import SparseArrayList from './SparseArrayList.js';

export default class ATOMValue {

  constructor(val) {
    this.type = null;
    this.boolVal = null;
    this.intVal = null;
    this.strVal = null;
    this.arrVal = null;
    this.objVal = null;
    if (typeof val == 'boolean') {
      this.type = 'BOOLEAN';
      this.boolVal = val;
    }
    if (typeof val == 'number') {
      this.type = 'INT';
      this.intVal = val;
    }
    if (typeof val == 'string') {
      this.type = 'STRING';
      this.strVal = val;
    }
    if (typeof val == 'object') {
      if (Array.isArray(val)) {
        this.type = 'ARRAY';
        this.arrVal = val;
      } else {
        this.type = 'OBJECT';
        this.objVal = val;
      }
    }
  }

  static NULL() {
    let toReturn = new ATOMValue();
    toReturn.type = 'NULL';
    return toReturn;
  }

  eval() {
    return this;
  }

  compute() {
    if (this.type == 'OBJECT') {
      return this.objVal.compute();
    }
    return this;
  }

  toString() {
    switch(this.type) {
      case 'OBJECT':
        return this.objVal.toString();
      case 'ARRAY':
        return '['+this.arrVal.toString()+']';
      case 'INT':
        return '' + this.intVal;
      case 'STRING':
        return '"' + this.strVal + '"';
      case 'BOOLEAN':
        return '' + this.boolVal;
      case 'NULL':
        return "NULL";
      default:
        return null;
    }
  }

  isTruthy() {
    switch(this.type) {
      case 'NULL':
        return false;
      case 'BOOLEAN':
        return this.boolVal;
      case 'INT':
        return this.intVal != 0;
      case 'ARRAY':
        return true;
      case 'OBJECT':
        return true;
    }
    return true;
  }

  getType() {
    return this.type;
  }

  setType(newType) {
    this.type = newType;
    if (newType == 'ARRAY') {
      arrVal = new SparseArrayList();
    }
  }

  equals(other) {
    switch(this.type) {
      case 'NULL': return true;
      case 'BOOLEAN': return this.boolVal == other.boolVal;
      case 'INT': return this.intVal == other.intVal;
      case 'STRING': return this.strVal == other.strVal;
    }
    return this==other;
  }

  static listEquals(first, second) {
    if (first.length!=second.length) {
      return false;
    }
    for (let i=0;i<first.length;i++) {
      if (!first[i].equals(second[i])) {
        return false;
      }
    }
    return true;
  }

}