
export default class ATOMScopeType{

  static opening(type) {
    if (type=='PARENTHESIS') {
      return '(';
    }
    if (type=='ARRAY') {
      return '[';
    }
    if (type=='OBJECT') {
      return '{';
    }
    throw 'Unknown type ' + type;
  }

  static closing(type) {
    if (type=='PARENTHESIS') {
      return ')';
    }
    if (type=='ARRAY') {
      return ']';
    }
    if (type=='OBJECT') {
      return '}';
    }
    throw 'Unknown type ' + type;
  }

}