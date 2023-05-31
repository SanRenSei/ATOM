import ATOMElement from './ATOMElement.js';
import ATOMRuntime from './ATOMRuntime.js';
import ATOMScope from './ATOMScope.js';
import ATOMValue from './ATOMValue.js';
import ATOMValueDynamic from './ATOMValueDynamic.js';

export default class ATOMOperation {

    static ORDER_DEREF = 1;
    static ORDER_UNARY = ATOMOperation.ORDER_DEREF+1;
    static ORDER_ARRGEN = ATOMOperation.ORDER_UNARY+1;
    static ORDER_MULT = ATOMOperation.ORDER_ARRGEN+1;
    static ORDER_ADD = ATOMOperation.ORDER_MULT+1;
    static ORDER_MINMAX = ATOMOperation.ORDER_ADD+1;
    static ORDER_COMPARE = ATOMOperation.ORDER_MINMAX+1;
    static ORDER_AND = ATOMOperation.ORDER_COMPARE+1;
    static ORDER_INJECT = ATOMOperation.ORDER_AND+1;
    static ORDER_ASSIGNMENT = ATOMOperation.ORDER_INJECT+1;
    static ORDER_SEP = ATOMOperation.ORDER_ASSIGNMENT+1;
    static ORDER_PRINT = ATOMOperation.ORDER_SEP+1;
    static ORDER_ARRIN = ATOMOperation.ORDER_PRINT+1;
    static ORDER_FUNC = ATOMOperation.ORDER_ARRIN+1;
    static ORDER_OPASSIGN = 100;

    static DEREFERENCE = new ATOMOperation(['.'], ATOMOperation.ORDER_DEREF, (left, right) => {
      let rightVal = right.eval();
      if (rightVal.getType() == 'STRING' && left.parent!=null) {
        let potentialVar = left.parent.getLocalVar(rightVal.strVal);
        if (!potentialVar.equals(ATOMValue.NULL())) {
          rightVal = potentialVar;
        }
      }
      if (left instanceof ATOMValueDynamic) {
        return left.generateChild(rightVal);
      }
      let leftVal = left.eval();

      if (leftVal.getType() == 'ARRAY' && rightVal.getType()=='STRING' && rightVal.strVal=='length') {
        return new ATOMValue(leftVal.arrVal.length);
      }
      if (leftVal.getType() == 'ARRAY' && rightVal.getType()=='INT') {
        return new ATOMValue(leftVal.arrVal[rightVal.intVal]).eval();
      }
      if (leftVal.getType() == 'OBJECT' && leftVal.objectVal.type=='OBJECT') {
        return leftVal.objVal.dereference(rightVal);
      }
      throw left + '.' + right;
    });

    static DEREFERENCE_NEGATIVE = new ATOMOperation(['.-'], ATOMOperation.ORDER_DEREF, (left, right) => {
      let rightVal = right.eval();
      if (rightVal.getType() != 'INT') {
        throw left + '.-' + right;
      }
      if (left instanceof ATOMValueDynamic) {
        return left.generateChild(new ATOMValue(-rightVal.intVal));
      }
      let leftVal = left.eval();
      if (leftVal.getType()=='ARRAY' && rightVal.getType()=='INT') {
        let index = leftVal.arrVal.length - rightVal.intVal;
        return leftVal.arrVal[index].eval();
      }
      throw left + '.-' + right;
    });

    static GET_LENGTH = new ATOMOperation(['🧵'], ATOMOperation.ORDER_UNARY, (left, right) => {
      let rightVal = right.eval();
      if (left == null && rightVal.getType()=='STRING') {
        return new ATOMValue(rightVal.strVal.length);
      }
      if (left == null && rightVal.getType()=='ARRAY') {
        return new ATOMValue(rightVal.arrVal.length);
      }
      throw left + '🧵' + right;
    });

    static STR_TRIM = new ATOMOperation(['✂'], ATOMOperation.ORDER_UNARY, (left, right) => {
      let rightVal = right.eval();
      if (left == null && rightVal.getType()=='STRING') {
        return new ATOMValue(rightVal.strVal.trim());
      }
      throw left + '✂' + right;
    });

