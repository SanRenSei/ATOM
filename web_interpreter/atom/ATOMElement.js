import ATOMOperation from './ATOMOperation.js';
import ATOMScope from './ATOMScope.js';
import ATOMValue from './ATOMValue.js';
import ATOMValueDynamic from './ATOMValueDynamic.js';

export default class ATOMElement {

  constructor() {
    this.parent = null;
  }

  static fromTemplate(atomTemplate) {
    let c = atomTemplate.currentValidChar();
    for (let op of ATOMOperation.operations) {
      for (let opStr of op.commands) {
        if (c==opStr[0] && atomTemplate.source.substring(atomTemplate.currentIndex, atomTemplate.currentIndex+opStr.length)==opStr) {
          atomTemplate.currentIndex += opStr.length;
          return op;
        }
      }
    }
    if (c=='(') {
      return new ATOMScope(atomTemplate, 'PARENTHESIS');
    }
    if (c=='[') {
      return new ATOMScope(atomTemplate, 'ARRAY');
    }
    if (c=='{') {
      return new ATOMScope(atomTemplate, 'OBJECT');
    }
    if (c=='"') {
      let startIndex = atomTemplate.currentIndex;
      let endIndex = atomTemplate.currentIndex+1;
      while (atomTemplate.source[endIndex]!='"') {
        if (atomTemplate.source[endIndex]=='\\' && atomTemplate.source[endIndex+1]=='"') {
          endIndex+=2;
        } else {
          endIndex++;
        }
      }
      atomTemplate.currentIndex = endIndex+1;
      return new ATOMValue(atomTemplate.source.substring(startIndex+1, endIndex).replace(/\\"/g, '"'));
    }
    if (c=='$') {
      let startIndex = atomTemplate.currentIndex+1;
      let endIndex = atomTemplate.currentIndex+1;
      let isString = false;
      c = atomTemplate.source[endIndex];
      while ((c>='0' && c<='9') || this.isAlphabet(c)) {
        if (this.isAlphabet(c)) {
          isString = true;
        }
        endIndex ++;
        c = atomTemplate.source[endIndex];
      }
      atomTemplate.currentIndex = endIndex;
      if (isString) {
        return new ATOMValueDynamic(atomTemplate.source.substring(startIndex, endIndex), true);
      }
      return new ATOMValueDynamic(parseInt(atomTemplate.source.substring(startIndex, endIndex)));
    }
    if (this.isAlphabet(c)) {
      let startIndex = atomTemplate.currentIndex;
      let endIndex = atomTemplate.currentIndex;
      while (this.isAlphabet(atomTemplate.source[endIndex])) {
        endIndex++;
      }
      atomTemplate.currentIndex = endIndex;
      return new ATOMValueDynamic(atomTemplate.source.substring(startIndex, endIndex));
    }
    if (c>='0' && c<='9') {
      let startIndex = atomTemplate.currentIndex;
      let endIndex = atomTemplate.currentIndex;
      while (atomTemplate.source[endIndex]>='0' && atomTemplate.source[endIndex]<='9') {
        endIndex++;
      }
      atomTemplate.currentIndex = endIndex;
      return new ATOMValue(parseInt(atomTemplate.source.substring(startIndex, endIndex)));
    }
    console.log('NO MATCH FOUND FOR CHAR CODE')
    console.log((''+c).charCodeAt(0))
    console.log('INDEX IS');
    console.log(atomTemplate.currentIndex);
    throw c;
  }

  static isAlphabet(c) {
    return c >= 'A' && c <= 'Z' || c>='a' && c<='z';
  }

}