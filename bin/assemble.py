#!/usr/bin/env python3
"""把 TeaVM 产出的 classes.js 组装成可双击运行的单文件 HTML。

用法:
    assemble.py <classes.js> <out.html> <w> <h> <mainClass> [assets.js]
"""
import sys, os, html as _html


def read(path):
    if path and os.path.exists(path):
        return open(path, encoding='utf-8').read()
    return ''


def main():
    if len(sys.argv) < 6:
        print("用法: assemble.py <classes.js> <out.html> <w> <h> <mainClass> [assets.js]",
              file=sys.stderr)
        sys.exit(1)

    js_path, out_path, w, h, main_class = sys.argv[1:6]
    assets_path = sys.argv[6] if len(sys.argv) > 6 else ''

    js = read(js_path)
    assets_js = read(assets_path)

    base = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'runtime')
    gl_runtime = read(os.path.join(base, 'jarjs-gl.js'))
    media_runtime = read(os.path.join(base, 'jarjs-media.js'))

    title = _html.escape(main_class)
    page = f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>{title} · JARJS</title>
<style>
html,body{{margin:0;height:100%;background:#0b0f14;display:flex;flex-direction:column;align-items:center;justify-content:center;font-family:system-ui,-apple-system,sans-serif;overflow:hidden}}
#game{{display:block;border-radius:8px;box-shadow:0 10px 40px rgba(0,0,0,.6);background:#000;image-rendering:auto}}
#tip{{margin-top:12px;color:#5a6472;font-size:12px;letter-spacing:.02em}}
</style>
</head>
<body>
<canvas id="game" width="{w}" height="{h}"></canvas>
<div id="tip">由 JARJS 转译 · 键盘操作 · 点击画面启用声音</div>
<script>
/* ---------- 1. 资源表（base64 内嵌） ---------- */
{assets_js}
</script>
<script>
/* ---------- 2. OpenGL → WebGL 运行时 ---------- */
{gl_runtime}
</script>
<script>
/* ---------- 3. 多媒体运行时（OpenAL / FreeType / 图像） ---------- */
{media_runtime}
</script>
<script>
/* ---------- 4. 应用字节码（TeaVM 输出） ---------- */
{js}
</script>
<script>
(function(){{
  var boot = (typeof window.main === 'function') ? window.main : (typeof main === 'function' ? main : null);
  if (boot) {{ try {{ boot(); }} catch (e) {{ console.error('[JARJS]', e); }} }}
  else console.error('[JARJS] 未找到 main 入口');
}})();
</script>
</body>
</html>
"""
    open(out_path, 'w', encoding='utf-8').write(page)


if __name__ == '__main__':
    main()