    static ARR_FLATTEN = new ATOMOperation(['🦶'], ATOMOperation.ORDER_UNARY, (left, right) => {
      let rightVal = right.eval();
      if (left == null && rightVal.getType() == 'ARRAY') {
        let flattened = [];
        rightVal.arrVal.forEach(val => {
          if (val.getType() == 'ARRAY') {
            val.arrVal.forEach(val2 => flattened.push(val2));
          } else {
            flattened.push(val);
          }
        });
        return new ATOMValue(flattened);
      }
      throw left + '🦶' + right;
    });

    static ARR_GEN = new ATOMOperation(['~'], ATOMOperation.ORDER_ARRGEN, (left, right) => {
      let rightVal = right.eval();
      if (left == null && rightVal.getType()=='OBJECT') {
        return rightVal.objVal.compute();
      }
      let leftVal = left.eval();
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'INT') {
        let startVal = leftVal.intVal, endVal = rightVal.intVal;
        let arrGen = [];
        for (let i=startVal;i<=endVal;i++) {
          arrGen.push(new ATOMValue(i));
        }
        return new ATOMValue(arrGen);
      }
      throw left + '~' + right;
    });

    static NEGATE = new ATOMOperation(['!'], ATOMOperation.ORDER_MULT, (left, right) => {
      return new ATOMValue(!right.eval().isTruthy());
    });

    static MULTIPLY = new ATOMOperation(['*'], ATOMOperation.ORDER_MULT, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (leftVal.getType() == 'NULL' && rightVal.getType() == 'INT') {
        return new ATOMValue(0);
      }
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'NULL') {
        return new ATOMValue(0);
      }
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'INT') {
        return new ATOMValue(leftVal.intVal * rightVal.intVal);
      }
      throw left + '*' + right;
    });

    static DIVISION = new ATOMOperation(['/'], ATOMOperation.ORDER_MULT, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (leftVal.getType() == 'NULL' && rightVal.getType() == 'INT') {
        return new ATOMValue(0);
      }
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'NULL') {
        return new ATOMValue(1/0);
      }
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'INT') {
        return new ATOMValue(leftVal.intVal / rightVal.intVal);
      }
      if (leftVal.getType() == 'STRING' && rightVal.getType() == 'STRING') {
        return new ATOMValue(leftVal.strVal.split(rightVal.strVal).map(s => new ATOMValue(s)));
      }
      throw left + '/' + right;
    });

    static MODULO = new ATOMOperation(['%'], ATOMOperation.ORDER_MULT, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'INT') {
        return new ATOMValue(leftVal.intVal % rightVal.intVal);
      }
      throw left + '%' + right;
    });

    static ADD = new ATOMOperation(['+'], ATOMOperation.ORDER_ADD, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (leftVal.getType() == 'NULL' && rightVal.getType() == 'INT') {
        return new ATOMValue(rightVal.intVal);
      }
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'NULL') {
        return new ATOMValue(leftVal.intVal);
      }
      if (leftVal.getType() == 'NULL' && rightVal.getType() == 'STRING') {
        return new ATOMValue(rightVal.strVal);
      }
      if (leftVal.getType() == 'STRING' && rightVal.getType() == 'NULL') {
        return new ATOMValue(leftVal.strVal);
      }
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'INT') {
        return new ATOMValue(leftVal.intVal + rightVal.intVal);
      }
      if (leftVal.getType() == 'STRING' && rightVal.getType() == 'STRING') {
        return new ATOMValue(leftVal.strVal + rightVal.strVal);
      }
      if (leftVal.getType() == 'ARRAY') {
        leftVal.arrVal.push(rightVal);
        return leftVal;
      }
      throw left + '+' + right;
    });

    static SUBTRACT = new ATOMOperation(['-'], ATOMOperation.ORDER_ADD, (left, right) => {
      let rightVal = right.eval();
      if (left == null) {
        if (rightVal.getType() == 'INT') {
          return new ATOMValue(-rightVal.intVal);
        }
        throw left + '-' + right;
      }
      let leftVal = left.eval();
      if (leftVal.getType() == 'NULL' && rightVal.getType() == 'INT') {
        return new ATOMValue(-rightVal.intVal);
      }
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'NULL') {
        return new ATOMValue(leftVal.intVal);
      }
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'INT') {
        return new ATOMValue(leftVal.intVal - rightVal.intVal);
      }
      throw left + '-' + right;
    });

    static MAXIMUM = new ATOMOperation(['><'], ATOMOperation.ORDER_MINMAX, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (leftVal.getType() == 'NULL' && rightVal.getType() == 'INT') {
        return new ATOMValue(rightVal.intVal);
      }
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'NULL') {
        return new ATOMValue(leftVal.intVal);
      }
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'INT') {
        return new ATOMValue(Math.max(leftVal.intVal, rightVal.intVal));
      }
      if (leftVal.getType() == 'STRING' && rightVal.getType() == 'STRING') {
        return new ATOMValue(rightVal.strVal > leftVal.strVal ? rightVal.strVal : leftVal.strVal);
      }
      throw left + '><' + right;
    });

    static MINIMUM = new ATOMOperation(['<>'], ATOMOperation.ORDER_MINMAX, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (leftVal.getType() == 'NULL' && rightVal.getType() == 'INT') {
        return new ATOMValue(rightVal.intVal);
      }
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'NULL') {
        return new ATOMValue(leftVal.intVal);
      }
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'INT') {
        return new ATOMValue(Math.min(leftVal.intVal, rightVal.intVal));
      }
      if (leftVal.getType() == 'STRING' && rightVal.getType() == 'STRING') {
        return new ATOMValue(rightVal.strVal < leftVal.strVal ? rightVal.strVal : leftVal.strVal);
      }
      throw left + '<>' + right;
    });

    static EQUALITY = new ATOMOperation(['=='], ATOMOperation.ORDER_COMPARE, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'INT') {
        return new ATOMValue(leftVal.intVal == rightVal.intVal);
      }
      throw left + '==' + right;
    });

    static NOTEQUAL = new ATOMOperation(['!='], ATOMOperation.ORDER_COMPARE, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'INT') {
        return new ATOMValue(leftVal.intVal != rightVal.intVal);
      }
      throw left + '!=' + right;
    });

    static LESSTHAN = new ATOMOperation(['<'], ATOMOperation.ORDER_COMPARE, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'INT') {
        return new ATOMValue(leftVal.intVal < rightVal.intVal);
      }
      throw left + '<' + right;
    });

    static LESSTHANOREQUAL = new ATOMOperation(['<='], ATOMOperation.ORDER_COMPARE, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'INT') {
        return new ATOMValue(leftVal.intVal <= rightVal.intVal);
      }
      throw left + '<=' + right;
    });

    static GREATER = new ATOMOperation(['>'], ATOMOperation.ORDER_COMPARE, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'INT') {
        return new ATOMValue(leftVal.intVal > rightVal.intVal);
      }
      throw left + '>' + right;
    });

    static GREATEROREQUAL = new ATOMOperation(['>='], ATOMOperation.ORDER_COMPARE, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (leftVal.getType() == 'INT' && rightVal.getType() == 'INT') {
        return new ATOMValue(leftVal.intVal >= rightVal.intVal);
      }
      throw left + '>=' + right;
    });

    static AND = new ATOMOperation(['&'], ATOMOperation.ORDER_AND, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (leftVal.getType() == 'BOOLEAN' && rightVal.getType() == 'BOOLEAN') {
        return new ATOMValue(leftVal.boolVal && rightVal.boolVal);
      }
      throw left + '&' + right;
    });

    static INJECT = new ATOMOperation(['INJECT', '->', '=>'], ATOMOperation.ORDER_INJECT, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (rightVal.getType() == 'OBJECT') {
        let rightValObj = rightVal.objVal;
        if (left instanceof ATOMValueDynamic) {
          rightValObj.localVars[left.name] = rightValObj.getLocalVar(left.name).compute();
          return rightVal;
        }
        if (leftVal.getType() == 'OBJECT') {
          let leftValScope = leftVal.getObjVal();
          if (leftValScope.type == 'OBJECT') {
            for (let branch of leftValScope.branches) {
              let predicate = null, result = null;
              for (let i=0;i<branch[0].length;i++) {
                predicate = branch[0][i].eval();
              }
              if (predicate!=null && predicate.getType() == 'STRING') {
                for (let i=0;i<branch[1].length;i++) {
                  result = branch[1][i].eval();
                }
                rightValObj.localVars[predicate.strVal] = result;
              }
            }
            return rightVal;
          }
        }
        return ATOMValue.NULL();
      }
      throw left + 'INJECT' + right;
    });

    static ASSIGNMENT = new ATOMOperation(['='], ATOMOperation.ORDER_ASSIGNMENT, (left, right) => {
      if (left instanceof ATOMValueDynamic) {
        left.setVal(right.eval());
        return left.eval();
      }
      throw left + 'ASSIGNMENT' + right;
    });

    static S_SEPERATOR = new ATOMOperation([','], ATOMOperation.ORDER_SEP, null);
    static P_SEPERATOR = new ATOMOperation([':'], ATOMOperation.ORDER_SEP, null);
    static END_STATEMENT = new ATOMOperation([';'], ATOMOperation.ORDER_SEP, null);

    static PRINT = new ATOMOperation(['PRINT','🖨'], ATOMOperation.ORDER_UNARY, (left, right) => {
      let rightVal = right.eval();
      console.log(rightVal.getType()=='STRING'?rightVal.strVal:rightVal.toString());
      return rightVal;
    });

    static IN = new ATOMOperation(['IN', '🏠'], ATOMOperation.ORDER_ARRIN, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (rightVal.getType() == 'ARRAY') {
        for (let i=0;i<rightVal.arrVal.length;i++) {
          if (leftVal.equals(rightVal.arrVal[i])) {
            return new ATOMValue(true);
          }
        }
        return new ATOMValue(false);
      }
      throw left + 'IN' + right;
    });

    static NOTIN = new ATOMOperation(['NOTIN', 'NIN', '🏕'], ATOMOperation.ORDER_ARRIN, (left, right) => {
      let leftVal = left.eval(), rightVal = right.eval();
      if (rightVal.getType() == 'ARRAY') {
        for (let i=0;i<rightVal.arrVal.length;i++) {
          if (leftVal.equals(rightVal.arrVal[i])) {
            return new ATOMValue(false);
          }
        }
        return new ATOMValue(true);
      }
      throw left + 'NOTIN' + right;
    });

    static INTO = new ATOMOperation(['INTO', '🚪'], ATOMOperation.ORDER_FUNC, (left, right) => {
      if (right instanceof ATOMScope) {
        ATOMRuntime.pushIndexedVar(left.eval());
        let toReturn = right.compute();
        ATOMRuntime.popIndexedVar();
        return toReturn;
      }
      throw left + 'INTO' + right;
    });

    static FOREACH = new ATOMOperation(['FOREACH', '∀'], ATOMOperation.ORDER_FUNC, (left, right) => {
      let leftVal = left.eval();
      if (leftVal.getType() == 'ARRAY' && right instanceof ATOMScope) {
        for (let i=0;i<leftVal.arrVal.length;i++) {
          let iterator = leftVal.arrVal[i];
          ATOMRuntime.pushIndexedVar(iterator.eval());
          right.compute();
          ATOMRuntime.popIndexedVar();
        }
        return ATOMValue.NULL();
      }
      throw left + 'FOREACH' + right;
    });

    static IFOREACH = new ATOMOperation(['iFOREACH', 'i∀', '🔢'], ATOMOperation.ORDER_FUNC, (left, right) => {
      let leftVal = left.eval();
      if (leftVal.getType() == 'ARRAY' && right instanceof ATOMScope) {
        for (let i=0;i<leftVal.arrVal.length;i++) {
          ATOMRuntime.pushIndexedVar(i);
          right.compute();
          ATOMRuntime.popIndexedVar();
        }
        return ATOMValue.NULL();
      }
      throw left + 'IFOREACH' + right;
    });

    static MAP = new ATOMOperation(['MAP', '🗺'], ATOMOperation.ORDER_FUNC, (left, right) => {
      let leftVal = left.eval();
      if (leftVal.getType() == 'ARRAY' && right instanceof ATOMScope) {
        let mappedVals = [];
        for (let i=0;i<leftVal.arrVal.length;i++) {
          let iterator = leftVal.arrVal[i];
          ATOMRuntime.pushIndexedVar(iterator.eval());
          mappedVals.push(right.compute());
          ATOMRuntime.popIndexedVar();
        }
        return new ATOMValue(mappedVals);
      }
      throw left + 'MAP' + right;
    });

    static WHERE = new ATOMOperation(['WHERE', '🔍'], ATOMOperation.ORDER_FUNC, (left, right) => {
      let leftVal = left.eval();
      if (leftVal.getType() == 'ARRAY' && right instanceof ATOMScope) {
        let filteredVals = [];
        for (let i=0;i<leftVal.arrVal.length;i++) {
          let iterator = leftVal.arrVal[i];
          ATOMRuntime.pushIndexedVar(iterator.eval());
          let filterVal = right.compute();
          if (filterVal.isTruthy()) {
            filteredVals.push(iterator);
          }
          ATOMRuntime.popIndexedVar();
        }
        return new ATOMValue(filteredVals);
      }
      throw left + 'WHERE' + right;
    });

    static IWHERE = new ATOMOperation(['iWHERE', 'i🔍'], ATOMOperation.ORDER_FUNC, (left, right) => {
      let leftVal = left.eval();
      if (leftVal.getType() == 'ARRAY' && right instanceof ATOMScope) {
        let filteredVals = [];
        for (let i=0;i<leftVal.arrVal.length;i++) {
          let iterator = new ATOMValue(i);
          ATOMRuntime.pushIndexedVar(iterator.eval());
          let filterVal = right.compute();
          if (filterVal.isTruthy()) {
            filteredVals.push(iterator);
          }
          ATOMRuntime.popIndexedVar();
        }
        return new ATOMValue(filteredVals);
      }
      throw left + 'WHERE' + right;
    });

    static THROUGH = new ATOMOperation(['THROUGH', '🕳'], ATOMOperation.ORDER_FUNC, (left, right) => {
      let leftVal = left.eval();
      if (right instanceof ATOMScope) {
        while (leftVal.isTruthy() && new Date().getTime() < window.atomBeginTime+window.atomTimeout) {
          ATOMRuntime.pushIndexedVar(leftVal);
          leftVal = right.compute();
          ATOMRuntime.popIndexedVar();
        }
        return leftVal;
      }
      throw left + 'THROUGH' + right;
    });

    static UNPACK = new ATOMOperation(['UNPACK', '🎒'], ATOMOperation.ORDER_FUNC, (left, right) => {
      let leftVal = left.eval();
      if (leftVal.getType() == 'OBJECT') {
        let leftValObj = leftVal.objVal;
        if (right instanceof ATOMValueDynamic) {
          right.setVal(leftValObj.derefereence(new ATOMValue(right.name)));
          return ATOMValue.NULL();
        }
        let rightVal = right.eval();
        if (rightVal.getType() == 'OBJECT') {
          let vars = rightVal.objVal;
          vars.branches.forEach(b => {
            let val = b[0][0].children[0];
            if (val instanceof ATOMValueDynamic) {
              let name = val.name;
              rightVal.parent.setLocalVar(name, leftValObj.dereference(new ATOMValue(name)));
            } else {
              throw left + 'UNPACK' + right;
            }
          });
          return ATOMValue.NULL();
        }
      }
      throw left + 'UNPACK' + right;
    });

    static PLUS_EQUALS = new ATOMOperation(['+='], ATOMOperation.ORDER_OPASSIGN, (left, right) => {
      return ATOMOperation.ASSIGNMENT.operate(left, ATOMOperation.ADD.operate(left, right));
    });

    static MINUS_EQUALS = new ATOMOperation(['-='], ATOMOperation.ORDER_OPASSIGN, (left, right) => {
      return ATOMOperation.ASSIGNMENT.operate(left, ATOMOperation.SUBTRACT.operate(left, right));
    });

    static TIMES_EQUALS = new ATOMOperation(['*='], ATOMOperation.ORDER_OPASSIGN, (left, right) => {
      return ATOMOperation.ASSIGNMENT.operate(left, ATOMOperation.MULTIPLY.operate(left, right));
    });

    static DIVIDE_EQUALS = new ATOMOperation(['/='], ATOMOperation.ORDER_OPASSIGN, (left, right) => {
      return ATOMOperation.ASSIGNMENT.operate(left, ATOMOperation.DIVISION.operate(left, right));
    });

    static MAX_EQUALS = new ATOMOperation(['><='], ATOMOperation.ORDER_OPASSIGN, (left, right) => {
      return ATOMOperation.ASSIGNMENT.operate(left, ATOMOperation.MAXIMUM.operate(left, right));
    });

    static MIN_EQUALS = new ATOMOperation(['<>='], ATOMOperation.ORDER_OPASSIGN, (left, right) => {
      return ATOMOperation.ASSIGNMENT.operate(left, ATOMOperation.MINIMUM.operate(left, right));
    });

    static operations = [
      ATOMOperation.EQUALITY, // ==
      ATOMOperation.NOTEQUAL, // !=
      ATOMOperation.PLUS_EQUALS, // +=
      ATOMOperation.MINUS_EQUALS, // -=
      ATOMOperation.TIMES_EQUALS, // *=
      ATOMOperation.DIVIDE_EQUALS, // /=
      ATOMOperation.MAX_EQUALS, // ><=
      ATOMOperation.MIN_EQUALS, // <>=
      ATOMOperation.INJECT, // INJECT -> =>
      ATOMOperation.ASSIGNMENT,  // =
      ATOMOperation.AND, // &
      ATOMOperation.ADD, // +
      ATOMOperation.SUBTRACT, // -
      ATOMOperation.MULTIPLY, // *
      ATOMOperation.DIVISION, // /
      ATOMOperation.MODULO, // %
      ATOMOperation.MAXIMUM, // ><
      ATOMOperation.MINIMUM, // <>
      ATOMOperation.ARR_GEN, // ~
      ATOMOperation.DEREFERENCE_NEGATIVE, // .-
      ATOMOperation.DEREFERENCE, // .
      ATOMOperation.LESSTHANOREQUAL, // <=
      ATOMOperation.LESSTHAN, // <
      ATOMOperation.GREATEROREQUAL, // >=
      ATOMOperation.GREATER, // >
      ATOMOperation.NEGATE, // !
      ATOMOperation.END_STATEMENT, // ;
      ATOMOperation.P_SEPERATOR, // :
      ATOMOperation.S_SEPERATOR, // ,
      ATOMOperation.PRINT, // PRINT 🖨️
      ATOMOperation.INTO, // INTO 🚪
      ATOMOperation.FOREACH, // FOREACH ∀
      ATOMOperation.IFOREACH, // iFOREACH i∀
      ATOMOperation.MAP, // MAP 🗺️
      ATOMOperation.WHERE, // WHERE 🔍
      ATOMOperation.IWHERE, // iWHERE i🔍
      ATOMOperation.IN, // IN 🏠
      ATOMOperation.NOTIN, // NOTIN NIN 🏕
      ATOMOperation.THROUGH, // THROUGH 🕳️
      ATOMOperation.UNPACK, // UNPACK 🎒
      ATOMOperation.GET_LENGTH, // 🧵
      ATOMOperation.STR_TRIM, // ✂
      ATOMOperation.ARR_FLATTEN, // 🦶
    ];

    constructor(commands, order, operate) {
      this.commands = commands;
      this.order = order;
      this.operate = operate;
    }

    eval(arg1, arg2) {
      if (this.operate==null) {
        throw 'Must implement operate for ' + this;
      }
      return this.operate(arg1, arg2);
    }

    compute() {
      throw 'Operations cannot be computed to a value';
    }

    toString() {
      return this.commands[0];
    }

}