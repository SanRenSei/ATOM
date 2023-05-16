
var env = 'local';

var baseUrl = {
  local: 'http://localhost:8080'
};

const KEY_BACKSPACE = 8, KEY_LEFT = 37, KEY_RIGHT = 39, KEY_UP = 38, KEY_DOWN = 40, KEY_ENTER = 13;

var state = {
  dirTree:[{name:'vfs', files:[]}],
  text:['abc', 'def'],
  activeCol:0,
  activeRow:0
};

var actions = {
  insertChar: (c) => {
    state.text[state.activeRow] = state.text[state.activeRow].substr(0, state.activeCol) + c + state.text[state.activeRow].substr(state.activeCol);
    state.activeCol++;
  },
  setCursor: (row, col) => {
    state.activeRow = row;
    state.activeCol = col;
  },
  moveLeft: () => {
    if (state.activeCol>=1) {
      state.activeCol--;
    }
  },
  moveRight: () => {
    state.activeCol++;
  },
  moveUp: () => {
    if (state.activeRow>=1) {
      state.activeRow--;
    } else {
      state.activeCol = 0;
    }
  },
  moveDown: () => {
    state.activeRow++;
  },
  newLine: () => {
    state.activeRow++;
  },
  backspace: () => {
    if (state.activeCol>=1) {
      state.text[state.activeRow] = state.text[state.activeRow].substr(0, state.activeCol-1) + state.text[state.activeRow].substr(state.activeCol);
      state.activeCol--;
    }
  }
}

var TextDisplay = {
  view: (vnode) => {
    var {text} = vnode.attrs;
    return m('div', text.map((l,row) => [m('tt',{
      onclick: (evt) => {
        var newActiveCol = Math.floor(l.length*evt.clientX/evt.target.getBoundingClientRect().width+0.5);
        actions.setCursor(row, newActiveCol);
      }
    },l), m('br')]));
  }
};

var TextInput = {
  view: (vnode) => {
    return m('input.text-input', {
      autofocus:true,
      style: {
        backgroundColor: 'rgba(0,0,0,0)',
        position: 'absolute',
        top: `${state.activeRow}em`,
        left: `calc(${state.activeCol}ch + ${state.activeCol/2}px)`
      },
      oninput: e => {
        actions.insertChar(e.target.value);
        e.target.value = '';
      },
      onkeydown: e => {
        switch (e.keyCode) {
          case KEY_LEFT: actions.moveLeft(); break;
          case KEY_UP: actions.moveUp(); break;
          case KEY_RIGHT: actions.moveRight(); break;
          case KEY_DOWN: actions.moveDown(); break;
          case KEY_ENTER: actions.newLine(); break;
          case KEY_BACKSPACE: actions.backspace(); break;
        }
      }
    });
  },
  onupdate: (vnode) => {
    vnode.dom.focus();
  }
}

var TextEditor = {
  view: (vnode) => {
    return m('div', {
      style:{
        position:'relative'
      }
    }, [m(TextDisplay, {text:state.text}), m(TextInput)]);
  }
}

var DirectoryTree = {
  view: (vnode) => {
    var {tree, indent} = vnode.attrs;
    if (!tree) {
      return;
    }
    var fList = tree.map(f => {
      return m('div', [
        m('div', {
          style: {
            "margin-left": `${indent}em`
          }
        }, f.name), 
        m(DirectoryTree, {
          indent: indent+1,
          tree: f.files
        })
      ]);
    });
    return m('div', fList);
  }
}

var App = {
  view: (vnode) => {
    return m('div', [
      m(DirectoryTree, {indent: 0, tree: state.dirTree}), 
      m(TextEditor)
    ]);
  },
  oninit: (vnode) => {
    m.request({
      method: "GET",
      url: `${baseUrl[env]}/vfs/?path=`
    })
    .then(function(result) {
        state.dirTree[0].files = result.map(r => {
          return {name: r};
        });
    })
  }
}

m.mount(document.body, App)