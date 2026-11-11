#!/usr/bin/env python3
"""把 TeaVM 产出的 classes.js 组装成可双击运行的单文件 HTML。"""
import sys, os, html as _html


def main():
    js_path, out_path, w, h, main_class = sys.argv[1:6]
    js = open(js_path, encoding='utf-8').read()
    runtime_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'runtime', 'jarjs-gl.js')
    runtime = ''
    if os.path.exists(runtime_path):
        runtime = open(runtime_path, encoding='utf-8').read()
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
<div id="tip">由 JARJS 转译 · 键盘操作</div>
<script>
{runtime}
</script>
<script>
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
