#!/usr/bin/env python3
"""JARJS 全自动补桩引擎 (v0.7)

目标：让调用方（包括其他 AI）不需要手写任何补桩逻辑，一行命令跑完全自动闭环。

自动完成：
  1. 编译转译，抓 TeaVM 报错
  2. 解析缺失类 / 缺失方法
  3. **递归解析参数与返回类型**，把依赖类一并补上（这是 v0.6 之前炸过的坑）
  4. 每轮从干净状态全量重建桩源码，绝不在脏补丁上叠加
  5. 桩类不 implements 任何接口，避免抽象方法未实现导致编译失败
  6. 循环直到通过 / 无可补项 / 达到轮次上限
  7. 输出 JSON 报告与可读日志

用法（由 jarjs.sh 调用，也可独立使用）:
    autostub.py <work_dir> <main_class> [--offline] [--max-rounds N] [--log FILE]
"""
import os
import re
import sys
import json
import shutil
import subprocess
import datetime

def _find_tool(env_key, candidates, fallback):
    v = os.environ.get(env_key)
    if v and os.path.exists(v):
        return v
    for c in candidates:
        if c and os.path.exists(c):
            return c
    found = shutil.which(fallback)
    return found or fallback


MVN = _find_tool('MVN', ['/data/apache-maven-3.9.16/bin/mvn'], 'mvn')
JAVA_HOME = _find_tool('JAVA_HOME', ['/data/jdk-21.0.13+9'], 'java')
if JAVA_HOME.endswith('/java'):
    JAVA_HOME = os.path.dirname(os.path.dirname(JAVA_HOME))
JAVAC = os.path.join(JAVA_HOME, 'bin', 'javac') if JAVA_HOME else 'javac'

PRIM = {'V': 'void', 'Z': 'boolean', 'B': 'byte', 'C': 'char',
        'S': 'short', 'I': 'int', 'J': 'long', 'F': 'float', 'D': 'double'}

DEFAULT = {'boolean': 'false', 'byte': '0', 'char': '0', 'short': '0',
           'int': '0', 'long': '0L', 'float': '0f', 'double': '0d', 'void': ''}

# 离线模式下允许桩掉的网络 / 加密链
OFFLINE_PREFIXES = (
    'java.net.', 'java.nio.channels.', 'java.security.', 'javax.crypto.',
    'javax.net.', 'sun.misc.', 'sun.nio.', 'io.netty.',
)

JAVA_PREFIXES = ('java.', 'javax.', 'jdk.', 'sun.', 'org.teavm.', 'org.w3c.', 'org.xml.')


