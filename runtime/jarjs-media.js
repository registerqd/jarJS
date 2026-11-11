/* JARJS 多媒体运行时 (v0.4)
 * 三块能力，全部走浏览器原生 API，不依赖任何 native 库：
 *   1. OpenAL  → Web Audio API（PCM 直接建 AudioBuffer）
 *   2. FreeType → Canvas2D 文字光栅化（生成位图给纹理上传）
 *   3. 图像/音频解码 → base64 data URL + Image / decodeAudioData
 * 依赖：window.JARJS_ASSET_TABLE（由 bin/pack_assets.py 在构建时生成）
 */
(function () {
  'use strict';

  var G = window.JARJS_GL = window.JARJS_GL || {};
  var M = window.JARJS_MEDIA = {};

  // ============================================================
  // ---------- 1. 音频：OpenAL → Web Audio ----------
  // ============================================================

  var audioCtx = null;
  var alBuffers = {};     // id -> AudioBuffer
  var alSources = {};     // id -> { bufferId, gain, pitch, node, loop, playing, position }
  var alListener = { gain: 1.0, x: 0, y: 0, z: 0 };
  var nextAudioId = 1;
  var audioUnlocked = false;
  var pendingAudio = {};  // bufferId -> { b64, format, freq }
  var pendingPlay = {};   // sourceId -> true

  /**
   * 浏览器自动播放策略要求用户手势后才能创建/恢复音频上下文。
   * 这里推迟到首次点击/按键：无手势环境下不创建 AudioContext，
   * 避免部分无头/软件渲染环境里音频线程与 WebGL 抢占导致上下文丢失。
   */
  function ensureAudio() {
    if (audioCtx) return audioCtx;
    if (!audioUnlocked) return null;
    var AC = window.AudioContext || window.webkitAudioContext;
    if (!AC) { console.warn('[JARJS Media] 浏览器不支持 Web Audio'); return null; }
    audioCtx = new AC();

    var queue = pendingAudio;
    pendingAudio = {};
    for (var id in queue) {
      if (queue.hasOwnProperty(id)) buildBuffer(parseInt(id, 10), queue[id]);
    }
    var play = pendingPlay;
    pendingPlay = {};
    for (var sid in play) {
      if (play.hasOwnProperty(sid)) M.alSourcePlay(parseInt(sid, 10));
    }
    return audioCtx;
  }

  // 首次点击/按键时解锁音频
  function installAudioUnlock() {
    var unlock = function () {
      audioUnlocked = true;
      ensureAudio();
      if (audioCtx && audioCtx.state === 'suspended') audioCtx.resume();
      window.removeEventListener('pointerdown', unlock);
      window.removeEventListener('keydown', unlock);
      window.removeEventListener('touchstart', unlock);
    };
    window.addEventListener('pointerdown', unlock);
    window.addEventListener('keydown', unlock);
    window.addEventListener('touchstart', unlock);
  }
  installAudioUnlock();

  /** 用当前 AudioContext 构建 AudioBuffer。 */
  function buildBuffer(bufferId, spec) {
    var ctx = audioCtx;
    if (!ctx) return;
    var b64 = spec.b64, format = spec.format, freq = spec.freq;
    var bin = atob(b64);
    var channels = (format === 0x1102 || format === 0x1103) ? 2 : 1;
    var is16 = (format === 0x1101 || format === 0x1103);
    var frames = Math.floor(bin.length / (channels * (is16 ? 2 : 1)));
    if (frames <= 0) return;

    var buf = ctx.createBuffer(channels, frames, freq || 22050);
    for (var c = 0; c < channels; c++) {
      var data = buf.getChannelData(c);
      for (var i = 0; i < frames; i++) {
        var off = (i * channels + c) * (is16 ? 2 : 1);
        var v;
        if (is16) {
          v = (bin.charCodeAt(off) | (bin.charCodeAt(off + 1) << 8));
          if (v > 32767) v -= 65536;
          data[i] = v / 32768;
        } else {
          v = bin.charCodeAt(off);
          data[i] = (v - 128) / 128;
        }
      }
    }
    alBuffers[bufferId] = buf;
  }

  /** PCM 字节（base64）→ AudioBuffer。format: 0x1100 单声道8位, 0x1101 单声道16位, 0x1102 立体声8位, 0x1103 立体声16位 */
  M.alBufferData = function (bufferId, b64, format, freq) {
    var ctx = ensureAudio();
    if (!ctx) {
      pendingAudio[bufferId] = { b64: b64, format: format, freq: freq };
      return;
    }
    buildBuffer(bufferId, { b64: b64, format: format, freq: freq });
  };

  /** 直接用 base64 音频文件（ogg/wav/mp3）解码成 AudioBuffer。 */
  M.alBufferDataEncoded = function (bufferId, dataUrl) {
    var ctx = ensureAudio();
    if (!ctx) return;
    var bin = atob(dataUrl.substring(dataUrl.indexOf(',') + 1));
    var bytes = new Uint8Array(bin.length);
    for (var i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
    ctx.decodeAudioData(bytes.buffer, function (buf) {
      alBuffers[bufferId] = buf;
    }, function (e) {
      console.warn('[JARJS Media] 音频解码失败', e);
    });
  };

  M.alGenBuffers = function (n) {
    var ids = [];
    for (var i = 0; i < n; i++) ids.push(nextAudioId++);
    return ids;
  };

  M.alGenSources = function (n) {
    var ids = [];
    for (var i = 0; i < n; i++) {
      var id = nextAudioId++;
      alSources[id] = { bufferId: 0, gain: 1, pitch: 1, loop: false, playing: false, node: null };
      ids.push(id);
    }
    return ids;
  };

  M.alSourcei = function (srcId, pname, value) {
    var s = alSources[srcId];
    if (!s) return;
    if (pname === 0x1009) s.bufferId = value;       // AL_BUFFER
    else if (pname === 0x1007) s.loop = !!value;    // AL_LOOPING
  };

  M.alSourcef = function (srcId, pname, value) {
    var s = alSources[srcId];
    if (!s) return;
    if (pname === 0x100A) s.gain = value;           // AL_GAIN
    else if (pname === 0x1003) s.pitch = value;     // AL_PITCH
    if (s.node) {
      if (pname === 0x100A && s.node.gain) s.node.gain.value = value * alListener.gain;
      if (pname === 0x1003 && s.node.playbackRate) s.node.playbackRate.value = value;
    }
  };

  M.alSource3f = function (srcId, pname, x, y, z) {
    var s = alSources[srcId];
    if (!s) return;
    if (pname === 0x1004) { s.x = x; s.y = y; s.z = z; }  // AL_POSITION
    if (s.node && s.node.panner && pname === 0x1004) {
      s.node.panner.setPosition(x, y, z);
    }
  };

  M.alSourcePlay = function (srcId) {
    var s = alSources[srcId];
    if (!s) return;
    var ctx = ensureAudio();
    if (!ctx) { pendingPlay[srcId] = true; return; }
    var buf = alBuffers[s.bufferId];
    if (!buf) return;
    if (ctx.state === 'suspended') ctx.resume();

    if (s.node) { try { s.node.stop(); } catch (e) {} }
    var node = ctx.createBufferSource();
    node.buffer = buf;
    node.loop = s.loop;
    node.playbackRate.value = s.pitch || 1;

    var gain = ctx.createGain();
    gain.gain.value = (s.gain || 1) * alListener.gain;
    node.connect(gain);

    if (ctx.createStereoPanner && (s.x || s.y || s.z)) {
      var panner = ctx.createStereoPanner();
      panner.pan.value = Math.max(-1, Math.min(1, (s.x || 0) / 10));
      gain.connect(panner);
      panner.connect(ctx.destination);
      s.panner = panner;
    } else {
      gain.connect(ctx.destination);
    }

    node.start(0);
    s.node = node;
    s.playing = true;
    node.onended = function () { s.playing = false; };
  };

  M.alSourceStop = function (srcId) {
    var s = alSources[srcId];
    if (s && s.node) {
      try { s.node.stop(); } catch (e) {}
      s.node = null;
      s.playing = false;
    }
  };

  M.alSourcePause = function (srcId) {
    var s = alSources[srcId];
    if (s && s.node && audioCtx) { try { s.node.stop(); } catch (e) {} s.playing = false; }
  };

  M.alSourceRewind = function (srcId) { M.alSourceStop(srcId); };

  M.alSourcePlayv = function (ids) {
    for (var i = 0; i < ids.length; i++) M.alSourcePlay(ids[i]);
  };

  M.alDeleteBuffers = function (ids) {
    for (var i = 0; i < ids.length; i++) delete alBuffers[ids[i]];
  };

  M.alDeleteSources = function (ids) {
    for (var i = 0; i < ids.length; i++) { M.alSourceStop(ids[i]); delete alSources[ids[i]]; }
  };

  M.alListenerf = function (pname, value) {
    if (pname === 0x100A) alListener.gain = value;  // AL_GAIN
  };

  M.alListener3f = function (pname, x, y, z) {
    if (pname === 0x1004) { alListener.x = x; alListener.y = y; alListener.z = z; }
  };

  M.alGetError = function () { return 0; };
  M.alIsSourcePlaying = function (srcId) {
    var s = alSources[srcId];
    return s && s.playing ? 1 : 0;
  };

  // ============================================================
  // ---------- 2. 图像解码：base64 → 像素 ----------
  // ============================================================

  var images = {};   // key -> { width, height, pixels: Uint8ClampedArray }

  /** 异步解码一张图片。dataUrl 来自 JARJS_ASSETS。 */
  M.loadImage = function (key, dataUrl, callbackName) {
    var img = new Image();
    img.onload = function () {
      var cv = document.createElement('canvas');
      cv.width = img.width;
      cv.height = img.height;
      var cx = cv.getContext('2d');
      cx.drawImage(img, 0, 0);
      var d = cx.getImageData(0, 0, img.width, img.height);
      images[key] = { width: img.width, height: img.height, pixels: d.data };
      if (callbackName && typeof window[callbackName] === 'function') {
        window[callbackName](key, img.width, img.height);
      }
    };
    img.onerror = function () {
      console.warn('[JARJS Media] 图片解码失败: ' + key);
      images[key] = { width: 0, height: 0, pixels: null, error: true };
    };
    img.src = dataUrl;
  };

  M.isImageReady = function (key) {
    return Object.prototype.hasOwnProperty.call(images, key) ? 1 : 0;
  };

  M.getImageWidth = function (key) { var i = images[key]; return i ? i.width : 0; };
  M.getImageHeight = function (key) { var i = images[key]; return i ? i.height : 0; };

  /** 返回 ARGB int 数组（Java 侧转 int[] 用）。 */
  M.getImagePixels = function (key) {
    var i = images[key];
    if (!i || !i.pixels) return [];
    var n = i.width * i.height;
    var out = new Array(n);
    var p = i.pixels;
    for (var k = 0; k < n; k++) {
      var a = p[k * 4 + 3], r = p[k * 4], g = p[k * 4 + 1], b = p[k * 4 + 2];
      out[k] = ((a << 24) | (r << 16) | (g << 8) | b) | 0;
    }
    return out;
  };

  // ============================================================
  // ---------- 3. 字体：FreeType 替代（Canvas2D 光栅化） ----------
  // ============================================================

  /** 每次新建离屏 canvas，避免复用导致的绘图状态被重置。 */
  function newFontCanvas(w, h) {
    var cv = document.createElement('canvas');
    cv.width = w;
    cv.height = h;
    return cv;
  }

  /**
   * 把一段文字光栅化成位图。
   * pixels 为 RGBA 四字节展开数组（长度 width*height*4，全部 0~255），
   * 由 Java 侧组装成 ARGB，避免 JS 负数在跨语言传递时被截断。
   */
  M.rasterizeText = function (text, fontFamily, fontSize, bold, italic) {
    var font = (italic ? 'italic ' : '') + (bold ? 'bold ' : '') +
               fontSize + 'px ' + (fontFamily || 'sans-serif');

    // 先用一个临时 canvas 量宽度
    var probe = newFontCanvas(8, 8);
    var pcx = probe.getContext('2d', { willReadFrequently: true });
    pcx.font = font;
    var metrics = pcx.measureText(text || ' ');
    var advance = Math.ceil(metrics.width);

    var w = Math.max(1, advance + 4);
    var h = Math.ceil(fontSize * 1.5);
    var baseline = Math.round(fontSize * 1.1);

    var cv = newFontCanvas(w, h);
    var cx = cv.getContext('2d', { willReadFrequently: true });
    cx.font = font;
    cx.textBaseline = 'alphabetic';
    cx.fillStyle = '#ffffff';
    cx.fillText(text || '', 2, baseline);

    var d = cx.getImageData(0, 0, w, h).data;
    var out = new Array(w * h * 4);
    for (var i = 0; i < out.length; i++) out[i] = d[i];

    return {
      width: w,
      height: h,
      advance: advance,
      baseline: baseline,
      pixels: out
    };
  };

  M.measureText = function (text, fontFamily, fontSize, bold, italic) {
    var cx = ensureFontCanvas();
    cx.font = (italic ? 'italic ' : '') + (bold ? 'bold ' : '') +
              fontSize + 'px ' + (fontFamily || 'sans-serif');
    return Math.ceil(cx.measureText(text || ' ').width);
  };

  // ============================================================
  // ---------- 4. Base64 工具与资源表 ----------
  // ============================================================

  M.assetExists = function (path) {
    var T = window.JARJS_ASSET_TABLE;
    return !!(T && Object.prototype.hasOwnProperty.call(T, path));
  };

  M.assetDataUrl = function (path) {
    var A = window.JARJS_ASSETS;
    return (A && A[path]) ? A[path] : null;
  };

  M.assetBase64 = function (path) {
    var T = window.JARJS_ASSET_TABLE;
    return (T && T[path]) ? T[path].b64 : null;
  };

  M.assetSize = function (path) {
    var T = window.JARJS_ASSET_TABLE;
    return (T && T[path]) ? T[path].size : 0;
  };

  M.assetList = function () {
    var T = window.JARJS_ASSET_TABLE || {};
    return Object.keys(T);
  };

  M.assetMime = function (path) {
    var T = window.JARJS_ASSET_TABLE;
    return (T && T[path]) ? T[path].mime : 'application/octet-stream';
  };

  M.audioState = function () {
    return audioCtx ? audioCtx.state : 'none';
  };

  M.debugStats = function () {
    return {
      assets: Object.keys(window.JARJS_ASSET_TABLE || {}).length,
      images: Object.keys(images).length,
      audioBuffers: Object.keys(alBuffers).length,
      sources: Object.keys(alSources).length,
      audioState: audioCtx ? audioCtx.state : 'none'
    };
  };
})();
