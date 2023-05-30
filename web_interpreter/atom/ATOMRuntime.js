import ATOMElement from './ATOMElement.js';
import ATOMTemplate from './ATOMTemplate.js';

export default class ATOMRuntime {

  static globalVars = {};
  static stack = [];

  static reset() {
    ATOMRuntime.globalVars = {};
  }

  static getIndexedVar(index) {
    return ATOMRuntime.stack[ATOMRuntime.stack.length-(index+1)];
  }

  static pushIndexedVar(val) {
    ATOMRuntime.stack.push(val);
  }

  static popIndexedVar() {
    let val = ATOMRuntime.stack.splice(ATOMRuntime.stack.length-1, 1);
    return val[0];
  }

  static processInput(input) {
    return ATOMElement.fromTemplate(new ATOMTemplate(input)).eval();
  }

  static injectVariable(name, value) {
    ATOMRuntime.globalVars[name] = value;
  }

}