import ATOMValue from './ATOMValue.js';

export default class SparseArrayList {

  constructor(arg) {
    this.listHead = [];
    this.listTail = {};
    if (arg) {
      this.listHead = [...arg];
    }
  }

  add(val) {
    this.listHead.push(val);
    return val;
  }

  remove(index) {
    if (this.listHead.length>=index) {
      this.listHead.splice(index, 1);
      return;
    }
    delete this.listTail[index];
  }

  size() {
    return this.listHead.length + Object.keys(this.listTail).length;
  }

  get(index) {
    if (index<0) {
      index = this.size()+index;
    }
    if (index < this.listHead.length && index>=0) {
      return this.listHead[index];
    }
    if (this.listTail[index]!=undefined) {
      return this.listTail[index];
    }
    return ATOMValue.NULL();
  }

  set(index, val) {
    if (index<0) {
      index = this.size()+index;
    }
    if (this.size()==0 && index==-1) {
      index = 0;
    }
    if (index > this.listHead.length) {
      listTail[index] = val;
    } else if (this.listHead.length==index) {
      this.add(val);
    } else {
      this.listHead[index] = val;
    }
    return val;
  }

  map(fn) {
    let toReturn = new SparseArrayList();
    for (let i=0;i<this.listHead.length;i++) {
      toReturn.add(fn(this.listHead[i]));
    }
    for (let i in this.listTail) {
      toReturn.listTail[i] = fn(this.listTail[i]);
    }
    return toReturn;
  }

  forEach(fn) {
    for (let i=0;i<this.listHead.length;i++) {
      fn(this.listHead[i])
    }
    for (let i in this.listTail) {
      fn(this.listTail[i]);
    }
  }

  toList() {
    let toReturn = [...this.listHead];
    for (let i in this.listTail) {
      toReturn.push(this.listTail[i]);
    }
    return toReturn;
  }

  toString() {
    if (this.listHead.length==0 && this.listTail.length==0) {
      return '[]';
    }
    let toReturn = '[';
    for (let val in this.listHead) {
      toReturn += this.listHead[val].toString() + ',';
    }
    for (let i in this.listTail) {
      window.oldlog(this.listTail[i].toString());
      toReturn += this.listTail[i].toString() + ',';
    }
    toReturn = toReturn.substring(0, toReturn.length-1);
    toReturn += ']';
    return toReturn;
  }

}