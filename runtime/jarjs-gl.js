/* JARJS OpenGL → WebGL 运行时
 * 职责：只做 WebGL 的搬运与状态映射。固定管线语义（矩阵、光照、图元展开）在 Java 侧。
 */
(function () {
  'use strict';

  var G = {};
  window.JARJS_GL = G;

  var canvas = null;
  var gl = null;
  var prog = null;
  var loc = {};
  var vbo = null;
  var textures = {};
  var dpr = 1;
  var W = 480, H = 480;

  var keys = {};
  var keyQueue = [];
  var mouse = { x: 0, y: 0, dx: 0, dy: 0, buttons: {}, queue: [], grabbed: false };
  var closeRequested = false;
  var lost = false;

  // ---------- 着色器 ----------
  var VS = [
    'attribute vec3 aPos;',
    'attribute vec4 aColor;',
    'attribute vec2 aUV;',
    'uniform mat4 uMVP;',
    'varying vec4 vColor;',
    'varying vec2 vUV;',
    'void main() {',
    '  gl_Position = uMVP * vec4(aPos, 1.0);',
    '  vColor = aColor;',
    '  vUV = aUV;',
    '}'
  ].join('\n');

  var FS = [
    'precision mediump float;',
    'uniform sampler2D uTex;',
    'uniform float uUseTex;',
    'varying vec4 vColor;',
    'varying vec2 vUV;',
    'void main() {',
    '  vec4 c = vColor;',
    '  if (uUseTex > 0.5) { c *= texture2D(uTex, vUV); }',
    '  if (c.a < 0.004) discard;',
    '  gl_FragColor = c;',
    '}'
  ].join('\n');

  function compile(type, src) {
    var s = gl.createShader(type);
    gl.shaderSource(s, src);
    gl.compileShader(s);
    if (!gl.getShaderParameter(s, gl.COMPILE_STATUS)) {
      console.error('[JARJS GL] shader 编译失败: ' + gl.getShaderInfoLog(s));
      return null;
    }
    return s;
  }

  G.init = function (w, h, d) {
    canvas = document.getElementById('game');
    if (!canvas) { console.error('[JARJS GL] 页面缺少 canvas#game'); return 0; }
    try {
      gl = canvas.getContext('webgl', {
        antialias: true, alpha: false, depth: true, stencil: false,
        preserveDrawingBuffer: true, premultipliedAlpha: false
      }) || canvas.getContext('experimental-webgl', { preserveDrawingBuffer: true });
    } catch (e) { gl = null; }
    if (!gl) { console.error('[JARJS GL] 当前浏览器不支持 WebGL'); return 0; }

    dpr = d || 1;
    if (dpr <= 0) dpr = 1;
    W = w; H = h;
    applySize();

    var vs = compile(gl.VERTEX_SHADER, VS);
    var fs = compile(gl.FRAGMENT_SHADER, FS);
    if (!vs || !fs) return 0;
    prog = gl.createProgram();
    gl.attachShader(prog, vs);
    gl.attachShader(prog, fs);
    gl.linkProgram(prog);
    if (!gl.getProgramParameter(prog, gl.LINK_STATUS)) {
      console.error('[JARJS GL] program 链接失败: ' + gl.getProgramInfoLog(prog));
      return 0;
    }
    gl.useProgram(prog);

    loc.pos = gl.getAttribLocation(prog, 'aPos');
    loc.color = gl.getAttribLocation(prog, 'aColor');
    loc.uv = gl.getAttribLocation(prog, 'aUV');
    loc.mvp = gl.getUniformLocation(prog, 'uMVP');
    loc.tex = gl.getUniformLocation(prog, 'uTex');
    loc.useTex = gl.getUniformLocation(prog, 'uUseTex');

    vbo = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, vbo);
    gl.enableVertexAttribArray(loc.pos);
    gl.enableVertexAttribArray(loc.color);
    gl.enableVertexAttribArray(loc.uv);
    var stride = 9 * 4;
    gl.vertexAttribPointer(loc.pos, 3, gl.FLOAT, false, stride, 0);
    gl.vertexAttribPointer(loc.color, 4, gl.FLOAT, false, stride, 12);
    gl.vertexAttribPointer(loc.uv, 2, gl.FLOAT, false, stride, 28);

    gl.enable(gl.DEPTH_TEST);
    gl.depthFunc(gl.LEQUAL);
    gl.clearColor(0, 0, 0, 1);
    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
    gl.disable(gl.CULL_FACE);
    gl.disable(gl.BLEND);

    canvas.addEventListener('webglcontextlost', function (e) {
      e.preventDefault(); lost = true; console.warn('[JARJS GL] WebGL 上下文丢失');
    }, false);
    canvas.addEventListener('webglcontextrestored', function () {
      lost = false; console.warn('[JARJS GL] WebGL 上下文已恢复，纹理需重新上传');
      for (var k in textures) { if (textures.hasOwnProperty(k)) textures[k].dirty = true; }
    }, false);

    installInput();
    return 1;
  };

  function applySize() {
    if (!canvas) return;
    canvas.width = Math.round(W * dpr);
    canvas.height = Math.round(H * dpr);
    canvas.style.width = W + 'px';
    canvas.style.height = H + 'px';
    if (gl) gl.viewport(0, 0, canvas.width, canvas.height);
  }

  G.setSize = function (w, h, title) {
    W = w; H = h;
    applySize();
    if (title) document.title = title;
  };

  G.setVSync = function (v) {};

  G.contextLost = function () { return lost; };

  G.clear = function (r, g, b, a, mask) {
    if (!gl) return;
    gl.clearColor(r, g, b, a);
    var bits = 0;
    if (mask & 0x4000) bits |= gl.COLOR_BUFFER_BIT;
    if (mask & 0x0100) bits |= gl.DEPTH_BUFFER_BIT;
    if (mask & 0x0400) bits |= gl.STENCIL_BUFFER_BIT;
    if (bits === 0) bits = gl.COLOR_BUFFER_BIT;
    gl.clear(bits);
  };

  var curFlags = -1;
  G.setState = function (flags) {
    if (!gl) return;
    if (flags === curFlags) return;
    curFlags = flags;
    if (flags & 1) { gl.enable(gl.DEPTH_TEST); gl.depthFunc(gl.LEQUAL); }
    else { gl.disable(gl.DEPTH_TEST); }
    if (flags & 2) { gl.enable(gl.BLEND); gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA); }
    else { gl.disable(gl.BLEND); }
    if (flags & 4) { gl.enable(gl.CULL_FACE); gl.cullFace(gl.BACK); }
    else { gl.disable(gl.CULL_FACE); }
  };

  G.blendFunc = function (src, dst) {
    if (!gl) return;
    gl.blendFunc(src, dst);
  };

  G.lineWidth = function (w) { if (gl) gl.lineWidth(w); };
  G.pointSize = function (s) { /* WebGL 固定 1px，保留接口 */ };

  G.draw = function (mode, verts, count, mvp, texId, flags) {
    if (!gl || !prog || count <= 0) return;
    gl.useProgram(prog);

    var n = count * 9;
    var data;
    if (verts.length === n) {
      data = new Float32Array(verts);
    } else {
      data = new Float32Array(n);
      for (var i = 0; i < n; i++) data[i] = verts[i];
    }

    gl.bindBuffer(gl.ARRAY_BUFFER, vbo);
    gl.bufferData(gl.ARRAY_BUFFER, data, gl.DYNAMIC_DRAW);

    gl.uniformMatrix4fv(loc.mvp, false, new Float32Array(mvp));

    var useTex = (flags & 8) ? 1 : 0;
    if (useTex) {
      var t = textures[texId];
      if (!t) { useTex = 0; }
      else {
        if (t.dirty) uploadTexture(texId, t);
        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, t.tex);
        gl.uniform1i(loc.tex, 0);
      }
    }
    gl.uniform1f(loc.useTex, useTex);

    gl.drawArrays(mode, 0, count);
  };

  // ---------- 纹理 ----------
  function uploadTexture(id, t) {
    if (!t || !t.pixels) { t && (t.dirty = false); return; }
    if (!t.tex) t.tex = gl.createTexture();
    gl.bindTexture(gl.TEXTURE_2D, t.tex);
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, t.w, t.h, 0, gl.RGBA, gl.UNSIGNED_BYTE, t.pixels);
    var mag = (t.filter === 0x2600) ? gl.NEAREST : gl.LINEAR;
    var min = mag;
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, mag);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, min);
    var wrap = (t.wrap === 0x2900 || t.wrap === 0x812F) ? gl.CLAMP_TO_EDGE : gl.REPEAT;
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, wrap);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, wrap);
    t.dirty = false;
  }

  G.texImage = function (id, w, h, argb, filter, wrap) {
    if (!gl || !argb) return;
    var px = new Uint8Array(w * h * 4);
    for (var i = 0; i < w * h; i++) {
      var v = argb[i] | 0;
      px[i * 4] = (v >> 16) & 255;
      px[i * 4 + 1] = (v >> 8) & 255;
      px[i * 4 + 2] = v & 255;
      px[i * 4 + 3] = (v >>> 24) & 255;
    }
    var t = textures[id] || (textures[id] = {});
    t.w = w; t.h = h; t.pixels = px; t.filter = filter; t.wrap = wrap; t.dirty = true;
    uploadTexture(id, t);
  };

  G.deleteTexture = function (id) {
    var t = textures[id];
    if (t && t.tex && gl) gl.deleteTexture(t.tex);
    delete textures[id];
  };

  G.isTextureReady = function (id) { return !!textures[id]; };

  G.getString = function (name) {
    if (!gl) return 'JARJS-GL (no context)';
    if (name === 0x1F00) return gl.getParameter(gl.VENDOR) || 'WebGL';
    if (name === 0x1F01) return gl.getParameter(gl.RENDERER) || 'WebGL';
    if (name === 0x1F02) return 'OpenGL ES 2.0 (JARJS 固定管线兼容层)';
    if (name === 0x1F03) return (gl.getSupportedExtensions() || []).join(' ');
    return '';
  };

  G.getError = function () { return gl ? gl.getError() : 0; };
  G.closeRequested = function () { return closeRequested; };
  G.requestClose = function () { closeRequested = true; };

  // ---------- 输入 ----------
  // 浏览器 keyCode → LWJGL 键码
  var KEYMAP = {
    8: 14, 9: 15, 12: 0x4C, 13: 28, 16: 42, 17: 29, 18: 56, 19: 197,
    20: 58, 27: 1, 32: 57, 33: 201, 34: 209, 35: 207, 36: 199, 37: 203,
    38: 200, 39: 205, 40: 208, 45: 210, 46: 211,
    48: 11, 49: 2, 50: 3, 51: 4, 52: 5, 53: 6, 54: 7, 55: 8, 56: 9, 57: 10,
    65: 30, 66: 48, 67: 46, 68: 32, 69: 18, 70: 33, 71: 34, 72: 35, 73: 23,
    74: 36, 75: 37, 76: 38, 77: 50, 78: 49, 79: 24, 80: 25, 81: 16, 82: 19,
    83: 31, 84: 20, 85: 22, 86: 47, 87: 17, 88: 45, 89: 21, 90: 44,
    91: 219, 92: 220, 93: 221,
    96: 82, 97: 79, 98: 80, 99: 81, 100: 75, 101: 76, 102: 77, 103: 71,
    104: 72, 105: 73, 106: 55, 107: 78, 109: 74, 110: 83, 111: 181,
    112: 59, 113: 60, 114: 61, 115: 62, 116: 63, 117: 64, 118: 65, 119: 66,
    120: 67, 121: 68, 122: 87, 123: 88,
    144: 69, 145: 70, 186: 39, 187: 13, 188: 51, 189: 12, 190: 52, 191: 53,
    192: 41, 219: 26, 220: 43, 221: 27, 222: 40
  };

  function toLwjgl(code) { return KEYMAP[code] || 0; }

  function installInput() {
    window.addEventListener('keydown', function (e) {
      var k = toLwjgl(e.keyCode);
      if (!k) return;
      if (e.keyCode === 32 || (e.keyCode >= 37 && e.keyCode <= 40) || e.keyCode === 9) e.preventDefault();
      if (!keys[k]) { keys[k] = true; keyQueue.push(k + ':1'); }
    }, false);
    window.addEventListener('keyup', function (e) {
      var k = toLwjgl(e.keyCode);
      if (!k) return;
      keys[k] = false;
      keyQueue.push(k + ':0');
    }, false);
    window.addEventListener('blur', function () { keys = {}; }, false);

    function pos(e) {
      var r = canvas.getBoundingClientRect();
      var sx = canvas.width / Math.max(1, r.width);
      var sy = canvas.height / Math.max(1, r.height);
      return {
        x: Math.round((e.clientX - r.left) * sx / dpr),
        y: Math.round(H - (e.clientY - r.top) * sy / dpr)
      };
    }

    canvas.addEventListener('mousemove', function (e) {
      var p = pos(e);
      mouse.dx += p.x - mouse.x;
      mouse.dy += p.y - mouse.y;
      mouse.x = p.x; mouse.y = p.y;
    }, false);
    canvas.addEventListener('mousedown', function (e) {
      var p = pos(e); mouse.x = p.x; mouse.y = p.y;
      mouse.buttons[e.button] = true;
      mouse.queue.push(p.x + ':' + p.y + ':' + e.button + ':1:0:0');
      e.preventDefault();
    }, false);
    canvas.addEventListener('mouseup', function (e) {
      var p = pos(e); mouse.x = p.x; mouse.y = p.y;
      mouse.buttons[e.button] = false;
      mouse.queue.push(p.x + ':' + p.y + ':' + e.button + ':0:0:0');
    }, false);
    canvas.addEventListener('contextmenu', function (e) { e.preventDefault(); }, false);
  }

  G.isKeyDown = function (k) { return !!keys[k]; };
  G.nextKeyEvent = function () { return keyQueue.length ? keyQueue.shift() : null; };
  G.clearKeys = function () { keys = {}; keyQueue.length = 0; };

  G.isButtonDown = function (b) { return !!mouse.buttons[b]; };
  G.getMouseX = function () { return mouse.x; };
  G.getMouseY = function () { return mouse.y; };
  G.getMouseDX = function () { var d = mouse.dx; mouse.dx = 0; return d; };
  G.getMouseDY = function () { var d = mouse.dy; mouse.dy = 0; return d; };
  G.nextMouseEvent = function () { return mouse.queue.length ? mouse.queue.shift() : null; };
  G.setGrabbed = function (g) {
    mouse.grabbed = g;
    if (canvas) canvas.style.cursor = g ? 'none' : 'default';
  };
  G.setCursorPosition = function (x, y) { mouse.x = x; mouse.y = y; };

  G.debugStats = function () {
    return { width: canvas ? canvas.width : 0, height: canvas ? canvas.height : 0, textures: Object.keys(textures).length };
  };
})();
