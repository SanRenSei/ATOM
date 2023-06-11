
import ATOMRuntime from './atom/ATOMRuntime.js';

let inputProgram = document.getElementById('input');
let runButton = document.getElementById('runButton');
let outputConsole = document.getElementById('output');

runButton.onclick = () => {
  window.atomBeginTime = new Date().getTime();
  window.atomTimeout = 5000;
  console.log = (str) => {
    outputConsole.innerHTML = outputConsole.innerHTML + '\n' + str.replace(/\\n/g, '\n');
  }
  outputConsole.innerHTML = '';
  ATOMRuntime.reset();
  console.log(ATOMRuntime.processInput(inputProgram.value).toString());
}