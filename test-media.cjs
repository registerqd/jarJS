const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

(async () => {
  const b = await chromium.launch({
    headless: true,
    args: ['--use-gl=swiftshader', '--enable-unsafe-swiftshader', '--autoplay-policy=no-user-gesture-required']
  });
  const page = await b.newPage({ viewport: { width: 820, height: 720 } });
  const logs = [];
  page.on('console', m => logs.push(m.type() + ': ' + m.text()));
  page.on('pageerror', e => logs.push('PAGEERROR ' + e.message));

  await page.goto('file://' + path.resolve(__dirname, 'media-gl.html'));
  await page.waitForTimeout(1800);
  // 触发一次用户手势，解锁音频
  await page.mouse.click(400, 300);
  await page.waitForTimeout(1200);

  const stats = await page.evaluate(() => {
    const c = document.getElementById('game');
    const gl = c.getContext('webgl');
    const out = { w: c.width, h: c.height, gl: !!gl };
    if (gl) {
      const px = new Uint8Array(c.width * c.height * 4);
      gl.readPixels(0, 0, c.width, c.height, gl.RGBA, gl.UNSIGNED_BYTE, px);
      const colors = new Set();
      let nonBg = 0;
      for (let i = 0; i < px.length; i += 4) {
        const r = px[i], g = px[i + 1], bb = px[i + 2];
        if (r > 20 || g > 20 || bb > 20) nonBg++;
        colors.add((r >> 3) + ',' + (g >> 3) + ',' + (bb >> 3));
      }
      out.nonBg = nonBg;
      out.ratio = +(nonBg / (c.width * c.height)).toFixed(3);
      out.colors = colors.size;
      out.glError = gl.getError();
    }
    out.assets = Object.keys(window.JARJS_ASSET_TABLE || {});
    out.assetSample = out.assets.length ? window.JARJS_ASSET_TABLE[out.assets[0]].mime : null;
    out.media = window.JARJS_MEDIA ? window.JARJS_MEDIA.debugStats() : null;
    out.gl = window.JARJS_GL ? window.JARJS_GL.debugStats() : null;
    return out;
  });

  const shot = '/tmp/.playwright-mcp/media-test.png';
  fs.mkdirSync('/tmp/.playwright-mcp', { recursive: true });
  await page.screenshot({ path: shot });

  console.log(JSON.stringify({ stats, logs: logs.slice(0, 15) }, null, 2));
  await b.close();
})();