class AutoStubber:

    def __init__(self, work_dir, main_class, offline=False, max_rounds=30, log_path=None):
        self.work = work_dir
        self.main_class = main_class
        self.offline = offline
        self.max_rounds = max_rounds
        self.log_path = log_path
        self.conv = os.path.join(work_dir, 'conv')
        self.stub_root = os.path.join(self.conv, 'stubsrc')
        self.classes_dir = os.path.join(self.conv, 'target', 'classes')
        self.stub_classes = set()
        self.stub_methods = {}      # cls -> {(name, desc): static_bool}
        self.rounds = []
        self.log_lines = []

    # ---------------- 日志 ----------------
    def log(self, msg):
        line = '[JARJS][autostub] ' + msg
        print(line, flush=True)
        self.log_lines.append(line)

    def dump_log(self):
        if not self.log_path:
            return
        try:
            with open(self.log_path, 'w', encoding='utf-8') as f:
                f.write('\n'.join(self.log_lines))
        except Exception:
            pass

    # ---------------- 编译 ----------------
    def build(self):
        r = subprocess.run([MVN, '-B', 'package'], cwd=self.conv,
                           capture_output=True, text=True)
        return r.returncode, (r.stdout or '') + (r.stderr or '')

    # ---------------- 错误解析 ----------------
    @staticmethod
    def collect_missing(text):
        classes, methods = set(), set()
        for pat in (r'Class ([\w.$]+) was not found',
                    r'Class ([\w.$]+) is missing',
                    r'Class ([\w.$]+) extends [\w.$]+, which is missing',
                    r'([\w.$]+) is missing in the classpath'):
            for m in re.finditer(pat, text):
                classes.add(m.group(1))
        for m in re.finditer(r'Method ([\w.$]+)\.([\w$<>]+)(\([^\n]*?\)[^\n]*?);?\s+was not found', text):
            cls, name, desc = m.group(1), m.group(2), m.group(3)
            if not desc.endswith(';'):
                desc += ';'
            classes.add(cls)
            methods.add((cls, name, desc))
        return classes, methods

    # ---------------- 类型与依赖 ----------------
    @staticmethod
    def read_type(s, i):
        dims = 0
        while i < len(s) and s[i] == '[':
            dims += 1
            i += 1
        if i >= len(s):
            return 'Object', i
        c = s[i]
        if c == 'L':
            end = s.find(';', i)
            if end < 0:
                end = len(s)
            name = s[i + 1:end].replace('/', '.')
            return name + '[]' * dims, end + 1
        return PRIM.get(c, 'Object') + '[]' * dims, i + 1

    @classmethod
    def parse_desc(cls, desc):
        i = desc.find('(')
        if i < 0:
            return None
        j = desc.find(')', i)
        params_s = desc[i + 1:j] if j > 0 else desc[i + 1:]
        ret_s = desc[j + 1:] if j > 0 else 'V'
        params, k = [], 0
        while k < len(params_s):
            t, k = cls.read_type(params_s, k)
            params.append(t)
        ret, _ = cls.read_type(ret_s, 0)
        return params, ret

    def is_stubbable(self, cls):
        if cls.startswith(OFFLINE_PREFIXES) and self.offline:
            return True
        return not cls.startswith(JAVA_PREFIXES)

    def dependency_types(self, desc):
        """从方法描述符里抽出需要一并补齐的类（递归依赖的关键）。"""
        parsed = self.parse_desc(desc)
        if not parsed:
            return set()
        params, ret = parsed
        deps = set()
        for t in params + [ret]:
            base = t.replace('[]', '').strip()
            if not base or base in DEFAULT or base in ('Object', 'String'):
                continue
            if '.' in base and self.is_stubbable(base):
                deps.add(base)
        return deps

    # ---------------- 桩生成 ----------------
    def render_method(self, name, desc, is_static):
        parsed = self.parse_desc(desc)
        if not parsed:
            return None
        params, ret = parsed
        args = ', '.join('%s a%d' % (t, i) for i, t in enumerate(params))
        static_kw = 'static ' if is_static else ''
        if ret == 'void':
            return ('    public %svoid %s(%s) '
                    '{ System.err.println("[JARJS Stub] %s"); }' % (static_kw, name, args, name))
        dflt = DEFAULT.get(ret, 'null')
        return ('    public %s%s %s(%s) '
                '{ System.err.println("[JARJS Stub] %s"); return %s; }'
                % (static_kw, ret, name, args, name, dflt))

    def rebuild_all(self):
        """每轮全量重建：先清空旧桩目录，绝不叠加脏补丁。"""
        if os.path.isdir(self.stub_root):
            shutil.rmtree(self.stub_root)
        os.makedirs(self.stub_root, exist_ok=True)

        for cls in sorted(self.stub_classes):
            pkg, _, simple = cls.rpartition('.')
            d = os.path.join(self.stub_root, *pkg.split('.')) if pkg else self.stub_root
            os.makedirs(d, exist_ok=True)

            methods = self.stub_methods.get(cls, {})
            lines = []
            for (name, desc), is_static in sorted(methods.items()):
                r = self.render_method(name, desc, is_static)
                if r:
                    lines.append(r)

            body = []
            if pkg:
                body.append('package %s;\n' % pkg)
            body.append('/** JARJS autostub 自动生成：缺失类占位实现（不 implements 任何接口）。 */')
            body.append('public class %s {' % simple)
            body.append('    public %s() {}' % simple)
            if lines:
                body.append('\n'.join(lines))
            body.append('}')
            with open(os.path.join(d, simple + '.java'), 'w', encoding='utf-8') as f:
                f.write('\n'.join(body) + '\n')

    def compile_stubs(self, extra_cp=''):
        sources = []
        for root, _, files in os.walk(self.stub_root):
            sources += [os.path.join(root, f) for f in files if f.endswith('.java')]
        if not sources:
            return True
        os.makedirs(self.classes_dir, exist_ok=True)
        cp = self.classes_dir + (os.pathsep + extra_cp if extra_cp else '')
        r = subprocess.run([JAVAC, '-nowarn', '-d', self.classes_dir, '-cp', cp] + sources,
                           capture_output=True, text=True)
        if r.returncode != 0:
            self.log('桩编译失败（本轮跳过，继续下一轮）：')
            for ln in (r.stderr or '').splitlines()[:8]:
                self.log('    ' + ln)
            return False
        return True

    # ---------------- 主循环 ----------------
    def run(self, extra_cp=''):
        self.log('开始自动补桩：main=%s，最大 %d 轮，offline=%s'
                 % (self.main_class, self.max_rounds, self.offline))

        for rnd in range(1, self.max_rounds + 1):
            code, out = self.build()
            if code == 0:
                self.log('第 %d 轮编译通过，补桩完成' % rnd)
                self.dump_log()
                return {
                    'ok': True,
                    'rounds': rnd,
                    'stub_classes': sorted(self.stub_classes),
                    'stub_methods': sum(len(v) for v in self.stub_methods.values()),
                    'log': self.log_path or '',
                }

            classes, methods = self.collect_missing(out)
            if not classes and not methods:
                self.log('第 %d 轮失败，但错误不在可补桩范围，停止' % rnd)
                self.log('错误尾部：')
                for ln in out.splitlines()[-12:]:
                    self.log('    ' + ln)
                self.dump_log()
                return {
                    'ok': False,
                    'rounds': rnd,
                    'reason': 'unrecognized-errors',
                    'tail': out.splitlines()[-12:],
                    'stub_classes': sorted(self.stub_classes),
                }

            added = 0

            # 1) 缺失类 + 递归依赖
            queue = [c for c in classes if self.is_stubbable(c)]
            seen = set()
            while queue:
                c = queue.pop(0)
                if c in seen or c in self.stub_classes:
                    continue
                seen.add(c)
                if not self.is_stubbable(c):
                    continue
                self.stub_classes.add(c)
                self.stub_methods.setdefault(c, {})
                added += 1
                self.log('第 %d 轮补类: %s' % (rnd, c))

            # 2) 缺失方法 + 参数/返回类型递归补齐
            for (c, name, desc) in sorted(methods):
                if not self.is_stubbable(c):
                    continue
                if c not in self.stub_classes:
                    self.stub_classes.add(c)
                    self.stub_methods.setdefault(c, {})
                    added += 1
                    self.log('第 %d 轮补类(方法宿主): %s' % (rnd, c))

                mm = self.stub_methods.setdefault(c, {})
                key = (name, desc)
                if key not in mm:
                    mm[key] = False
                    added += 1
                    self.log('第 %d 轮补方法: %s.%s' % (rnd, c, name))
                else:
                    mm[key] = not mm[key]
                    self.log('第 %d 轮换静态性重试: %s.%s static=%s'
                             % (rnd, c, name, mm[key]))

                # 递归依赖：参数/返回类型里的类也要补，否则桩编译不过
                for dep in self.dependency_types(desc):
                    if dep not in self.stub_classes:
                        self.stub_classes.add(dep)
                        self.stub_methods.setdefault(dep, {})
                        added += 1
                        self.log('第 %d 轮递归补依赖: %s（来自 %s.%s）' % (rnd, dep, c, name))

            if added == 0:
                self.log('第 %d 轮无可补项，停止' % rnd)
                self.dump_log()
                return {
                    'ok': False,
                    'rounds': rnd,
                    'reason': 'nothing-to-stub',
                    'stub_classes': sorted(self.stub_classes),
                }

            self.rebuild_all()
            self.compile_stubs(extra_cp)
            self.rounds.append({
                'round': rnd,
                'classes': len(self.stub_classes),
                'methods': sum(len(v) for v in self.stub_methods.values()),
            })

        self.log('超过最大轮次 %d，停止' % self.max_rounds)
        self.dump_log()
        return {
            'ok': False,
            'rounds': self.max_rounds,
            'reason': 'max-rounds',
            'stub_classes': sorted(self.stub_classes),
        }


