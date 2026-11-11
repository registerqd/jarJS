#!/usr/bin/env python3
"""扫描 jar 内资源，转成 base64 内嵌到 HTML。

产物是一个 JS 片段：
    window.JARJS_ASSETS = { "assets/logo.png": "data:image/png;base64,...", ... }
以及一份原始字节索引（供 Assets.getResourceAsStream 使用）：
    window.JARJS_ASSET_BYTES = { "assets/logo.png": "base64...", ... }

用法:
    pack_assets.py <input.jar> <output.js> [--max-file-kb 2048] [--max-total-mb 24]
"""
import sys, os, zipfile, base64, json, mimetypes

# 需要内嵌的资源类型（按扩展名）
EMBED_EXTS = {
    # 图像
    '.png', '.jpg', '.jpeg', '.gif', '.bmp', '.webp',
    # 音频
    '.ogg', '.wav', '.mp3', '.flac',
    # 字体
    '.ttf', '.otf', '.fnt',
    # 文本 / 配置 / 着色器
    '.txt', '.json', '.xml', '.properties', '.atlas', '.frag', '.vert',
    '.glsl', '.vs', '.fs', '.shader', '.csv', '.tmx', '.tsx', '.obj', '.mtl',
    # 二进制游戏数据（小文件）
    '.dat', '.bin', '.pak',
}

# 这些扩展名作为 data URL 时需要 MIME
MIME = {
    '.png': 'image/png', '.jpg': 'image/jpeg', '.jpeg': 'image/jpeg',
    '.gif': 'image/gif', '.bmp': 'image/bmp', '.webp': 'image/webp',
    '.ogg': 'audio/ogg', '.wav': 'audio/wav', '.mp3': 'audio/mpeg',
    '.flac': 'audio/flac',
    '.ttf': 'font/ttf', '.otf': 'font/otf',
}


def main():
    if len(sys.argv) < 3:
        print("用法: pack_assets.py <input.jar> <output.js> [--max-file-kb N] [--max-total-mb N]",
              file=sys.stderr)
        sys.exit(1)

    jar_path, out_path = sys.argv[1], sys.argv[2]
    max_file = 2048 * 1024
    max_total = 24 * 1024 * 1024
    args = sys.argv[3:]
    for i, a in enumerate(args):
        if a == '--max-file-kb' and i + 1 < len(args):
            max_file = int(args[i + 1]) * 1024
        elif a == '--max-total-mb' and i + 1 < len(args):
            max_total = int(args[i + 1]) * 1024 * 1024

    assets = {}
    skipped = []
    total = 0

    if not os.path.exists(jar_path):
        print("[JARJS] 资源打包：找不到 %s" % jar_path, file=sys.stderr)
        write_empty(out_path)
        return

    with zipfile.ZipFile(jar_path) as z:
        for info in z.infolist():
            if info.is_dir():
                continue
            name = info.filename
            if name.startswith('META-INF/') or name.startswith('WEB-INF/'):
                continue
            ext = os.path.splitext(name)[1].lower()
            if ext not in EMBED_EXTS:
                continue
            if info.file_size > max_file:
                skipped.append((name, 'too-large'))
                continue
            if total + info.file_size > max_total:
                skipped.append((name, 'budget'))
                continue
            try:
                raw = z.read(info)
            except Exception:
                skipped.append((name, 'read-error'))
                continue
            b64 = base64.b64encode(raw).decode('ascii')
            mime = MIME.get(ext) or mimetypes.guess_type(name)[0] or 'application/octet-stream'
            assets[name] = {
                'mime': mime,
                'b64': b64,
                'size': len(raw)
            }
            total += len(raw)

    payload = json.dumps(assets, ensure_ascii=False, separators=(',', ':'))
    js = (
        "/* JARJS 资源包：%d 个文件，原始 %d 字节，base64 后 %.1f MB */\n"
        "window.JARJS_ASSET_TABLE = %s;\n"
        "(function(){\n"
        "  var T = window.JARJS_ASSET_TABLE || {};\n"
        "  window.JARJS_ASSETS = window.JARJS_ASSETS || {};\n"
        "  for (var k in T) { if (T.hasOwnProperty(k)) {\n"
        "    window.JARJS_ASSETS[k] = 'data:' + T[k].mime + ';base64,' + T[k].b64;\n"
        "  }}\n"
        "})();\n"
    ) % (len(assets), total, len(payload) / 1024.0 / 1024.0, payload)

    with open(out_path, 'w', encoding='utf-8') as f:
        f.write(js)

    print("[JARJS] 资源内嵌：%d 个文件，原始 %.1f KB" % (len(assets), total / 1024.0))
    if skipped:
        names = ', '.join(n for n, _ in skipped[:5])
        print("[JARJS] 跳过 %d 个资源（超限）：%s%s" %
              (len(skipped), names, ' ...' if len(skipped) > 5 else ''))


def write_empty(out_path):
    with open(out_path, 'w', encoding='utf-8') as f:
        f.write("window.JARJS_ASSET_TABLE = {};\nwindow.JARJS_ASSETS = {};\n")


if __name__ == '__main__':
    main()
