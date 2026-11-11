/* JARJS OpenGL → WebGL 运行时 (v0.3 - 含现代可编程管线)
 * 职责：
 * 1. 完整保持固定管线内置 Shader 和图元绘制后端（GL 1.1 语义）
 * 2. 纯增量提供现代管线桥接（GL 2.0 / 1.5 / 1.4 / 1.3 / 3.0：Shader, Program, Uniform, Attrib, VBO, FBO）
 * 3. 桌面 GLSL 自动预处理
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

  // ---------- 固定管线内置着色器 ----------
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
      console.error('[JARJS GL] 内置 shader 编译失败: ' + gl.getShaderInfoLog(s));
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
      console.error('[JARJS GL] 内置 program 链接失败: ' + gl.getProgramInfoLog(prog));
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

    // 与 OpenGL 纹理坐标习惯对齐：图像/Canvas 上传时翻转 Y，使 v=0 对应图像底部
    gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true);

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
      lost = false; console.warn('[JARJS GL] WebGL 上下文已恢复');
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

  G.blendFunc = function (src, dst) { if (gl) gl.blendFunc(src, dst); };
  G.blendFuncSeparate = function (sR, dR, sA, dA) { if (gl) gl.blendFuncSeparate(sR, dR, sA, dA); };
  G.blendEquation = function (mode) { if (gl) gl.blendEquation(mode); };
  G.lineWidth = function (w) { if (gl) gl.lineWidth(w); };
  G.pointSize = function (s) {};
  G.viewport = function (x, y, w, h) { if (gl) gl.viewport(x, y, w, h); };
  G.scissor = function (x, y, w, h) { if (gl) gl.scissor(x, y, w, h); };
  G.enable = function (cap) { if (gl) gl.enable(cap); };
  G.disable = function (cap) { if (gl) gl.disable(cap); };
  G.depthFunc = function (f) { if (gl) gl.depthFunc(f); };
  G.depthMask = function (m) { if (gl) gl.depthMask(!!m); };
  G.colorMask = function (r, g, b, a) { if (gl) gl.colorMask(!!r, !!g, !!b, !!a); };
  G.cullFace = function (m) { if (gl) gl.cullFace(m); };
  G.frontFace = function (m) { if (gl) gl.frontFace(m); };

  // ---------- 固定管线立即模式提交 ----------
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

    // 确保固定管线属性槽位正确
    gl.enableVertexAttribArray(loc.pos);
    gl.enableVertexAttribArray(loc.color);
    gl.enableVertexAttribArray(loc.uv);
    var stride = 9 * 4;
    gl.vertexAttribPointer(loc.pos, 3, gl.FLOAT, false, stride, 0);
    gl.vertexAttribPointer(loc.color, 4, gl.FLOAT, false, stride, 12);
    gl.vertexAttribPointer(loc.uv, 2, gl.FLOAT, false, stride, 28);

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

  // ---------- 纹理管理 ----------
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

  /** 文字直接光栅化到 Canvas 后上传为纹理（避免大数组跨语言传递），返回 [w, h]。 */
  G.texImageText = function (id, text, family, size, bold, italic) {
    if (!gl) return [1, 1];
    var font = (italic ? 'italic ' : '') + (bold ? 'bold ' : '') +
               size + 'px ' + (family || 'sans-serif');

    var probe = document.createElement('canvas');
    var pcx = probe.getContext('2d');
    pcx.font = font;
    var w = Math.max(1, Math.ceil(pcx.measureText(text || ' ').width) + 4);
    var h = Math.max(1, Math.ceil(size * 1.5));

    var cv = document.createElement('canvas');
    cv.width = w;
    cv.height = h;
    var cx = cv.getContext('2d');
    cx.font = font;
    cx.textBaseline = 'alphabetic';
    cx.fillStyle = '#ffffff';
    cx.fillText(text || '', 2, Math.round(size * 1.1));

    // 先把采样参数设好（非 2 的幂尺寸必须 CLAMP），再上传像素，避免 WebGL1 纹理不完整
    var t = textures[id] || (textures[id] = {});
    if (!t.tex) t.tex = gl.createTexture();
    gl.bindTexture(gl.TEXTURE_2D, t.tex);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, cv);
    t.w = w; t.h = h; t.pixels = null; t.dirty = false;
    return [w, h];
  };

  G.isTextureReady = function (id) { return !!textures[id]; };

  G.bindTexture = function (target, id) {
    if (!gl) return;
    var t = textures[id];
    gl.bindTexture(target || gl.TEXTURE_2D, t ? t.tex : null);
  };

  G.activeTexture = function (unit) {
    if (!gl) return;
    var idx = (unit >= 0x84C0) ? (unit - 0x84C0) : unit;
    gl.activeTexture(gl.TEXTURE0 + idx);
  };

  // ==============================================================
  // ---------- 现代 OpenGL 2.0+ 着色器管线纯增量扩展 ----------
  // ==============================================================

  var nextShaderId = 1, shaders = {};
  var nextProgId = 1, programs = {};
  var nextLocId = 1, locations = {};
  var nextBufId = 1, buffers = {};
  var nextFboId = 1, framebuffers = {};

  function preprocessGLSL(src, type) {
    if (!src) return '';
    var s = src.replace(/^\s*#version\s+\d+.*$/gm, '');
    if (type === 0x8B30) { // FRAGMENT_SHADER
      if (!/precision\s+(mediump|highp|lowp)\s+float;/.test(s)) {
        s = 'precision mediump float;\n' + s;
      }
    }
    return s;
  }

  G.createShader = function (type) {
    if (!gl) return 0;
    var s = gl.createShader(type);
    if (!s) return 0;
    var id = nextShaderId++;
    shaders[id] = { obj: s, type: type, src: '', log: '' };
    return id;
  };

  G.shaderSource = function (id, src) {
    var s = shaders[id];
    if (s) s.src = src;
  };

  G.compileShader = function (id) {
    var s = shaders[id];
    if (!s || !gl) return;
    var processed = preprocessGLSL(s.src, s.type);
    gl.shaderSource(s.obj, processed);
    gl.compileShader(s.obj);
    var ok = gl.getShaderParameter(s.obj, gl.COMPILE_STATUS);
    s.log = gl.getShaderInfoLog(s.obj) || '';
    if (!ok) console.error('[JARJS GL] Shader 编译错误: ' + s.log + '\n源码:\n' + processed);
  };

  G.getShaderi = function (id, pname) {
    var s = shaders[id];
    if (!s || !gl) return 0;
    if (pname === 0x8B81) return gl.getShaderParameter(s.obj, gl.COMPILE_STATUS) ? 1 : 0;
    if (pname === 0x8B84) return (s.log ? s.log.length + 1 : 0);
    if (pname === 0x8B4F) return s.type;
    return 0;
  };

  G.getShaderInfoLog = function (id) {
    var s = shaders[id];
    return s ? (s.log || gl.getShaderInfoLog(s.obj) || '') : '';
  };

  G.deleteShader = function (id) {
    var s = shaders[id];
    if (s && gl) gl.deleteShader(s.obj);
    delete shaders[id];
  };

  // Program
  G.createProgram = function () {
    if (!gl) return 0;
    var p = gl.createProgram();
    if (!p) return 0;
    var id = nextProgId++;
    programs[id] = { obj: p, log: '' };
    return id;
  };

  G.attachShader = function (pid, sid) {
    var p = programs[pid], s = shaders[sid];
    if (p && s && gl) gl.attachShader(p.obj, s.obj);
  };

  G.detachShader = function (pid, sid) {
    var p = programs[pid], s = shaders[sid];
    if (p && s && gl) gl.detachShader(p.obj, s.obj);
  };

  G.linkProgram = function (pid) {
    var p = programs[pid];
    if (!p || !gl) return;
    gl.linkProgram(p.obj);
    var ok = gl.getProgramParameter(p.obj, gl.LINK_STATUS);
    p.log = gl.getProgramInfoLog(p.obj) || '';
    if (!ok) console.error('[JARJS GL] Program 链接错误: ' + p.log);
  };

  G.validateProgram = function (pid) {
    var p = programs[pid];
    if (p && gl) { gl.validateProgram(p.obj); p.log = gl.getProgramInfoLog(p.obj) || ''; }
  };

  G.getProgrami = function (pid, pname) {
    var p = programs[pid];
    if (!p || !gl) return 0;
    if (pname === 0x8B82) return gl.getProgramParameter(p.obj, gl.LINK_STATUS) ? 1 : 0;
    if (pname === 0x8B83) return gl.getProgramParameter(p.obj, gl.VALIDATE_STATUS) ? 1 : 0;
    if (pname === 0x8B84) return (p.log ? p.log.length + 1 : 0);
    return 0;
  };

  G.getProgramInfoLog = function (pid) {
    var p = programs[pid];
    return p ? (p.log || gl.getProgramInfoLog(p.obj) || '') : '';
  };

  G.useProgram = function (pid) {
    if (!gl) return;
    if (pid === 0) {
      if (prog) gl.useProgram(prog);
    } else {
      var p = programs[pid];
      if (p) gl.useProgram(p.obj);
    }
  };

  G.deleteProgram = function (pid) {
    var p = programs[pid];
    if (p && gl) gl.deleteProgram(p.obj);
    delete programs[pid];
  };

  // Uniform
  G.getUniformLocation = function (pid, name) {
    var p = programs[pid];
    if (!p || !gl) return -1;
    var uLoc = gl.getUniformLocation(p.obj, name);
    if (uLoc === null) return -1;
    var lid = nextLocId++;
    locations[lid] = uLoc;
    return lid;
  };

  G.uniform1i = function (lid, v) { var l = locations[lid]; if (l && gl) gl.uniform1i(l, v); };
  G.uniform2i = function (lid, v0, v1) { var l = locations[lid]; if (l && gl) gl.uniform2i(l, v0, v1); };
  G.uniform3i = function (lid, v0, v1, v2) { var l = locations[lid]; if (l && gl) gl.uniform3i(l, v0, v1, v2); };
  G.uniform4i = function (lid, v0, v1, v2, v3) { var l = locations[lid]; if (l && gl) gl.uniform4i(l, v0, v1, v2, v3); };

  G.uniform1f = function (lid, v) { var l = locations[lid]; if (l && gl) gl.uniform1f(l, v); };
  G.uniform2f = function (lid, v0, v1) { var l = locations[lid]; if (l && gl) gl.uniform2f(l, v0, v1); };
  G.uniform3f = function (lid, v0, v1, v2) { var l = locations[lid]; if (l && gl) gl.uniform3f(l, v0, v1, v2); };
  G.uniform4f = function (lid, v0, v1, v2, v3) { var l = locations[lid]; if (l && gl) gl.uniform4f(l, v0, v1, v2, v3); };

  G.uniform1fv = function (lid, arr) { var l = locations[lid]; if (l && gl) gl.uniform1fv(l, new Float32Array(arr)); };
  G.uniform2fv = function (lid, arr) { var l = locations[lid]; if (l && gl) gl.uniform2fv(l, new Float32Array(arr)); };
  G.uniform3fv = function (lid, arr) { var l = locations[lid]; if (l && gl) gl.uniform3fv(l, new Float32Array(arr)); };
  G.uniform4fv = function (lid, arr) { var l = locations[lid]; if (l && gl) gl.uniform4fv(l, new Float32Array(arr)); };

  G.uniformMatrix2fv = function (lid, t, m) { var l = locations[lid]; if (l && gl) gl.uniformMatrix2fv(l, !!t, new Float32Array(m)); };
  G.uniformMatrix3fv = function (lid, t, m) { var l = locations[lid]; if (l && gl) gl.uniformMatrix3fv(l, !!t, new Float32Array(m)); };
  G.uniformMatrix4fv = function (lid, t, m) { var l = locations[lid]; if (l && gl) gl.uniformMatrix4fv(l, !!t, new Float32Array(m)); };

  // Attribute
  G.getAttribLocation = function (pid, name) {
    var p = programs[pid];
    return (p && gl) ? gl.getAttribLocation(p.obj, name) : -1;
  };

  G.bindAttribLocation = function (pid, index, name) {
    var p = programs[pid];
    if (p && gl) gl.bindAttribLocation(p.obj, index, name);
  };

  G.enableVertexAttribArray = function (idx) { if (gl) gl.enableVertexAttribArray(idx); };
  G.disableVertexAttribArray = function (idx) { if (gl) gl.disableVertexAttribArray(idx); };

  G.vertexAttribPointer = function (idx, size, type, norm, stride, offset) {
    if (gl) gl.vertexAttribPointer(idx, size, type, !!norm, stride, offset);
  };

  G.vertexAttrib1f = function (idx, x) { if (gl) gl.vertexAttrib1f(idx, x); };
  G.vertexAttrib2f = function (idx, x, y) { if (gl) gl.vertexAttrib2f(idx, x, y); };
  G.vertexAttrib3f = function (idx, x, y, z) { if (gl) gl.vertexAttrib3f(idx, x, y, z); };
  G.vertexAttrib4f = function (idx, x, y, z, w) { if (gl) gl.vertexAttrib4f(idx, x, y, z, w); };

  // Buffer Objects (GL15)
  G.createBuffer = function () {
    if (!gl) return 0;
    var b = gl.createBuffer();
    if (!b) return 0;
    var id = nextBufId++;
    buffers[id] = b;
    return id;
  };

  G.bindBuffer = function (target, id) {
    if (!gl) return;
    gl.bindBuffer(target, id ? buffers[id] : null);
  };

  G.bufferDataFloat = function (target, arr, usage) {
    if (!gl) return;
    gl.bufferData(target, new Float32Array(arr), usage);
  };

  G.bufferDataInt = function (target, arr, usage) {
    if (!gl) return;
    gl.bufferData(target, new Int32Array(arr), usage);
  };

  G.bufferDataShort = function (target, arr, usage) {
    if (!gl) return;
    gl.bufferData(target, new Int16Array(arr), usage);
  };

  G.bufferDataByte = function (target, arr, usage) {
    if (!gl) return;
    gl.bufferData(target, new Uint8Array(arr), usage);
  };

  G.bufferSubDataFloat = function (target, offset, arr) {
    if (!gl) return;
    gl.bufferSubData(target, offset, new Float32Array(arr));
  };

  G.deleteBuffer = function (id) {
    var b = buffers[id];
    if (b && gl) gl.deleteBuffer(b);
    delete buffers[id];
  };

  // 原生 Draw
  G.drawArraysNative = function (mode, first, count) {
    if (gl && count > 0) gl.drawArrays(mode, first, count);
  };

  G.drawElementsNative = function (mode, count, type, offset) {
    if (gl && count > 0) gl.drawElements(mode, count, type, offset);
  };

  // Framebuffer (GL30)
  G.createFramebuffer = function () {
    if (!gl) return 0;
    var fb = gl.createFramebuffer();
    if (!fb) return 0;
    var id = nextFboId++;
    framebuffers[id] = fb;
    return id;
  };

  G.bindFramebuffer = function (target, id) {
    if (!gl) return;
    gl.bindFramebuffer(target, id ? framebuffers[id] : null);
  };

  G.framebufferTexture2D = function (target, attach, textarget, texId, level) {
    if (!gl) return;
    var t = textures[texId];
    gl.framebufferTexture2D(target, attach, textarget, t ? t.tex : null, level);
  };

  G.checkFramebufferStatus = function (target) {
    return gl ? gl.checkFramebufferStatus(target) : 0;
  };

  G.deleteFramebuffer = function (id) {
    var fb = framebuffers[id];
    if (fb && gl) gl.deleteFramebuffer(fb);
    delete framebuffers[id];
  };

  // ---------- 查询与系统 ----------
  G.getString = function (name) {
    if (!gl) return 'JARJS-GL (no context)';
    if (name === 0x1F00) return gl.getParameter(gl.VENDOR) || 'WebGL';
    if (name === 0x1F01) return gl.getParameter(gl.RENDERER) || 'WebGL';
    if (name === 0x1F02) return 'OpenGL ES 2.0 (JARJS 现代管线兼容层)';
    if (name === 0x1F03) return (gl.getSupportedExtensions() || []).join(' ');
    if (name === 0x8B8C) return gl.getParameter(gl.SHADING_LANGUAGE_VERSION) || 'WebGL GLSL ES 1.0';
    return '';
  };

  G.getError = function () { return gl ? gl.getError() : 0; };
  G.closeRequested = function () { return closeRequested; };
  G.requestClose = function () { closeRequested = true; };

  // ---------- 输入 ----------
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
    return {
      width: canvas ? canvas.width : 0,
      height: canvas ? canvas.height : 0,
      textures: Object.keys(textures).length,
      programs: Object.keys(programs).length,
      shaders: Object.keys(shaders).length,
      buffers: Object.keys(buffers).length
    };
  };
})();