def main():
    if len(sys.argv) < 3:
        print('用法: autostub.py <work_dir> <main_class> [--offline] [--max-rounds N] [--log FILE] [--cp PATH]',
              file=sys.stderr)
        sys.exit(2)

    work, main_class = sys.argv[1], sys.argv[2]
    offline = '--offline' in sys.argv
    max_rounds = 30
    log_path = os.path.join(work, 'autostub.log')
    extra_cp = ''

    for i, a in enumerate(sys.argv):
        if a == '--max-rounds' and i + 1 < len(sys.argv):
            max_rounds = int(sys.argv[i + 1])
        elif a == '--log' and i + 1 < len(sys.argv):
            log_path = sys.argv[i + 1]
        elif a == '--cp' and i + 1 < len(sys.argv):
            extra_cp = sys.argv[i + 1]

    if offline:
        os.environ['JARJS_OFFLINE'] = '1'

    stubber = AutoStubber(work, main_class, offline=offline,
                          max_rounds=max_rounds, log_path=log_path)
    result = stubber.run(extra_cp=extra_cp)

    report_path = os.path.join(work, 'autostub-report.json')
    with open(report_path, 'w', encoding='utf-8') as f:
        json.dump(result, f, ensure_ascii=False, indent=2)

    print('[JARJS][autostub] 报告: ' + report_path)
    sys.exit(0 if result.get('ok') else 1)


if __name__ == '__main__':
    main()
