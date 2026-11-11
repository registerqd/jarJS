#!/usr/bin/env python3
"""通过 GitHub Git Data API 推送（绕过 github.com 直连超时，只走 api.github.com）。"""
import base64, json, os, sys, urllib.request, urllib.error

TOKEN = os.environ["GH_TOKEN"]
REPO = "registerqd/jarJS"
API = "https://api.github.com"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SKIP_DIRS = {".git", "target", "node_modules"}
# 上一次误传到仓库根目录的文件，需要从树里删掉
STRAY = ["assemble.py", "jarjs.sh", "lenient.py", "pack_assets.py", "push_via_api.py"]


def req(method, path, data=None):
    body = json.dumps(data).encode("utf-8") if data is not None else None
    r = urllib.request.Request(API + path, data=body, method=method)
    r.add_header("Authorization", "token " + TOKEN)
    r.add_header("Accept", "application/vnd.github+json")
    r.add_header("Content-Type", "application/json")
    r.add_header("User-Agent", "jarjs-pusher")
    with urllib.request.urlopen(r, timeout=60) as resp:
        return json.loads(resp.read().decode("utf-8"))


def main():
    ref = req("GET", "/repos/%s/git/ref/heads/main" % REPO)
    parent = ref["object"]["sha"]
    commit = req("GET", "/repos/%s/git/commits/%s" % (REPO, parent))
    base_tree = commit["tree"]["sha"]
    print("远程当前提交:", parent[:10])

    entries = []
    count = 0
    for dirpath, dirnames, filenames in os.walk(ROOT):
        dirnames[:] = [d for d in dirnames if d not in SKIP_DIRS]
        for fn in filenames:
            full = os.path.join(dirpath, fn)
            rel = os.path.relpath(full, ROOT).replace(os.sep, "/")
            if rel.startswith(".") and not rel.startswith(".gitignore"):
                continue
            if "target/" in rel:
                continue
            data = open(full, "rb").read()
            blob = req("POST", "/repos/%s/git/blobs" % REPO, {
                "content": base64.b64encode(data).decode("ascii"),
                "encoding": "base64"
            })
            mode = "100755" if fn.endswith((".sh", ".py")) else "100644"
            entries.append({"path": rel, "mode": mode, "type": "blob", "sha": blob["sha"]})
            count += 1
            if count % 10 == 0:
                print("  已上传 blob:", count)

    print("共上传文件:", count)

    # 清理上一轮误传到根目录的脚本
    for stray in STRAY:
        entries.append({"path": stray, "mode": "100644", "type": "blob", "sha": None})

    tree = req("POST", "/repos/%s/git/trees" % REPO, {"base_tree": base_tree, "tree": entries})
    newc = req("POST", "/repos/%s/git/commits" % REPO, {
        "message": "feat(media): add OpenAL/WebAudio, FreeType/CanvasFont, base64 asset pipeline and MediaDemo",
        "tree": tree["sha"],
        "parents": [parent]
    })
    req("PATCH", "/repos/%s/git/refs/heads/main" % REPO, {"sha": newc["sha"]})
    print("推送完成:", newc["sha"])


if __name__ == "__main__":
    main()
