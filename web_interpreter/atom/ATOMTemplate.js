
export default class ATOMTemplate {

  constructor(source) {
    this.source = '(' + source + ')';
    this.currentIndex = 0;
  }

  currentValidChar() {
    let c = this.source[this.currentIndex];
    while (c==' ' || c=='\n' || c=='#' || c=='️') {
        if (c=='#') {
          while (c!='\n') {
            this.currentIndex++;
            c = this.source.charAt(this.currentIndex);
          }
        }
        this.currentIndex++;
        c = this.source.charAt(this.currentIndex);
    }
    return c;
  }

}